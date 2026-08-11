# Test media fixtures

The repository does not commit copyrighted sample media. Generate small, deterministic fixtures locally with FFmpeg:

```bash
./scripts/generate-test-media.sh
```

This creates an ignored `test-media/` directory containing:

| Fixture | Tracks | Purpose |
|---|---|---|
| `janus-avc-aac.mp4` | H.264 video + AAC audio | Compatible remux, metadata, progress, and splitting |
| `janus-audio-aac.m4a` | AAC audio | Audio extraction and audio-only metadata |
| `janus-vp9-opus.webm` | VP9 video + Opus audio | Unsupported-container/codec fallback and transcoding probe |

## Copy to a device

Install Android platform tools, enable USB debugging, connect a device, then use:

```bash
adb push test-media/janus-avc-aac.mp4 /sdcard/Movies/
adb push test-media/janus-audio-aac.m4a /sdcard/Music/
adb push test-media/janus-vp9-opus.webm /sdcard/Movies/
```

On an emulator, drag the files onto the emulator window or use the same `adb push` commands after starting an AVD. The Janus picker uses the system document provider, so the files can be selected from Movies/Music.

## Local inspection

```bash
ffprobe -hide_banner test-media/janus-avc-aac.mp4
```

The fixtures are intentionally short and synthetic; they are suitable for smoke tests, not performance benchmarking.
