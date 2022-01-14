package com.recording.trans.bean

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/3/26 15:49
 */
data class TaskResult(
    var task_id: String,
    var task_status: String,
    var task_result: Result?
)