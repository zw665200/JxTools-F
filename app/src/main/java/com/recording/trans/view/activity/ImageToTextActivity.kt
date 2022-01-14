package com.recording.trans.view.activity

import android.content.Intent
import android.net.Uri
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.recording.trans.R
import com.recording.trans.callback.HttpCallback
import com.recording.trans.controller.AudioManager
import com.recording.trans.controller.LogReportManager
import com.recording.trans.controller.PayManager
import com.recording.trans.utils.FileUtil
import com.recording.trans.utils.ToastUtil
import com.recording.trans.view.base.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ImageToTextActivity : BaseActivity() {
    private lateinit var back: ImageView
    private lateinit var firstPic: ImageView
    private lateinit var secondPic: ImageView
    private lateinit var import: Button
    private lateinit var task: Button
    private lateinit var content: TextView
    private var imageUri: Uri? = null

    override fun setLayout(): Int {
        return R.layout.a_image_to_text
    }

    override fun initView() {
        back = findViewById(R.id.iv_back)
        firstPic = findViewById(R.id.first_pic)
        secondPic = findViewById(R.id.second_pic)
        content = findViewById(R.id.content)

        back.setOnClickListener { finish() }

        import = findViewById(R.id.open_album)
        task = findViewById(R.id.do_task)

        import.setOnClickListener { chooseAlbum() }
        task.setOnClickListener { doTask() }
        firstPic.setOnClickListener { choosePic(0) }
        secondPic.setOnClickListener { choosePic(1) }

    }

    override fun initData() {
        LogReportManager.logReport("图片转文字", "访问页面", LogReportManager.LogType.OPERATION)
    }

    private fun choosePic(index: Int) {
        when (index) {
            0 -> content.text = getString(R.string.pic_to_text_01)
            1 -> content.text = getString(R.string.pic_to_text_02)
        }
    }

    private fun chooseAlbum() {
        val intent = Intent()
        intent.action = Intent.ACTION_PICK
        intent.data = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        intent.type = "image/*"
        startActivityForResult(intent, 0x1001)
    }

    private fun doTask() {
        if (imageUri == null) {
            ToastUtil.showShort(this, "请导入图片")
            return
        }

        PayManager.get().checkPay(this) {
            if (it) {
                LogReportManager.logReport("图片转文字", "使用功能", LogReportManager.LogType.OPERATION)

                task.isEnabled = false
                task.text = "提取文字..."
                task.setBackgroundResource(R.drawable.shape_corner_grey)
                val realPath = FileUtil.getPathFromUri(this, imageUri)
                if (realPath != null) {
                    launch(Dispatchers.IO) {
                        AudioManager.get().getTextFromImage(realPath, object : HttpCallback {
                            override fun onSuccess() {
                                runOnUiThread {
                                    val intent = Intent(this@ImageToTextActivity, TextToVoiceActivity::class.java)
                                    intent.putExtra("from", true)
                                    startActivity(intent)
                                    finish()
                                }
                            }

                            override fun onFailed(msg: String) {
                                runOnUiThread {
                                    task.text = "转换失败"
                                    task.setBackgroundResource(R.drawable.shape_corner_red)
                                    ToastUtil.showShort(this@ImageToTextActivity, msg)
                                }
                            }
                        })
                    }
                }
            } else {
                openPayPage()
            }
        }
    }

    private fun openPayPage() {
        startActivity(Intent(this, PayActivity::class.java))
    }

    private fun checkFileSize(uri: Uri) {
        val length = contentResolver.openFileDescriptor(uri, "r")?.statSize ?: 0
        if (length > 3 * 1024 * 1024) {
            ToastUtil.show(this, "上传图片大小不能超过3MB")
        } else {
            imageUri = uri
            import.text = "已选择图片"
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 0x1001) {
            if (data != null) {
                val uri = data.data
                if (uri != null) {
                    checkFileSize(uri)
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()

    }

}