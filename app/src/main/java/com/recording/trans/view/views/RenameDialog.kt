package com.recording.trans.view.views

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.recording.trans.R
import com.recording.trans.callback.FuncCallback
import com.recording.trans.utils.AppUtil
import java.io.File


class RenameDialog(private val activity: Activity, private val filePath: String, callback: FuncCallback) : Dialog(activity, R.style.app_dialog) {
    private lateinit var name: EditText
    private lateinit var submit: Button
    private var mCallback = callback


    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(activity).inflate(R.layout.d_customer_service, null)
        setContentView(dialogContent)
        setCancelable(true)

        name = dialogContent.findViewById(R.id.file_name)
        submit = dialogContent.findViewById(R.id.update)

        val file = File(filePath)
        if (file.exists()) {
            val srcName = file.name
            val srcPath = filePath.replace(srcName, "")
            val dot = srcName.lastIndexOf(".")
            val fileEx = srcName.substring(dot + 1)
            val realName = srcName.replace(".$fileEx", "")
            name.setText(realName, TextView.BufferType.EDITABLE)
            submit.setOnClickListener {
                val text = name.text.toString()
                if (text != realName) {
                    mCallback.onSuccess("$srcPath$text.$fileEx")
                    cancel()
                }
            }
        }


    }


    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER
            width = AppUtil.getScreenWidth(context) * 5 / 6
            height = AppUtil.getScreenWidth(context) / 2
        }
        super.show()
    }


}