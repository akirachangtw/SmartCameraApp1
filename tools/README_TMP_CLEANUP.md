# tmp Expiry Cleanup

`cleanup_expired_tmp.py` cleans only these approved paths:

- `D:\DevSources\AndroidProj\SmartCameraApp1\tmp`
- `D:\DevSources\AndroidProj\SmartCameraApp1\.tmp`
- `D:\DevSources\workflow\mcp-ticket-platform\tmp`
- `D:\DevSources\workflow\mcp-ticket-platform\.tmp`

It uses local time and each item's last-modified time. Files older than seven days are removed individually. A directory older than seven days is removed as a complete tree only if no symbolic link is found inside it. Symbolic links, the `tmp` folders themselves, and anything outside the two approved paths are never removed.

Preview first:

```powershell
python D:\DevSources\AndroidProj\SmartCameraApp1\tools\cleanup_expired_tmp.py --dry-run
```

Run cleanup:

```powershell
python D:\DevSources\AndroidProj\SmartCameraApp1\tools\cleanup_expired_tmp.py
```

Each target appends aggregate-only start and result records to its own `tmp\cleanup-audit.jsonl`; it deliberately does not log filenames or error details that could contain secrets. The script fails closed before cleanup if it cannot create that audit record, so the scheduled account needs write access to both approved `tmp` folders.

## Proposed daily schedule — confirmation required

Suggested Windows Task Scheduler entry:

- Task name: `ProjectTmpExpiryCleanup`
- Run time: every day at 03:30 local time
- Program: `C:\Python314\python.exe`
- Arguments: `D:\DevSources\AndroidProj\SmartCameraApp1\tools\cleanup_expired_tmp.py`

The task is not registered by this script or this change. Confirm the proposed name and time before creating it.
