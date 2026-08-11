package com.magnetar.janus.model

enum class MediaKind { VIDEO, AUDIO }
enum class Operation { CONVERT, SPLIT, AUDIO }

data class MediaInfo(
    val name: String,
    val kind: MediaKind,
    val durationSeconds: Long,
    val sourceUri: String? = null,
    val previewPath: String? = null,
    val sizeBytes: Long? = null,
    val resolution: String? = null,
    val frameRate: String? = null,
    val bitrate: String? = null,
    val codec: String? = null,
    val container: String? = null
)

data class Segment(val startSeconds: Long, val endSeconds: Long) {
    val durationSeconds: Long get() = endSeconds - startSeconds
}

object SplitPlanner {
    fun presetDurations(): List<Long> = listOf(30L, 60L, 90L)
    fun validateDuration(durationSeconds: Long, mediaDurationSeconds: Long): Boolean =
        durationSeconds > 0 && mediaDurationSeconds > 0 && durationSeconds <= mediaDurationSeconds

    fun automaticSegments(mediaDurationSeconds: Long, segmentDurationSeconds: Long): List<Segment> {
        require(mediaDurationSeconds > 0) { "Media duration must be positive" }
        require(segmentDurationSeconds > 0) { "Segment duration must be positive" }
        return (0 until mediaDurationSeconds step segmentDurationSeconds).map { start ->
            Segment(start, minOf(start + segmentDurationSeconds, mediaDurationSeconds))
        }
    }

    fun manualSegments(mediaDurationSeconds: Long, boundaries: List<Long>): List<Segment> {
        require(mediaDurationSeconds > 0) { "Media duration must be positive" }
        val cuts = boundaries.filter { it > 0 && it < mediaDurationSeconds }.distinct().sorted()
        return (listOf(0L) + cuts + mediaDurationSeconds).zipWithNext(::Segment)
    }

    fun formatDuration(totalSeconds: Long): String {
        val safe = totalSeconds.coerceAtLeast(0)
        val hours = safe / 3_600
        val minutes = (safe % 3_600) / 60
        val seconds = safe % 60
        return if (hours > 0) "%02d:%02d:%02d".format(hours, minutes, seconds) else "%02d:%02d".format(minutes, seconds)
    }
}

object ConversionSupport {
    fun supportedOutputContainers(kind: MediaKind): List<String> =
        if (kind == MediaKind.VIDEO) listOf("MP4") else listOf("MP4")

    fun canRemuxToMp4(kind: MediaKind): Boolean = kind == MediaKind.VIDEO || kind == MediaKind.AUDIO
}
