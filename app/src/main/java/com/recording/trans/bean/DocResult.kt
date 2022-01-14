package com.recording.trans.bean

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/6/24 15:24
 */
data class DocResult(
    var log_id: Long,
    var results_num: Int,
    var results: List<DocWords>
)
