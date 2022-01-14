package com.recording.trans.controller

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import com.frank.ffmpeg.FFmpegCmd
import com.frank.ffmpeg.listener.OnHandleListener
import com.google.gson.Gson
import com.recording.trans.ffmpeg.FFmpegUtil
import com.recording.trans.bean.*
import com.recording.trans.callback.*
import com.recording.trans.http.loader.OssLoader
import com.recording.trans.http.loader.ReportLoader
import com.recording.trans.http.request.AuthService
import com.recording.trans.http.response.ResponseTransformer
import com.recording.trans.http.schedulers.SchedulerProvider
import com.recording.trans.utils.*
import com.tencent.mmkv.MMKV
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URLEncoder
import kotlin.concurrent.thread


/**
 * @author Herr_Z
 * @description:
 * @date : 2021/6/24 14:59
 */
class AudioManager private constructor() : CoroutineScope by MainScope() {

    companion object {
        private const val APP_ID = "24596362"
        private const val APP_KEY = "ox0Uz65dzs60GHqIloRYcxyL"
        private const val SECRET_KEY = "5OUUiU62kaR6jujt1d5me9kVTV7DuC9v"

//        private const val APP_ID = "24433333"
//        private const val APP_KEY = "BZznWwsm4PxCOoGY5V8SDuUa"
//        private const val SECRET_KEY = "mpnDji1vfDqsj3ffDGp5jnNXZhQ3rETe"

        @Volatile
        private var instance: AudioManager? = null

        fun get(): AudioManager {
            if (instance == null) {
                synchronized(WxManager::class) {
                    if (instance == null) {
                        instance = AudioManager()
                    }
                }
            }

            return instance!!
        }
    }


    fun createVoiceFolder(activity: Activity) {
        launch {
            val rootFile = activity.getExternalFilesDir("voice")
            if (rootFile != null) {
                val path = rootFile.path + File.separator + "示例音频.mp3"
                val file = File(path)
                if (!file.exists()) {
                    FileUtil.copyFileFromAsset(activity, "示例音频.mp3", rootFile.path, null)
                }
            }
        }
    }

    fun createNewFolder(activity: Activity, name: String) {
        val rootFile = activity.getExternalFilesDir("voice")
        if (rootFile != null) {
            val path = rootFile.path + File.separator + name
            FileUtil.createFolder(path)
        }
    }

    fun addNewFile(activity: Activity, srcPath: String, callback: FileCallback) {
        val rootFile = activity.getExternalFilesDir("voice")
        if (rootFile != null) {
            val file = File(srcPath)
            val desPath = rootFile.path + File.separator + file.name
            FileUtil.copyFile(srcPath, desPath, callback)
        }
    }

    fun addCacheFile(activity: Activity, srcPath: String, callback: FileCallback) {
        val cacheFile = File(activity.cacheDir, "示例音频.mp3")
        if (cacheFile.exists()) {
            JLog.i("cacheFile = ${cacheFile.path}")

            val desPath = activity.cacheDir.path
            FileUtil.copyFile(srcPath, desPath, callback)
        }
    }

    fun saveAudio(activity: Activity, name: String, bytes: ByteArray, isAppend: Boolean, callback: FileCallback) {
        val rootFile = activity.getExternalFilesDir("voice")
        if (rootFile != null) {
            val filePath = rootFile.path + File.separator + name
            FileUtil.saveFile(activity, filePath, bytes, isAppend, callback)
            val file = File(filePath)
            if (file.exists()) {
                val fileWithType = FileWithType(file.name, file.path, file.length(), file.lastModified(), "voice", false)
                DBManager.insert(activity, fileWithType)
            }
        }
    }

    fun saveVideo(activity: Activity, name: String, text: String, isAppend: Boolean, callback: FileCallback) {
        val rootFile = activity.getExternalFilesDir("video")
        if (rootFile != null) {
            val fileName = rootFile.path + File.separator + name
            FileUtil.saveFile(activity, text, fileName, callback)
            val file = File(fileName)
            if (file.exists()) {
                val fileWithType = FileWithType(file.name, file.path, file.length(), file.lastModified(), "video", false)
                DBManager.insert(activity, fileWithType)
            }
        }
    }

