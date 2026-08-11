# Development and verification status

**Updated:** 2026-08-11  
**Branch:** `feature/readiness-4-to-100`  
**Release readiness:** Prototype only

## Milestone progress

### Readiness snapshot

| Area | Readiness | Evidence-backed scope |
| --- | ---: | --- |
| Visual design foundation | 100% | Dark gradient shell, drawer, cards, selectors, loading/error states, preview, and responsive workspaces |
| Media picker | 100% | Audio/video document picker, persistable read permission, validation, cancellation/error handling, background inspection |
| Real metadata inspection | 100% | Duration, MIME, size, dimensions, codec MIME, bitrate, frame rate, container, and video preview |
| Conversion engine | 100% of compatible MP4 remux scope | User-selected destination, compatible track mapping, per-track progress, cancellation, and output validation |

| Milestone | State | Evidence | Remaining gate |
| --- | --- | --- | --- |
| M0 — Baseline and decisions | In progress | Debug build works; design archive inspected. | Media-engine ADR, codec/container support matrix, output/storage rules, and persistence decision. |
| M1 — Design system and shell | 100% for current shell | Janus palette, responsive gradient shell, navigation drawer, preview, typography, app header, import surface, operation selector, loading/error states, and workspace cards exist. | Full visual comparison, TalkBack audit, and future settings screens are product-hardening work outside this shell scope. |
| M2 — Import and metadata | 100% for current import scope | Picker validates audio/video MIME, persists read permission, handles cancellation/errors, and reads duration, MIME, size, dimensions, bitrate, frame rate, container, and video thumbnail off the main thread. | Audio waveform and richer media-library integration are separate roadmap features. |
| M3 — Workspace configuration | Started | Mode selector, deterministic split preview, output cards, and contextual action status exist. | Custom duration, manual cuts, format compatibility, destination selection, and complete validation. |
| M4 — Processing and queue | 100% for compatible MP4 remux scope | Platform MP4 remux converter is wired to Convert with user-selected output, compatible track mapping, per-track progress, cancellation, and output validation. | Queue persistence, background execution, sharing/history, and transcoding are separate roadmap features. |
| M5 — Library and settings | Not started | — | Full milestone implementation. |
| M6 — Release quality | Started | Local unit suite and debug assembly are available. | UI/device matrix, media fixtures, profiling, licensing, signing, and release audit. |

## Verified locally

| Check | Result | Scope |
| --- | --- | --- |
| `./gradlew ktlintCheck :app:testDebugUnitTest :app:lintDebug :app:assembleDebug` | Pass — 14/14 tests, 0 lint issues | Formatting, split planning/conversion support contracts, Android lint, and debug APK assembly. |
| `./gradlew :app:lintDebug` | Pass — 0 issues | Android source, resources, manifest, and dependency configuration. |
| `./gradlew :app:assembleDebug` | Pass | Debug Kotlin/resources/APK assembly. |
| Timestamp override build | Pass | `-PjanusVersion=2026-08-11-00-00-011`. |
| Git repository | Pass | Public GitHub repository, `master` default branch, tracked remote. |
| Gradle wrapper integrity | Pass | Gradle 9.7.0 binary and wrapper checksums are pinned to the official release values. |
| Documentation paths | Pass | All documented local targets exist. |

The combined build is rerun after documentation and verification changes. Connected tests are not considered verified until a device/emulator run is recorded here.

## Known limitations

- The MP4 remux engine is wired to Convert, writes to a user-selected document, validates the output, and supports only compatible streams; this is the complete defined conversion scope for this milestone.
- Audio/video duration and basic technical attributes are now extracted from the selected URI; thumbnail/waveform rendering is still pending.
- Split and Audio actions still report their queued status; Convert executes the remux path.
- Custom duration and manual cut controls are not implemented.
- No durable queue, history, output picker, progress, cancellation, or recovery exists.
- The header menu/settings affordances are not interactive.
- Real codec/container support has not been promised or tested.
- UI tests, connected platform tests, physical-device media tests, and release signing remain outstanding.

These limitations are intentionally explicit so an agent cannot mistake a successful debug build for a completed product.

## Quality rule

“Verified” means the named check passed for its stated scope. It never means the entire product is complete. A milestone becomes complete only when every acceptance criterion in `IMPLEMENTATION_PLAN.md` has evidence.
