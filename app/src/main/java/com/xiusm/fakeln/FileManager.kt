package com.xiusm.fakeln

import android.content.Context
import android.net.Uri
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter

class FileManager(private val context: Context) {

    private val directory = context.filesDir
    private val fileName = "leave_request_data.json"

    // 保存数据到文件
    fun saveJsonData(data: LeaveRequestData) {
        val file = File(directory, fileName)
        val json = Gson().toJson(data)  // 使用Gson将数据对象转换为JSON
        FileOutputStream(file).use { outputStream ->
            OutputStreamWriter(outputStream).use { writer ->
                writer.write(json)
            }
        }
    }

    // 读取文件中的数据
    fun readLastSavedJsonData(): LeaveRequestData? {
        val file = File(directory, fileName)
        if (!file.exists()) return null

        val json = file.readText()
        return Gson().fromJson(json, LeaveRequestData::class.java)  // 使用Gson将JSON转换为数据对象
    }

    // 清空文件内容
    fun clearJsonData() {
        val file = File(directory, fileName)
        if (file.exists()) {
            file.delete()
        }
    }

    // 将图片保存到应用内部存储，并返回图片路径
    fun saveImageToInternalStorage(context: Context, imageUri: Uri, newFileName: String): String {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val file = File(context.filesDir, "uploaded_images") // 创建专用文件夹
        if (!file.exists()) file.mkdirs() // 创建文件夹（如果不存在）
        val newFile = File(file, newFileName) // 为图片分配唯一文件名
        val outputStream = FileOutputStream(newFile)

        try {
            inputStream?.copyTo(outputStream)
            return newFile.absolutePath // 返回保存的文件路径
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            inputStream?.close()
            outputStream.close()
        }

        return ""
    }

    fun saveHtmlToFile(context: Context, htmlContent: String): String {
        try {
            // 创建文件对象
            val file = File(context.filesDir, "preview.html")

            // 写入 HTML 内容到文件
            val outputStream: FileOutputStream = FileOutputStream(file)
            outputStream.write(htmlContent.toByteArray())
            outputStream.close()

            // 返回文件路径
            return file.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            return ""  // 如果发生错误返回空路径
        }
    }

    // 获取已上传的图片列表
    fun getUploadedImages(context: Context): List<String> {
        val imageFolder = File(context.filesDir, "uploaded_images")
        return if (imageFolder.exists()) {
            imageFolder.listFiles()?.map { it.absolutePath } ?: emptyList()
        } else {
            emptyList()
        }
    }

}