    fun saveText(activity: Activity, name: String, text: String, isAppend: Boolean, callback: FileCallback) {
        val rootFile = activity.getExternalFilesDir("doc")
        if (rootFile != null) {
            val fileName = rootFile.path + File.separator + name
            FileUtil.saveFile(activity, text, fileName, callback)
            val file = File(fileName)
            if (file.exists()) {
                val fileWithType = FileWithType(file.name, file.path, file.length(), file.lastModified(), "doc", false)
                DBManager.insert(activity, fileWithType)
            }
        }
    }

    fun getRootPath(activity: Activity): String {
        val rootFile = activity.getExternalFilesDir("voice")
        if (rootFile != null) {
            return rootFile.path + File.separator
        }

        return ""
    }

    fun deleteFile(activity: Activity, path: String) {
        val rootFile = activity.getExternalFilesDir("")
        if (rootFile != null) {
            FileUtil.deleteFile(path)
        }
    }

    fun getAllVoice(activity: Activity, callback: VoiceCallback) {
        val rootFile = activity.getExternalFilesDir("")
        if (rootFile != null) {
            val list = FileUtil.getFiles(rootFile.path)
            if (list.isNullOrEmpty()) return
            for (child in list) {
//                if (child.isDirectory && child.name != "default") {
//                    val fileList = FileUtil.getFiles(child.path)
//                    for (file in fileList) {
//                        val fileWith = FileWithType(file.name, file.path, file.length(), file.lastModified(), "audio", false)
//                        callback.onProgress(fileWith)
//                        DBManager.insert(activity, fileWith)
//                    }
//                }

                val name = child.name
                if (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".aac") || name.endsWith(".wma") ||
                    name.endsWith(".flac") || name.endsWith(".aiff") || name.endsWith(".au") || name.endsWith(".ogg") ||
                    name.endsWith(".m4a")
                ) {
                    val fileWithType = FileWithType(child.name, child.path, child.length(), child.lastModified(), "audio", false)
                    callback.onProgress(fileWithType)
                    DBManager.insert(activity, fileWithType)
                    continue
                }

                if (name.endsWith(".mp4") || name.endsWith(".avi") || name.endsWith(".rmvb") || name.endsWith(".rm") ||
                    name.endsWith(".3gp") || name.endsWith(".mov") || name.endsWith(".mkv") || name.endsWith(".flv") ||
                    name.endsWith(".asf")
                ) {
                    val fileWithType = FileWithType(child.name, child.path, child.length(), child.lastModified(), "video", false)
                    callback.onProgress(fileWithType)
                    DBManager.insert(activity, fileWithType)
                    continue
                }

                if (name.endsWith(".txt")) {
                    val fileWithType = FileWithType(child.name, child.path, child.length(), child.lastModified(), "doc", false)
                    callback.onProgress(fileWithType)
                    DBManager.insert(activity, fileWithType)
                    continue
                }

                val fileWithType = FileWithType(child.name, child.path, child.length(), child.lastModified(), "other", false)
                callback.onProgress(fileWithType)
                DBManager.insert(activity, fileWithType)

            }

            callback.onSuccess()
        }
    }


    @SuppressLint("CheckResult")
    fun getTextFromVoice(activity: Activity, filePath: Uri, format: String, language: Int, callback: HttpCallback) {
        if (Constant.CLIENT_TOKEN == "") return

        OssLoader.getOssToken(Constant.CLIENT_TOKEN)
            .compose(ResponseTransformer.handleResult())
            .compose(SchedulerProvider.getInstance().applySchedulers())
            .subscribe({ ossParam ->
                OSSManager.get().uploadFileToFix(activity, ossParam, filePath, format, object : UploadCallback {
                    override fun onSuccess(path: String) {

                        val url = "https://aip.baidubce.com/rpc/2.0/aasr/v1/create"

                        val paramMap = HashMap<String, Any>()
                        paramMap["speech_url"] = path
                        paramMap["format"] = format
                        paramMap["rate"] = 16000
                        if (language == 0) {
                            paramMap["pid"] = 80001
                        } else {
                            paramMap["pid"] = 1737
                        }

                        val param = GsonUtils.toJson(paramMap)
                        val accessToken = AuthService.getAuth()
                        val result = HttpUtil.post(url, accessToken, "application/json", param)

                        if (result != null) {
                            JLog.i("result = $result")
                            getResult(activity, result, Bitmap.CompressFormat.JPEG, callback)
                        } else {
                            callback.onFailed("请求失败")
                        }

                    }

                    override fun onFailed(msg: String) {
                    }
                })

            }, {
                callback.onFailed("文件上传失败")
            })
    }

    fun getTextResult(activity: Activity, taskId: String, callback: FileCallback) {
        val url = "https://aip.baidubce.com/rpc/2.0/aasr/v1/query"
        val taskIds = arrayListOf<String>()
        taskIds.add(taskId)

        val paramMap = HashMap<String, Any>()
        paramMap["task_ids"] = taskIds

        val param = GsonUtils.toJson(paramMap)
        val accessToken = AuthService.getAuth()
        val result = HttpUtil.post(url, accessToken, "application/json", param)

        if (result != null) {
            JLog.i("result = $result")
            val task = Gson().fromJson(result, VoiceTaskResult::class.java)
            if (!task.tasks_info.isNullOrEmpty()) {
                val taskResult = task.tasks_info!![0].task_result
                if (taskResult != null) {
                    val res = taskResult.result
                    if (!res.isNullOrEmpty()) {
                        val name = "${System.currentTimeMillis()}.txt"
                        saveText(activity, name, res[0], false, callback)
                    } else {
                        callback.onFailed("转写失败")
                    }
                } else {
                    JLog.i("${taskId}转写中")
                }
            }
        }
    }

    /**
     * get text from image
     * @param filePath the path of image
     */
    fun getTextFromImage(filePath: String, callback: HttpCallback) {
        val url = "https://aip.baidubce.com/rest/2.0/ocr/v1/webimage"
        try {
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "image=$imgParam"

            //get access_token
            val accessToken = AuthService.getAuth()

            //get result
            val result = HttpUtil.post(url, accessToken, param)
            if (result != null) {
                JLog.i("result = $result")
                if (result.contains("words_result")) {
                    val bodyResult = Gson().fromJson(result, BodyResult::class.java)
                    if (bodyResult == null) {
                        callback.onFailed("数据转换错误")
                    }

                    if (bodyResult.words_result_num > 0) {
                        val build = StringBuilder()
                        for (word in bodyResult.words_result) {
                            build.append("${word.words} ")
                        }
                        //save text
                        MMKV.defaultMMKV()?.encode("words_from_image", build.toString())
                        callback.onSuccess()
                    }
                }

                if (result.contains("error_code")) {
                    val errorResult = Gson().fromJson(result, ImageErrorResult::class.java)
                    if (errorResult == null) {
                        callback.onFailed("数据转换错误")
                    }

                    when (errorResult.error_code) {
                        1 -> callback.onFailed("服务器内部错误")
                        4 -> callback.onFailed("配额超限")
                        6 -> callback.onFailed("无权限访问")
                        14 -> callback.onFailed("鉴权失败")
                        17 -> callback.onFailed("请求超额")
                        18 -> callback.onFailed("QPS超限额")
                        216200 -> callback.onFailed("图片为空")
                        216201 -> callback.onFailed("图片格式错误")
                        216202 -> callback.onFailed("图片大小错误")
                        else -> callback.onFailed(errorResult.error_msg)
                    }
                }

            } else {
                callback.onFailed("请求失败")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * get text from doc(only support pdf)
     * @param filePath the path of pdf
     */
    fun getTextFromDoc(filePath: String, callback: HttpCallback) {
        val url = "https://aip.baidubce.com/rest/2.0/ocr/v1/doc_analysis_office"
        try {
            val imgData = FileUtils.readFileByBytes(filePath)
            val imgStr = Base64Util.encode(imgData)
            val imgParam = URLEncoder.encode(imgStr, "UTF-8")

            val param = "pdf_file=$imgParam"

            //get access_token
            val accessToken = AuthService.getAuth()

            //get result
            val result = HttpUtil.post(url, accessToken, param)
            if (result != null) {
                JLog.i("result = $result")
                if (result.contains("results")) {
                    val bodyResult = Gson().fromJson(result, DocResult::class.java)
                    if (bodyResult == null) {
                        callback.onFailed("数据转换错误")
                    }

                    if (bodyResult.results_num > 0) {
                        val build = StringBuilder()
                        for (word in bodyResult.results) {
                            build.append("${word.words.word} ")
                        }
                        //save text
                        MMKV.defaultMMKV()?.encode("words_from_image", build.toString())
                        callback.onSuccess()
                    }
                }

                if (result.contains("error_code")) {
                    val errorResult = Gson().fromJson(result, ImageErrorResult::class.java)
                    if (errorResult == null) {
                        callback.onFailed("数据转换错误")
                    }

                    when (errorResult.error_code) {
                        1 -> callback.onFailed("服务器内部错误")
                        4 -> callback.onFailed("配额超限")
                        6 -> callback.onFailed("无权限访问")
                        14 -> callback.onFailed("鉴权失败")
                        17 -> callback.onFailed("请求超额")
                        18 -> callback.onFailed("QPS超限额")
                        216200 -> callback.onFailed("图片为空")
                        216201 -> callback.onFailed("图片格式错误")
                        216202 -> callback.onFailed("图片大小错误")
                        else -> callback.onFailed(errorResult.error_msg)
                    }
                }

            } else {
                callback.onFailed("请求失败")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun audioTrans(activity: Activity, srcPath: String, format: String, callback: OnHandleListener) {
        val rootFile = activity.getExternalFilesDir("voice")
        if (rootFile != null) {
            val outPath = rootFile.path + File.separator + System.currentTimeMillis() + "_trans.$format"
            val command = FFmpegUtil.transformAudio(srcPath, outPath)

            JLog.i("outPath = $outPath")

            FFmpegCmd.execute(command, callback)
        }
    }

    fun videoTrans(activity: Activity, srcPath: String, format: String, callback: OnHandleListener) {
        val rootFile = activity.getExternalFilesDir("video")
        if (rootFile != null) {
            val outPath = rootFile.path + File.separator + System.currentTimeMillis() + "_trans.$format"
            val command = FFmpegUtil.transformVideo(srcPath, outPath)

            JLog.i("outPath = $outPath")

            FFmpegCmd.execute(command, callback)
        }
    }


    /**
     * 分块日志输出
     */
    private fun blockPrint(result: String) {
        val r = result.length / 4096
        if (r < 1) {
            JLog.i("result = $result")
        } else {
            for (index in 0..(r + 1)) {
                if (index < r) {
                    JLog.i("result $index = ${result.substring(index * 4096, (index + 1) * 4096)}")
                } else {
                    JLog.i("result $index = ${result.substring(index * 4096)}")
                }
            }
        }
    }


    private fun getResult(activity: Activity, res: String, type: Bitmap.CompressFormat, callback: HttpCallback) {
        val gson = Gson()
        if (res.contains("error_msg")) {
            JLog.i("发生错误")
            val result = gson.fromJson(res, ImageErrorResult::class.java)

            when (result.error_code) {
                3301 -> callback.onFailed("音频格式错误")
                4001 -> callback.onFailed("打开音频文件失败")
                4016 -> callback.onFailed("音频格式错误")
                4009 -> callback.onFailed("音频文件长度太短")
                100101, 100102, 100103 -> callback.onFailed("请求出错")
                100105 -> callback.onFailed("回包大小超限")
                200100 -> callback.onFailed("参数错误")
                else -> callback.onFailed(result.error_msg)
            }
            return
        }


        if (res.contains("task_id")) {
            val result = gson.fromJson(res, ImageResult::class.java)
            if (result != null) {
                val status = result.task_status

                JLog.i("result logId = ${result.log_id}")
                JLog.i("result status = $status")

                if (status == "Created") {
                    val mmkv = MMKV.defaultMMKV()
                    mmkv?.encode("task_id", result.task_id)
                    JLog.i("create task successfully")
                    callback.onSuccess()
                }


            } else {
                callback.onFailed("未知错误")
            }
        }
    }


    private fun base64ToByteArray(imageBase64: String): ByteArray? {
        var bytes: ByteArray? = null
        try {

            bytes = if (imageBase64.indexOf("data:image/jpeg;base64,") != -1) {
                Base64Util.decode(imageBase64.replace("data:image/jpeg;base64,".toRegex(), ""))
            } else {
                Base64Util.decode(imageBase64.replace("data:image/jpg;base64,".toRegex(), ""))
            }

            for (i in bytes.indices) {
                if (bytes[i] < 0) { // 调整异常数据
                    (bytes[i].plus(256)).toByte()
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bytes
    }

    @SuppressLint("CheckResult")
    fun report() {
        thread {
            ReportLoader.report()
                .compose(ResponseTransformer.handleResult())
                .compose(SchedulerProvider.getInstance().applySchedulers())
                .subscribe({

                }, {

                })
        }
    }

    // Checks if a volume containing external storage is available
// for read and write.
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    // Checks if a volume containing external storage is available to at least read.
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

}