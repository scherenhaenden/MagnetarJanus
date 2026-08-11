package com.magnetar.janus.model

enum class MediaKind { VIDEO, AUDIO }
enum class Operation { CONVERT, SPLIT, AUDIO }

data class MediaInfo(val name: String, val kind: MediaKind, val durationSeconds: Long, val sizeBytes: Long? = null, val resolution: String? = null, val frameRate: String? = null, val codec: String? = null, val container: String? = null)
data class Segment(val startSeconds: Long, val endSeconds: Long) { val durationSeconds: Long get() = endSeconds - startSeconds }

object SplitPlanner {
    fun presetDurations(): List<Long> = listOf(30L, 60L, 90L)
    fun validateDuration(durationSeconds: Long, mediaDurationSeconds: Long): Boolean = durationSeconds > 0 && mediaDurationSeconds > 0 && durationSeconds <= mediaDurationSeconds
    fun automaticSegments(mediaDurationSeconds: Long, segmentDurationSeconds: Long): List<Segment> {
        require(validateDuration(segmentDurationSeconds, mediaDurationSeconds)) { "Segment duration must be within media duration" }
        return (0 until mediaDurationSeconds step segmentDurationSeconds).map { start -> Segment(start, minOf(start + segmentDurationSeconds, mediaDurationSeconds)) }
    }
    fun manualSegments(mediaDurationSeconds: Long, boundaries: List<Long>): List<Segment> {
        require(mediaDurationSeconds > 0) { "Media duration must be positive" }
        val cuts = boundaries.filter { it > 0 && it < mediaDurationSeconds }.distinct().sorted()
        return (listOf(0L) + cuts + mediaDurationSeconds).zipWithNext(::Segment)
    }
    fun formatDuration(totalSeconds: Long): String { val safe = totalSeconds.coerceAtLeast(0); return "%02d:%02d".format(safe / 60, safe % 60) }
}
