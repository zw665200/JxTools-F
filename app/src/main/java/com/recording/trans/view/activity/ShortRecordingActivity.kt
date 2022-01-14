package com.recording.trans.view.activity

import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSpinner
import com.baidu.speech.EventListener
import com.baidu.speech.EventManager
import com.baidu.speech.EventManagerFactory
import com.baidu.speech.asr.SpeechConstant
import com.google.gson.Gson
import com.recording.trans.R
import com.recording.trans.bean.RecogResult
import com.recording.trans.utils.JLog
import com.recording.trans.view.base.BaseActivity
import org.json.JSONObject

class ShortRecordingActivity : BaseActivity() {
    private lateinit var record: ImageView
    private lateinit var recordText: TextView
    private lateinit var asr: EventManager
    private lateinit var listener: EventListener
    private lateinit var from: AppCompatSpinner
    private var isRecording = false
    private var isReady = false
    private var type = ""

    override fun setLayout(): Int {
        return R.layout.a_short_voice_to_text
    }


    override fun initView() {
        record = findViewById(R.id.record)
        recordText = findViewById(R.id.record_text)
        from = findViewById(R.id.spinner_from)
        record.setOnClickListener { beginRecording() }
    }

    override fun initData() {
        spinnerListener()

        val builder = StringBuilder()
        asr = EventManagerFactory.create(this, "asr")
        listener = EventListener { name, params, data, offset, length ->

            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)) {
                // 引擎就绪，可以说话，一般在收到此事件后通过UI通知用户可以说话了
                isReady = true
                JLog.i("is ready")
            }

            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                // 一句话的临时结果，最终结果及语义结果
                JLog.i(params)
                if (params == null || params.isEmpty()) {
                    return@EventListener
                }

                val result = Gson().fromJson(params, RecogResult::class.java)
                builder.append(result.best_result)
                recordText.text = builder.toString()


                if (params.contains("\"nlu_result\"")) {
                    // 一句话的语义解析结果
                    if (length > 0 && data.size > 0) {
                    }
                } else if (params.contains("\"partial_result\"")) {
                    // 一句话的临时识别结果
                } else if (params.contains("\"final_result\"")) {
                    // 一句话的最终识别结果
                } else {
                    // 一般这里不会运行
                    if (data != null) {
                    }
                }
            } else {
                // 识别开始，结束，音量，音频数据回调
                if (params != null && !params.isEmpty()) {
                }
                if (data != null) {
                }
            }

            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
                // 一句话的临时结果，最终结果及语义结果
                JLog.i(params)
            }
            // ... 支持的输出事件和事件支持的事件参数见“输入和输出参数”一节

        }

        asr.registerListener(listener)
    }

    private fun spinnerListener() {
//        from.setPopupBackgroundResource(R.color.color_content)

        from.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {

                when (position) {
                    0 -> {
                        type = "default"
                    }

                    1 -> {
                        type = "date_desc"
                    }

                    2 -> {
                        type = "date_asc"
                    }

                    3 -> {
                        type = "size_desc"
                    }

                    4 -> {
                        type = "size_asc"
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    override fun onPause() {
        super.onPause()
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0)
    }

    private fun beginRecording() {
        val map = LinkedHashMap<String, Any>()
        map[SpeechConstant.ACCEPT_AUDIO_DATA] = false
        map[SpeechConstant.DISABLE_PUNCTUATION] = false
        map[SpeechConstant.ACCEPT_AUDIO_VOLUME] = false
        map[SpeechConstant.PID] = 1537

        val json = JSONObject(map as Map<*, *>).toString()
        if (isRecording) {
            stop()
            return
        }

        asr.send(SpeechConstant.ASR_START, json, null, 0, 0)
        isRecording = true
    }

    private fun stop() {
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        asr.send(SpeechConstant.ASR_CANCEL, "{}", null, 0, 0)
        asr.unregisterListener(listener)
    }
}