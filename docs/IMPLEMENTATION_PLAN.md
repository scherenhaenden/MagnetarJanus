# MagnetarJanus implementation plan

**Status:** In development; see [STATUS.md](STATUS.md) for current progress
**Last updated:** 2026-08-11  
**Product:** Android app for professional media conversion, extraction, and splitting.

## 1. Objective and release boundary

Build an Android application that lets a user select local audio/video media, inspect it, convert or extract it in supported formats, and split it manually or into WhatsApp-friendly duration presets. The original input must never be modified.

The first production release is complete only when a user can reliably perform this primary flow on a device:

`Select video → choose Split → choose 60 s → choose destination → Split → see progress → open/share the outputs.`

Implement audio conversion/extraction alongside this flow only where the selected processing engine demonstrably supports it. Unsupported combinations must be disabled with an explanatory message, never silently downgraded.

## 2. Authoritative inputs and decision rule

| Priority | Source | Use |
| --- | --- | --- |
| 1 | This plan | Scope, sequencing, architecture, and acceptance criteria. |
| 2 | `design/stitch_magnetar_janus_media_converter.zip` | Screen layouts, interaction direction, PRD, and visual design system. |
| 3 | `design/ChatGPT Image 11. Aug. 2026, 08_08_59.png` | Overall visual mood and sibling-product relationship. |
| 4 | Current app source | Technical constraints and existing implementation. |

When references disagree, preserve the interaction described in the PRD and the visual language in `DESIGN.md`. Capture the decision in the PR/hand-off notes. Example: consolidate the design greens into semantic theme tokens instead of scattering the slightly different supplied hex values.

## 3. Non-negotiable product rules

- Use a dark, instrument-grade custom Compose UI—not a default Material sample.
- Emerald is reserved for active/selected/success/progress states; normal actions remain restrained.
- Keep `CONVERT`, `SPLIT`, and `AUDIO` on one main workspace; switching mode changes the content below the selector.
- The split workspace is the product centerpiece. Presets are 30, 60, and 90 seconds plus a validated custom duration.
- Show manual cuts as precise thin timeline boundaries. Manual cuts and automatic duration splitting are distinct modes; do not combine them ambiguously.
- The app writes new output files only. Input URIs are read-only from the app's perspective.
- The UI always distinguishes: ready, validating, queued, processing, succeeded, cancelled, and failed.
- Process work survives configuration changes and reports recoverable failures. Background processing must obey current Android foreground-service/notification requirements.

## 4. Architecture target

Use a layered, feature-oriented structure. Exact packages may vary, but dependencies must point inward:

```text
ui (Compose screens/components) → presentation (ViewModels/UI state) → domain (use cases/models)
                                                                  ↓
                                                        data/platform (MediaStore, SAF, worker, codec engine)
```

Suggested modules remain a single `app` module initially to avoid premature Gradle complexity. Create package boundaries first:

```text
com.magnetar.janus
├── app                 # activity, navigation, dependency wiring
├── core/designsystem   # tokens and reusable visual primitives
├── core/model          # MediaInfo, Operation, Job, SplitPlan, OutputOptions
├── feature/importmedia
├── feature/workspace   # convert/split/audio screens and state
├── feature/queue
├── domain              # interfaces and use cases
└── data                # Android URI, metadata, persistence, processor implementations
```

### State and processing contracts

- UI state is immutable and renders from state alone. File picking and processors are injected behind interfaces.
- Persist selected-job configuration and queue/history metadata locally; persist only URI permissions granted by the picker.
- A `MediaJob` contains input URI, operation, output options, split plan, output destination, state, progress, diagnostics, and output URIs.
- A processor emits structured progress: fraction, current unit/total units, elapsed time, and optional estimated output size. Never infer successful output from progress alone.
- Cancellation is cooperative and cleans incomplete temporary outputs. Final outputs are made visible only after success.

## 5. Milestones

Work in order. An agent may start a milestone only after the previous milestone's acceptance criteria pass, except for explicitly read-only research.

### M0 — Baseline and technical decisions

**Goal:** establish the implementation constraints before visual or processing work.

Tasks:

1. Build and run the starter app; record the Android/Gradle/Kotlin versions actually used.
2. Inspect the design archive and create a traceable screen/component inventory.
3. Decide and document the media engine after a small proof of concept. Evaluate Android platform APIs first; select a bundled/native engine only if it is necessary for the promised container/codec matrix.
4. Define the v1 support matrix by input type, operation, output container, codec, and API level. Include explicit exclusions.
5. Decide output naming, destination selection, file collision behavior, and free-space preflight rules.
6. Decide the persistence approach for queue/history and URI permissions.

Deliverables: support matrix, processor decision record, and a runnable baseline.

Acceptance criteria:

- `./gradlew :app:assembleDebug` succeeds.
- Each v1 operation has a confirmed implementation path, or is explicitly marked out of scope in the UI/product matrix.
- No library is introduced merely because a design prototype uses it.

### M1 — Design system and app shell

**Goal:** create the visual foundation shared by every screen.

Tasks:

1. Replace the generated starter theme with semantic color, typography, spacing, shape, border, and glow tokens derived from `DESIGN.md`.
2. Add packaged/licensed fonts only after confirming distribution rights; otherwise choose deliberate compatible fallbacks.
3. Implement the header brand, navigation drawer, settings affordance, dark background treatment, and edge-to-edge insets.
4. Create reusable primitives: studio card, outlined selection control, technical label/readout, thin progress bar, primary restrained action, and empty/error states.
5. Add Compose previews covering normal, selected, disabled, and narrow-screen states.

Acceptance criteria:

- Empty app shell visually follows the supplied `janus_import_empty` reference without default Material styling leaking through.
- Text, borders, focus/pressed states, and icon contrast are accessible in the dark theme.
- No screen hard-codes colors, spacing, or typography that belongs in a token.

