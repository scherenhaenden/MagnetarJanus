#!/usr/bin/env bash
set -euo pipefail

INPUT="${1:?Usage: $0 INPUT_VIDEO OUTPUT_DIRECTORY [SEGMENT_SECONDS]}"
OUTPUT_DIR="${2:?Usage: $0 INPUT_VIDEO OUTPUT_DIRECTORY [SEGMENT_SECONDS]}"
SEGMENT_SECONDS="${3:-90}"

command -v ffmpeg >/dev/null || { echo "ffmpeg is required" >&2; exit 1; }
command -v ffprobe >/dev/null || { echo "ffprobe is required" >&2; exit 1; }
mkdir -p "$OUTPUT_DIR"

ffmpeg -hide_banner -loglevel error \
    -i "$INPUT" -map 0 -c copy -f segment \
    -segment_time "$SEGMENT_SECONDS" -reset_timestamps 1 \
    "$OUTPUT_DIR/part-%02d.mp4"

count=0
for output in "$OUTPUT_DIR"/part-*.mp4; do
    [ -f "$output" ] || continue
    codecs="$(ffprobe -v error -select_streams v:0 -show_entries stream=codec_name -of csv=p=0 "$output")"
    audio_codecs="$(ffprobe -v error -select_streams a:0 -show_entries stream=codec_name -of csv=p=0 "$output")"
    duration="$(ffprobe -v error -show_entries format=duration -of csv=p=0 "$output")"
    awk -v duration="$duration" 'BEGIN { exit !(duration > 0) }'
    case "$codecs" in h264|hevc|mpeg4) ;; *) echo "Missing compatible video in $output: $codecs" >&2; exit 1 ;; esac
    case "$audio_codecs" in aac|ac3|mp3) ;; *) echo "Missing compatible audio in $output: $audio_codecs" >&2; exit 1 ;; esac
    printf 'verified %s: video=%s audio=%s duration=%ss\n' "$output" "$codecs" "$audio_codecs" "$duration"
    count=$((count + 1))
done

test "$count" -gt 0
printf 'verified %s split outputs\n' "$count"
