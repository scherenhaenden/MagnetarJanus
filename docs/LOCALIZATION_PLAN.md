# Localization plan

Janus will ship English as the source language and keep all user-facing text in Android resources. Codec names, file extensions, and technical units remain unchanged across locales.

## Language rollout

| Phase | Locale | Scope |
|---|---|---|
| L0 | `en` | Source strings, accessibility labels, errors, empty/loading states, and technical help |
| L1 | `es` | Spanish UI and onboarding; first translation because the product owner uses Spanish and English |
| L2 | `de` | German UI for the initial European release |
| L3 | `fr`, `pt-BR` | Community-supported translations after the string catalog stabilizes |

## Implementation rules

1. Add every visible string to `res/values/strings.xml`; never concatenate translated sentences in Kotlin.
2. Use `plurals` for files, segments, tracks, and queue counts.
3. Keep technical labels such as `H.264`, `AAC`, `MP4`, `FPS`, and `kbps` as placeholders or non-translatable values.
4. Give every icon-only control a translated `contentDescription`.
5. Preserve layout space for German and Spanish expansion; do not encode text into images.
6. Translate error messages by intent, not word-for-word implementation details.
7. Run pseudo-localization and RTL layout checks before adding a locale to release builds.

## Translation workflow

1. Extract hard-coded Compose labels into English resources.
2. Add Spanish resources and a string review checklist.
3. Add locale screenshots for empty, import, processing, error, split, convert, and audio states.
4. Run `lintDebug` and Compose UI tests with each locale selected.
5. Record translator attribution and update the release notes.

## Current status

The resource catalog currently contains the application name only. The next localization implementation step is to migrate `MainScreen.kt` and `MainActivity.kt` labels/messages into resources before adding translated values.
