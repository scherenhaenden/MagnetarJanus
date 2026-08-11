# MagnetarJanus documentation

This directory is the implementation source of truth for agents working on MagnetarJanus.

| Document | Purpose |
| --- | --- |
| [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) | Ordered milestones, ownership boundaries, acceptance criteria, and verification commands. |
| [STATUS.md](STATUS.md) | Current implementation progress, verified checks, and known limitations. |

The repository-level [`README.md`](../README.md) is the product overview and onboarding entry point. Keep it concise and accurate; put detailed execution requirements in the plan and time-sensitive completion evidence in the status document.

## Design sources

Do not copy or edit design-source files during product work. Use them as references:

- `../design/ChatGPT Image 11. Aug. 2026, 08_08_59.png` — visual direction image.
- `../design/stitch_magnetar_janus_media_converter.zip` — screen references, HTML prototypes, PRD, and design-system notes.

The archive contains `magnetarjanus_prd.md`, `magnetar_janus/DESIGN.md`, and four screen variants: empty import, split mode, navigation drawer, and active conversion. Extract it into a temporary directory only when inspection is needed; do not commit extracted copies unless the team explicitly decides to version them.

## Working agreement

1. Read the whole implementation plan before changing code, then work only on the assigned milestone.
2. Treat the design references and plan acceptance criteria as requirements. Record a deliberate product decision when they conflict.
3. Keep UI state independent of Android and codec APIs so preview/tests do not require a real file or encoder.
4. Do not claim format, codec, or metadata support until it is implemented and tested on a physical/emulated device.
5. Run the milestone verification steps and update its status when handing work to the next agent.
6. Never mark a milestone complete in `STATUS.md` without evidence for every acceptance criterion.

## Before pushing

Install the tracked pre-push hook once per clone:

```bash
./scripts/install-git-hooks.sh
```

It blocks `git push` unless Ktlint formatting, Android Lint, unit tests, and debug assembly pass. Run `./gradlew ktlintFormat` only when you intend to apply the official Kotlin formatting changes; review the diff before committing.
