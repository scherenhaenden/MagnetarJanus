@file:Suppress("ktlint:standard:function-expression-body")

package com.magnetar.janus.data

import android.net.Uri
import com.magnetar.janus.model.AudioCodec
import com.magnetar.janus.model.TranscodeTarget
import com.magnetar.janus.model.VideoCodec

data class TranscodeRequest(
    val input: Uri,
    val output: Uri,
    val target: TranscodeTarget,
    val isCancelled: () -> Boolean = { false },
)

enum class TranscodeStage { INSPECT, DEMUX, DECODE, ENCODE, MUX, VALIDATE }

data class TranscodeProgress(
    val stage: TranscodeStage,
    val fraction: Float,
)

interface UniversalTranscoder {
    fun transcode(
        request: TranscodeRequest,
        onProgress: (TranscodeProgress) -> Unit = {},
    ): Result<Unit>
}

/** Capability-gated boundary for the upcoming MediaCodec decode/encode worker. */
class CapabilityCheckedTranscoder(
    private val probe: CodecCapabilityProbe = CodecCapabilityProbe(),
) : UniversalTranscoder {
    override fun transcode(
        request: TranscodeRequest,
        onProgress: (TranscodeProgress) -> Unit,
    ): Result<Unit> = transcodeInternal(request, onProgress)

    private fun transcodeInternal(
        request: TranscodeRequest,
        onProgress: (TranscodeProgress) -> Unit,
    ): Result<Unit> {
        return runCatching {
            onProgress(TranscodeProgress(TranscodeStage.INSPECT, 0f))
            val videoMimeType = request.target.videoCodec?.mimeType
            val audioMimeType = request.target.audioCodec?.mimeType
            val targetMimeType = videoMimeType ?: audioMimeType.orEmpty()
            if (!probe.supportsEncoder(targetMimeType)) {
                throw IllegalStateException("No compatible encoder is available on this device")
            }
            throw IllegalStateException("The MediaCodec encode worker is not installed yet")
        }
    }
}

private val VideoCodec.mimeType: String
    get() = videoMimeTypes[this].orEmpty()

private val AudioCodec.mimeType: String
    get() = audioMimeTypes[this].orEmpty()

private val videoMimeTypes =
    mapOf(
        VideoCodec.AVC to "video/avc",
        VideoCodec.HEVC to "video/hevc",
        VideoCodec.VP8 to "video/x-vnd.on2.vp8",
        VideoCodec.VP9 to "video/x-vnd.on2.vp9",
        VideoCodec.AV1 to "video/av01",
    )

private val audioMimeTypes =
    mapOf(
        AudioCodec.AAC to "audio/mp4a-latm",
        AudioCodec.OPUS to "audio/opus",
        AudioCodec.VORBIS to "audio/vorbis",
        AudioCodec.FLAC to "audio/flac",
    )
