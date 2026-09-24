---
id: exp-path-token-directory-prefix-01
status: active
topic: scope-verification
trigger: "A scope verifier's token↔file match logic gives an unexpected result for a directory-form ALLOWED/FORBIDDEN token (one ending in `/`) against a file nested under it — either a false SCOPE VIOLATION on legitimate nested files, or a false miss that lets an out-of-scope file through."
decision: "Before trusting either outcome, read the actual matcher function and ask specifically: does it branch on a trailing `/` and treat that case as a repo-relative path PREFIX match, or does it fall through to a plain literal/glob compare (`[[ \"$file\" == $token ]]`)? A directory token with no trailing `*` never matches a nested path under plain glob compare — that mismatch is the defect, not the diff. Verify by hand: `git diff --name-only <scope>` against the token list, file by file, before accepting the tool's verdict."
why: "A directory-form token communicates 'everything under this path,' but naive glob/literal comparison only matches exact strings or explicit wildcard patterns. The trailing `/` carries prefix semantics that the matcher must implement explicitly — it is not automatic from `[[ ]]` pattern matching alone. Adding a basename-only fallback branch does not fix this: basename comparison is orthogonal to path-based tokens and only masks the same defect under a different label."
reuse_when: "Any time a scope/ALLOWED/FORBIDDEN gate uses a directory-style token (trailing separator) and reports a scope result — PASS or FAIL — for files nested under that directory, especially when the reported violator list is exactly the set of files under one such token."
avoid_when: "The token in question has no trailing `/` (a literal file path or an explicit glob with `*`/`**`), or the matcher has already been confirmed (by reading its source, not by re-running it) to implement prefix semantics for directory tokens — re-litigating an already-fixed matcher wastes a cycle."
confidence: high
source_refs:
  - "RGR-VERIFY-CHECKLIST-01 (CLOSED) — .claude/skills/qa/scripts/verify_checklist.sh token_matches_file(), commit c73e6ba"
  - "RGR-AUTO-CLEANUP-01 (CLOSED) — the false-positive event that surfaced this pattern"
last_confirmed: 2026-09-24
retire_when: "All scope-token forms this repo's verifiers accept (directory-prefix, literal path, glob) have a documented, tested prefix/boundary contract, and that contract has held under real usage for long enough that a scope-token mismatch of this shape has not recurred across multiple unrelated tickets — not merely because RGR-VERIFY-CHECKLIST-01 itself is closed, since closing one instance does not retire the general pattern."
---

# Directory-form scope token vs nested file: prefix, not glob

See `trigger`/`decision`/`why` above for the full pattern. Concretely: `token_matches_file()` in
`verify_checklist.sh` branched on `token == *"/"* || token == *"\\"*` and then ran
`[[ "$file" == $token ]]` unconditionally — a directory token like
`.../fixtures/rgr-auto/` can never match `.../fixtures/rgr-auto/cases/c1.json` under that compare,
because there is no trailing wildcard. The fix adds a sub-branch: token ends in `/` → prefix match
(`[[ "$file" == "$token"* ]]`); otherwise the existing glob compare is untouched. Regression added
one positive (nested file under the directory token) and one negative (sibling directory with a
similar name prefix, e.g. `rgr-auto-sibling/`) to guard against over-matching by string prefix alone.
