package com.recording.trans.view.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent
import com.recording.trans.utils.ToastUtil

class HelpService : AccessibilityService() {
    override fun onServiceConnected() {
        super.onServiceConnected()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        TODO("Not yet implemented")
    }

    override fun onInterrupt() {
        ToastUtil.show(this, "任务中断")
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}