package com.recording.trans.view.views

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.TextView
import com.recording.trans.R
import com.recording.trans.bean.FileBean
import com.recording.trans.callback.DialogCallback
import com.recording.trans.utils.AppUtil


class QuitDialog(private val activity: Activity, val content: String, callback: DialogCallback) :
    Dialog(activity, R.style.app_dialog) {
    private lateinit var title: TextView
    private lateinit var agree: TextView
    private lateinit var cancel: TextView
    private var mTitle = content
    private var mCallback = callback


    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(activity).inflate(R.layout.d_quit, null)
        setContentView(dialogContent)
        setCancelable(true)

        title = dialogContent.findViewById(R.id.quit_title)
        agree = dialogContent.findViewById(R.id.quit_ok)
        cancel = dialogContent.findViewById(R.id.quite_cancel)

        title.text = mTitle

        cancel.setOnClickListener {
            cancel()
        }

        agree.setOnClickListener {
            mCallback.onSuccess(FileBean("", "", "", 0))
            cancel()
        }

    }


    override fun show() {
        val w = AppUtil.getScreenWidth(context)
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
//            width = w * 5 / 6
        }
        super.show()
    }


}