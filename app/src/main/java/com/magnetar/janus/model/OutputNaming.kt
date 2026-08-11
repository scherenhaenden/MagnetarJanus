package com.magnetar.janus.model

object OutputNaming {
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
