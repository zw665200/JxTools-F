package com.recording.trans.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.recording.trans.R
import com.recording.trans.adapter.DataAdapter
import com.recording.trans.bean.FileWithType
import com.recording.trans.callback.FileCallback
import com.recording.trans.callback.FuncCallback
import com.recording.trans.callback.VoiceCallback
import com.recording.trans.controller.AudioManager
import com.recording.trans.controller.DBManager
import com.recording.trans.utils.*
import com.recording.trans.view.activity.AudioToTextActivity
import com.recording.trans.view.base.BaseFragment
import com.recording.trans.view.views.AudioSortDialog
import com.recording.trans.view.views.ExtraFuncDialog
import com.recording.trans.view.views.RenameDialog
import com.recording.trans.view.views.WaveView
import kotlinx.android.synthetic.main.item_pic.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

open class FFile : BaseFragment() {
    private lateinit var model: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var waveView: WaveView
    private lateinit var importFile: TextView
    private lateinit var createFolder: TextView
    private lateinit var sort: TextView
    private lateinit var search: EditText

    private lateinit var progressTimer: CountDownTimer
    private var prepared = true

    private val list = arrayListOf<FileWithType>()
    private val searchList = arrayListOf<FileWithType>()

    private lateinit var mAdapter: DataAdapter<FileWithType>

