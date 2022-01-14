package com.recording.trans.view.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.*
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.CompositePageTransformer
import androidx.viewpager2.widget.MarginPageTransformer
import androidx.viewpager2.widget.ViewPager2
import com.bytedance.sdk.openadsdk.*
import com.recording.trans.R
import com.recording.trans.adapter.DataAdapter
import com.recording.trans.bean.FileWithType
import com.recording.trans.bean.Resource
import com.recording.trans.callback.FuncCallback
import com.recording.trans.callback.VoiceCallback
import com.recording.trans.controller.AudioManager
import com.recording.trans.controller.Constant
import com.recording.trans.controller.DBManager
import com.recording.trans.controller.IMManager
import com.recording.trans.utils.*
import com.recording.trans.view.activity.*
import com.recording.trans.view.base.BaseFragment
import com.recording.trans.view.views.ExtraFuncDialog
import com.recording.trans.view.views.RenameDialog
import com.recording.trans.view.views.ScaleInTransformer
import kotlinx.android.synthetic.main.item_heart.view.*
import kotlinx.android.synthetic.main.item_heart.view.iv_icon
import kotlinx.android.synthetic.main.item_heart_small.view.*
import kotlinx.android.synthetic.main.item_pic.view.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList

open class FHome : BaseFragment() {
    private lateinit var rv: RecyclerView
    private lateinit var otherRv: RecyclerView
    private lateinit var currentRv: RecyclerView

    private lateinit var pager: ViewPager2
    private var lastClickTime = 0L
    private var mAdapter: DataAdapter<FileWithType>? = null

