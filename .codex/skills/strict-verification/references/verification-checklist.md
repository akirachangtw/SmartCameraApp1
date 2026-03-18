# Strict Verification Checklist

Use this checklist before issuing the final verdict.

## 1. Intent Match

- Does the code match the Tech Plan Spec?
- Does the code match the executed ticket rather than an improvised approach?

## 2. Defensive Pattern Preservation

- Were state, UDF, and hardware invariants preserved?
- Did the implementation introduce hacks, bypasses, or silent semantic drift?

## 3. Evidence From Runtime

- Do logs or user tests reveal only local bugs, or do they invalidate the plan?
- Is the observed failure architectural or implementation-specific?

## 4. Verdict Discipline

- Approve only when intent and behavior both hold
- Create fix tickets when the plan survives but implementation missed
- Escalate when the plan itself no longer stands
