# Development and verification status

**Updated:** 2026-08-11  
**Branch:** `feature/readiness-4-to-100`  
**Release readiness:** Prototype only

## Milestone progress

| Milestone | State | Evidence | Remaining gate |
| --- | --- | --- | --- |
| M0 — Baseline and decisions | In progress | Debug build works; design archive inspected. | Media-engine ADR, codec/container support matrix, output/storage rules, and persistence decision. |
| M1 — Design system and shell | In progress | Janus palette, responsive gradient shell, typography, app header, import surface, operation selector, and workspace cards exist. | Navigation drawer, previews, semantic spacing/shape tokens, accessibility audit, and visual comparison. |
| M2 — Import and metadata | In progress | System document picker now reads duration, MIME, size, video dimensions, and container metadata off the main thread. | Add thumbnail/waveform and richer codec/frame-rate recovery states. |
| M3 — Workspace configuration | Started | Mode selector, deterministic split preview, output cards, and contextual action status exist. | Custom duration, manual cuts, format compatibility, destination selection, and complete validation. |
| M4 — Processing and queue | Started | Platform MP4 remux converter is implemented behind `MediaConverter` and wired to Convert with app-private output. | User-selected destination, progress, cancellation, queue persistence, output validation, and transcoding. |
| M5 — Library and settings | Not started | — | Full milestone implementation. |
| M6 — Release quality | Started | Local unit suite and debug assembly are available. | UI/device matrix, media fixtures, profiling, licensing, signing, and release audit. |

## Verified locally

| Check | Result | Scope |
| --- | --- | --- |
| `./gradlew :app:testDebugUnitTest` | Pass — 13/13 | Split planning and conversion support contracts. |
| `./gradlew :app:lintDebug` | Pass — 0 issues | Android source, resources, manifest, and dependency configuration. |
| `./gradlew :app:assembleDebug` | Pass | Debug Kotlin/resources/APK assembly. |
| Timestamp override build | Pass | `-PjanusVersion=2026-08-11-00-00-011`. |
| Git repository | Pass | Public GitHub repository, `master` default branch, tracked remote. |
| Gradle wrapper integrity | Pass | Gradle 9.7.0 binary and wrapper checksums are pinned to the official release values. |
| Documentation paths | Pass | All documented local targets exist. |

The combined build is rerun after documentation and verification changes. Connected tests are not considered verified until a device/emulator run is recorded here.

## Known limitations

- The MP4 remux engine is wired to Convert, but currently writes to app-private cache and supports only compatible streams.
- Audio/video duration and basic technical attributes are now extracted from the selected URI; thumbnail/waveform rendering is still pending.
- The main action buttons are still visual and do not execute media jobs.
- Custom duration and manual cut controls are not implemented.
- No durable queue, history, output picker, progress, cancellation, or recovery exists.
- The header menu/settings affordances are not interactive.
- Real codec/container support has not been promised or tested.
- UI tests, connected platform tests, physical-device media tests, and release signing remain outstanding.

These limitations are intentionally explicit so an agent cannot mistake a successful debug build for a completed product.

## Quality rule

“Verified” means the named check passed for its stated scope. It never means the entire product is complete. A milestone becomes complete only when every acceptance criterion in `IMPLEMENTATION_PLAN.md` has evidence.
