package com.recording.trans.view.views

import android.app.Activity
import android.app.Dialog
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.ImageView
import com.recording.trans.R
import com.recording.trans.bean.FileBean
import com.recording.trans.callback.Callback
import com.recording.trans.callback.DialogCallback


class DiscountDialog(private val activity: Activity, callback: Callback) : Dialog(activity, R.style.app_dialog) {
    private var mCallback = callback
    private lateinit var close: ImageView
    private lateinit var discount: ImageView

    init {
        initVew()
    }

    private fun initVew() {
        val dialogContent = LayoutInflater.from(activity).inflate(R.layout.d_pay_discount, null)
        setContentView(dialogContent)
        setCancelable(false)

        close = findViewById(R.id.close)
        discount = findViewById(R.id.discount)

        close.setOnClickListener { cancel() }

        discount.setOnClickListener {
            mCallback.onSuccess()
            cancel()
        }

    }


    override fun show() {
        window!!.decorView.setPadding(0, 0, 0, 0)
        window!!.attributes = window!!.attributes.apply {
            gravity = Gravity.CENTER

            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
        }
        super.show()
    }


}