### M2 — Import and metadata inspection

**Goal:** safely select local media and render real metadata.

Tasks:

1. Implement an Android document/media picker that accepts audio and video, including persisted read permission where supported.
2. Validate URI readability, media type, duration, and basic metadata with graceful fallbacks for missing fields.
3. Build empty, loading, populated-video, populated-audio, and unsupported/error import states.
4. Render a video frame/thumbnail and audio waveform placeholder pipeline; use production waveform analysis only when the selected architecture can generate it off the main thread.
5. Add remove/replace-media behavior that safely clears dependent draft options.

Acceptance criteria:

- A picked media item shows filename, duration, size, and available technical attributes without blocking the main thread.
- Audio and video appear materially different as required by the designs.
- Revoked/unreadable URI produces a useful recovery action, not a crash.

### M3 — Workspace configuration

**Goal:** implement all decisions before executing media transformations.

Tasks:

1. Implement the `CONVERT | SPLIT | AUDIO` selector and preserve/reconcile per-operation draft state.
2. Convert: source-to-target mapping, only compatible target formats, and progressive disclosure for advanced options.
3. Split: timeline display, seek/playhead state, 30/60/90/custom presets, deterministic segment preview, and manual `Split here` boundaries.
4. Audio: extraction choices for video and audio-specific conversion/splitting choices for audio inputs.
5. Implement output format/quality/destination cards and a contextual primary action label.
6. Validate the whole configuration before the action becomes enabled; present specific, local validation messages.

Required split-plan rules:

- Duration preset segments cover `[0, duration]` consecutively, with each segment no longer than the selected duration.
- Custom duration is positive, bounded by the media duration, and displayed consistently.
- Manual boundaries are sorted, unique, strictly within the source duration, and result in non-empty segments.
- A selected automatic preset replaces its own generated preview; it does not mutate manual boundaries.

Acceptance criteria:

- For `04:37` with `60 s`, the UI preview is five pieces: `01:00, 01:00, 01:00, 01:00, 00:37`.
- Changing source media or operation cannot execute stale options from the previous selection.
- All controls are usable with touch, TalkBack labels, and small-screen scrolling.

### M4 — Processing engine and queue

**Goal:** execute validated jobs correctly and transparently.

Tasks:

1. Implement the processor interface and one job execution path end-to-end before adding every format variation.
2. Build durable queue/job persistence and foreground/background execution appropriate to the supported Android versions.
3. Implement conversion, audio extraction, and automatic/manual splitting according to the M0 support matrix.
4. Write to a temporary target, validate output existence/readability, then finalize it atomically where the destination supports it.
5. Surface progress, current segment, output estimate when available, cancellation, retry, and actionable failures.
6. Add output actions: open, share, reveal destination, and remove failed temporary artifacts.

Acceptance criteria:

- The primary v1 flow produces playable segments with no segment longer than the chosen duration.
- Cancelling does not alter the source and leaves no falsely completed output in the media library.
- Process interruption/restart is represented honestly in the queue; the app never reports a job successful without validated outputs.

### M5 — Library, history, and settings

**Goal:** make completed work manageable.

Tasks:

1. Implement the navigation destinations shown by the reference: Library, Processing Queue, History, Device Settings, and About.
2. History lists input summary, operation, time, state, output location, and retry/open/share actions as applicable.
3. Settings expose only implemented preferences, such as default destination and naming behavior.
4. Add privacy-first disclosures: local processing behavior, selected-file permission expectations, and diagnostic data policy.

Acceptance criteria:

- Navigation destinations are functional or explicitly omitted until implemented—never dead UI.
- History and queue states remain understandable after app recreation.

### M6 — Quality, compatibility, and release

**Goal:** prove that the app is dependable on its stated support boundary.

Tasks:

1. Add unit tests for split calculations, validation, output names, state transitions, and failure mapping.
2. Add UI tests for empty/imported states, operation switching, duration presets, manual split, and disabled-action validation.
3. Add device/instrumentation tests for picker permissions, worker recovery, and representative media jobs.
4. Test the complete support matrix with real fixtures: short/long media, unusual metadata, no audio track, audio-only, large file, near-full storage, cancellation, revoked URI, and unsupported codec.
5. Profile main-thread work, memory use for thumbnails/waveforms, battery/thermal behavior for long jobs, and storage cleanup.
6. Perform visual comparison against all supplied design screens at common phone widths; correct visible regressions.
7. Complete release basics: app label/icon, versioning, privacy/data safety disclosures, notices for bundled codecs/licenses, signed build, and install/upgrade smoke test.

Acceptance criteria:

- All automated checks pass and the documented v1 support matrix has a recorded device-test result for every supported row.
- No known source-file mutation, output loss, crash, or false-success defect remains open.
- `./gradlew :app:assembleDebug :app:testDebugUnitTest` succeeds; run connected tests when a device/emulator is available.

## 6. Agent hand-off template

Every milestone hand-off must include the following, in the PR description or task note:

```md
Milestone: M?
Scope completed:
Design references consulted:
Decisions made (and rationale):
Files changed:
Verification run (command + result):
Known limitations / deferred work:
Next exact task:
```

Do not hand off work with vague phrases such as “UI is mostly complete.” Name the implemented states and the verification result.

## 7. Definition of done for any change

- The behavior matches a requirement in this plan or an approved decision record.
- Existing unrelated changes are preserved.
- Compose code separates rendering from side effects and has preview/test coverage appropriate to the change.
- User-visible failure paths are considered, not just happy paths.
- Build/test commands relevant to the touched area pass, or the exact pre-existing failure is recorded.
- The next agent can discover the feature, state model, verification method, and remaining limitation from code and hand-off notes alone.
