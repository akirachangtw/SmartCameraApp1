import sys, json, os, re

try:
    sys.stdout.reconfigure(encoding="utf-8")
except Exception:
    pass

REGISTRY_PATH = "plans/cso_briefing/project/TICKET_REGISTRY.json"
PENDING_DIR = "plans/cso_briefing/project/Pending"
DELIVER_DIR = "plans/cso_briefing/project/deliver"
ACTIVE_JSON = "tmp/verify/active.json"
VERIFY_CONF = "tmp/verify_cmd.conf"
STOP_GUARD_DIR = "tmp/.stop_guard"
TERMINAL_LIFECYCLES = ("REVIEW", "CLOSED", "VOID")


def fail_open():
    sys.exit(0)


def read_ticket_id():
    try:
        if os.path.isfile(ACTIVE_JSON):
            with open(ACTIVE_JSON, "r", encoding="utf-8") as f:
                data = json.load(f)
            tid = data.get("ticket_id")
            if tid:
                return tid
    except Exception:
        pass
    try:
        if os.path.isfile(VERIFY_CONF):
            with open(VERIFY_CONF, "r", encoding="utf-8") as f:
                for line in f:
                    if line.startswith("TICKET_ID="):
                        tid = line.split("=", 1)[1].strip()
                        if tid:
                            return tid
    except Exception:
        pass
    return None


def read_registry_lifecycle(ticket_id):
    try:
        with open(REGISTRY_PATH, "r", encoding="utf-8") as f:
            data = json.load(f)
        for t in data.get("tickets", []):
            if t.get("id") == ticket_id:
                return t.get("lifecycle")
        return None
    except Exception:
        return None


def locate_ticket_file(ticket_id):
    prefix = ticket_id + "_"
    matches = {"Pending": [], "deliver": []}
    for label, d in (("Pending", PENDING_DIR), ("deliver", DELIVER_DIR)):
        try:
            if not os.path.isdir(d):
                continue
            for name in os.listdir(d):
                if name.startswith(prefix) and name.endswith(".md"):
                    matches[label].append(name)
        except Exception:
            return "AMBIGUOUS"
    if len(matches["Pending"]) > 1 or len(matches["deliver"]) > 1:
        return "AMBIGUOUS"
    if matches["deliver"]:
        return ("deliver", matches["deliver"][0])
    if matches["Pending"]:
        return ("Pending", matches["Pending"][0])
    return None


def ac_all_checked(location):
    label, filename = location
    d = DELIVER_DIR if label == "deliver" else PENDING_DIR
    path = os.path.join(d, filename)
    try:
        with open(path, "r", encoding="utf-8") as f:
            content = f.read()
        m = re.search(r"^## Acceptance Criteria\s*$", content, re.MULTILINE)
        if not m:
            return None
        rest = content[m.end():]
        m2 = re.search(r"^## ", rest, re.MULTILINE)
        ac_block = rest[:m2.start()] if m2 else rest
        return "- [ ]" not in ac_block
    except Exception:
        return None


def read_owner():
    try:
        with open(ACTIVE_JSON, "r", encoding="utf-8") as f:
            owner = json.load(f).get("owner_session_id")
        return owner if isinstance(owner, str) and owner else None
    except Exception:
        return None


def main():
    try:
        raw = sys.stdin.buffer.read()
        payload = json.loads(raw.decode("utf-8", errors="replace"))
    except Exception:
        fail_open()

    session_id = payload.get("session_id") if isinstance(payload, dict) else None
    if not session_id:
        fail_open()

    if read_owner() != session_id:
        fail_open()

    ticket_id = read_ticket_id()
    if not ticket_id:
        fail_open()

    lifecycle = read_registry_lifecycle(ticket_id)
    if lifecycle is None:
        fail_open()

    location = locate_ticket_file(ticket_id)
    if location == "AMBIGUOUS":
        fail_open()

    indication1 = bool(location) and location[0] == "deliver"
    ac_result = ac_all_checked(location) if location else False
    if ac_result is None:
        fail_open()
    indication2 = ac_result

    if lifecycle in TERMINAL_LIFECYCLES:
        fail_open()

    if not (indication1 or indication2):
        fail_open()

    try:
        os.makedirs(STOP_GUARD_DIR, exist_ok=True)
        marker = os.path.join(STOP_GUARD_DIR, f"{session_id}_{ticket_id}.blocked")
        if os.path.isfile(marker):
            fail_open()
        with open(marker, "w", encoding="utf-8") as f:
            f.write("")
    except Exception:
        fail_open()

    reason = (
        f"[registry_review_stop_guard] {ticket_id} 收尾跡象已成立（票檔位於 deliver/ 或 AC 全勾），"
        f"但冊面 lifecycle='{lifecycle}' 未達 REVIEW/CLOSED/VOID。"
        f"請先呼叫 registry_set_status(project=\"SmartCameraApp1\", ticket_id=\"{ticket_id}\", "
        f"lifecycle=\"REVIEW\", ...) 完成轉態後再停止（本次為單一 session 內對此票的唯一一次攔截）。"
    )
    print(json.dumps({"decision": "block", "reason": reason}, ensure_ascii=False))
    sys.exit(0)


if __name__ == "__main__":
    main()
