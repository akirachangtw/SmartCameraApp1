---
name: android-ui-design
description: Design, critique, redesign, implement, or verify Android app interfaces, especially Jetpack Compose and Material 3. Use when a task involves Android UI screenshots, Compose or XML layouts, visual polish, UX hierarchy, accessibility, touch ergonomics, adaptive layouts, system bars/insets, UI states, camera overlays, HTML design prototypes, or screenshot-based regression review. Enforces an evidence-first workflow and separates analyze, prototype, implement, and verify modes. Produces an HTML prototype for design discussion before implementation when a meaningful redesign is requested, and requires explicit approval before changing Android UI code.
compatibility: Intended for Android projects using Jetpack Compose or legacy Views/XML. Optimized for vision-capable coding agents such as Claude Opus 5.5. HTML prototypes are discussion artifacts; native Compose Preview, screenshot tests, emulator, or device evidence remains authoritative.
metadata:
  version: "0.1.0-mvp"
  domain: "android-ui"
---

# Android UI Design Review

Improve Android UI quality without confusing visual taste with verified defects. Analyze evidence first, prototype meaningful redesigns before coding, implement only after approval, and verify with native Android evidence.

## Core principles

1. **Task before decoration.** Identify the screen's primary user task before changing style.
2. **Evidence before verdict.** Separate visible facts from inference and preference.
3. **Android before web.** Respect Android interaction, accessibility, system UI, adaptive layout, and runtime constraints.
4. **Prototype before implementation.** For meaningful redesigns, create an HTML discussion prototype and wait for approval before editing Android UI code.
5. **Native evidence is authoritative.** HTML validates direction only. Compose Preview, screenshot tests, emulator, and device captures validate implementation.
6. **MVP restraint.** Prefer the smallest reversible change that fixes the proven problem. Reuse existing theme tokens and components before adding dependencies or abstractions.
7. **Do not invent defects.** A review may conclude that no change is necessary.

## Select one mode

Infer the mode from the request. If ambiguous, default to `analyze` and do not edit code.

- **analyze** — Inspect screenshots, UI code, design specs, or runtime evidence. Produce findings and recommendations only.
- **prototype** — Produce a reviewable HTML prototype or compare up to two design directions. Do not modify Android code.
- **implement** — Apply an explicitly approved design to Compose or Views/XML.
- **verify** — Compare the native result against the approved direction and Android constraints. Modify code only if explicitly authorized.

Never jump from `analyze` or `prototype` to `implement` merely because code access exists.

## Minimum inputs

Use available inputs; do not block unnecessarily. For a reliable review, prefer:

- Screenshot or screen recording preserving system bars, keyboard, dialogs, and relevant window boundaries.
- One-sentence primary task and target user.
- Relevant `@Composable`, XML layout, View code, theme, and design-token files.
- Required UI states: loading, content, empty, error, disabled, selected, permission, offline, or domain states.
- Device constraints: phone, tablet, foldable, portrait, landscape, multi-window, minimum SDK, input methods.
- For camera or overlay screens: preview crop/scale type, coordinate spaces, sensor/display rotation, overlay ownership, gesture bounds, and runtime state authority.

If evidence is missing, continue with clearly labeled uncertainty instead of pretending certainty.

## Workflow

### Step 1 — Establish scope

State briefly:

- Target screen or flow.
- Current mode.
- Primary task.
- Available evidence.
- Missing evidence that could change the verdict.
- Files or modules that are in scope.

Do not inspect or modify unrelated modules without a demonstrated dependency.

### Step 2 — Extract observable facts

Perform a fact pass before giving opinions. Record only what the evidence supports.

Good observations:

- "Primary and secondary actions use the same visual weight."
- "The selected state is visible by color alone."
- "The content overlaps the bottom control region in the supplied landscape capture."

Do not claim properties a screenshot cannot prove. Label uncertainty as:

- `needs-code-check` — semantics, touch target, token source, navigation behavior.
- `needs-runtime-check` — insets, IME, rotation, animation, performance, camera coordinates.
- `needs-user-test` — comprehension, preference, discoverability, task speed.
- `aesthetic-preference` — optional style choice, not a defect.

### Step 3 — Classify the screen

Classify the dominant interaction model before proposing layout:

- Content browsing
- Form or data entry
- List/detail or management
- Review/approval
- Capture/camera
- Creation/editor
- Monitoring/operator console
- Settings/configuration
- Onboarding/education

Use the classification to choose information density, navigation, action placement, and state presentation. Do not reuse landing-page patterns in product screens.

### Step 4 — Review nine axes

