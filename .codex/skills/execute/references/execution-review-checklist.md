# Execution Review Checklist

Use this checklist after Claude Code returns work.

## 1. Ticket Alignment

- Did the implementation satisfy the ticket goal?
- Did it respect the allowed file list?
- Were any extra refactors introduced?

## 2. Tech Plan Alignment

- Were the agreed architectural decisions preserved?
- Were state, UDF, and hardware defenses kept intact?

## 3. Codebase Health

- Does the result appear internally coherent?
- Is there evidence that the codebase remains buildable or logically consistent?
- Did the change leave partial work that blocks the next batch?

## 4. Next Action

- Mark done
- Issue a fix prompt
- Stop and escalate
