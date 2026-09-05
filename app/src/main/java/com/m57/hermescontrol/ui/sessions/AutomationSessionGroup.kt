package com.m57.hermescontrol.ui.sessions

import com.m57.hermescontrol.data.model.SessionInfo

internal data class AutomationSessionGroup(
    val key: String,
    val jobId: String?,
    val sessions: List<SessionInfo>,
)

// cron.scheduler mints durable run IDs as cron_{job_id}_{YYYYMMDD_HHMMSS}.
// Never infer job identity from a title: different jobs may have the same name.
private val CRON_RUN_ID = Regex("^cron_(.+)_[0-9]{8}_[0-9]{6}$")

internal fun automationGroups(sessions: List<SessionInfo>): List<AutomationSessionGroup> =
    sessions
        .distinctBy { it.id }
        .groupBy { session ->
            val jobId =
                if (session.source == "cron") CRON_RUN_ID.matchEntire(session.id)?.groupValues?.get(1) else null
            jobId?.let { "job:$it" } ?: "session:${session.id}"
        }.map { (key, runs) ->
            AutomationSessionGroup(
                key = key,
                jobId = key.takeIf { it.startsWith("job:") }?.removePrefix("job:"),
                sessions = runs,
            )
        }