Review only relevant axes, but explicitly mark omitted axes.

1. **Task hierarchy** — Is the primary task obvious? Are primary and secondary actions distinguishable?
2. **Information architecture** — Are navigation, content, actions, and status grouped logically?
3. **Component semantics** — Do Material or custom components match their behavioral meaning?
4. **Touch ergonomics** — Are targets, spacing, reachability, gestures, and destructive actions safe?
5. **Accessibility** — Check semantics, labels, focus order, contrast, font scaling, non-color cues, and reduced motion.
6. **State completeness** — Check loading, empty, error, disabled, selected, permission, offline, and domain-specific states.
7. **Adaptive behavior** — Check resize, orientation, aspect ratio, tablet/foldable posture, and multi-window behavior.
8. **System integration** — Check edge-to-edge, cutouts, status/navigation bars, IME, predictive back, permissions, and lifecycle-visible feedback.
9. **Implementation health** — Check state hoisting, recomposition risks, hardcoded values, token reuse, component reuse, Preview coverage, and testability.

Use Android and Material guidance as constraints, not as a command to make every app look identical.

### Step 5 — Report findings

Use four severities:

- **BLOCKER** — Prevents the primary flow, hides critical information, produces incorrect UI meaning, or creates an accessibility barrier.
- **MAJOR** — Materially harms hierarchy, navigation, state clarity, adaptive behavior, or task efficiency.
- **MINOR** — Local spacing, typography, icon, alignment, density, or consistency issue.
- **AESTHETIC** — Optional visual refinement with no demonstrated task impact.

Each finding must include:

- **Evidence** — What was observed and from which artifact/state.
- **Impact** — Why it matters to the task or platform behavior.
- **Recommendation** — The smallest feasible correction.
- **Verification** — How to prove the correction worked.
- **Confidence** — High, medium, or low.

Do not inflate severity. External visual advice must be re-evaluated against local runtime and code evidence.

## Redesign direction

For a meaningful redesign, define one primary direction and at most one fallback. Do not generate many superficial alternatives.

Before prototyping, specify:

- Product purpose and audience.
- Interaction model.
- Visual tone in concrete terms.
- Information-density target.
- Typography roles.
- Color and surface strategy.
- Shape/elevation strategy.
- Motion purpose.
- Distinctive product characteristic.
- Existing tokens/components to preserve.

Avoid generic AI aesthetics unless explicitly requested:

- Purple/blue gradient as the default identity.
- Giant hero regions inside functional screens.
- Excessive glassmorphism, glow, or decorative blobs.
- Every region becoming a large rounded card.
- Nested cards that hide hierarchy.
- Web-dashboard layouts forced onto phones.
- Material components used without a coherent hierarchy.
- Decoration replacing state clarity.

## HTML prototype gate

Create an HTML prototype before Android implementation when the request involves meaningful visual restructuring, a new screen, a disputed direction, or multi-state interaction redesign.

Skip HTML only when:

- The user explicitly requests direct implementation.
- The change is a small local correction with no design ambiguity.
- A native Compose prototype already exists and is the approved design source.

### Prototype requirements

The HTML artifact must:

- Represent an Android product screen, not a marketing page.
- Show realistic phone/tablet framing only when it helps review.
- Include actual information architecture and representative content.
- Provide controls to inspect relevant UI states.
- Demonstrate hierarchy, surfaces, typography, spacing, navigation, actions, and feedback.
- Include light/dark variants when in scope.
- Include phone and larger-window views when adaptive behavior is in scope.
- Identify which details are conceptual and require native validation.
- Avoid behavior that cannot map plausibly to Compose or Android Views.

The prototype validates:

- Direction
- Hierarchy
- Grouping
- State model
- Interaction concept
- Visual language

The prototype does **not** validate:

- Exact dp/sp metrics or Android font rendering
- Material component geometry or ripple behavior
- TalkBack semantics or focus order
- System bars, display cutouts, IME, or edge-to-edge insets
- Predictive back and permission behavior
- Compose performance/recomposition
- Camera preview surfaces or coordinate transforms

After producing the prototype, stop and request one decision:

- `APPROVE` — proceed to native implementation.
- `REVISE: <specific changes>` — update prototype only.
- `REJECT` — preserve current implementation.

Do not treat silence as approval.

## Implementation mode

Enter only after explicit approval or an explicit request to bypass prototyping.

### Before editing

- Re-read the approved direction and findings.
- Identify the smallest file set.
- Reuse the existing design system, theme, components, icons, and dependencies.
- Confirm which behaviors and contracts must not change.
- Preserve business logic and runtime state authority unless the task explicitly includes them.

