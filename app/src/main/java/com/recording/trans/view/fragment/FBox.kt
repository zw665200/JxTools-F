package com.recording.trans.view.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.recording.trans.R
import com.recording.trans.adapter.DataAdapter
import com.recording.trans.bean.Resource
import com.recording.trans.view.activity.*
import com.recording.trans.view.base.BaseFragment
import kotlinx.android.synthetic.main.item_function.view.*
import kotlinx.android.synthetic.main.item_heart.view.*
import kotlinx.android.synthetic.main.item_heart.view.iv_icon
import kotlinx.android.synthetic.main.item_heart_small.view.*

open class FBox : BaseFragment() {
    private lateinit var rvA: RecyclerView
    private lateinit var rvB: RecyclerView
    private var lastClickTime = 0L

    override fun initView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val rootView = inflater.inflate(R.layout.f_box, container, false)
        rvA = rootView.findViewById(R.id.rv_tools_a)
        rvB = rootView.findViewById(R.id.rv_tools_b)
        return rootView
    }

    override fun initData() {
        loadFuncA()
        loadFuncB()
    }

    override fun click(v: View?) {
    }

    private fun loadFuncA() {
        val otherPics = mutableListOf<Resource>()
        otherPics.add(Resource("file_to_text", R.drawable.home_other_func_a, "音频转文字"))
        otherPics.add(Resource("online_trans", R.drawable.home_other_func_h, "录音转文字"))
        otherPics.add(Resource("audio_format_trans", R.drawable.home_other_func_b, "音频格式转换"))
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
                            "online_trans" -> openOnlineTransPage()
                            "audio_format_trans" -> openAudioFormatTransPage()
                            "recorder" -> openRecorderPage()
                        }
                    }

                }
            }
            .create()

        rvA.adapter = otherAdapter
        rvA.layoutManager = GridLayoutManager(activity, 4)
        otherAdapter.notifyItemRangeChanged(0, otherPics.size)
    }

    private fun loadFuncB() {
        val otherPics = mutableListOf<Resource>()
        otherPics.add(Resource("doc_trans", R.drawable.home_other_func_e, "文档翻译"))
        otherPics.add(Resource("text_to_voice", R.drawable.home_other_func_f, "文字转语音"))
        otherPics.add(Resource("video_format_trans", R.drawable.home_other_func_g, "视频格式转换"))
        otherPics.add(Resource("pic_to_text", R.drawable.home_other_func_c, "图片转文字"))

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
                            "doc_trans" -> openDocTransPage()
                            "text_to_voice" -> openTextToVoicePage()
                            "video_format_trans" -> openVideoFormatTransPage()
                            "pic_to_text" -> openImageToTextPage()
                        }
                    }

                }
            }
            .create()

        rvB.adapter = otherAdapter
        rvB.layoutManager = GridLayoutManager(activity, 4)
        otherAdapter.notifyItemRangeChanged(0, otherPics.size)
    }

    private fun openOnlineTransPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), OnlineRecordingActivity::class.java)
        startActivity(intent)
    }

    private fun openTextToVoicePage() {
        val intent = Intent()
        intent.setClass(requireActivity(), TextToVoiceActivity::class.java)
        startActivity(intent)
    }

    private fun openFileToTextPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), AudioToTextActivity::class.java)
        startActivity(intent)
    }

    private fun openAudioFormatTransPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), VoiceFormatTransActivity::class.java)
        startActivity(intent)
    }

    private fun openVideoFormatTransPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), VideoFormatTransActivity::class.java)
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

    private fun openDocTransPage() {
        val intent = Intent()
        intent.setClass(requireActivity(), DocToTextActivity::class.java)
        startActivity(intent)
    }


}