    private val mHandler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            fileSort(msg.what)
            super.handleMessage(msg)
        }
    }

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.f_file, container, false)

        search = rootView.findViewById(R.id.search)
        model = rootView.findViewById(R.id.model)
        recyclerView = rootView.findViewById(R.id.rv_file_list)
        waveView = rootView.findViewById(R.id.waveView)
        importFile = rootView.findViewById(R.id.import_file)
        createFolder = rootView.findViewById(R.id.create_folder)
        sort = rootView.findViewById(R.id.sort)

        return rootView
    }

    override fun initData() {
        loadFunction()
        loadDeviceInfo()

        importFile.setOnClickListener { importFiles() }
        sort.setOnClickListener { AudioSortDialog(requireActivity(), mHandler).show() }

        search.setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_GO) {
                searchFile(v.text.toString())
            }
            false
        }
    }

    override fun click(v: View?) {
    }

    @SuppressLint("NotifyDataSetChanged", "SetTextI18n")
    private fun loadFunction() {
        list.clear()

        mAdapter = DataAdapter.Builder<FileWithType>()
            .setData(list)
            .setLayoutId(R.layout.item_pic)
            .addBindView { itemView, itemData ->
                val date = AppUtil.timeStamp2Date(itemData.date.toString(), null)
                JLog.i("date = $date")
                val size = itemData.size
                itemView.file_name.text = itemData.name

                when (itemData.type) {
//                    "folder" -> {
//                        itemView.file_src.setImageResource(R.drawable.default_folder)
//                        itemView.file_des.text = "$date"
//                    }
                    "audio" -> {
                        itemView.file_src.setImageResource(R.drawable.default_voice)
                        if (size < 1024) {
                            itemView.file_des.text = "$date  ${size}B"
                        } else if (size > 1024 * 1024) {
                            itemView.file_des.text = "$date  ${size / 1024 / 1024}MB"
                        } else {
                            itemView.file_des.text = "$date  ${size / 1024}KB"
                        }
                    }

                    "video" -> {
                        itemView.file_src.setImageResource(R.drawable.default_video)
                        if (size < 1024) {
                            itemView.file_des.text = "$date  ${size}B"
                        } else if (size > 1024 * 1024) {
                            itemView.file_des.text = "$date  ${size / 1024 / 1024}MB"
                        } else {
                            itemView.file_des.text = "$date  ${size / 1024}KB"
                        }
                    }

                    "doc" -> {
                        itemView.file_src.setImageResource(R.drawable.default_txt)
                        if (size < 1024) {
                            itemView.file_des.text = "$date  ${size}B"
                        } else if (size > 1024 * 1024) {
                            itemView.file_des.text = "$date  ${size / 1024 / 1024}MB"
                        } else {
                            itemView.file_des.text = "$date  ${size / 1024}KB"
                        }
                    }

                    "other" -> {
                        itemView.file_src.setImageResource(R.drawable.default_other)
                        if (size < 1024) {
                            itemView.file_des.text = "$date  ${size}B"
                        } else if (size > 1024 * 1024) {
                            itemView.file_des.text = "$date  ${size / 1024 / 1024}MB"
                        } else {
                            itemView.file_des.text = "$date  ${size / 1024}KB"
                        }
                    }
                }

                itemView.more.setOnClickListener {
                    ExtraFuncDialog(requireActivity(), object : FuncCallback {
                        override fun onSuccess(message: String) {
                            when (message) {
                                "file_to_text" -> openFileToTextPage(itemData.path)
                                "rename" -> RenameDialog(requireActivity(), itemData.path, object : FuncCallback {
                                    override fun onSuccess(message: String) {
                                        JLog.i("path = $message")

                                        FileUtil.renameTo(itemData.path, message)
                                        ToastUtil.showShort(requireActivity(), "修改成功")
                                        loadFiles()
                                    }

                                    override fun onCancel() {
                                    }
                                }).show()

                                "delete" -> {
                                    AudioManager.get().deleteFile(requireActivity(), itemData.path)
                                    list.remove(itemData)
                                    mAdapter.notifyDataSetChanged()
                                }

                                "from" -> {
                                    ToastUtil.showLong(requireActivity(), "位置：${itemData.path}")
                                }
                            }
                        }

                        override fun onCancel() {
                        }
                    }).show()
                }

            }
            .create()

        recyclerView.layoutManager = LinearLayoutManager(requireActivity())
        recyclerView.adapter = mAdapter

        loadFiles()
    }


    @SuppressLint("SetTextI18n")
    private fun loadDeviceInfo() {
        if (!prepared) return

        val total = DeviceUtil.getTotalExternalMemorySize()
        val free = DeviceUtil.getFreeSpace()
        if (total != 0L && free != 0L) {
            val per = 100 * (total - free) / total
            val totalGB = String.format("%.2f", total.toFloat() / 1024 / 1024 / 1024)
            val usedGB = String.format("%.2f", (total - free).toFloat() / 1024 / 1024 / 1024)
            model.text = "${usedGB}G/${totalGB}G"

            progressTimer = object : CountDownTimer(4000, 50) {
                override fun onFinish() {
                    waveView.progress = per.toInt()
                    prepared = true
                }

                override fun onTick(millisUntilFinished: Long) {
                    prepared = false
                    waveView.progress = ((4000 - millisUntilFinished) * per / 4000).toInt()
                    waveView.bottomText = "${waveView.progress}%"
                }
            }

            progressTimer.start()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadFiles() {
        launch(Dispatchers.IO) {
            list.clear()
            searchList.clear()

            AudioManager.get().getAllVoice(requireActivity(), object : VoiceCallback {
                @SuppressLint("NotifyDataSetChanged")
                override fun onSuccess() {

                }

                override fun onProgress(file: FileWithType) {
                    list.add(file)
                    searchList.add(file)
                    requireActivity().runOnUiThread {
                        mAdapter.notifyDataSetChanged()
                    }
                }

                override fun onFailed(message: String) {
                }
            })
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun searchFile(key: String) {
        JLog.i("key = $key")
        if (key == "") {
            list.clear()
            list.addAll(searchList)
            mAdapter.notifyDataSetChanged()
        } else {
            list.clear()
            for (child in searchList) {
                val name = child.name
                if (name.contains(key)) {
                    list.add(child)
                }
            }
            mAdapter.notifyDataSetChanged()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun fileSort(index: Int) {
        if (list.size == 0) return

        val sortList = arrayListOf<FileWithType>()
        sortList.addAll(list)
        list.clear()
        when (index) {
            0 -> list.addAll(searchList)
            1 -> {
                sortList.sortByDescending { it.size }
                list.addAll(sortList)
            }
            2 -> {
                sortList.sortBy { it.size }
                list.addAll(sortList)
            }
            3 -> {
                sortList.sortByDescending { it.date }
                list.addAll(sortList)
            }
            4 -> {
                sortList.sortBy { it.date }
                list.addAll(sortList)
            }
        }

        mAdapter.notifyDataSetChanged()
    }

    private fun importFiles() {
        checkPermissions {
            val intent = Intent(Intent.ACTION_GET_CONTENT, null)
            intent.type = "*/*"
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            startActivityForResult(intent, 0x1001)
        }
    }

    private fun openFileToTextPage(path: String) {
        val index = path.lastIndexOf(".")
        if (index > 0) {
            val type = path.substring(index - 1).lowercase()
            if (type == "mp3" || type == "wav" || type == "pcm" || type == "m4a" || type == "amr") {
                val intent = Intent(requireActivity(), AudioToTextActivity::class.java)
                intent.putExtra("path", path)
                startActivity(intent)
                return
            } else {
                ToastUtil.showShort(activity, "此格式不支持转换")
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1001) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    checkFile(uri)
                }
            }
        }
    }

    private fun checkFile(uri: Uri) {
        val path = FileUtil.getPathFromUri(requireActivity(), uri) ?: return
        if (path.endsWith(".mp3") || path.endsWith(".wav") || path.endsWith(".aac")) {
            AudioManager.get().addNewFile(requireActivity(), path, object : FileCallback {
                override fun onSuccess(filePath: String) {
                    val file = File(filePath)
                    val fileWithType = FileWithType(file.name, file.path, file.length(), file.lastModified(), "file", false)
                    list.add(fileWithType)
                    mAdapter.notifyItemInserted(list.size - 1)
                }

                override fun onFailed(message: String) {
                }
            })
        } else {
            ToastUtil.showShort(activity, "文件格式不支持")
        }
    }


}