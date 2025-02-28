package com.xiusm.fakeln

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                MainScreen()
            }
        }
    }
}

@Composable
fun MainScreen() {
    val navController = rememberNavController()  // 创建 NavController

    // 使用 NavHost 设置导航
    NavHost(navController = navController, startDestination = "edit") {
        composable("edit") {
            EditPage(navController = navController)  // 将 navController 传递给 EditPage
        }

        // 定义 preview 页面，并接收 filePath 参数
        composable("preview?filePath={filePath}") { backStackEntry ->
            // 获取 filePath 参数
            val filePath = backStackEntry.arguments?.getString("filePath") ?: ""
            // 将 filePath 传递给 PreviewPage
            PreviewPageScreen(filePath = filePath, onBackPressed = {
                navController.popBackStack() // 返回到编辑页面
            })
        }
    }
}