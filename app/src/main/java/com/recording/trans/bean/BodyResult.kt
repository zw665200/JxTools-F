package com.recording.trans.bean

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/6/24 15:24
 */
data class BodyResult(
    var log_id: Long,
    var words_result_num: Int,
    var words_result: List<Words>
)
