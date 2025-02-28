package com.xiusm.fakeln

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.media.MediaScannerConnection
import android.os.Environment
import android.webkit.WebView
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

fun captureWebViewAndSave(context: Context) {
    val webView = WebView(context)
    val bitmap = Bitmap.createBitmap(webView.width, webView.height, Bitmap.Config.ARGB_8888)
    webView.draw(Canvas(bitmap))

    // 保存Bitmap到文件
    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath
    val file = File(path, "edited_image.png")
    try {
        val outputStream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        outputStream.flush()
        outputStream.close()

        // 使图库更新
        MediaScannerConnection.scanFile(context, arrayOf(file.toString()), null, null)
        Toast.makeText(context, "图片已保存到相册", Toast.LENGTH_SHORT).show()
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "保存图片失败", Toast.LENGTH_SHORT).show()
    }
}