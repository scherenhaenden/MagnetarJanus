#!/usr/bin/env bash
set -euo pipefail

output_dir="${1:-test-media}"
mkdir -p "$output_dir"

echo "Generating deterministic Janus test media in $output_dir"

# Five seconds of moving colour bars plus a 440 Hz tone. These fixtures are
# synthetic, small, redistributable, and exercise both audio and video tracks.
ffmpeg -hide_banner -loglevel error -y \
    -f lavfi -i "testsrc2=size=640x360:rate=30" \
    -f lavfi -i "sine=frequency=440:sample_rate=48000" \
    -t 5 -c:v libx264 -pix_fmt yuv420p -preset ultrafast -crf 28 \
    -c:a aac -b:a 128k -movflags +faststart "$output_dir/janus-avc-aac.mp4"

ffmpeg -hide_banner -loglevel error -y \
    -f lavfi -i "sine=frequency=880:sample_rate=44100" \
    -t 5 -c:a aac -b:a 128k "$output_dir/janus-audio-aac.m4a"

ffmpeg -hide_banner -loglevel error -y \
    -f lavfi -i "testsrc2=size=640x360:rate=24" \
    -f lavfi -i "sine=frequency=220:sample_rate=48000" \
    -t 5 -c:v libvpx-vp9 -b:v 700k -c:a libopus -b:a 96k "$output_dir/janus-vp9-opus.webm"

echo "Created:"
find "$output_dir" -maxdepth 1 -type f -printf '  %f (%s bytes)\n' | sort
