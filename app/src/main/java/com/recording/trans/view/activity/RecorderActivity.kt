package com.recording.trans.view.activity

import android.media.AudioFormat
import android.media.MediaRecorder
import android.view.View
import android.widget.Button
import android.widget.Chronometer
import android.widget.ImageView
import android.widget.LinearLayout
import com.recording.trans.R
import com.recording.trans.controller.AudioManager
import com.recording.trans.controller.LogReportManager
import com.recording.trans.utils.ToastUtil
import com.recording.trans.view.base.BaseActivity
import com.recording.trans.view.views.VoiceWaveView
import com.tencent.mmkv.MMKV
import tech.oom.idealrecorder.IdealRecorder
import tech.oom.idealrecorder.StatusListener


class RecorderActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var record: ImageView
    private lateinit var timer: Chronometer
    private lateinit var reset: Button
    private lateinit var save: Button
    private lateinit var waveView: VoiceWaveView
    private lateinit var recordTips: LinearLayout

    private var isRecording = false
    private var current = 0
    private var fileName = ""
    private var mDataLength: Int = 0

    private var mmkv = MMKV.defaultMMKV()

    private lateinit var idealRecorder: IdealRecorder
    private lateinit var idealConfig: IdealRecorder.RecordConfig

    override fun setLayout(): Int {
        return R.layout.a_recorder
    }


    override fun initView() {
        back = findViewById(R.id.iv_back)
        record = findViewById(R.id.record)
        timer = findViewById(R.id.timer)
        reset = findViewById(R.id.reset)
        save = findViewById(R.id.save)
        waveView = findViewById(R.id.waveView)
        recordTips = findViewById(R.id.record_tips)

        back.setOnClickListener { finish() }
        record.setOnClickListener { beginRecording() }
        reset.setOnClickListener { reset() }
        save.setOnClickListener { save() }

        idealRecorder = IdealRecorder.getInstance()

        idealConfig = IdealRecorder.RecordConfig(
            MediaRecorder.AudioSource.MIC,
            IdealRecorder.RecordConfig.SAMPLE_RATE_22K_HZ, AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
    }

    override fun initData() {
        initTimer()
        initRecordingEng()

        LogReportManager.logReport("录音机", "访问页面", LogReportManager.LogType.OPERATION)
    }


    private fun initTimer() {
        timer.setOnChronometerTickListener {
            current++
            timer.text = formatMiss(current)
        }
    }

    private fun formatMiss(miss: Int): String {
        val hh = if (miss / 3600 > 9) miss / 3600 else "0" + miss / 3600
        val mm = if (miss % 3600 / 60 > 9) miss % 3600 / 60 else "0" + miss % 3600 / 60
        val ss = if (miss % 3600 % 60 > 9) miss % 3600 % 60 else "0" + miss % 3600 % 60
        return "$hh:$mm:$ss"
    }

    private fun initRecordingEng() {

        fileName = "recode_" + System.currentTimeMillis() + ".mp3"
        //如果需要保存录音文件  设置好保存路径就会自动保存  也可以通过onRecordData 回调自己保存  不设置 不会保存录音
        idealRecorder.setRecordFilePath("${AudioManager.get().getRootPath(this)}$fileName")
        //设置录音配置 最长录音时长 以及音量回调的时间间隔
        idealRecorder.setRecordConfig(idealConfig).setMaxRecordTime(Long.MAX_VALUE).setVolumeInterval(200)

        idealRecorder.setStatusListener(object : StatusListener() {
            override fun onStartRecording() {
                super.onStartRecording()
            }

            override fun onRecordData(data: ShortArray?, length: Int) {
                if (data != null) {
                    mDataLength = length
                    for (index in 1..length step 60) {
                        waveView.addData(data[index])
                    }
                }
            }

            override fun onVoiceVolume(volume: Int) {
                super.onVoiceVolume(volume)
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
                super.onStopRecording()
            }

            override fun onRecordedAllData(arr: ByteArray?) {
                super.onRecordedAllData(arr)
            }
        })
    }

    private fun reset() {
        if (!isRecording && mDataLength != 0) {
            reset.visibility = View.GONE
            save.visibility = View.GONE
            AudioManager.get().deleteFile(this, fileName)
            current = 0
            timer.text = formatMiss(current)
            waveView.addData(0)
            mDataLength = 0
        }
    }


    private fun save() {
        if (!isRecording && mDataLength != 0) {
            ToastUtil.showShort(this, "文件已保存")
            fileName = "recode_" + System.currentTimeMillis() + ".mp3"
            idealRecorder.setRecordFilePath("${AudioManager.get().getRootPath(this)}$fileName")
            mDataLength = 0
        }
    }

    private fun beginRecording() {
        if (isRecording) {
            stop()
            return
        }

        LogReportManager.logReport("录音机", "使用功能", LogReportManager.LogType.OPERATION)

        reset.visibility = View.VISIBLE
        save.visibility = View.VISIBLE

        record.setImageResource(R.drawable.ic_recorder_recording)

        current = 0
        timer.text = formatMiss(current)
        timer.start()

        isRecording = true
        idealRecorder.start()
    }

    private fun stop() {
        record.setImageResource(R.drawable.ic_recorder_default)
        timer.stop()
        idealRecorder.stop()
        isRecording = false
        waveView.addData(0)
    }


    override fun onDestroy() {
        super.onDestroy()
        isRecording = false
    }
}