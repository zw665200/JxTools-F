package com.recording.trans.bean

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/6/24 15:24
 */
data class VoiceTaskResult(
    var log_id: Long,
    var tasks_info: List<TaskResult>?
)
