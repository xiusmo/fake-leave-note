package com.xiusm.fakeln

import android.content.Context
import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import java.io.File


@Composable
fun PreviewPage(filePath: String) {
    val context = LocalContext.current
    val htmlContent = loadHtmlFromFile(context, filePath)  // 从文件路径读取内容

//    // 使用 WebView 来显示文件中的 HTML 内容
//    Column(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = {
                WebView(context).apply {
                    // 配置 WebView 设置
                    settings.apply {
                        allowFileAccess = true  // 允许访问本地文件
                        allowContentAccess = true // 允许加载内容
                        javaScriptEnabled = false // 启用 JavaScript（如果需要）
                    }
                    loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                }
            }
        )
//    }
}

fun loadHtmlFromFile(context: Context, filePath: String): String {
    val file = File(filePath)
    return if (file.exists()) {
        file.readText()  // 读取文件内容
    } else {
        ""
    }
}

@Composable
fun CustomTopBar(title: String, onBackClicked: () -> Unit, onMenuClicked: () -> Unit) {
    TopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp),
                color = Color.White,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            IconButton(onClick = { onBackClicked() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = { onMenuClicked() }) {
                Icon(
                    imageVector = Icons.Default.MoreHoriz,
                    contentDescription = "More",
                    tint = Color.White
                )
            }
        },
        backgroundColor = Color(0xff7caef3),
//        elevation = 4.dp
    )
}

@Composable
fun PreviewPageScreen(filePath: String, onBackPressed: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        CustomTopBar(
            title = "请销假",
            onBackClicked = { onBackPressed() },
            onMenuClicked = { /* 处理菜单点击事件 */ }
        )

        PreviewPage(filePath)
    }
}