package com.recording.trans.view.views

import android.app.Activity
import android.app.Dialog
import android.net.Uri
import android.os.Build
import android.os.CountDownTimer
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import androidx.core.content.FileProvider
import com.recording.trans.R
import com.recording.trans.callback.FileCallback
import com.recording.trans.callback.HttpCallback
import com.recording.trans.controller.AudioManager
import com.recording.trans.controller.LogReportManager
import com.recording.trans.utils.JLog
import com.recording.trans.utils.ToastUtil
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/10/26 10:39
 */
class AudioToTextDialog(activity: Activity, filePath: String) : Dialog(activity, R.style.app_dialog),
    CoroutineScope by MainScope() {
    private val mContext = activity
    private val mFilePath = filePath

    private lateinit var converter: Button
    private lateinit var close: Button
    private lateinit var progressBar: ProgressBar

    private var isTasking = false
    private lateinit var timer: CountDownTimer
    private var index = 0

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(mContext).inflate(R.layout.d_file_to_text, null)
        setContentView(view)
        setCancelable(false)


        converter = findViewById(R.id.converter)
        close = findViewById(R.id.close)
        progressBar = findViewById(R.id.progress)

        initTimer()

        close.setOnClickListener {
            isTasking = false
            timer.cancel()
            cancel()
        }


        converter.setOnClickListener {

            converter.setBackgroundResource(R.drawable.shape_corner_grey)
            converter.isEnabled = false
            converter.text = "转换中..."
            isTasking = true

            val uri: Uri?
            val format: String

            if (Build.VERSION.SDK_INT >= 24) {
                uri = FileProvider.getUriForFile(mContext, "com.recording.trans.fileprovider", File(mFilePath))
            } else {
                uri = Uri.fromFile(File(mFilePath))
            }

            val file = File(mFilePath)
            if (file.name.endsWith("amr")) {
                format = "amr"
            } else if (file.name.endsWith("wav")) {
                format = "wav"
            } else if (file.name.endsWith("pcm")) {
                format = "pcm"
            } else if (file.name.endsWith("m4a")) {
                format = "m4a"
            } else {
                format = "mp3"
            }

            JLog.i(uri.toString())

            LogReportManager.logReport("音频文件转写", "使用功能", LogReportManager.LogType.OPERATION)

            launch(Dispatchers.IO) {
                AudioManager.get().getTextFromVoice(mContext, uri!!, format, 0, object : HttpCallback {
                    override fun onSuccess() {
                        mContext.runOnUiThread {
                            if (isTasking) {
                                index = 10
                                progressBar.progress = index
                                timer.start()
                            }
                        }
                    }

                    override fun onFailed(msg: String) {
                        mContext.runOnUiThread {
                            if (isTasking) {
                                ToastUtil.showShort(mContext, msg)
                                index = 0
                                progressBar.progress = index
                                timer.cancel()
                                converter.setBackgroundResource(R.drawable.shape_corner_red)
                            }
                        }
                    }
                })
            }
        }
    }

    private fun initTimer() {
        timer = object : CountDownTimer(15000 * 90L, 15000) {
            override fun onFinish() {
                ToastUtil.show(mContext, "转写时间过长，请重试")
            }

            override fun onTick(millisUntilFinished: Long) {
                if (index <= 90) {
                    index += 1
                    progressBar.progress = index
                }

                val taskId = MMKV.defaultMMKV()?.decodeString("task_id") ?: return
                JLog.i("taskId = $taskId")
                launch(Dispatchers.IO) {
                    AudioManager.get().getTextResult(mContext, taskId, object : FileCallback {
                        override fun onSuccess(filePath: String) {
                            mContext.runOnUiThread {
                                if (isTasking) {
                                    timer.cancel()
                                    index = 0
                                    progressBar.progress = 100
                                    ToastUtil.showLong(mContext, "文件已保存在$filePath")
                                    converter.setBackgroundResource(R.drawable.shape_corner_blue)
                                    converter.text = "已完成"
                                }
                            }
                        }

                        override fun onFailed(message: String) {
                            mContext.runOnUiThread {
                                if (isTasking) {
                                    ToastUtil.showShort(mContext, message)
                                    index = 0
                                    progressBar.progress = index
                                    timer.cancel()
                                    converter.setBackgroundResource(R.drawable.shape_corner_red)
                                }
                            }
                        }
                    })
                }
            }
        }

    }


    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER

            width = WindowManager.LayoutParams.MATCH_PARENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }
}