package com.recording.trans.view.activity

import android.app.AlertDialog
import android.content.Intent
import android.media.AudioFormat
import android.media.MediaRecorder
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import com.baidu.speech.EventListener
import com.baidu.speech.EventManager
import com.baidu.speech.EventManagerFactory
import com.baidu.speech.asr.SpeechConstant
import com.google.gson.Gson
import com.recording.trans.R
import com.recording.trans.bean.FileBean
import com.recording.trans.bean.RecogResult
import com.recording.trans.callback.DialogCallback
import com.recording.trans.callback.FileCallback
import com.recording.trans.callback.TaskCallback
import com.recording.trans.controller.*
import com.recording.trans.utils.*
import com.recording.trans.view.base.BaseActivity
import com.recording.trans.view.views.QuitDialog
import com.recording.trans.view.views.VoiceWaveView
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import tech.oom.idealrecorder.IdealRecorder
import tech.oom.idealrecorder.StatusListener


class OnlineRecordingActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var record: ImageView
    private lateinit var recordText: TextView
    private lateinit var timer: Chronometer
    private lateinit var from: AppCompatSpinner
    private lateinit var asr: EventManager
    private lateinit var listener: EventListener
    private lateinit var counter: TextView
    private lateinit var clear: ImageView
    private lateinit var copy: Button
    private lateinit var init: Button
    private lateinit var save: Button
    private lateinit var waveView: VoiceWaveView
    private lateinit var recordTips: LinearLayout
    private lateinit var openPayTips: LinearLayout
    private lateinit var openPay: ImageView

    private var isRecording = false
    private var pid = 1537
    private var current = 0
    private var isPay = false
    private var fileName = "${System.currentTimeMillis()}.txt"

    private var mmkv = MMKV.defaultMMKV()

    private lateinit var idealRecorder: IdealRecorder
    private lateinit var idealConfig: IdealRecorder.RecordConfig

    override fun setLayout(): Int {
        return R.layout.a_voice_to_text
    }


    override fun initView() {
        back = findViewById(R.id.iv_back)
        record = findViewById(R.id.record)
        recordText = findViewById(R.id.record_text)
        from = findViewById(R.id.spinner_from)
        timer = findViewById(R.id.timer)
        counter = findViewById(R.id.counter)
        clear = findViewById(R.id.clear)
        copy = findViewById(R.id.copy)
        init = findViewById(R.id.init)
        save = findViewById(R.id.save)
        waveView = findViewById(R.id.waveView)
        recordTips = findViewById(R.id.record_tips)
        openPayTips = findViewById(R.id.open_vip_tips)
        openPay = findViewById(R.id.open_pay)

        back.setOnClickListener { finish() }
        openPay.setOnClickListener { openPayPage() }
        record.setOnClickListener { beginRecording() }
        clear.setOnClickListener { clear() }
        init.setOnClickListener { reset() }
        copy.setOnClickListener { copy() }
        save.setOnClickListener { save() }

        recordText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val text = "${s?.length}"
                if (text.isNotEmpty()) {
                    counter.text = text
                    mmkv?.encode("online_recording", recordText.text.toString())
                    recordTips.visibility = View.GONE
                } else {
                    recordTips.visibility = View.VISIBLE
                }

            }
        })

        idealRecorder = IdealRecorder.getInstance()
        idealConfig = IdealRecorder.RecordConfig(
            MediaRecorder.AudioSource.MIC,
            IdealRecorder.RecordConfig.SAMPLE_RATE_22K_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        idealRecorder.setRecordConfig(idealConfig).setMaxRecordTime(Long.MAX_VALUE).setVolumeInterval(100)
    }

    override fun initData() {
        spinnerListener()
        initTimer()
        initRecordingEng()

        val text = mmkv?.decodeString("online_recording")
        if (text != null && text.isNotEmpty()) {
            recordText.text = text
        }

        LogReportManager.logReport("实时语音转文字", "访问页面", LogReportManager.LogType.OPERATION)
    }

    override fun onResume() {
        super.onResume()
        checkPay()
    }

    private fun spinnerListener() {
        from.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> pid = 1537
                    1 -> pid = 1737
                    2 -> pid = 1637
                    3 -> pid = 1837
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun initTimer() {
        timer.setOnChronometerTickListener {
            current++
            timer.text = formatMiss(current)

            if (!isPay) {
                //非vip用户最多1分钟
                if (current == 60) {
                    timer.stop()
                    stop()
                    openPayPage()
                }
            }
        }
    }

    private fun checkPay() {
        PayManager.get().checkPayResult(this) {
            if (it) {
                isPay = true
                openPayTips.visibility = View.GONE
            }
        }
    }


    private fun openPayPage() {
        if (Constant.CLIENT_TOKEN == "") {
            startActivity(Intent(this, LoginActivity::class.java))
        } else {
            startActivity(Intent(this, PayActivity::class.java))
        }
    }

    private fun formatMiss(miss: Int): String {
        val hh = if (miss / 3600 > 9) miss / 3600 else "0" + miss / 3600
        val mm = if (miss % 3600 / 60 > 9) miss % 3600 / 60 else "0" + miss % 3600 / 60
        val ss = if (miss % 3600 % 60 > 9) miss % 3600 % 60 else "0" + miss % 3600 % 60
        return "$hh:$mm:$ss"
    }

    private fun initRecordingEng() {
        asr = EventManagerFactory.create(this, "asr")
        listener = EventListener { name, params, _, _, _ ->

            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_READY)) {
                JLog.i("is ready")
                isRecording = true
                record.setImageResource(R.drawable.ic_recorder_recording)
                timer.start()

            }

            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_PARTIAL)) {
                JLog.i("params = $params")

                if (params == null || params.isEmpty()) {
                    return@EventListener
                }

                val result = Gson().fromJson(params, RecogResult::class.java)
                if (result.result_type == "final_result") {
                    val text = recordText.text.toString() + result.best_result
                    recordText.text = text
                }
            }

            if (name.equals(SpeechConstant.CALLBACK_EVENT_ASR_FINISH)) {
                JLog.i("params2 = $params")
                if (params.contains("sub_error")) {
//                    ToastUtil.showShort(this, "打开录音错误，请重试")
                }
            }

        }

        asr.registerListener(listener)

        idealRecorder.setStatusListener(object : StatusListener() {
            override fun onStartRecording() {
                JLog.i("onStartRecording")
            }

            override fun onRecordData(data: ShortArray?, length: Int) {
                if (data != null) {
                    for (index in 1..length step 60) {
                        waveView.addData(data[index])
                    }
                }
            }

            override fun onVoiceVolume(volume: Int) {
                JLog.i("onVoiceVolume")
            }

            override fun onRecordError(code: Int, errorMsg: String?) {
                super.onRecordError(code, errorMsg)
            }

            override fun onFileSaveFailed(error: String?) {
                super.onFileSaveFailed(error)
            }

            override fun onFileSaveSuccess(fileUri: String?) {
                super.onFileSaveSuccess(fileUri)
            }

            override fun onStopRecording() {
                JLog.i("onStopRecording")
            }

            override fun onRecordedAllData(arr: ByteArray?) {
                if (arr != null) {
                    JLog.i("onRecordedAllData")
//                    launch(Dispatchers.IO) {
//                        AsrManager.get().doTask(arr, object : TaskCallback {
//                            override fun onSuccess(msg: String) {
//                                JLog.i("msg = $msg")
//                            }
//
//                            override fun onFailed(msg: String) {
//                            }
//                        })
//                    }
                }
            }
        })
    }

    private fun reset() {
        if (isRecording) {
            ToastUtil.showShort(this, "请先关闭语音识别再操作")
            return
        }

        current = 0
        timer.text = formatMiss(current)
    }


    private fun clear() {
        if (isRecording) {
            ToastUtil.showShort(this, "请先关闭语音识别再操作")
            return
        }

        val text = recordText.text.toString()
        if (text == "") {
            ToastUtil.showShort(this, "检测到文本内容为空")
            return
        }

        AlertDialog.Builder(this)
            .setMessage("确定要清除文本（不可撤回）")
            .setPositiveButton("确定") { dialog, _ ->
                recordText.text = ""
                recordTips.visibility = View.VISIBLE
                current = 0
                timer.text = formatMiss(current)

                val text = mmkv?.decodeString("online_recording")
                if (text != null && text.isNotEmpty()) {
                    mmkv?.encode("online_recording", "")
                }
                dialog.cancel()
            }.show()
    }

    private fun copy() {
        if (recordText.text.toString() == "") {
            ToastUtil.showShort(this, "已成功复制到剪贴板")
            return
        }

        AppUtil.copyContentToClipboard(this, recordText.text.toString())
        ToastUtil.showShort(this, "已成功复制到剪贴板")
    }

    private fun save() {
        if (isRecording) {
            ToastUtil.showShort(this, "请先关闭语音识别再操作")
            return
        }

        val text = recordText.text.toString()
        if (text == "") {
            ToastUtil.showShort(this, "检测到文本内容为空")
            return
        }

        launch(Dispatchers.IO) {
            AudioManager.get().saveText(this@OnlineRecordingActivity, fileName, text, true, object : FileCallback {
                override fun onSuccess(filePath: String) {
                    runOnUiThread {
                        ToastUtil.showLong(this@OnlineRecordingActivity, "保存成功！")
                    }
                }

                override fun onFailed(message: String) {
                    runOnUiThread {
                        ToastUtil.showLong(this@OnlineRecordingActivity, "保存失败！")
                    }
                }
            })
        }

    }

    private fun beginRecording() {
        if (isRecording) {
            stop()
            return
        }

        init.visibility = View.VISIBLE
        save.visibility = View.VISIBLE

        isRecording = true
        idealRecorder.start()

        val map = LinkedHashMap<String, Any>()
        map[SpeechConstant.ACCEPT_AUDIO_DATA] = false
        map[SpeechConstant.DISABLE_PUNCTUATION] = false
        map[SpeechConstant.ACCEPT_AUDIO_VOLUME] = true
        map[SpeechConstant.BDS_ASR_ENABLE_LONG_SPEECH] = true
        map[SpeechConstant.PID] = pid

        val json = JSONObject(map as Map<*, *>).toString()

        asr.send(SpeechConstant.ASR_START, json, null, 0, 0)
    }

    private fun stop() {
        record.setImageResource(R.drawable.ic_recorder_default)
        asr.send(SpeechConstant.ASR_STOP, null, null, 0, 0)
        timer.stop()
        isRecording = false
        idealRecorder.stop()

        //report time
        if (current != 0) {
            LogReportManager.useTimesReport(current)
            LogReportManager.logReport("实时语音转文字", "使用功能", LogReportManager.LogType.OPERATION, current)
        }
    }

    override fun onStop() {
        super.onStop()
        if (isRecording) {
            stop()
        }
    }

    override fun onBackPressed() {
        if (!isRecording) {
            finish()
        } else {
            QuitDialog(this, "正在录音中，确定要退出吗？", object : DialogCallback {
                override fun onSuccess(file: FileBean) {
                    stop()
                    finish()
                }

                override fun onCancel() {
                }
            }).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        asr.unregisterListener(listener)
    }

}