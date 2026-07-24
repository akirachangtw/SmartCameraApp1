import sys
import os
import re
import shutil
import json
from datetime import datetime

BAK_NAME_RE = re.compile(r'^memory\.jsonl\.\d{8}_\d{6}\.bak$')

def find_session_id(data):
    if isinstance(data, dict):
        if 'session_id' in data:
            return str(data['session_id'])
        for v in data.values():
            res = find_session_id(v)
            if res:
                return res
    elif isinstance(data, list):
        for item in data:
            res = find_session_id(item)
            if res:
                return res
    return None

def resolve_memory_dir():
    # AI-2: absolute path resolution, CWD banned. CUESHOT_MEMORY_DIR takes
    # priority; otherwise derive repo root from this script's own location.
    env_dir = os.environ.get('CUESHOT_MEMORY_DIR')
    if env_dir:
        return env_dir
    repo_root = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
    return os.path.join(repo_root, '.mcp-memory')

def main():
    try:
        memory_dir = resolve_memory_dir()
        memory_file = os.path.join(memory_dir, 'memory.jsonl')

        if not os.path.exists(memory_file):
            # AI-2: warn on stderr instead of silent return
            print(f"Warning: Cueshot-memory target not found: {memory_file}", file=sys.stderr)
            return 0

        # True session identity comes from stdin JSON payload provided by Claude Code.
        # We must NOT use a shared fallback like 'default_session'.
        session_id = None
        is_fallback = False

        if not sys.stdin.isatty():
            try:
                stdin_data = sys.stdin.read()
                if stdin_data.strip():
                    payload = json.loads(stdin_data)
                    session_id = find_session_id(payload)
            except Exception as e:
                print(f"Warning: Failed to parse PreToolUse stdin JSON: {e}", file=sys.stderr)

        if not session_id:
            # Fallback to process ID if session_id is genuinely missing in the payload,
            # because the hook process is a child of the Claude Code process handling this session.
            # This avoids the catastrophic silent failure of a shared 'default_session' latch.
            # Using os.getppid() (Parent Process ID) is a reliable per-session proxy in this environment.
            session_id = f"ppid_{os.getppid()}"
            is_fallback = True
            print("Warning: session_id not found in stdin payload. Using PPID as fallback.", file=sys.stderr)

        latch_file = os.path.join(memory_dir, f'.snapshot_latch_{session_id}')

        if os.path.exists(latch_file):
            # Already snapshotted in this session
            return 0

        # Snapshot
        timestamp = datetime.now().strftime('%Y%m%d_%H%M%S')
        snapshot_name = f'memory.jsonl.{timestamp}.bak'
        snapshot_path = os.path.join(memory_dir, snapshot_name)

        # HC-1: Copy file (snapshot)
        shutil.copy2(memory_file, snapshot_path)

        if is_fallback:
            # AI-5: mark PPID-fallback degraded provenance via sidecar, so the
            # .bak filename itself keeps matching the hook's own naming style.
            with open(snapshot_path + '.fallback', 'w') as f:
                f.write(session_id)

        # Touch latch file
        with open(latch_file, 'w') as f:
            f.write(timestamp)

        # AI-6: snapshot rotation (keep max 5) — only sweep files that strictly
        # match this hook's own naming style; any other .bak/evidence file is
        # left untouched.
        bak_files = []
        for f in os.listdir(memory_dir):
            if BAK_NAME_RE.match(f):
                bak_files.append(os.path.join(memory_dir, f))

        if len(bak_files) > 5:
            # Sort by mtime ascending (oldest first)
            bak_files.sort(key=lambda x: os.path.getmtime(x))
            # Delete oldest until only 5 remain
            while len(bak_files) > 5:
                oldest = bak_files.pop(0)
                os.remove(oldest)

    except Exception as e:
        # Fail-open (print warning to stderr, but exit 0)
        print(f"Warning: Cueshot-memory snapshot failed: {e}", file=sys.stderr)

    return 0

if __name__ == '__main__':
    sys.exit(main())
