# Trigger Workflow Discovery Checklist

Use this checklist before writing the L1 output.

## 1. Authority And Intake

- Is this a provided L1/L2 or Tech Plan intake, or a fresh discovery request?
- Which SSOT, contract, ticket, or handoff version is referenced?
- Does the referenced authority match the current project authority?
- If versions drift, has the workflow stopped instead of silently adapting stale input?

## 2. Code Area

- Which files or modules currently implement the feature or bug path?
- Which existing docs or feature records already describe it?
- Has the current behavior been read from code, not guessed?
- If a complete spec was provided, is code exploration limited to contradictions or missing evidence?

## 3. Reality Check

- Does the user's problem statement match what the code and platform can actually do?
- Are there hardware constraints that invalidate part of the request?
- Is this a bug, a missing feature, a UX issue, or a misdiagnosed hardware limitation?
- Are CameraX, Camera2, ML Kit, IMU, lifecycle, or device-tier constraints relevant?

## 4. Scope Boundaries

- What is explicitly in scope?
- What must remain out of scope for the next phase?
- Are there risky neighboring modules that should be protected from accidental expansion?
- Are forbidden files/modules named as an absolute ban rather than implied?

## 5. Readiness

- Is the L1 output specific enough for `$architecture-review` to proceed without rediscovery?
- Are the target devices, paths, and constraints explicit?
- Does the output include downstream flags or warnings for `$architecture-review`?
