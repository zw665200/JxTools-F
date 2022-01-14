package com.recording.trans.view.views

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.*
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.frank.ffmpeg.listener.OnHandleListener
import com.recording.trans.R
import com.recording.trans.adapter.DataAdapter
import com.recording.trans.controller.AudioManager
import com.recording.trans.controller.LogReportManager
import com.recording.trans.utils.JLog
import com.recording.trans.utils.ToastUtil
import kotlinx.android.synthetic.main.item_format_type.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/10/26 10:39
 */
class VideoFormatTransDialog(activity: Activity, filePath: String) : Dialog(activity, R.style.app_dialog), CoroutineScope by MainScope() {
    private val mContext = activity
    private val mFilePath = filePath

    private lateinit var type: RecyclerView
    private lateinit var converter: Button
    private lateinit var close: Button

    private var isTasking = false
    private var format = ""
    private var currentPosition = -1
    private var adapter: DataAdapter<String>? = null

    init {
        initView()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun initView() {
        val view = LayoutInflater.from(mContext).inflate(R.layout.p_video_format_trans, null)
        setContentView(view)
        setCancelable(false)

        type = findViewById(R.id.video_type)
        converter = findViewById(R.id.converter)
        close = findViewById(R.id.close)

        val list = arrayListOf<String>()
        list.add("mp4")
        list.add("avi")
        list.add("rmvb")
        list.add("rm")
        list.add("3gp")
        list.add("mov")
        list.add("mkv")
        list.add("flv")
        list.add("asf")

        adapter = DataAdapter.Builder<String>()
            .setLayoutId(R.layout.item_format_type)
            .setData(list)
            .addBindView { itemView, itemData, position ->
                itemView.format.text = itemData

                if (position == currentPosition) {
                    itemView.setBackgroundResource(R.drawable.shape_corner_blue)
                    itemView.format.setTextColor(Color.WHITE)
                } else {
                    itemView.setBackgroundResource(R.drawable.shape_corner_grey)
                    itemView.format.setTextColor(Color.BLACK)
                }

                itemView.setOnClickListener {
                    currentPosition = position
                    format = itemData
                    adapter!!.notifyDataSetChanged()
                }
            }
            .create()

        type.layoutManager = GridLayoutManager(mContext, 3)
        type.adapter = adapter
        adapter!!.notifyItemRangeInserted(0, list.size)

        close.setOnClickListener {
            isTasking = false
            cancel()
        }

        converter.setOnClickListener {
            if (format != "") {
                convert(format)
                return@setOnClickListener
            }

            ToastUtil.showShort(mContext, "选择转换格式")
        }

    }

    private fun convert(format: String) {
        LogReportManager.logReport("视频格式转换", "使用功能", LogReportManager.LogType.OPERATION)
        converter.text = "转换中..."
        converter.isEnabled = false
        converter.setBackgroundResource(R.drawable.shape_corner_grey)
        isTasking = true


        doTask(mFilePath, format)

    }

    private fun doTask(path: String, format: String) {
        JLog.i("path = $path")

        val callback = object : OnHandleListener {

            override fun onBegin() {
                JLog.i("onBegin")
            }

            override fun onMsg(msg: String) {
                JLog.i("msg = $msg")
            }

            override fun onEnd(resultCode: Int, resultMsg: String) {
                JLog.i("resultCode = $resultCode, resultMsg = $resultMsg")
                mContext.runOnUiThread {
                    if (resultCode == 0) {
                        if (isTasking) {
                            converter.text = "转换成功"
                            converter.setBackgroundResource(R.drawable.shape_corner_blue)
                            ToastUtil.showShort(mContext, "转换成功")
                            converter.isEnabled = true
                            isTasking = false
                        }
                    } else {
                        if (isTasking) {
                            converter.text = "转换失败"
                            converter.setBackgroundResource(R.drawable.shape_corner_red)
                            ToastUtil.showShort(mContext, resultMsg)
                            isTasking = false
                        }
                    }
                }
            }

            override fun onProgress(progress: Int, duration: Int) {
                JLog.i("progress = $progress")
                JLog.i("duration = $duration")
            }

        }

        launch(Dispatchers.IO) {
            AudioManager.get().videoTrans(mContext, path, format, callback)
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