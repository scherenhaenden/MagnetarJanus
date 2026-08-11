# Release checklist

These gates cannot be marked complete from a host-only build. They require a connected Android device/emulator and a release signing identity.

## Device and codec validation

- [ ] Run `./gradlew connectedDebugAndroidTest` on API 26, 30, 34, and 37.
- [ ] Probe hardware/software decoders and encoders with `CodecCapabilityProbe`.
- [ ] Test H.264/AAC MP4 remux and transcode.
- [ ] Test HEVC, VP9, AV1, Opus, FLAC, and PCM according to device support.
- [ ] Test resolution, bitrate, frame-rate, sample-rate, channel, cancellation, and recovery controls.
- [ ] Verify output duration, track count, codec, size, and playback in an independent player.

## Release artifact

- [ ] Configure a protected release keystore outside the repository.
- [ ] Enable release shrinking only after codec/MediaCodec reflection rules are reviewed.
- [ ] Build with an explicit `-PjanusVersion=yyyy-MM-dd-HH-mm-sss`.
- [ ] Verify signing, checksum, install, upgrade, and uninstall behavior.
- [ ] Publish license notices for bundled code and test media attribution.
