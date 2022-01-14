package com.recording.trans.view.views

import android.app.Activity
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.widget.AppCompatSeekBar
import com.recording.trans.R

/**
 * @author Herr_Z
 * @description:
 * @date : 2021/10/26 10:39
 */
class VoiceSettingDialog(activity: Activity, var spe: Int, var into: Int, var vol: Int, handler: Handler) : Dialog(activity, R.style.app_dialog) {
    private val mContext = activity
    private val mHandler = handler

    private lateinit var speed: AppCompatSeekBar
    private lateinit var intonation: AppCompatSeekBar
    private lateinit var volume: AppCompatSeekBar
    private lateinit var cancel: TextView
    private lateinit var finish: TextView
    private lateinit var speedText: TextView
    private lateinit var intonationText: TextView
    private lateinit var volumeText: TextView


    init {
        initView()
    }

    private fun initView() {
        val view = LayoutInflater.from(mContext).inflate(R.layout.p_voice_setting, null)
        setContentView(view)
        setCancelable(true)

        speed = view.findViewById(R.id.speed)
        intonation = view.findViewById(R.id.intonation)
        volume = view.findViewById(R.id.volume)

        finish = view.findViewById(R.id.finish)
        cancel = view.findViewById(R.id.cancel)

        speedText = findViewById(R.id.speed_text)
        intonationText = findViewById(R.id.intonation_text)
        volumeText = findViewById(R.id.volume_text)

        speed.progress = spe
        intonation.progress = into
        volume.progress = vol
        speedText.text = "$spe"
        intonationText.text = "$into"
        volumeText.text = "$vol"


        finish.setOnClickListener { finish() }
        cancel.setOnClickListener { dismiss() }

        speed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                spe = progress
                speedText.text = "$spe"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        intonation.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                into = progress
                intonationText.text = "$into"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        volume.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                vol = progress
                volumeText.text = "$vol"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

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

    private fun finish() {
        val message = Message()
        message.what = 0x1
        val bundle = Bundle()
        bundle.putInt("speech", spe)
        bundle.putInt("intonation", into)
        bundle.putInt("volume", vol)
        message.data = bundle
        mHandler.sendMessage(message)
        dismiss()
    }
}