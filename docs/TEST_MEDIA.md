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

## Openly licensed downloads

For manual testing with real footage, download the following into the ignored `test-media-open/` directory:

```bash
mkdir -p test-media-open
curl -L -o test-media-open/Audio.wav \
  https://upload.wikimedia.org/wikipedia/commons/b/b5/Audio.wav
curl -L -o test-media-open/big-buck-bunny-720p-5mb.webm \
  https://upload.wikimedia.org/wikipedia/commons/e/e7/Big_buck_bunny_720p_5mb.webm
```

`Audio.wav` is released under CC0 by its uploader; the source page is [Wikimedia Commons — Audio.wav](https://commons.wikimedia.org/wiki/File:Audio.wav). The 30-second Big Buck Bunny WebM is VP9/Opus and is published under CC BY-SA 4.0; see [Wikimedia Commons — Big buck bunny 720p 5mb.webm](https://commons.wikimedia.org/wiki/File:Big_buck_bunny_720p_5mb.webm). Credit the listed authors and retain the license when redistributing modified copies.

These downloads are deliberately ignored by Git because they are test fixtures, not application assets. After downloading, copy them to a device:

```bash
adb push test-media-open/Audio.wav /sdcard/Music/
adb push test-media-open/big-buck-bunny-720p-5mb.webm /sdcard/Movies/
```
