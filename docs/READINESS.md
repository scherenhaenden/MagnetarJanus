# Magnetar Janus — readiness

**Updated:** 2026-08-11  
**Branch:** `feature/universal-transcoding`

## Product readiness

| Area | Readiness | Current status |
|---|---:|---|
| Repository setup | 100% | Public GitHub repository, `master`, feature branch, and CI |
| Documentation | 100% | README, plan, status, design references, and transcoding matrix |
| Build system | 100% | Gradle 9.7.0; debug APK assembles |
| Unit tests | 100% of current domain | 17/17 tests pass |
| Code quality | 100% | Ktlint + Android Lint; 0 issues |
| Visual design foundation | 100% of current shell | Dark gradient shell, drawer, cards, selectors, states, preview, and responsive workspaces |
| Media picker | 100% of import scope | Audio/video picker, persistable permissions, MIME validation, and background inspection |
| Real metadata inspection | 100% of current scope | Duration, MIME, size, dimensions, codec, bitrate, frame rate, container, and preview |
| Conversion engine | 100% of compatible remux scope | MP4 remux, user-selected destination, compatible tracks, progress, cancellation, and validation |
| Audio extraction | 100% of compatible scope | Audio tracks exported to MP4/M4A without re-encoding |
| Actual file splitting | 100% of compatible scope | Real time-bounded segments with normalized timestamps |
| Custom/manual cuts | 100% of current scope | Normalized manual boundaries executed as independent outputs |
| Queue/history/recovery | 100% of durable record scope | Queued/running/complete/failed/cancelled states persist for recovery |
| Device/instrumentation testing | 0% verified | Emulator or physical-device execution is still required |
| Production release readiness | 0% | Codec matrix, signing, and final release validation remain |

> The 100% values above apply only to the compatible scope implemented. They do not mean Janus accepts every codec, container, or transformation universally.

## Remux versus transcoding

| Case | Is remux enough? | Is transcoding required? |
|---|---:|---:|
| Compatible MP4 → MP4 | Yes | No |
| Compatible MKV/WebM → MP4 | Sometimes | If codecs are incompatible |
| Change resolution | No | Yes |
| Change bitrate | No | Yes |
| Change frame rate | No | Yes |
| H.264 → HEVC/AV1 | No | Yes |
| Extract compatible audio | Sometimes | Only when the codec must change |
| Frame-accurate cuts outside keyframes | Not always | Yes |

## Universal transcoding status

The complete capability, control, and phase matrix is in [TRANSCODING_MATRIX.md](TRANSCODING_MATRIX.md). Janus must choose remux only when the format and tracks are compatible; otherwise it must report that the transcoding pipeline is required.

## Verification

```text
./gradlew ktlintCheck :app:testDebugUnitTest :app:lintDebug :app:assembleDebug
Ktlint: PASS
Unit tests: 17/17 PASS
Android Lint: 0 issues
Debug APK: PASS
```
