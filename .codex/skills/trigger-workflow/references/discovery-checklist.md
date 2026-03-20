# Trigger Workflow Discovery Checklist

Use this checklist before writing the L1 output.

## 1. Code Area

- Which files or modules currently implement the feature or bug path?
- Which existing docs or feature records already describe it?
- Has the current behavior been read from code, not guessed?

## 2. Reality Check

- Does the user's problem statement match what the code and platform can actually do?
- Are there hardware constraints that invalidate part of the request?
- Is this a bug, a missing feature, a UX issue, or a misdiagnosed hardware limitation?

## 3. Scope Boundaries

- What is explicitly in scope?
- What must remain out of scope for the next phase?
- Are there risky neighboring modules that should be protected from accidental expansion?

## 4. Readiness

- Is the L1 output specific enough for `$architecture-review` to proceed without rediscovery?
- Are the target devices, paths, and constraints explicit?
