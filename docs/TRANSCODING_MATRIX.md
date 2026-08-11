# Universal transcoding matrix

The current engine performs compatible MP4 remuxing. Universal conversion requires the encoder/decoder pipeline described below.

## Container and codec palette

| Target container | Video codecs | Audio codecs | Subtitle/metadata policy |
|---|---|---|---|
| MP4/M4V | H.264/AVC, HEVC, AV1 | AAC, ALAC, Opus where supported | Timed text, chapters, metadata |
| WebM | VP8, VP9, AV1 | Opus, Vorbis | WebVTT |
| MKV | H.264, HEVC, VP9, AV1, ProRes where supported | AAC, Opus, Vorbis, FLAC, PCM | SRT, ASS, PGS, chapters |
| MOV | H.264, HEVC, ProRes where supported | AAC, ALAC, PCM | Timed text and metadata |
| M4A | — | AAC, ALAC, Opus where supported | Tags and artwork |
| ADTS | — | AAC | Transport metadata only |
| WAV/FLAC | — | PCM, FLAC | Tags |

## Video controls

| Part | Options | Required behavior |
|---|---|---|
| Codec | H.264, HEVC, VP8, VP9, AV1 | Probe device decoder/encoder support first |
| Resolution | Original, 4K, 1080p, 720p, 480p, custom | Preserve aspect ratio and validate dimensions |
| Frame rate | Original, 24, 25, 30, 50, 60, custom | Resample timestamps monotonically |
| Bitrate | Auto, quality-based, CBR, VBR, custom | Enforce encoder limits and estimate output size |
| Keyframes | Automatic or interval in seconds | Align fast cuts and report keyframe limitations |
| Rotation/color | Preserve metadata or bake pixels; SDR/HDR | Never silently rotate or downgrade color |

## Audio controls

| Part | Options | Required behavior |
|---|---|---|
| Codec | AAC, Opus, Vorbis, FLAC, PCM, ALAC | Expose only codecs supported by the device |
| Sample rate | Original, 8/16/22.05/44.1/48/96 kHz | Resample with explicit quality policy |
| Channels | Original, mono, stereo, 5.1 | Deterministic downmix/upmix with warning |
| Bitrate/quality | CBR, VBR, lossless | Show quality/size trade-off |
| Loudness | Preserve or normalize to target LUFS | Apply limiter and report gain |

## Universal pipeline phases

| Phase | Deliverable | Acceptance evidence |
|---|---|---|
| U1 | Capability probe and compatibility planner | Unit tests for remux-vs-transcode decisions |
| U2 | Video decode/encode | Fixtures proving codec, resolution, FPS and bitrate changes |
| U3 | Audio decode/encode | Fixtures proving codec, sample-rate and channel changes |
| U4 | Unified fallback | Clear unsupported-format errors; no false remux success |
| U5 | Progress/cancellation | Demux, decode, encode, mux and validation progress |
| U6 | Device matrix | API/device runs across representative codecs and containers |
