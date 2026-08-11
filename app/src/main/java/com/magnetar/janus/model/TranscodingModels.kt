package com.magnetar.janus.model

enum class MediaContainer {
    MP4,
    WEBM,
    MKV,
    MOV,
    M4A,
    WAV,
    FLAC,
    UNKNOWN,
}

enum class VideoCodec {
    AVC,
    HEVC,
    VP8,
    VP9,
    AV1,
    PRORES,
    UNKNOWN,
}

enum class AudioCodec {
    AAC,
    OPUS,
    VORBIS,
    FLAC,
    PCM,
    ALAC,
    UNKNOWN,
}

data class TranscodeTarget(
    val container: MediaContainer,
    val videoCodec: VideoCodec? = null,
    val audioCodec: AudioCodec? = null,
    val width: Int? = null,
    val height: Int? = null,
    val frameRate: Float? = null,
    val videoBitrateKbps: Int? = null,
    val audioBitrateKbps: Int? = null,
    val sampleRateHz: Int? = null,
    val channels: Int? = null,
)

enum class ProcessingMode { REMUX, TRANSCODE, UNSUPPORTED }

data class TranscodePlan(
    val mode: ProcessingMode,
    val reason: String,
)

object TranscodePlanner {
    fun plan(
        sourceContainer: MediaContainer,
        sourceVideo: VideoCodec?,
        sourceAudio: AudioCodec?,
        target: TranscodeTarget,
    ): TranscodePlan {
        val targetIsAudioOnly = target.videoCodec == null
        if (targetIsAudioOnly &&
            sourceAudio == null
        ) {
            return TranscodePlan(
                ProcessingMode.UNSUPPORTED,
                "Audio target requires an audio source track",
            )
        }
        if (!targetIsAudioOnly &&
            sourceVideo == null
        ) {
            return TranscodePlan(
                ProcessingMode.UNSUPPORTED,
                "Video target requires a video source track",
            )
        }
        val containerCompatible =
            sourceContainer == target.container ||
                (sourceContainer == MediaContainer.MP4 && target.container == MediaContainer.M4A && sourceVideo == null)
        val tracksCompatible =
            sourceVideo == target.videoCodec &&
                sourceAudio == target.audioCodec
        val settingsUnchanged =
            listOf(
                target.width,
                target.height,
                target.frameRate,
                target.videoBitrateKbps,
                target.audioBitrateKbps,
                target.sampleRateHz,
                target.channels,
            ).all {
                it ==
                    null
            }
        return when {
            containerCompatible && tracksCompatible && settingsUnchanged -> {
                TranscodePlan(
                    ProcessingMode.REMUX,
                    "Container and tracks are compatible",
                )
            }

            target.container == MediaContainer.UNKNOWN -> {
                TranscodePlan(ProcessingMode.UNSUPPORTED, "Target container is not supported")
            }

            else -> {
                TranscodePlan(ProcessingMode.TRANSCODE, "Target changes container, codec, or media settings")
            }
        }
    }
}
