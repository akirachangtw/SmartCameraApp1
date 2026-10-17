## 目標

讓 `python check_backoff.py` 通過（輸出 `OK`、exit 0）。問題在 `backoff.py` 的 `next_backoff`：呼叫端傳入 `cap` 上限，但回傳值沒有受到上限約束。請給出具體修法。

## 失敗證據

在僅含下列兩個檔案的目錄執行 `python check_backoff.py`，實際輸出（路徑已縮為檔名；行號以含首行 `# file:` 標記之檔案計）：

```text
Traceback (most recent call last):
  File "check_backoff.py", line 6, in <module>
    assert next_backoff(10, base=1.0, cap=30.0) == 30.0, f"cap not applied: got {next_backoff(10, base=1.0, cap=30.0)}"
           ^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
AssertionError: cap not applied: got 1024.0
```

exit code：1。

## 相關內容

```python
# file: backoff.py
def next_backoff(attempt: int, base: float = 1.0, cap: float = 30.0) -> float:
    return base * (2 ** attempt)
```

```python
# file: check_backoff.py
from backoff import next_backoff

assert next_backoff(0) == 1.0
assert next_backoff(3) == 8.0
assert next_backoff(10, base=1.0, cap=30.0) == 30.0, f"cap not applied: got {next_backoff(10, base=1.0, cap=30.0)}"
print("OK")
```

## 已試策略與結果

無。

## 既有授權

- 僅可修改 `backoff.py`。
- 不得修改 `check_backoff.py`。
- 不得新增依賴。

## 要求輸出

請依 `ADVICE_SCHEMA` 欄位回答：`strategy`（修法策略）、`steps`（具體步驟：檔案＋改法）、`minimal_verification`（最小局部驗證法）、`authority_gap`（是否需要超出上列既有授權之裁決；本題若不需要則為 `false`）、`authority_gap_reason`、`evidence_needed`。
