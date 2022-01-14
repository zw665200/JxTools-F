package com.recording.trans.view.activity

import android.annotation.SuppressLint
import android.content.Intent
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatSpinner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.recording.trans.R
import com.recording.trans.adapter.DataAdapter
import com.recording.trans.bean.FileBean
import com.recording.trans.bean.FileWithType
import com.recording.trans.callback.DialogCallback
import com.recording.trans.callback.VoiceCallback
import com.recording.trans.controller.*
import com.recording.trans.utils.AppUtil
import com.recording.trans.utils.ToastUtil
import com.recording.trans.view.base.BaseActivity
import com.recording.trans.view.views.AudioToTextDialog
import com.recording.trans.view.views.QuitDialog
import kotlinx.android.synthetic.main.item_pic.view.*
import kotlinx.android.synthetic.main.item_voice.view.*
import kotlinx.coroutines.*
import kotlin.concurrent.thread

class AudioToTextActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var picRv: RecyclerView
    private lateinit var mAdapter: DataAdapter<FileWithType>

    private var mainVoices = mutableListOf<FileWithType>()
    private var sortVoices = mutableListOf<FileWithType>()

    private lateinit var from: AppCompatSpinner
    private lateinit var size: AppCompatSpinner
    private lateinit var time: AppCompatSpinner
    private lateinit var noData: ImageView
    private var prepared = false
    private lateinit var title: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var searchStatus: TextView
    private lateinit var progressBarLayout: LinearLayout
    private lateinit var option: ImageView
    private lateinit var sortView: LinearLayout
    private lateinit var desc: TextView

    private var type = "default"
    private var minSize: Long = 0L
    private var maxSize: Long = 1024 * 1024 * 1024L
    private var minDate: Long = 0L
    private var maxDate = System.currentTimeMillis()
    private var initSpinnerSort = false
    private var initSpinnerSize = false
    private var initSpinnerTime = false

    override fun setLayout(): Int {
        return R.layout.a_file_to_voice
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        noData = findViewById(R.id.no_data)
        picRv = findViewById(R.id.rv_voice)
        title = findViewById(R.id.wx_name)
        progressBar = findViewById(R.id.progress)
        searchStatus = findViewById(R.id.search_status)
        progressBarLayout = findViewById(R.id.ll_progressbar)
        sortView = findViewById(R.id.ll_1)
        option = findViewById(R.id.option)
        desc = findViewById(R.id.progress_des)
        from = findViewById(R.id.spinner_from)
        size = findViewById(R.id.spinner_size)
        time = findViewById(R.id.spinner_time)

        back.setOnClickListener { onBackPressed() }

        option.visibility = View.GONE
        title.text = getString(R.string.file_to_text_title)
        desc.text = getString(R.string.progress_des)

        loadVoices()
        spinnerListener()
    }

    override fun initData() {
        searchVoices()

        val path = intent.getStringExtra("path")
        if (path != null) {
            PayManager.get().checkPay(this) {
                if (it) {
                    AudioToTextDialog(this, path).show()
                } else {
                    toPayPage()
                }
            }
        }

        LogReportManager.logReport("音频文件转写", "访问页面", LogReportManager.LogType.OPERATION)
    }

    @SuppressLint("SetTextI18n")
    private fun loadVoices() {
        mAdapter = DataAdapter.Builder<FileWithType>()
            .setData(mainVoices)
            .setLayoutId(R.layout.item_file_to_text)
            .addBindView { itemView, itemData ->

                val date = AppUtil.timeStamp2Date(itemData.date.toString(), "yyyy-MM-dd")
                val size = itemData.size / 1024
                itemView.voice_name.text = itemData.name
                itemView.voice_description.text = "$date   ${size}KB"

                itemView.voice_select.setOnClickListener {
                    PayManager.get().checkPay(this) {
                        if (it) {
                            AudioToTextDialog(this, itemData.path).show()
                        } else {
                            toPayPage()
                        }
                    }
                }
            }
            .create()

        picRv.layoutManager = LinearLayoutManager(this)
        picRv.adapter = mAdapter
    }

    private fun spinnerListener() {
        from.setPopupBackgroundResource(R.color.color_white)
        size.setPopupBackgroundResource(R.color.color_white)
        time.setPopupBackgroundResource(R.color.color_white)

        from.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!initSpinnerSort) {
                    initSpinnerSort = true
                    return
                }

                when (position) {
                    0 -> {
                        type = "default"
                        sortAllSelectedList()
                    }

                    1 -> {
                        type = "date_desc"
                        sortAllSelectedList()
                    }

                    2 -> {
                        type = "date_asc"
                        sortAllSelectedList()
                    }

                    3 -> {
                        type = "size_desc"
                        sortAllSelectedList()
                    }

                    4 -> {
                        type = "size_asc"
                        sortAllSelectedList()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        size.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!initSpinnerSize) {
                    initSpinnerSize = true
                    return
                }
                when (position) {
                    0 -> {
                        minSize = 0
                        maxSize = 1024 * 1024 * 1024L
                        sortAllSelectedList()
                    }

                    1 -> {
                        minSize = 0
                        maxSize = 10 * 1024L
                        sortAllSelectedList()
                    }

                    2 -> {
                        minSize = 10 * 1024L
                        maxSize = 1024 * 1024 * 1024L
                        sortAllSelectedList()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }

        time.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!initSpinnerTime) {
                    initSpinnerTime = true
                    return
                }
                when (position) {
                    0 -> {
                        minDate = 0
                        maxDate = System.currentTimeMillis()
                        sortAllSelectedList()
                    }

                    1 -> {
                        minDate = System.currentTimeMillis() - 3 * 86400000L
                        maxDate = System.currentTimeMillis()
                        sortAllSelectedList()
                    }

                    2 -> {
                        minDate = System.currentTimeMillis() - 30 * 86400000L
                        maxDate = System.currentTimeMillis() - 3 * 86400000L
                        sortAllSelectedList()
                    }

                    3 -> {
                        minDate = 0
                        maxDate = System.currentTimeMillis() - 30 * 86400000L
                        sortAllSelectedList()
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun sortAllSelectedList() {
        if (!prepared && sortVoices.isEmpty()) return
        launch(Dispatchers.IO) {
            sortVoices = DBManager.getVoiceByKey(this@AudioToTextActivity, type, minSize, maxSize, minDate, maxDate)
            launch(Dispatchers.Main) {
                mainVoices.clear()
                if (sortVoices.size > 0) {
                    mainVoices.addAll(sortVoices)

                    val tText = getString(R.string.file_to_text_title) + "(${sortVoices.size})"
                    title.text = tText
                } else {
                    title.text = getString(R.string.file_to_text_title)
                }
                mAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun searchVoices() {

        val callback = object : VoiceCallback {
            override fun onSuccess() {
                launch(Dispatchers.Main) {
                    searchFinish()
                }
            }

            @SuppressLint("SetTextI18n")
            override fun onProgress(file: FileWithType) {
                launch(Dispatchers.Main) {
                    mainVoices.add(file)
                    sortVoices.add(file)

                    //如果没有按下了暂停键，继续刷新界面
                    if (progressBar.progress >= 90) {
                        progressBar.progress = 90
                    } else {
                        progressBar.progress = progressBar.progress + 1
                    }
                    mAdapter.notifyItemInserted(mainVoices.indexOf(file))
                    title.text = getString(R.string.file_to_text_title) + "(${sortVoices.size})"
                }
            }

            override fun onFailed(message: String) {
                launch(Dispatchers.Main) {
                    prepared = true
                    progressBarLayout.visibility = View.GONE
                    noData.visibility = View.VISIBLE
                    ToastUtil.showShort(this@AudioToTextActivity, message)
                }
            }
        }

        thread { WxManager.getInstance(this@AudioToTextActivity).getWxVoices(this, callback) }
    }


    private fun searchFinish() {
        prepared = true
        progressBar.progress = progressBar.max
        searchStatus.text = getString(R.string.search_status_finish)
        progressBarLayout.visibility = View.GONE
        sortView.visibility = View.VISIBLE
    }


    private fun toPayPage() {
        if (Constant.CLIENT_TOKEN == "") {
            val intent = Intent()
            intent.setClass(this, LoginActivity::class.java)
            startActivity(intent)
        } else {
            val intent = Intent()
            intent.setClass(this, PayActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onBackPressed() {
        if (prepared) {
            finish()
        } else {
            QuitDialog(this, "正在扫描中，确定要退出吗？", object : DialogCallback {
                override fun onSuccess(file: FileBean) {
                    Constant.ScanStop = true
                    finish()
                }

                override fun onCancel() {
                }
            }).show()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        mainVoices.clear()
        sortVoices.clear()
    }

}