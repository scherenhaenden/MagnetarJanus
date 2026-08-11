package com.magnetar.janus.data

import android.content.Context
import java.util.UUID

enum class JobKind { CONVERT, SPLIT, AUDIO }

enum class JobState { QUEUED, RUNNING, COMPLETE, FAILED, CANCELLED }

data class MediaJob(
    val id: String = UUID.randomUUID().toString(),
    val kind: JobKind,
    val inputName: String,
    val outputPath: String? = null,
    val state: JobState = JobState.QUEUED,
    val message: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
)

/** Small durable queue/history store; records are recoverable after process recreation. */
class JobHistoryStore(
    context: Context,
) {
    private val preferences = context.getSharedPreferences("janus_jobs", Context.MODE_PRIVATE)

    fun list(): List<MediaJob> =
        preferences
            .getStringSet(KEY_JOBS, emptySet())
            .orEmpty()
            .mapNotNull(::decode)
            .sortedByDescending { it.createdAt }

    fun save(job: MediaJob) {
        val updated = list().filterNot { it.id == job.id } + job
        preferences.edit().putStringSet(KEY_JOBS, updated.map(::encode).toSet()).apply()
    }

    fun update(
        id: String,
        state: JobState,
        message: String? = null,
        outputPath: String? = null,
    ) {
        list().firstOrNull { it.id == id }?.let {
            save(it.copy(state = state, message = message, outputPath = outputPath ?: it.outputPath))
        }
    }

    fun clear() = preferences.edit().remove(KEY_JOBS).apply()

    private fun encode(job: MediaJob): String =
        listOf(job.id, job.kind.name, job.inputName, job.outputPath.orEmpty(), job.state.name, job.message.orEmpty(), job.createdAt)
            .joinToString("|") { it.toString().replace("|", " ") }

    private fun decode(value: String): MediaJob? =
        value.split('|', limit = 7).takeIf { it.size == 7 }?.let { parts ->
            runCatching {
                MediaJob(
                    id = parts[0],
                    kind = JobKind.valueOf(parts[1]),
                    inputName = parts[2],
                    outputPath = parts[3].ifBlank { null },
                    state = JobState.valueOf(parts[4]),
                    message = parts[5].ifBlank { null },
                    createdAt = parts[6].toLong(),
                )
            }.getOrNull()
        }

    private companion object {
        const val KEY_JOBS = "jobs"
    }
}
