package com.magnetar.janus.model

object OutputNaming {
    fun splitDirectoryName(
        originalName: String,
        timestampMillis: Long,
    ): String {
        val baseName = originalName.substringBeforeLast('.', originalName).ifBlank { "output" }
        return "$baseName-$timestampMillis"
    }

    fun splitFileName(
        originalName: String,
        index: Int,
        extension: String = "mp4",
    ): String {
        require(index > 0) { "Split index must be positive" }
        val baseName = originalName.substringBeforeLast('.', originalName).ifBlank { "output" }
        return "$baseName-${index.toString().padStart(2, '0')}.$extension"
    }
}