### Compose-first rules

For modern Android UI:

- Prefer Jetpack Compose for new UI when consistent with the project.
- Hoist state to the lowest appropriate owner; keep presentational composables stateless where practical.
- Use `MaterialTheme`, project tokens, and reusable components instead of scattered literals.
- Preserve semantic roles and content descriptions; prefer native semantics before custom workarounds.
- Make previews represent real states, not only a happy-path placeholder.
- Avoid speculative dependencies and abstractions.
- Avoid unrelated architecture migration during a visual task.

For legacy Views/XML, improve within the current stack unless migration is explicitly requested and justified.

### Required native previews

Create or update previews for applicable combinations:

- Light and dark theme
- Compact phone
- Landscape or resized window when relevant
- Medium/expanded window when adaptive behavior is relevant
- Default/content state
- Empty/error/loading/disabled/selected states as applicable
- Increased font scale for text-heavy screens

Do not fabricate preview data that could be mistaken for production evidence; clearly identify preview fixtures.

## Verification mode

HTML approval is not completion. Verify the native result.

Use the least expensive available proof, in this order:

1. Compose Preview or layout preview.
2. Build/compile and relevant UI tests.
3. Preview screenshot comparison or existing screenshot-test framework.
4. Emulator evidence.
5. Physical-device evidence for camera, sensors, cutouts, OEM behavior, or performance-sensitive UI.

Verify:

- Approved hierarchy and interaction direction.
- All required UI states.
- Light/dark behavior.
- Font scaling and text truncation.
- Touch target and spacing behavior.
- Edge-to-edge, insets, IME, and navigation.
- Adaptive layouts rather than merely stretched compact layouts.
- Accessibility semantics and keyboard/D-pad behavior where relevant.
- Camera overlay alignment across required orientations and crop modes when applicable.

If native evidence conflicts with the HTML prototype, native evidence wins. Record the conflict and revise the implementation or the approved design assumption.

## Camera and overlay screens

Treat camera UI as a runtime-contract task, not only a visual-design task.

Before changing overlays, determine:

- The state owner and rendering consumer.
- Sensor, buffer, analysis, preview, view, and screen coordinate spaces.
- Rotation and mirroring transforms.
- Preview scale/crop type.
- Overlay clipping and system inset boundaries.
- Gesture ownership and conflict with camera controls.
- Detection eligibility, stabilization, suppression, and fallback states.

Do not infer coordinate correctness from a visually plausible static screenshot. Require runtime evidence across the supported orientations and representative devices.

## Output formats

### Analyze output

```markdown
## Scope
- Mode:
- Screen/flow:
- Primary task:
- Evidence reviewed:
- Evidence gaps:

## Verdict
One concise paragraph.

## Findings
### [SEVERITY] Short title
- Evidence:
- Impact:
- Recommendation:
- Verification:
- Confidence:

## Direction
- Primary direction:
- Optional fallback:
- Preserved constraints:

## Next gate
Prototype, implement, verify, or no change.
```

### Prototype output

Provide:

1. Design diagnosis.
2. Primary direction and optional fallback.
3. Reviewable HTML prototype.
4. State coverage list.
5. Native-feasibility notes.
6. Explicit `APPROVE / REVISE / REJECT` gate.

### Implement output

Provide:

1. Approved direction used.
2. Files changed.
3. Behavior deliberately preserved.
4. Preview/test evidence produced.
5. Known gaps requiring emulator/device validation.

### Verify output

Provide:

1. `PASS`, `PASS WITH GAPS`, or `REVISE`.
2. Evidence matrix by state/configuration.
3. Visual or behavioral deviations.
4. Minimal corrective action.
5. Unverified device/runtime risks.

## Stop conditions

Stop and ask for evidence or approval when:

- The requested implementation has no approved direction and redesign scope is meaningful.
- Screenshots and code materially contradict each other.
- Runtime behavior cannot be inferred safely.
- A proposed change would alter business logic or state authority beyond scope.
- Camera coordinate correctness lacks runtime evidence.
- The only remaining disagreements are subjective aesthetic preferences.

## Self-check

Before finishing, verify:

- Did the response select exactly one mode?
- Were observations separated from inference?
- Does every defect have evidence and verification?
- Were local code/runtime protections considered before severity?
- Was HTML kept as a discussion artifact rather than Android truth?
- Was explicit approval required before implementation?
- Were changes kept minimal and reversible?
- Were adaptive, accessibility, state, and system-UI concerns considered where relevant?
- Was native evidence used for the final verdict?
- Was unnecessary governance, dependency, or abstraction avoided?