    private lateinit var pointOne: ImageView
    private lateinit var pointTwo: ImageView
    private lateinit var pointThree: ImageView
    private lateinit var otherTools: TextView
    private lateinit var otherFiles: TextView

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.f_home, container, false)
        rv = rootView.findViewById(R.id.ry_billboard)
        otherRv = rootView.findViewById(R.id.ry_other_func)
        currentRv = rootView.findViewById(R.id.ry_current_file)
        pager = rootView.findViewById(R.id.pager)

        pointOne = rootView.findViewById(R.id.point_one)
        pointTwo = rootView.findViewById(R.id.point_two)
        pointThree = rootView.findViewById(R.id.point_three)
        otherTools = rootView.findViewById(R.id.other_tools)
        otherFiles = rootView.findViewById(R.id.other_files)

        return rootView
    }

    override fun initData() {

        initMainRecycleView()
        initOtherRecycleView()
        initCurrentRecycleView()
        initPager()

        otherTools.setOnClickListener { (activity as MainActivity).changeFragment(2) }
        otherFiles.setOnClickListener { (activity as MainActivity).changeFragment(1) }

    }

    override fun onResume() {
        super.onResume()
        initCustomerService()
    }


    override fun click(v: View?) {
    }

    private fun initCustomerService() {
        IMManager.setMessageListener {
            AppUtil.sendNotification(activity, Constant.Notification_title, Constant.Notification_content)
        }
    }

    private fun initMainRecycleView() {
        val mainPics = mutableListOf<Resource>()
        mainPics.add(Resource("online_trans", R.drawable.home_main_func_a, "语音实时转文字"))
        mainPics.add(Resource("text_to_voice", R.drawable.home_main_func_b, "文字转语音"))

        val mainAdapter = DataAdapter.Builder<Resource>()
            .setData(mainPics)
            .setLayoutId(R.layout.item_heart)
            .addBindView { itemView, itemData ->

                itemView.iv_icon.setImageResource(itemData.icon)

                itemView.setOnClickListener {
                    if (lastClickTime == 0L) {
                        lastClickTime = System.currentTimeMillis()
                    } else {
                        if (System.currentTimeMillis() - lastClickTime < 1000) return@setOnClickListener
                    }

                    lastClickTime = System.currentTimeMillis()

                    checkPermissions {
                        when (itemData.type) {
                            "online_trans" -> openOnlineTransPage()
                            "text_to_voice" -> openTextToVoicePage()
                        }
                    }

                }
            }
            .create()

        rv.adapter = mainAdapter
        rv.layoutManager = GridLayoutManager(activity, 2)
        mainAdapter.notifyItemRangeChanged(0, mainPics.size)
    }

    private fun initOtherRecycleView() {
        val otherPics = mutableListOf<Resource>()
        otherPics.add(Resource("file_to_text", R.drawable.home_other_func_a, "音频转文字"))
        otherPics.add(Resource("format_trans", R.drawable.home_other_func_b, "音频格式转换"))
        otherPics.add(Resource("pic_to_text", R.drawable.home_other_func_c, "图片转文字"))
        otherPics.add(Resource("recorder", R.drawable.home_other_func_d, "录音机"))

        val otherAdapter = DataAdapter.Builder<Resource>()
            .setData(otherPics)
            .setLayoutId(R.layout.item_heart_small)
            .addBindView { itemView, itemData ->

                itemView.iv_icon.setImageResource(itemData.icon)
                itemView.tv_name.text = itemData.name

                itemView.setOnClickListener {
                    if (lastClickTime == 0L) {
                        lastClickTime = System.currentTimeMillis()
                    } else {
                        if (System.currentTimeMillis() - lastClickTime < 1000) return@setOnClickListener
                    }

                    lastClickTime = System.currentTimeMillis()

                    checkPermissions {
                        when (itemData.type) {
                            "file_to_text" -> openFileToTextPage()
                            "format_trans" -> openFormatTransPage()
                            "recorder" -> openRecorderPage()
                            "pic_to_text" -> openImageToTextPage()
                        }
                    }
                }
            }
            .create()

        otherRv.adapter = otherAdapter
        otherRv.layoutManager = GridLayoutManager(activity, 4)
        otherAdapter.notifyItemRangeChanged(0, otherPics.size)
    }

    @SuppressLint("SetTextI18n")
    private fun initCurrentRecycleView() {
        val list = arrayListOf<FileWithType>()
        val sortList = arrayListOf<FileWithType>()

        mAdapter = DataAdapter.Builder<FileWithType>()
            .setData(list)
            .setLayoutId(R.layout.item_pic)
            .addBindView { itemView, itemData ->
                val date = AppUtil.timeStamp2Date(itemData.date.toString(), null)
                val size = itemData.size
                itemView.file_name.text = itemData.name

                when (itemData.type) {
                    "audio" -> {
                        itemView.file_src.setImageResource(R.drawable.default_video)
                        if (size < 1024) {
                            itemView.file_des.text = "$date  ${size}B"
                            return@addBindView
                        }

                        if (size > 1024 * 1024) {
                            itemView.file_des.text = "$date  ${size / 1024 / 1024}MB"
                            return@addBindView
                        }

                        itemView.file_des.text = "$date  ${size / 1024}KB"

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
                        @SuppressLint("NotifyDataSetChanged")
                        override fun onSuccess(message: String) {
                            when (message) {
                                "file_to_text" -> openFileToTextPage(itemData.path)
                                "rename" -> RenameDialog(requireActivity(), itemData.path, object : FuncCallback {
                                    override fun onSuccess(message: String) {
                                        JLog.i("path = $message")

                                        FileUtil.renameTo(itemData.path, message)
                                        ToastUtil.showShort(requireActivity(), "修改成功")
                                        loadFiles(list, sortList)
                                    }

                                    override fun onCancel() {
                                    }
                                }).show()

                                "delete" -> {
                                    AudioManager.get().deleteFile(requireActivity(), itemData.path)
                                    list.remove(itemData)
                                    mAdapter?.notifyDataSetChanged()
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

        currentRv.layoutManager = LinearLayoutManager(requireActivity())
        currentRv.adapter = mAdapter

        loadFiles(list, sortList)

    }

    private fun loadFiles(list: ArrayList<FileWithType>, sortList: ArrayList<FileWithType>) {
        launch(Dispatchers.IO) {
            list.clear()
            AudioManager.get().getAllVoice(requireActivity(), object : VoiceCallback {

                @SuppressLint("NotifyDataSetChanged")
                override fun onSuccess() {
                    requireActivity().runOnUiThread {
                        sortList.sortByDescending { it.date }
                        if (sortList.size > 3) {
                            list.addAll(sortList.subList(0, 3))
                        } else {
                            list.addAll(sortList)
                        }
                        mAdapter?.notifyDataSetChanged()
                    }
                }

                override fun onProgress(file: FileWithType) {
                    sortList.add(file)
                }

                override fun onFailed(message: String) {
                }
            })
        }
    }

    private fun initPager() {
        val mainPics = mutableListOf<Resource>()
        mainPics.add(Resource("pic", R.drawable.home_banner_01, "图片恢复"))
        mainPics.add(Resource("audio", R.drawable.home_banner_02, "语音恢复"))
        mainPics.add(Resource("video", R.drawable.home_banner_03, "视频恢复"))

        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
                super.onPageScrolled(position, positionOffset, positionOffsetPixels)
            }

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                when (position) {
                    0 -> {
                        pointOne.setBackgroundResource(R.drawable.ic_point_select)
                        pointTwo.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointThree.setBackgroundResource(R.drawable.ic_point_unselect)
                    }

                    1 -> {
                        pointOne.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointTwo.setBackgroundResource(R.drawable.ic_point_select)
                        pointThree.setBackgroundResource(R.drawable.ic_point_unselect)
                    }

                    2 -> {
                        pointOne.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointTwo.setBackgroundResource(R.drawable.ic_point_unselect)
                        pointThree.setBackgroundResource(R.drawable.ic_point_select)
                    }
                }

            }

        })

        val pagerAdapter = DataAdapter.Builder<Resource>()
            .setData(mainPics)
            .setLayoutId(R.layout.item_banner)
            .addBindView { itemView, itemData ->
                itemView.iv_icon.setImageResource(itemData.icon)
            }
            .create()

        val compositePageTransformer = CompositePageTransformer()
        compositePageTransformer.addTransformer(ScaleInTransformer())
        compositePageTransformer.addTransformer(MarginPageTransformer(resources.getDimension(R.dimen.dp_5).toInt()))

        pager.apply {
            adapter = pagerAdapter
            setPageTransformer(compositePageTransformer)
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
            offscreenPageLimit = 3
        }
    }


    private fun openOnlineTransPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), OnlineRecordingActivity::class.java)
        startActivity(intent)
    }

    private fun openVoiceToTextPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), ShortRecordingActivity::class.java)
        startActivity(intent)
    }

    private fun openTextToVoicePage() {
        val intent = Intent()
        intent.setClass(requireActivity(), TextToVoiceActivity::class.java)
        startActivity(intent)
    }

    private fun openFileToTextPage() {
        val intent = Intent(requireActivity(), AudioToTextActivity::class.java)
        startActivity(intent)
    }

    private fun openFileToTextPage(path: String) {
        if (!path.endsWith(".txt")) {
            val intent = Intent(requireActivity(), AudioToTextActivity::class.java)
            intent.putExtra("path", path)
            startActivity(intent)
        } else {
            ToastUtil.showShort(activity, "文本文件不用转换")
        }
    }

    private fun openFormatTransPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), VoiceFormatTransActivity::class.java)
        startActivity(intent)
    }

    private fun openRecorderPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), RecorderActivity::class.java)
        startActivity(intent)
    }

    private fun openImageToTextPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), ImageToTextActivity::class.java)
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        IMManager.removeMessageListener()
        IMManager.logout()
    }
}