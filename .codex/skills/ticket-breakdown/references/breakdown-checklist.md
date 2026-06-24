# Ticket Breakdown Checklist

Use this checklist before finalizing tickets.

## 1. Coverage

- Does the ticket set cover the full Tech Plan?
- Is each architectural decision mapped to at least one ticket?
- Does each ticket cite the specific SSOT, contract, or Tech Plan section it implements?
- Has untraceable work been flagged instead of converted into a ticket?

## 2. Isolation

- Can each ticket be implemented without broad refactoring?
- Does each ticket have a narrow file boundary?
- Would a coding agent know exactly where it is allowed to work?
- Is the absolute ban list explicit enough to prevent adjacent cleanup or opportunistic rewrites?

## 3. Sequencing

- Are dependencies ordered correctly?
- Are state and type definitions introduced before downstream consumers?
- Are adapter or hardware changes introduced before UI wiring that depends on them?
- Are parallel-safe tickets separated from tickets that depend on the same file or state contract?
- Does each ticket state the expected codebase state before it starts?

## 4. Verification

- Does each ticket have concrete acceptance criteria?
- Is the required defensive warning included verbatim?
- Would `$strict-verification` be able to judge the ticket outcome clearly?
- Are verification commands or named tests precise enough to run without interpretation?
