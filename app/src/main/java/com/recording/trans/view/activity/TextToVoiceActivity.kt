package com.recording.trans.view.activity

import android.app.AlertDialog
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy
import com.baidu.tts.client.*
import com.recording.trans.R
import com.recording.trans.adapter.DataAdapter
import com.recording.trans.bean.Resource
import com.recording.trans.callback.FileCallback
import com.recording.trans.controller.AudioManager
import com.recording.trans.controller.Constant
import com.recording.trans.controller.LogReportManager
import com.recording.trans.controller.PayManager
import com.recording.trans.utils.AppUtil
import com.recording.trans.utils.FileUtil
import com.recording.trans.utils.JLog
import com.recording.trans.utils.ToastUtil
import com.recording.trans.view.base.BaseActivity
import com.recording.trans.view.views.ChangeAnchorDialog
import com.recording.trans.view.views.ProgressSeekBar
import com.recording.trans.view.views.VoiceSettingDialog
import com.tencent.mmkv.MMKV
import kotlinx.android.synthetic.main.item_text_to_voice.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TextToVoiceActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var record: ImageView
    private lateinit var recordText: TextView
    private lateinit var counter: TextView
    private lateinit var clear: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var copy: Button
    private lateinit var openPayTips: LinearLayout
    private lateinit var openPay: ImageView
    private lateinit var save: TextView
    private lateinit var timer: Chronometer
    private lateinit var progressBar: ProgressSeekBar

    private lateinit var speechSynthesizer: SpeechSynthesizer
    private lateinit var listener: SpeechSynthesizerListener
    private lateinit var mainAdapter: DataAdapter<Resource>
    private var mainPics = mutableListOf<Resource>()
    private var voiceIndex = 0
    private var speech = 5
    private var intonation = 5
    private var volume = 10
    private var isPay = false
    private var current = 0

    private var mmkv = MMKV.defaultMMKV()
    private var dataList = mutableListOf<ByteArray>()
    private var textList = mutableListOf<String>()
    private var utteranceIdList = mutableListOf<String>()
    private var synthesizeFinish = true
    private var isPlaying = false


    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            when (msg.what) {
                0x1 -> {
                    val bundle = msg.data
                    if (bundle != null) {
                        speech = bundle.getInt("speech")
                        intonation = bundle.getInt("intonation")
                        volume = bundle.getInt("volume")
                    }
                }
                0x2 -> {
                    voiceIndex = msg.arg1
                    JLog.i("index = $voiceIndex")
                }
            }
        }
    }

    override fun setLayout(): Int {
        return R.layout.a_text_to_voice
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        record = findViewById(R.id.record)
        recordText = findViewById(R.id.record_text)
        recyclerView = findViewById(R.id.recy_voice_to_font)
        counter = findViewById(R.id.counter)
        clear = findViewById(R.id.clear)
        copy = findViewById(R.id.copy)
        openPayTips = findViewById(R.id.open_vip_tips)
        openPay = findViewById(R.id.open_pay)
        save = findViewById(R.id.save)
        timer = findViewById(R.id.timer)
        progressBar = findViewById(R.id.progress)

        back.setOnClickListener { finish() }
        openPay.setOnClickListener { openPayPage() }
        record.setOnClickListener { beginRecording() }
        clear.setOnClickListener { clear() }
        copy.setOnClickListener { copy() }
        save.setOnClickListener { saveAudio() }

        recordText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                val length = s?.length
                if (length != null && length > 10000) {
                    ToastUtil.show(this@TextToVoiceActivity, "字数已达上线")
                    recordText.text = recordText.text.toString().substring(10000)
                    return
                }

                val text = "$length/10000"
                counter.text = text
                mmkv?.encode("text_to_voice", recordText.text.toString())
            }
        })
    }

    override fun initData() {
        initTimer()
        loadSetting()
        initSpeechEng()
        checkPay()

        val from = intent.getBooleanExtra("from", false)
        if (from) {
            val text = mmkv?.decodeString("words_from_image")
            if (text != null && text.isNotEmpty()) {
                recordText.text = text
            }
        } else {
            val text = mmkv?.decodeString("text_to_voice")
            if (text != null && text.isNotEmpty()) {
                recordText.text = text
            }
        }

        LogReportManager.logReport("文字转语音", "访问页面", LogReportManager.LogType.OPERATION)
    }

    private fun initTimer() {
        timer.setOnChronometerTickListener {
            current++
            timer.text = formatMiss(current)

            if (current <= 90) {
                progressBar.progress = current
            } else {
                progressBar.progress = 90
            }
        }
    }

    private fun loadSetting() {
        mainPics.clear()
        mainPics.add(Resource("1", R.drawable.ic_setting, "声音设置"))
        mainPics.add(Resource("2", R.drawable.ic_archor, "切换主播"))
        mainPics.add(Resource("3", R.drawable.ic_txt, "保存文本"))
        mainPics.add(Resource("4", R.drawable.ic_save, "保存音频"))

        mainAdapter = DataAdapter.Builder<Resource>()
            .setData(mainPics)
            .setLayoutId(R.layout.item_text_to_voice)
            .addBindView { itemView, itemData, position ->
                itemView.iv_icon.setImageResource(itemData.icon)
                itemView.tv_name.text = itemData.name

                if (position == 0 || position == 1) {
                    itemView.iv_vip.visibility = View.GONE
                }

                itemView.setOnClickListener {
                    when (itemData.type) {
                        "1" -> VoiceSettingDialog(this, speech, intonation, volume, mHandler).show()
                        "2" -> ChangeAnchorDialog(this, voiceIndex, mHandler).show()
                        "3" -> saveTxt()
                        "4" -> saveAudio()
                    }
                }
            }
            .create()

        recyclerView.adapter = mainAdapter
        recyclerView.layoutManager = GridLayoutManager(this, 4)
        mainAdapter.notifyItemRangeChanged(0, mainPics.size)
    }

    private fun initSpeechEng() {
        LoggerProxy.printable(true)
        speechSynthesizer = SpeechSynthesizer.getInstance()
        speechSynthesizer.setContext(this)
        speechSynthesizer.setAppId(Constant.BAIDU_SPEECH_APP_ID)
        speechSynthesizer.setApiKey(Constant.BAIDU_SPEECH_APP_KEY, Constant.BAIDU_SPEECH_APP_SECRET)
        val result = speechSynthesizer.setStereoVolume(1.0f, 1.0f)
        JLog.i("resu = $result")

        listener = object : SpeechSynthesizerListener {
            override fun onSynthesizeStart(utteranceId: String?) {
            }

            override fun onSynthesizeDataArrived(utteranceId: String?, audioData: ByteArray?, progress: Int, engineType: Int) {
                if (utteranceId != null) {
                    if (audioData != null) {
                        dataList.add(audioData)
                    }
                } else {
                    if (audioData != null) {
                        dataList.add(audioData)
                    }
                }
            }

            override fun onSynthesizeFinish(utteranceId: String?) {
                JLog.i("Synthesize finish")
                if (utteranceId != null) {
                    utteranceIdList.add(utteranceId)
                    if (utteranceIdList.size == textList.size) {
                        synthesizeFinish = true
                    }
                } else {
                    synthesizeFinish = true
                }
            }

            override fun onSpeechStart(utteranceId: String?) {
                runOnUiThread {
                    JLog.i("play start")
                    isPlaying = true
                    timer.start()
                }
            }

            override fun onSpeechProgressChanged(utteranceId: String?, p1: Int) {
            }

            override fun onSpeechFinish(utteranceId: String?) {
                runOnUiThread {
                    JLog.i("play finished")
                    isPlaying = false
                    progressBar.progress = 100
                    timer.stop()
                    record.setImageResource(R.drawable.ic_recorder_pause)
                }
            }

            override fun onError(utteranceId: String?, p1: SpeechError?) {
                JLog.i("error = $p1")
                runOnUiThread {
                    isPlaying = false
                    progressBar.progress = 100
                    timer.stop()
                    record.setImageResource(R.drawable.ic_recorder_pause)
                }
            }
        }

        speechSynthesizer.setSpeechSynthesizerListener(listener)
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


    private fun beginRecording() {
        if (isPlaying) {
            pause()
            return
        }

        if (dataList.size > 0) {
            stop()
        }

        LogReportManager.logReport("文字转语音", "使用功能", LogReportManager.LogType.OPERATION)

        dataList.clear()
        current = 0
        timer.text = formatMiss(current)

        var text = recordText.text.toString()
        if (text != "") {
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "$voiceIndex")
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "$speech")
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "$intonation")
            speechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "$volume")
            speechSynthesizer.initTts(TtsMode.ONLINE)

            if (!isPay && text.length > 99) {
                text = text.substring(0, 101)
            }

            //文本字符数大于60需要分割
            if (text.length > 60) {
                val bags = arrayListOf<SpeechSynthesizeBag>()
                val times = text.length / 60
                for (index in 0..times) {
                    if (index == times) {
                        val t = text.substring(60 * index)
                        bags.add(getSpeechSynthesizeBag(t, "$index"))
                        textList.add(t)
                    } else {
                        val t = text.substring(60 * index, 60 * (index + 1))
                        bags.add(getSpeechSynthesizeBag(t, "$index"))
                        textList.add(t)
                    }
                }

                //批量播放
                val result = speechSynthesizer.batchSpeak(bags)
                if (result != 0) {
                    JLog.i("error code = $result")
                } else {
                    synthesizeFinish = false
                    record.setImageResource(R.drawable.ic_recorder_recording)
                }

            } else {
                record.setImageResource(R.drawable.ic_recorder_recording)
                //单个播放
                val result = speechSynthesizer.speak(text)
                if (result != 0) {
                    JLog.i("error code = $result")
                }
            }
        }
    }

    private fun pause() {
        record.setImageResource(R.drawable.ic_recorder_pause)
        speechSynthesizer.pause()
        isPlaying = false
        timer.stop()
    }

    private fun stop() {
        record.setImageResource(R.drawable.ic_recorder_pause)
        speechSynthesizer.stop()
        isPlaying = false
        synthesizeFinish = true
        textList.clear()
        dataList.clear()
        utteranceIdList.clear()
    }

    private fun release() {
        stop()
        speechSynthesizer.release()
        speechSynthesizer == null
    }


    private fun clear() {
        if (recordText.text.toString() == "") {
            return
        }

        AlertDialog.Builder(this)
            .setTitle("提示")
            .setMessage("确定要清除文本吗？")
            .setPositiveButton("确定") { dialog, _ ->
                recordText.text = ""
                progressBar.progress = 0
                val text = mmkv?.decodeString("text_to_voice")
                if (text != null && text.isNotEmpty()) {
                    mmkv?.encode("text_to_voice", "")
                }
                dialog.cancel()
            }
            .create().show()
    }

    private fun copy() {
        if (recordText.text.toString() == "") {
            return
        }

        AppUtil.copyContentToClipboard(this, recordText.text.toString())
        ToastUtil.showShort(this, "已成功复制到剪贴板")
    }

    private fun saveTxt() {
        if (!isPay) {
            openPayPage()
            return
        }

        val text = recordText.text.toString()
        if (text == "") {
            return
        }

        launch(Dispatchers.IO) {
            AudioManager.get().saveText(this@TextToVoiceActivity, text, "${System.currentTimeMillis()}.txt", false, object : FileCallback {
                override fun onSuccess(filePath: String) {
                    runOnUiThread {
                        ToastUtil.showLong(this@TextToVoiceActivity, "保存成功")
                    }
                }

                override fun onFailed(message: String) {
                    runOnUiThread {
                        ToastUtil.showLong(this@TextToVoiceActivity, "保存失败")
                    }
                }
            })
        }
    }

    private fun saveAudio() {
        if (!isPay) {
            openPayPage()
            return
        }
        if (synthesizeFinish && dataList.size > 0) {
            val name = "${System.currentTimeMillis()}.pcm"
            launch(Dispatchers.IO) {
                for ((index, data) in dataList.withIndex()) {
                    AudioManager.get().saveAudio(this@TextToVoiceActivity, name, data, true, object : FileCallback {
                        override fun onSuccess(filePath: String) {
                            if (index == dataList.size - 1) {
                                runOnUiThread {
                                    ToastUtil.showLong(this@TextToVoiceActivity, "保存成功")
                                }
                            }
                        }

                        override fun onFailed(message: String) {
                            if (index == dataList.size - 1) {
                                runOnUiThread {
                                    ToastUtil.showLong(this@TextToVoiceActivity, "保存失败")
                                }
                            }
                        }
                    })
                }
            }
        }
    }

    private fun getSpeechSynthesizeBag(text: String, utteranceId: String): SpeechSynthesizeBag {
        val speechSynthesizeBag = SpeechSynthesizeBag()
        speechSynthesizeBag.text = text
        speechSynthesizeBag.utteranceId = utteranceId
        return speechSynthesizeBag
    }

    private fun formatMiss(miss: Int): String {
        val hh = if (miss / 3600 > 9) miss / 3600 else "0" + miss / 3600
        val mm = if (miss % 3600 / 60 > 9) miss % 3600 / 60 else "0" + miss % 3600 / 60
        val ss = if (miss % 3600 % 60 > 9) miss % 3600 % 60 else "0" + miss % 3600 % 60
        return "$hh:$mm:$ss"
    }

    override fun onDestroy() {
        super.onDestroy()
        release()
    }
}