package com.recording.trans.controller

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.recording.trans.bean.*
import com.recording.trans.callback.*
import com.recording.trans.utils.Dict
import com.recording.trans.utils.AppUtil
import com.recording.trans.utils.FileUtil
import com.recording.trans.utils.JLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*


class WxManager private constructor() {

    companion object {
        private var localPath = ""
        private var backupPath = ""
        private var jxBackupPath = ""
        private var exPortPath = ""

        @Volatile
        private var instance: WxManager? = null

        fun getInstance(c: Context): WxManager {
            if (instance == null) {
                synchronized(WxManager::class) {
                    if (instance == null) {
                        instance = WxManager()
                    }
                }
            }

            //初始化路径
            localPath = FileUtil.getSDPath(c)
            jxBackupPath = c.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)!!.absolutePath + Constant.JX_BACKUP_PATH
            exPortPath = localPath + Constant.EXPORT_PATH

            FileUtil.createFolder(backupPath)
            FileUtil.createFolder(jxBackupPath)
            FileUtil.createFolder(exPortPath)

            return instance!!
        }
    }


    fun checkBackupPath(): Boolean {
        if (Constant.ROM == "") {
            return false
        } else {

            when (Constant.ROM) {
                Constant.ROM_EMUI -> backupPath = localPath + Constant.BACKUP_PATH
                Constant.ROM_MIUI -> backupPath = localPath + Constant.XM_BACKUP_PATH
                Constant.ROM_FLYME -> backupPath = localPath + Constant.FLYME_BACKUP_PATH
                Constant.ROM_OPPO -> backupPath = localPath + Constant.OPPO_BACKUP_PATH
                Constant.ROM_VIVO -> backupPath = localPath + Constant.BACKUP_PATH
            }

            JLog.i("localPath = $localPath")
            JLog.i("backupPath = $backupPath")
            JLog.i("jxBackupPath = $jxBackupPath")

            return true
        }
    }


    /**
     * 获取音频文件
     */
    fun getWxVoices(context: Context, callback: VoiceCallback) {
        Constant.ScanStop = false

        val rootPath = localPath

        val voicesFiles = arrayListOf<FileWithType>()

        FileUtil.getVoiceFiles(rootPath, object : FCallback {
            override fun onSuccess(step: Enum<FileStatus>) {
            }

            override fun onProgress(step: Enum<FileStatus>, file: File) {
                val path = file.path
                val size = file.length()
                if (size <= 50 * 1024 * 1024L) {
                    val date = file.lastModified()
                    val fileWithType = FileWithType(file.name, path, file.length(), date, "voice_trans")
                    callback.onProgress(fileWithType)
                    DBManager.insert(context, fileWithType)
                    voicesFiles.add(fileWithType)
                }
            }

            override fun onFailed(step: Enum<FileStatus>, message: String) {
            }
        })


        if (voicesFiles.size > 0) {
            callback.onSuccess()
        } else {
            callback.onFailed("没有找到音频文件")
        }
    }

    /**
     * 获取视频文件
     */
    fun getVideos(context: Context, callback: VideoCallback) {
        Constant.ScanStop = false

        val rootPath = localPath

        val voicesFiles = arrayListOf<FileWithType>()

        FileUtil.getVideoFiles(rootPath, object : FCallback {
            override fun onSuccess(step: Enum<FileStatus>) {
            }

            override fun onProgress(step: Enum<FileStatus>, file: File) {
                val path = file.path

                val date = file.lastModified()
                val fileWithType = FileWithType(file.name, path, file.length(), date, "video_trans")
                callback.onProgress(fileWithType)
                DBManager.insert(context, fileWithType)
                voicesFiles.add(fileWithType)
            }

            override fun onFailed(step: Enum<FileStatus>, message: String) {
            }
        })


        if (voicesFiles.size > 0) {
            callback.onSuccess()
        } else {
            callback.onFailed("没有找到音频文件")
        }
    }

    /**
     * 保存支付数据到数据库
     */
    fun savePayData(context: Context, serviceId: String, srcTime: String, isPayed: Boolean, isMenu: Boolean) {
        val payData = PayData(srcTime, Constant.USER_NAME, serviceId, System.currentTimeMillis(), isPayed, isMenu)
        DBManager.insert(context, payData)
    }

    /**
     * 获得支付数据到数据库
     */
    fun checkPay(context: Context, srcTime: String): Boolean {
        val payData = DBManager.getPayDataByKey(context, srcTime)
        if (payData != null) {
            return true
        }

        return false
    }

    /**
     * 获得支付数据到数据库
     */
    fun checkPay(context: Context, srcTime: String, isMenu: Boolean): Boolean {
        val payData = DBManager.getPayDataByKey(context, srcTime, isMenu)
        if (payData != null) {
            return true
        }

        return false
    }


    /**
     * 删除文件
     */
    fun deleteFile(fileList: MutableList<FileWithType>, callback: FileWithTypeCallback) {
        Constant.ScanStop = false

        if (fileList.isNotEmpty()) {
            for (child in fileList) {
                if (!Constant.ScanStop) {
                    FileUtil.deleteFile(child.path)
                    callback.onProgress(FileStatus.DELETE, child)
                }
            }
            callback.onSuccess(FileStatus.DELETE)
        }
    }


    fun deleteUnzipBackupFiles() {
        if (jxBackupPath.isEmpty()) return
        FileUtil.deleteDirection(File(jxBackupPath))
    }

    fun getRecoveryUser(): String {
        val list = arrayListOf<String>()
//        list.add("成功恢复微信聊天记录")
//        list.add("成功恢复微信通讯录")
        list.add("成功恢复微信文档")
        list.add("成功恢复微信图片")
        list.add("成功恢复微信视频")
        list.add("成功恢复微信语音")
        list.add("成功恢复了账单")
//        list.add("成功删除了微信记录")
        list.add("成功删除微信文档")
        list.add("成功删除微信视频")
        list.add("成功删除微信语音")
        list.add("成功删除微信图片")

        val map = Dict.getPhoneModel()

        val indexL = Random().nextInt(list.size - 1)
        val indexM = Random().nextInt(map.size - 1)
        return "恭喜" + map[map.keyAt(indexM)] + "用户" + list[indexL] + "  " + Random().nextInt(50) + "分钟前"
    }

    fun savePriceList(context: Context, list: List<Price>) {
        if (list.isEmpty()) return
        for (child in list) {
            DBManager.insert(context, child)
        }
    }

    fun openFileInNative(context: Context, FILE_NAME: String, type: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        if (Build.VERSION.SDK_INT <= 23) {
            val file = File(context.externalCacheDir, FILE_NAME)
            val uri = Uri.parse(file.path)
            intent.setDataAndType(uri, "application/$type")
        } else {
            val file = File(FILE_NAME)
            val uri: Uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
            intent.setDataAndType(uri, "application/$type")
        }

        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            JLog.i("Activity was not found for intent, $intent")
        }
    }

    fun savePicsOrVideoToAlbum(context: Context, type: String, files: MutableList<FileWithType>, callback: FileCallback) {
        if (files.isEmpty()) return
        val size = files.size
        when (type) {
            "pic" -> {
                for ((index, file) in files.withIndex()) {
                    FileUtil.saveImage(context, File(file.path))
//                    callback.onProgress(index / size)
                }
                callback.onSuccess("")
            }

            "video" -> {
                for ((index, file) in files.withIndex()) {
                    FileUtil.saveVideo(context, File(file.path))
//                    callback.onProgress(index / size)
                }
                callback.onSuccess("")
            }
        }
    }

    fun replaceBackupAPkForHuawei(context: Activity) {
        CoroutineScope(Dispatchers.IO).launch {
            val packName = "huaweibackup.apk"
            val path = jxBackupPath + packName
            val file = File(path)
            if (!file.exists()) {
                AppUtil.copyApkFromAssets(context, packName, jxBackupPath)
            }
        }
    }

    fun installKoBackupApk(activity: Activity) {
        val packName = "huaweibackup.apk"
        val path = jxBackupPath + packName
        val file = File(path)
        AppUtil.installApk(file, activity)
    }

    fun clearCache() {

    }
}