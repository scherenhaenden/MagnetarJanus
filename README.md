# Magnetar Janus

Professional Android media conversion and splitting, designed as the transformation-focused sibling of Magnetar Orpheus.

> [!IMPORTANT]
> Magnetar Janus is currently an early development prototype. The design foundation, real basic metadata inspection, split-planning logic, and a compatible MP4 remux path are implemented; extraction, transcoding, durable jobs, and production validation are not complete. Do not use the current build for important media.

<p align="center">
  <img src="design/ChatGPT%20Image%2011.%20Aug.%202026,%2008_08_59.png" alt="Magnetar Janus design direction" width="360">
</p>

## Product vision

Magnetar Janus opens local audio or video and provides three focused workspaces:

- **Convert** — change supported containers, codecs, resolution, frame rate, bitrate, or audio quality.
- **Split** — divide media using precise manual cuts or 30, 60, and 90-second WhatsApp presets.
- **Audio** — extract audio from video or perform audio-specific conversion and splitting.

The intended primary flow is:

```text
Select video → Split → 60 s → choose destination → Split → inspect/share outputs
```

The original source must always remain unchanged. Outputs are new files and must only be presented as complete after validation.

## Current status

| Area | Status | Notes |
| --- | --- | --- |
| Product and implementation plan | Complete | Agent-oriented milestones and acceptance criteria are documented. |
| Visual design foundation | Complete for current shell | Responsive gradient shell, drawer, preview, custom dark palette, typography, cards, selectors, loading, and error states exist. |
| Split-planning domain logic | Implemented | Automatic presets and normalized manual boundaries have unit tests. |
| Android media picker | Complete for import scope | Selects audio/video documents, retains read permission, validates MIME, and inspects off the main thread. |
| Real metadata inspection | Complete for current metadata scope | Duration, MIME, size, dimensions, codec MIME, bitrate, frame rate, container, and video preview are read from the selected URI. |
| Conversion engine | Complete for compatible MP4 remux scope | User-selected output, track progress, cancellation, and output validation are implemented; transcoding is out of scope. |
| Queue, history, and recovery | Not started | Planned for M4–M5. |
| Production release | Not ready | No signed production artifact or validated codec matrix exists. |

See [project status](docs/STATUS.md) for the verification record and [implementation plan](docs/IMPLEMENTATION_PLAN.md) for the complete roadmap.

## Design language

Janus uses a restrained, instrument-grade visual system:

- obsidian blue/charcoal backgrounds and subtly separated surfaces;
- luminous emerald for selection, progress, and success;
- monospaced technical labels with readable sans-serif body text;
- thin outlines and tonal depth instead of generic elevated Material cards;
- one continuous workspace for Convert, Split, and Audio.

The original design sources are preserved in [`design/`](design/). The archive contains the PRD, design tokens, HTML references, and screen images for the empty state, split mode, convert mode, and navigation drawer.

## Technology

- Kotlin 2.4.10
- Jetpack Compose with the 2026.06.01 BOM
- Android Gradle Plugin 9.3.1
- Gradle 9.7.0
- Minimum Android API 26
- Target/compile Android API 37

The project deliberately remains a single Android application module while its domain, UI, and platform boundaries are established.

## Build locally

Requirements:

- Android Studio or Android command-line tools with SDK 37;
- a compatible JDK (the Gradle daemon criteria currently select JDK 25);
- Git.

Clone and verify:

```bash
git clone https://github.com/scherenhaenden/MagnetarJanus.git
cd MagnetarJanus
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
```

The debug APK is generated beneath `app/build/outputs/apk/debug/`.

Install the tracked pre-push quality gate once per clone:

```bash
./scripts/install-git-hooks.sh
./gradlew ktlintCheck
```

The hook runs Ktlint, Android Lint, unit tests, and debug assembly before any push. Use `./gradlew ktlintFormat` to apply Kotlin formatting, then review the resulting diff.

Connected UI/instrumentation tests require an emulator or physical Android device:

```bash
./gradlew :app:connectedDebugAndroidTest
```

## Versioning

Android `versionName` values use the requested timestamp format:

```text
yyyy-MM-dd-HH-mm-sss
```

The last field is the second of the minute padded to three digits. A normal build generates the value at configuration time. Reproducible release or CI builds should supply it explicitly:

```bash
./gradlew :app:assembleDebug -PjanusVersion=2026-08-11-08-21-005
```

Git release tags use the same value prefixed with `v`.

## Repository map

```text
app/src/main/java/com/magnetar/janus/
├── MainActivity.kt          Android entry point and document picker
├── model/MediaModels.kt     Domain models and deterministic split logic
├── ui/MainScreen.kt         Current Compose application shell
└── ui/theme/                Janus color and typography system

design/                      Original design references
docs/                        Plan, status, and agent hand-off guidance
```

## Roadmap overview

| Milestone | Outcome |
| --- | --- |
| M0 | Confirm support matrix, media engine, storage rules, and persistence decisions. |
| M1 | Complete the design system, responsive app shell, previews, and accessibility states. |
| M2 | Read trustworthy media metadata and render real thumbnails/waveforms. |
| M3 | Finish validated Convert, Split, Audio, output, and custom/manual-cut configuration. |
| M4 | Implement safe processing, progress, cancellation, recovery, and output actions. |
| M5 | Add library, queue, history, settings, and privacy disclosures. |
| M6 | Complete compatibility testing, performance QA, licensing, signing, and release checks. |

Milestone work must meet the acceptance criteria in [docs/IMPLEMENTATION_PLAN.md](docs/IMPLEMENTATION_PLAN.md); status is tracked separately so the plan remains readable.

## Testing policy

Every deterministic domain method must have local unit coverage for its normal, boundary, and failure behavior. Compose interactions and Android platform integration require UI/instrumentation tests. Processing-engine tests will additionally require representative real-media fixtures and device-level output validation.

Passing unit tests means the covered domain logic behaves as expected; it does not mean conversion support is production-ready.

## Contributing

Read [docs/README.md](docs/README.md) and the complete implementation plan before changing code. Keep work scoped to one milestone, preserve the design sources, and include the documented hand-off fields with exact commands and results.

## License

No open-source license has been selected yet. Unless a license is added, the repository is publicly visible but no reuse rights are granted.
