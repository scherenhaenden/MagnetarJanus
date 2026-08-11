package com.magnetar.janus.data

import android.media.MediaCodecInfo
import android.media.MediaCodecList

data class CodecCapability(
    val name: String,
    val isEncoder: Boolean,
    val mimeTypes: Set<String>,
)

/** Reads the device codec matrix without opening a codec or touching the selected media. */
class CodecCapabilityProbe {
    fun snapshot(): List<CodecCapability> =
        MediaCodecList(MediaCodecList.ALL_CODECS).codecInfos.map { info ->
            CodecCapability(
                name = info.name,
                isEncoder = info.isEncoder,
                mimeTypes = info.supportedTypes.map(String::lowercase).toSet(),
            )
        }

    fun supportsEncoder(mimeType: String): Boolean = hasCodec(mimeType, encoder = true)

    fun supportsDecoder(mimeType: String): Boolean = hasCodec(mimeType, encoder = false)

    private fun hasCodec(
        mimeType: String,
        encoder: Boolean,
    ): Boolean {
        val normalizedMimeType = mimeType.lowercase()
        return snapshot().any { it.isEncoder == encoder && normalizedMimeType in it.mimeTypes }
    }
}

fun MediaCodecInfo.isSoftwareCodec(): Boolean =
    name.startsWith("c2.android.") || name.startsWith("omx.google.") || name.startsWith("c2.google.")
