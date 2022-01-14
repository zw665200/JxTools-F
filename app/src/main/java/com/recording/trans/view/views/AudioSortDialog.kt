package com.recording.trans.view.views

import android.app.Activity
import android.app.Dialog
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.*
import com.recording.trans.R

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/10/26 10:39
 */
class AudioSortDialog(activity: Activity, handler: Handler) : Dialog(activity, R.style.app_dialog) {
    private val mContext = activity
    private val mHandler = handler

    private lateinit var defaultSort: TextView
    private lateinit var sizeDesc: TextView
    private lateinit var sizeAsc: TextView
    private lateinit var dateDesc: TextView
    private lateinit var dateAsc: TextView

    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(mContext).inflate(R.layout.p_audio_sort, null)
        setContentView(view)
        setCancelable(true)

        defaultSort = findViewById(R.id.default_sort)
        sizeDesc = findViewById(R.id.size_desc)
        sizeAsc = findViewById(R.id.size_asc)
        dateDesc = findViewById(R.id.date_desc)
        dateAsc = findViewById(R.id.date_asc)

        defaultSort.setOnClickListener {
            mHandler.sendEmptyMessage(0)
            cancel()
        }

        sizeDesc.setOnClickListener {
            mHandler.sendEmptyMessage(1)
            cancel()
        }

        sizeAsc.setOnClickListener {
            mHandler.sendEmptyMessage(2)
            cancel()
        }

        dateDesc.setOnClickListener {
            mHandler.sendEmptyMessage(3)
            cancel()
        }

        dateAsc.setOnClickListener {
            mHandler.sendEmptyMessage(4)
            cancel()
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