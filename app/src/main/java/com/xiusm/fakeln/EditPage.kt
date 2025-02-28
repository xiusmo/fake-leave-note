package com.xiusm.fakeln

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.Uri
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import java.io.File
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar
import java.util.Locale
import java.util.UUID


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPage(navController: NavController) {
    val context = LocalContext.current
    val fileManager = FileManager(context)

    // 读取文件中的数据
    var originalData = fileManager.readLastSavedJsonData()
    var defaultValues = if (originalData != null) {
        originalData
    } else {
        // 默认值
        LeaveRequestData(
            title = "移动管理",
            applicant = ApplicantData(
                name = "",
                leave_type = "",
                student_id = "",
                student_pic_patch = "file:///assets/placeholder.jpg",
                student_pic_name = "",
                leave_reason = "",
                leave_course = "",
                leave_duration = "",
                start_date = "",
                start_time = "",
                end_date = "",
                end_time = "",
                contact_info = ContactInfo(
                    phone = "",
                    parent_name = "",
                    parent_phone = "",
                    address = ""
                ),
                location = Location(
                    leave_location = "",
                    dismiss_location = ""
                ),
                attachments = listOf("file:///android_asset/tg.b709d381.png")
            ),
            approval_progress = listOf(
                ApprovalStep("发起申请", "发起学生", "20230000000",  "", "", "已发起"),
                ApprovalStep("审批通过", "", "", "", "", "已通过")
            ),
            button = ButtonData("返回", "back")
        )
    }




    // 存储图片路径的状态
    var studentPicPath by remember { mutableStateOf<String?>(null) }
    var studentPicName by remember { mutableStateOf<String?>(null) }

    // 获取上传图片的列表
    val uploadedImages = fileManager.getUploadedImages(context)

    // 使用 remember 来保持数据状态
    var leaveData by remember { mutableStateOf(defaultValues) }

    val startDateTime = "${leaveData.applicant.start_date} ${leaveData.applicant.start_time}"
    val endDateTime = "${leaveData.applicant.end_date} ${leaveData.applicant.end_time}"
    val passDateandTime = "${leaveData.approval_progress.getOrNull(1)?.date} ${leaveData.approval_progress.getOrNull(1)?.time}"
    val requestDateandTime = "${leaveData.approval_progress.getOrNull(0)?.date} ${leaveData.approval_progress.getOrNull(0)?.time}"

    var startDateDialogVisible by remember { mutableStateOf(false) }
    var endDateDialogVisible by remember { mutableStateOf(false) }
    var startTimeDialogVisible by remember { mutableStateOf(false) }
    var endTimeDialogVisible by remember { mutableStateOf(false) }
    var requestDateDialogVisible by remember { mutableStateOf(false) }
    var passDateDialogVisible by remember { mutableStateOf(false) }
    var requestTimeDialogVisible by remember { mutableStateOf(false) }
    var passTimeDialogVisible by remember { mutableStateOf(false) }

    var expanded by remember { mutableStateOf(false) }
    val options = remember { listOf("病假", "事假", "其他") }


    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())


    if (startDateDialogVisible) {
        val currentDate = LocalDate.now()
        val datePickerDialog = DatePickerDialog(
            LocalContext.current,
            { _, year, monthOfYear, dayOfMonth ->
                // 用户选择了日期，格式化日期
                val formattedDate = dateFormat.format(Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }.time)

                // 更新 leaveData
                leaveData = leaveData.copy(applicant = leaveData.applicant.copy(
                    start_date = formattedDate
                ))

                // 关闭日期对话框
                startDateDialogVisible = false
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        )

        // 处理取消按钮事件
        datePickerDialog.setOnCancelListener {
            // 用户点击取消按钮时关闭对话框
            startDateDialogVisible = false
        }

        // 显示日期选择对话框
        datePickerDialog.show()
    }

    if (endDateDialogVisible) {
        val currentDate = LocalDate.now()
        val datePickerDialog = DatePickerDialog(
            LocalContext.current,
            { _, year, monthOfYear, dayOfMonth ->
                // 用户选择了日期，格式化日期
                val formattedDate = dateFormat.format(Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }.time)

                // 更新 leaveData
                leaveData = leaveData.copy(applicant = leaveData.applicant.copy(
                    end_date = formattedDate
                ))

                // 关闭日期对话框
                endDateDialogVisible = false
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        )

        // 处理取消按钮事件
        datePickerDialog.setOnCancelListener {
            // 用户点击取消按钮时关闭对话框
            endDateDialogVisible = false
        }

        // 显示日期选择对话框
        datePickerDialog.show()
    }

    if (startTimeDialogVisible) {
        val currentTime = LocalTime.now()
        val timePickerDialog = TimePickerDialog(
            LocalContext.current,
            { _, hourOfDay, minute ->
                // 用户选择了日期，格式化日期
                val formattedTime = timeFormat.format(Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }.time)

                // 更新 leaveData
                leaveData = leaveData.copy(applicant = leaveData.applicant.copy(
                    start_time = formattedTime
                ))

                // 关闭日期对话框
                startTimeDialogVisible = false
            },
            currentTime.hour,
            currentTime.minute,
            true
        )

        // 处理取消按钮事件
        timePickerDialog.setOnCancelListener {
            // 用户点击取消按钮时关闭对话框
            startTimeDialogVisible = false
        }

        // 显示日期选择对话框
        timePickerDialog.show()
    }

    if (endTimeDialogVisible) {
        val currentTime = LocalTime.now()
        val timePickerDialog = TimePickerDialog(
            LocalContext.current,
            { _, hourOfDay, minute ->
                // 用户选择了日期，格式化日期
                val formattedTime = timeFormat.format(Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }.time)

                // 更新 leaveData
                leaveData = leaveData.copy(applicant = leaveData.applicant.copy(
                    end_time = formattedTime
                ))

                // 关闭日期对话框
                endTimeDialogVisible = false
            },
            currentTime.hour,
            currentTime.minute,
            true
        )

        // 处理取消按钮事件
        timePickerDialog.setOnCancelListener {
            // 用户点击取消按钮时关闭对话框
            endTimeDialogVisible = false
        }

        // 显示日期选择对话框
        timePickerDialog.show()
    }

    if (requestDateDialogVisible) {
        val currentDate = LocalDate.now()
        val datePickerDialog = DatePickerDialog(
            LocalContext.current,
            { _, year, monthOfYear, dayOfMonth ->
                // 用户选择了日期，格式化日期
                val formattedDate = dateFormat.format(Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }.time)

                // 更新 leaveData
                leaveData = leaveData.copy(
                    approval_progress = leaveData.approval_progress.mapIndexed { index, approvalStep ->
                        if (index == 0) {
                            approvalStep.copy(date = formattedDate)
                        } else {
                            approvalStep
                        }
                    }
                )

                // 关闭日期对话框
                requestDateDialogVisible = false
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        )

        // 处理取消按钮事件
        datePickerDialog.setOnCancelListener {
            // 用户点击取消按钮时关闭对话框
            requestDateDialogVisible = false
        }

        // 显示日期选择对话框
        datePickerDialog.show()
    }

    if (requestTimeDialogVisible) {
        val currentTime = LocalTime.now()
        val timePickerDialog = TimePickerDialog(
            LocalContext.current,
            { _, hourOfDay, minute ->
                // 用户选择了日期，格式化日期
                val formattedTime = timeFormat.format(Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }.time)

                // 更新 leaveData
                leaveData = leaveData.copy(
                    approval_progress = leaveData.approval_progress.mapIndexed { index, approvalStep ->
                        if (index == 0) {
                            approvalStep.copy(time = formattedTime)
                        } else {
                            approvalStep
                        }
                    }
                )

                // 关闭日期对话框
                requestTimeDialogVisible = false
            },
            currentTime.hour,
            currentTime.minute,
            true
        )

        // 处理取消按钮事件
        timePickerDialog.setOnCancelListener {
            // 用户点击取消按钮时关闭对话框
            requestTimeDialogVisible = false
        }

        // 显示日期选择对话框
        timePickerDialog.show()
    }

    if (passDateDialogVisible) {
        val currentDate = LocalDate.now()
        val datePickerDialog = DatePickerDialog(
            LocalContext.current,
            { _, year, monthOfYear, dayOfMonth ->
                // 用户选择了日期，格式化日期
                val formattedDate = dateFormat.format(Calendar.getInstance().apply {
                    set(year, monthOfYear, dayOfMonth)
                }.time)

                // 更新 leaveData
                leaveData = leaveData.copy(
                    approval_progress = leaveData.approval_progress.mapIndexed { index, approvalStep ->
                        if (index == 1) {
                            approvalStep.copy(date = formattedDate)
                        } else {
                            approvalStep
                        }
                    }
                )

                // 关闭日期对话框
                passDateDialogVisible = false
            },
            currentDate.year,
            currentDate.monthValue - 1,
            currentDate.dayOfMonth
        )

        // 处理取消按钮事件
        datePickerDialog.setOnCancelListener {
            // 用户点击取消按钮时关闭对话框
            passDateDialogVisible = false
        }

        // 显示日期选择对话框
        datePickerDialog.show()
    }

    if (passTimeDialogVisible) {
        val currentTime = LocalTime.now()
        val timePickerDialog = TimePickerDialog(
            LocalContext.current,
            { _, hourOfDay, minute ->
                // 用户选择了日期，格式化日期
                val formattedTime = timeFormat.format(Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, hourOfDay)
                    set(Calendar.MINUTE, minute)
                    set(Calendar.SECOND, 0)
                }.time)

                // 更新 leaveData
                leaveData = leaveData.copy(
                    approval_progress = leaveData.approval_progress.mapIndexed { index, approvalStep ->
                        if (index == 1) {
                            approvalStep.copy(time = formattedTime)
                        } else {
                            approvalStep
                        }
                    }
                )

                // 关闭日期对话框
                passTimeDialogVisible = false
            },
            currentTime.hour,
            currentTime.minute,
            true
        )

        // 处理取消按钮事件
        timePickerDialog.setOnCancelListener {
            // 用户点击取消按钮时关闭对话框
            passTimeDialogVisible = false
        }

        // 显示日期选择对话框
        timePickerDialog.show()
    }

    val getContent = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        // 选择图片后处理
        uri?.let {
            val newFileName = UUID.randomUUID().toString() + ".jpg" // 使用唯一名称
            val filePath = fileManager.saveImageToInternalStorage(context, it, newFileName)

            val TAG = "filedebug"
            Log.d(TAG, "EditPage: $filePath")
            // 更新图片路径到学生信息
            leaveData = leaveData.copy(
                applicant = leaveData.applicant.copy(
                    student_pic_patch = filePath,
                    student_pic_name = newFileName
                )
            )
        }
    }
    

    Scaffold(
        topBar = {
            TopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                title = {
                    Text("编辑信息")
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                // 保存数据到文件
                val updatedData = leaveData
                fileManager.saveJsonData(updatedData)  // 假设你有一个方法将数据保存为 JSON

                // 生成 HTML 预览
                val html = generateHtmlPreview(updatedData)

                // 保存 HTML 到文件，并获取文件路径
                val filePath = fileManager.saveHtmlToFile(context, html)

                // 将文件路径传递给 Preview 页面（作为导航参数）
                navController.navigate("preview?filePath=$filePath")
            }) {
                Icon(Icons.Default.Save, contentDescription = "Save")
            }
        },
        floatingActionButtonPosition = FabPosition.End, // 将 FAB 放置在右下角


        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(start = 24.dp, end = 24.dp)
            ) {

                item{
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = startDateTime,
                            onValueChange = { },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                            readOnly = true,
                            label = { Text("开始时间") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        )
                        IconButton(onClick = { startDateDialogVisible = true }) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = "Pick Date")
                        }
                        IconButton(onClick = { startTimeDialogVisible = true }) {
                            Icon(Icons.Filled.AccessTime, contentDescription = "Pick Time")
                        }
                    }
                }

                item{
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = endDateTime,
                            onValueChange = { },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                            readOnly = true,
                            label = { Text("开始时间") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        )
                        IconButton(onClick = { endDateDialogVisible = true }) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = "Pick Date")
                        }
                        IconButton(onClick = { endTimeDialogVisible = true }) {
                            Icon(Icons.Filled.AccessTime, contentDescription = "Pick Time")
                        }
                    }
                }

                item{
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = requestDateandTime,
                            onValueChange = { },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                            readOnly = true,
                            label = { Text("申请时间") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        )
                        IconButton(onClick = { requestDateDialogVisible = true }) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = "Pick Date")
                        }
                        IconButton(onClick = { requestTimeDialogVisible = true }) {
                            Icon(Icons.Filled.AccessTime, contentDescription = "Pick Time")
                        }
                    }
                }

                item{
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = passDateandTime,
                            onValueChange = { },
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                            readOnly = true,
                            label = { Text("通过时间") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        )
                        IconButton(onClick = { passDateDialogVisible = true }) {
                            Icon(Icons.Filled.CalendarToday, contentDescription = "Pick Date")
                        }
                        IconButton(onClick = { passTimeDialogVisible = true }) {
                            Icon(Icons.Filled.AccessTime, contentDescription = "Pick Time")
                        }
                    }
                }

                item {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // 输入框部分
                            OutlinedTextField(
                                value = leaveData.applicant.leave_type, // 使用实际的请假类型
                                onValueChange = {},
                                label = { Text("类型") },
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                                    .menuAnchor(), // 添加这个修饰符来关联下拉菜单
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                            )
                
                            // 下拉菜单部分
                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                options.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            // 更新选中的类型
                                            leaveData = leaveData.copy(
                                                applicant = leaveData.applicant.copy(
                                                    leave_type = option
                                                )
                                            )
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                item{
                    OutlinedTextField(
                        value = leaveData.applicant.leave_reason,
                        onValueChange = { newReason ->
                            leaveData =
                                leaveData.copy(applicant = leaveData.applicant.copy(leave_reason = newReason))
                        },
                        label = { Text("申请事由") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                item{
                    OutlinedTextField(
                        value = leaveData.applicant.leave_course,
                        onValueChange = { newCourse ->
                            leaveData =
                                leaveData.copy(applicant = leaveData.applicant.copy(leave_course = newCourse))
                        },
                        label = { Text("请假课程") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                item{
                    OutlinedTextField(
                        value = leaveData.applicant.leave_duration,
                        onValueChange = { newDuration ->
                            leaveData =
                                leaveData.copy(applicant = leaveData.applicant.copy(leave_duration = newDuration))
                        },
                        label = { Text("请假时长") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                item{
                    OutlinedTextField(
                        value = leaveData.applicant.name,
                        onValueChange = { newName ->
                            leaveData =
                                leaveData.copy(applicant = leaveData.applicant.copy(name = newName))
                        },
                        label = { Text("本人姓名") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                item{
                    OutlinedTextField(
                        value = leaveData.applicant.student_id,
                        onValueChange = { newStudentId ->
                            leaveData =
                                leaveData.copy(applicant = leaveData.applicant.copy(student_id = newStudentId))
                        },
                        label = { Text("本人学号") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                item{
                    OutlinedTextField(
                        value = leaveData.applicant.contact_info.phone,
                        onValueChange = { newPhone ->
                            leaveData = leaveData.copy(
                                applicant = leaveData.applicant.copy(
                                    contact_info = leaveData.applicant.contact_info.copy(phone = newPhone)
                                )
                            )
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                        label = { Text("本人电话") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                item{
                    OutlinedTextField(
                        value = leaveData.applicant.contact_info.parent_name,
                        onValueChange = { newParentName ->
                            leaveData = leaveData.copy(
                                applicant = leaveData.applicant.copy(
                                    contact_info = leaveData.applicant.contact_info.copy(parent_name = newParentName)
                                )
                            )
                        },
                        label = { Text("家长姓名") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                item{
                    OutlinedTextField(
                        value = leaveData.applicant.contact_info.parent_phone,
                        onValueChange = { newPhone ->
                            leaveData = leaveData.copy(
                                applicant = leaveData.applicant.copy(
                                    contact_info = leaveData.applicant.contact_info.copy(
                                        parent_phone = newPhone
                                    )
                                )
                            )
                        },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Phone),
                        label = { Text("家长电话") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                item{
                    OutlinedTextField(
                        value = leaveData.applicant.contact_info.address,
                        onValueChange = { newAddress ->
                            leaveData = leaveData.copy(
                                applicant = leaveData.applicant.copy(
                                    contact_info = leaveData.applicant.contact_info.copy(address = newAddress)
                                )
                            )
                        },
                        label = { Text("家庭住址") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                item {
                    val locationUtils = remember { LocationUtils(context) }
                    val permissionLauncher = LocationUtils.rememberLocationPermissionLauncher { permissions ->
                        locationUtils.handlePermissionResult(permissions) { address ->
                            leaveData = leaveData.copy(
                                applicant = leaveData.applicant.copy(
                                    location = leaveData.applicant.location.copy(
                                        leave_location = address
                                    )
                                )
                            )
                        }
                    }
                
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = leaveData.applicant.location.leave_location,
                            onValueChange = { newLeaveLocation ->
                                leaveData = leaveData.copy(
                                    applicant = leaveData.applicant.copy(
                                        location = leaveData.applicant.location.copy(
                                            leave_location = newLeaveLocation
                                        )
                                    )
                                )
                            },
                            label = { Text("请假位置") },
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 8.dp)
                        )
                        IconButton(
                            onClick = {
                                locationUtils.requestLocation(permissionLauncher) { address ->
                                    leaveData = leaveData.copy(
                                        applicant = leaveData.applicant.copy(
                                            location = leaveData.applicant.location.copy(
                                                leave_location = address
                                            )
                                        )
                                    )
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Get Location"
                            )
                        }
                    }
                }

                item{
                    OutlinedTextField(
                        value = leaveData.applicant.location.dismiss_location,
                        onValueChange = { newDismissLocation ->
                            leaveData = leaveData.copy(
                                applicant = leaveData.applicant.copy(
                                    location = leaveData.applicant.location.copy(dismiss_location = newDismissLocation)
                                )
                            )
                        },
                        label = { Text("销假位置") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                item{
                    OutlinedTextField(
                        value = leaveData.approval_progress.getOrNull(1)?.name
                            ?: "",  // 获取 approval_progress 中第二个元素的 `name`
                        onValueChange = { newCounselorName ->
                            leaveData = leaveData.copy(
                                approval_progress = leaveData.approval_progress.mapIndexed { index, approvalStep ->
                                    if (index == 1) { // 如果是第二个元素，更新其 `name`
                                        approvalStep.copy(name = newCounselorName)
                                    } else {
                                        approvalStep // 保留其他元素不变
                                    }
                                }
                            )
                        },
                        label = { Text("导员姓名") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                item{
                    OutlinedTextField(
                        value = leaveData.approval_progress.getOrNull(1)?.id
                            ?: "",  // 获取 approval_progress 中第二个元素的 `name`
                        onValueChange = { newCounselorId ->
                            leaveData = leaveData.copy(
                                approval_progress = leaveData.approval_progress.mapIndexed { index, approvalStep ->
                                    if (index == 1) { // 如果是第二个元素，更新其 `name`
                                        approvalStep.copy(id = newCounselorId)
                                    } else {
                                        approvalStep // 保留其他元素不变
                                    }
                                }
                            )
                        },
                        label = { Text("导员编号") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                }

                item {
                    // 创建权限请求启动器
                    val permissionLauncher = rememberLauncherForActivityResult(
                        ActivityResultContracts.RequestPermission()
                    ) { isGranted ->
                        if (isGranted) {
                            getContent.launch("image/*")
                        } else {
                            Toast.makeText(context, "需要存储权限才能选择图片", Toast.LENGTH_SHORT).show()
                        }
                    }

                    // 上传图片按钮部分
                    OutlinedButton(
                        onClick = {
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                                Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            
                            when {
                                ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED -> {
                                    // 已有权限，直接打开图片选择器
                                    getContent.launch("image/*")
                                }
                                ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, permission) -> {
                                    // 用户之前拒绝过权限，显示解释
                                    Toast.makeText(context, "需要存储权限来选择图片，请在权限弹窗中允许", Toast.LENGTH_LONG).show()
                                    permissionLauncher.launch(permission)
                                }
                                else -> {
                                    // 首次请求权限
                                    permissionLauncher.launch(permission)
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        Text("点击上传绝美证件照")
                    }
                
                    // 只在有图片路径时显示图片
                    if (leaveData.applicant.student_pic_patch.isNotEmpty() && 
                        !leaveData.applicant.student_pic_patch.startsWith("file:///assets/")) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(500.dp)
                                .padding(vertical = 4.dp)
                        ) {
                            ImageFromFile(leaveData.applicant.student_pic_patch)
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ImageListView(context: Context, paddingValues: PaddingValues) {
    // 获取存储图片的文件夹路径
    val imageDirectory = File(context.filesDir, "uploaded_images")

    // 获取该目录下的所有图片文件
    val imageFiles = imageDirectory.listFiles()?.filter { it.isFile && it.name.endsWith(".jpg") } ?: emptyList()

    // 使用 LazyColumn 显示图片列表
    LazyColumn(modifier = Modifier
        .padding(paddingValues)
        .fillMaxSize() // 确保有明确的尺寸约束
    ) {
        // 头部标题
        item {
            Text("历史上传的图片:", modifier = Modifier.padding(8.dp))
        }

        // 图片列表项
        items(imageFiles.size) { index ->
            // 获取当前图片的文件路径
            val imageFile = imageFiles[index]
            val bitmap = loadBitmapFromFile(imageFile)

            // 加载并显示图片
            bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(), // 显示图片
                    contentDescription = "Uploaded Image $index",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }
    }
}

// 辅助函数：通过文件路径加载 Bitmap
fun loadBitmapFromFile(file: File): android.graphics.Bitmap? {
    return if (file.exists()) {
        BitmapFactory.decodeFile(file.absolutePath)
    } else {
        null
    }
}

@Composable
fun RequestStoragePermission(
    context: Context,
    onPermissionGranted: () -> Unit
) {
    // 在Composable中初始化 ActivityResultLauncher
    val permissionRequestLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // 权限被授予，执行文件操作
            onPermissionGranted()
        } else {
            // 权限被拒绝，显示提示
            Toast.makeText(context, "权限被拒绝，无法选择图片", Toast.LENGTH_SHORT).show()
        }
    }

    // 使用 SideEffect 确保只在第一次请求权限时触发
    SideEffect {
        // 检查当前 Android 版本
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            // Android 14+ 需要请求新的权限
            val permission = Manifest.permission.READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(
                    context,
                    permission
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                // 权限已被授予，可以执行文件操作
                onPermissionGranted()
            } else {
                // 动态请求权限
                permissionRequestLauncher.launch(permission)
            }
        } else {
            // 对于低版本，仍然使用 `READ_EXTERNAL_STORAGE` 权限
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                onPermissionGranted()
            } else {
                ActivityCompat.requestPermissions(
                    context as Activity,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    1001
                )
            }
        }
    }
}

fun generateHtmlPreview(leaveData: LeaveRequestData): String {
    val counselorName = leaveData.approval_progress.getOrNull(1)?.name ?: "未知"
    val counselorID = leaveData.approval_progress.getOrNull(1)?.id ?: "未知"
    val passDateandTime = "${leaveData.approval_progress.getOrNull(1)?.date} ${leaveData.approval_progress.getOrNull(1)?.time}"
    val requestDateandTime = "${leaveData.approval_progress.getOrNull(0)?.date} ${leaveData.approval_progress.getOrNull(0)?.time}"

    return """
        <!DOCTYPE html>
        <!-- saved from url=(0014)about:internet -->
        <html lang="zh-CN">

        <head>
            <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
            <style type="text/css" class="AMap.style">
                .vml {
                    behavior: url(#default#VML);
                    display: inline-block;
                    position: absolute
                }

                .amap-custom {
                    top: 0;
                    left: 0;
                    position: absolute
                }

                .amap-container img {
                    max-width: none !important;
                    max-height: none !important
                }

                .amap-container {
                    touch-action: none;
                    position: relative;
                    overflow: hidden;
                    background: #fcf9f2 url(data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAfQAAAH0AgMAAAC2uDcZAAAADFBMVEX////////////////1pQ5zAAAABHRSTlMAgP/AWuZC2AAAAVhJREFUeAFiYGAQYGDEQjAB2rcDC4BiGIqiU7abdKlO2QkeIClyPsDHweMKtOPHIJ1Op6/w7Y4fdqfT6VpndzqdrnV2p9PpWmd3Oj3qWndSoKp+2J1Op7vr7E6n07XO7nQ6XevsTqfTtc7udPo4/f787E6n0911dqfT6VpndzqdrnV2p9PpWmd3Ot27Ce8m6HS6u85dR6fTtU7r6HS61mkdnU7XOrvT6XTvJuxOp9PddXan0+laZ3c6na51dDpd67SOTqd7N+HdBJ1Od9e56+h0utZpHZ1O1zq70+l0rbM7nU73bsLudDrdXWd3Ol3rtI5Op2ud1tHpdK3TOjqd7t2EdxN0Ot1dZ3c6na51dqfT6VpndzqdrnV2p9Pp3k3Q6XR3nbuOTqdrndbR6XSt0zo6na51Wken072bsDudTnfX2Z1Op2ud3el0utbZnU7XOq2j0+t0uncTD1gO4zoT5doZAAAAAElFTkSuQmCC);
                    -ms-touch-action: none
                }

                .amap-drags,
                .amap-layers {
                    width: 100%;
                    height: 100%;
                    position: absolute;
                    overflow: hidden
                }

                .amap-layers canvas {
                    -webkit-touch-callout: none;
                    -webkit-user-select: none;
                    -khtml-user-select: none;
                    -moz-user-select: none;
                    -ms-user-select: none;
                    user-select: none
                }

                .amap-layer img {
                    pointer-events: none
                }

                .amap-e,
                .amap-maps {
                    width: 100%;
                    height: 100%
                }

                .amap-maps,
                .amap-e,
                .amap-layers,
                .amap-tile,
                .amap-tile-container {
                    position: absolute;
                    left: 0;
                    top: 0;
                    overflow: hidden
                }

                .amap-context {
                    position: absolute;
                    left: 0;
                    top: 0
                }

                .amap-overlays,
                .amap-markers,
                .amap-marker {
                    position: absolute;
                    left: 0;
                    top: 0
                }

                .amap-layers {
                    z-index: 0
                }

                .amap-overlays {
                    z-index: 110;
                    cursor: default
                }

                .amap-markers {
                    z-index: 120
                }

                .amap-controls {
                    z-index: 150
                }

                .amap-copyright {
                    position: absolute;
                    display: block !important;
                    left: 85px;
                    height: 16px;
                    bottom: .1px;
                    padding-bottom: 3px;
                    font-size: 11px;
                    font-family: Arial, sans-serif;
                    z-index: 160
                }

                .amap-logo {
                    position: absolute;
                    bottom: 1.5px;
                    left: 4px;
                    z-index: 160;
                    height: 20px
                }

                .amap-logo img {
                    width: 73px !important;
                    height: 20px !important;
                    border: 0;
                    vertical-align: baseline !important
                }

                .amap-icon {
                    position: relative;
                    z-index: 1
                }

                .amap-icon img {
                    position: absolute;
                    z-index: -1
                }

                .amap-marker-label {
                    position: absolute;
                    z-index: 2;
                    border: 1px solid blue;
                    background-color: white;
                    white-space: nowrap;
                    cursor: default;
                    padding: 3px;
                    font-size: 12px;
                    line-height: 14px
                }

                .amap-info {
                    position: absolute;
                    left: 0;
                    z-index: 140;
                    width: 320px
                }

                .amap-menu {
                    position: absolute;
                    z-index: 140;
                    _width: 100px
                }

                .amap-info-close {
                    position: absolute;
                    right: 5px;
                    _right: 12px;
                    +right: 11px;
                    top: 5px;
                    _top: 2px;
                    +top: 2px;
                    color: #c3c3c3;
                    text-decoration: none;
                    font: bold 16px/14px Tahoma, Verdana, sans-serif;
                    width: 14px;
                    height: 14px
                }

                .amap-info-outer,
                .amap-menu-outer {
                    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
                    background: none repeat scroll 0 0 white;
                    border-radius: 2px;
                    padding: 1px;
                    text-align: left
                }

                .amap-menu-outer:hover {
                    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.3)
                }

                .amap-info-contentContainer:hover .amap-info-outer {
                    box-shadow: 0 1px 2px rgba(0, 0, 0, 0.3)
                }

                .amap-info-content {
                    position: relative;
                    background: #fff;
                    padding: 10px 18px 10px 10px;
                    line-height: 1.4;
                    overflow: auto
                }

                .amap-marker-content {
                    position: relative
                }

                .amap-info {
                    _width: 320px
                }

                .amap-menu {
                    _width: 100px
                }

                .amap-info-sharp-old {
                    overflow: hidden;
                    position: absolute;
                    background-image: url(http://webapi.amap.com/images/arrows.png)
                }

                .bottom-center .amap-info-sharp-old {
                    height: 12px;
                    margin: 0 auto;
                    width: 20px;
                    background-position: center 12px;
                    top: 100%;
                    margin-top: -9px;
                    left: 50%;
                    margin-left: -10px
                }

                .bottom-left .amap-info-sharp-old {
                    height: 12px;
                    width: 13px;
                    background-position: -16px -46px;
                    top: 100%;
                    margin-top: -9px
                }

                .bottom-right .amap-info-sharp-old {
                    height: 12px;
                    width: 13px;
                    top: -1px;
                    background-position: -56px -46px;
                    left: 100%;
                    margin-left: -13px;
                    top: 100%;
                    margin-top: -9px
                }

                .middle-left .amap-info-sharp-old {
                    height: 20px;
                    width: 12px;
                    background-position: left;
                    top: 50%;
                    margin-top: -10px;
                    margin-left: -11px
                }

                .center .amap-info-sharp-old {
                    display: none
                }

                .middle-right .amap-info-sharp-old {
                    height: 20px;
                    margin-right: 0;
                    width: 12px;
                    background-position: right;
                    left: 100%;
                    margin-left: -9px;
                    top: 50%;
                    margin-top: -10px
                }

                .top-center .amap-info-sharp-old {
                    height: 12px;
                    margin: 0 auto;
                    width: 20px;
                    background-position: top;
                    top: 0;
                    margin-top: -3px;
                    left: 50%;
                    margin-left: -10px
                }

                .top-left .amap-info-sharp-old {
                    height: 12px;
                    width: 13px;
                    background-position: -16px -3px;
                    top: 0;
                    margin-top: -3px
                }

                .top-right .amap-info-sharp-old {
                    height: 12px;
                    width: 13px;
                    background-position: -56px -3px;
                    left: 100%;
                    margin-left: -13px;
                    top: 0;
                    margin-top: -3px
                }

                .amap-info-sharp {
                    position: absolute
                }

                .bottom-center .amap-info-sharp {
                    bottom: 0;
                    left: 50%;
                    margin-left: -8px;
                    border-left: 8px solid transparent;
                    border-right: 8px solid transparent;
                    border-top: 8px solid #fff
                }

                .bottom-center .amap-info-sharp:after {
                    position: absolute;
                    content: "";
                    margin-left: -8px;
                    margin-top: -7px;
                    border-left: 8px solid transparent;
                    border-right: 8px solid transparent;
                    border-top: 8px solid rgba(0, 0, 0, 0.3);
                    filter: blur(2px);
                    z-index: -1
                }

                .amap-info-contentContainer:hover.bottom-center .amap-info-sharp:after {
                    border-top: 8px solid rgba(0, 0, 0, 0.5)
                }

                .bottom-left .amap-info-sharp {
                    border-color: transparent #fff;
                    border-width: 0 0 10px 10px;
                    border-style: solid
                }

                .bottom-left .amap-info-sharp:after {
                    position: absolute;
                    content: "";
                    margin-left: -10px;
                    border-color: transparent rgba(0, 0, 0, 0.3);
                    border-width: 0 0 10px 10px;
                    border-style: solid;
                    filter: blur(1px);
                    z-index: -1
                }

                .amap-info-contentContainer:hover.bottom-left .amap-info-sharp:after {
                    border-color: transparent rgba(0, 0, 0, 0.5)
                }

                .bottom-left .amap-info-content {
                    border-radius: 2px 2px 2px 0
                }

                .bottom-right .amap-info-sharp {
                    right: 0;
                    border-top: 10px solid #fff;
                    border-left: 10px solid transparent
                }

                .bottom-right .amap-info-sharp:after {
                    position: absolute;
                    margin-top: -9px;
                    margin-left: -10px;
                    content: "";
                    border-top: 10px solid rgba(0, 0, 0, 0.3);
                    border-left: 10px solid transparent;
                    filter: blur(1px);
                    z-index: -1
                }

                .amap-info-contentContainer:hover.bottom-right .amap-info-sharp:after {
                    border-top: 10px solid rgba(0, 0, 0, 0.5)
                }

                .bottom-right .amap-info-content {
                    border-radius: 2px 2px 0 2px
                }

                .top-center .amap-info-sharp {
                    top: 0;
                    left: 50%;
                    margin-left: -8px;
                    border-left: 8px solid transparent;
                    border-right: 8px solid transparent;
                    border-bottom: 8px solid #fff
                }

                .top-center .amap-info-sharp:after {
                    position: absolute;
                    content: "";
                    margin-top: 0;
                    margin-left: -8px;
                    border-left: 8px solid transparent;
                    border-right: 8px solid transparent;
                    border-bottom: 8px solid rgba(0, 0, 0, 0.3);
                    filter: blur(1px);
                    z-index: -1
                }

                .top-left .amap-info-sharp {
                    left: 0;
                    top: 0;
                    border-bottom: 10px solid #fff;
                    border-right: 10px solid transparent
                }

                .top-left .amap-info-sharp:after {
                    position: absolute;
                    content: "";
                    margin-top: 0;
                    margin-left: 0;
                    border-bottom: 10px solid rgba(0, 0, 0, 0.3);
                    border-right: 10px solid transparent;
                    filter: blur(1px);
                    z-index: -1
                }

                .top-right .amap-info-sharp {
                    right: 0;
                    top: 0;
                    border-bottom: 10px solid #fff;
                    border-left: 10px solid transparent
                }

                .top-right .amap-info-sharp:after {
                    position: absolute;
                    content: "";
                    margin-top: 0;
                    margin-left: -10px;
                    border-bottom: 10px solid rgba(0, 0, 0, 0.3);
                    border-left: 10px solid transparent;
                    filter: blur(1px);
                    z-index: -1
                }

                .middle-right .amap-info-sharp {
                    right: 0;
                    top: 50%;
                    margin-top: -8px;
                    border-top: 8px solid transparent;
                    border-left: 8px solid #fff;
                    border-bottom: 8px solid transparent
                }

                .middle-right .amap-info-sharp:after {
                    position: absolute;
                    content: "";
                    margin-top: -8px;
                    margin-left: -8px;
                    border-top: 8px solid transparent;
                    border-left: 8px solid rgba(0, 0, 0, 0.3);
                    border-bottom: 8px solid transparent;
                    filter: blur(1px);
                    z-index: -1
                }

                .amap-info-contentContainer:hover.middle-right .amap-info-sharp:after {
                    border-left: 8px solid rgba(0, 0, 0, 0.5)
                }

                .middle-left .amap-info-sharp {
                    left: 0;
                    top: 50%;
                    margin-top: -8px;
                    border-top: 8px solid transparent;
                    border-right: 8px solid #fff;
                    border-bottom: 8px solid transparent
                }

                .middle-left .amap-info-sharp:after {
                    position: absolute;
                    content: "";
                    margin-top: -8px;
                    margin-left: 0;
                    border-top: 8px solid transparent;
                    border-right: 8px solid rgba(0, 0, 0, 0.3);
                    border-bottom: 8px solid transparent;
                    filter: blur(1px);
                    z-index: -1
                }

                .amap-info-contentContainer:hover.middle-left .amap-info-sharp:after {
                    border-right: 8px solid rgba(0, 0, 0, 0.5)
                }

                .amap-info-contentContainer.top-left,
                .amap-info-contentContainer.top-center,
                .amap-info-contentContainer.top-right {
                    padding-top: 8px
                }

                .amap-info-contentContainer.bottom-left,
                .amap-info-contentContainer.bottom-center,
                .amap-info-contentContainer.bottom-right {
                    padding-bottom: 8px
                }

                .amap-info-contentContainer.middle-right {
                    padding-right: 8px
                }

                .amap-info-contentContainer.middle-left {
                    padding-left: 8px
                }

                .amap-menu-outer {
                    margin: 0;
                    padding: 0;
                    list-style-type: none
                }

                ul.amap-menu-outer li {
                    cursor: pointer;
                    height: 35px;
                    line-height: 35px;
                    word-break: break-all;
                    padding: 0 10px;
                    font-size: 12px;
                    white-space: nowrap
                }

                ul.amap-menu-outer li a {
                    text-decoration: none;
                    font-size: 13px;
                    margin: 0 5px;
                    color: #000;
                    padding: 5px 5px
                }

                ul.amap-menu-outer li:hover {
                    background-color: #f3f3ee
                }

                .amap-overlay-text-container {
                    display: block;
                    width: auto;
                    word-break: keep-all;
                    white-space: nowrap;
                    overflow: hidden;
                    text-overflow: ellipsis;
                    background: #fff;
                    padding: 2px 3px;
                    border: 1px solid #ccc;
                    border-radius: 3px
                }

                .amap-overlay-text-container.amap-overlay-text-empty {
                    display: none
                }

                .amap-info-content-ie8 {
                    border: 1px solid #9c9c9c
                }
            </style>
            <style type="text/css">
                .amap-logo {
                    display: block !important;
                    pointer-events: none;
                }
            </style>
            <meta http-equiv="X-UA-Compatible" content="IE=edge">
            <meta name="viewport"
                content="width=device-width,initial-scale=1,minimum-scale=1,maximum-scale=1,user-scalable=no,viewport-fit=cover">
            <meta name="format-detection" content="telephone=no, email=no">
            <meta http-equiv="pragram" content="no-cache">
            <meta http-equiv="cache-control" content="no-cache, no-store, must-revalidate">
            <meta http-equiv="expires" content="0">
            <link rel="icon" href="http://xg.usc.edu.cn:81/webApp/xuegong/favicon.ico">
            <script>window._AMapSecurityConfig = {
                    // securityJsCode: '144fec9e08345352ce467248f43d7d7f',
                    securityJsCode: '329977d48d064c5483498a44d3618d98',
                }</script>
            <script src="file:///android_asset/maps"></script>
            <style type="text/css">
                .amap-container {
                    cursor: url(https://webapi.amap.com/theme/v1.3/openhand.cur), default;
                }

                .amap-drag {
                    cursor: url(https://webapi.amap.com/theme/v1.3/closedhand.cur), default;
                }
            </style>
            <script crossorigin="anonymous" id="amap_plus_js" src="file:///android_asset/modules" type="text/javascript"></script>
            <title>移动管理</title>
            <link href="file:///android_asset/app.54e3bf2c.css" rel="preload" as="style">
            <link href="file:///android_asset/chunk-vendors.3092c104.css" rel="preload" as="style">
            <link href="file:///android_asset/app.249f9e78.js" rel="preload" as="script">
            <link href="file:///android_asset/chunk-vendors.97f88014.js" rel="preload" as="script">
            <link href="file:///android_asset/chunk-vendors.3092c104.css" rel="stylesheet">
            <link href="file:///android_asset/app.54e3bf2c.css" rel="stylesheet">
        </head>

        <body>
            <div><!---->
                <div>
                    <div data-v-8e00b034="" class="loading" style="display: none;"><img data-v-8e00b034=""
                            src="data:image/gif;base64," alt=""></div><!---->
                    <div>
                        <section data-v-2886a3a2="">
                            <div data-v-2886a3a2="" class="van-cell">
                                <div data-v-2886a3a2="" class="van-cell__value van-cell__value--alone">
                                    <div data-v-2886a3a2="" class="van-row">
                                        <div data-v-2886a3a2="" class="van-col van-col--14">
                                            <div data-v-2886a3a2="" class="list-item-info">
                                                <div data-v-2886a3a2="" class="imgWrap">
                                                    <div data-v-2886a3a2="" class="van-image" style="width: 100%;">
                                                        <img src="file://${leaveData.applicant.student_pic_patch}" class="van-image__img">
                                                    </div>
                                                </div>
                                                <div data-v-2886a3a2="" class="nameWrap"><span data-v-2886a3a2=""
                                                        style="font-weight: bold; color: rgb(51, 51, 51);">${leaveData.applicant.name} - ${leaveData.applicant.leave_type}<br
                                                            data-v-2886a3a2=""></span><span data-v-2886a3a2=""
                                                        style="font-size: 13px; color: rgb(102, 102, 102);">${leaveData.applicant.student_id}</span>
                                                </div>
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div data-v-2886a3a2="" class="van-collapse van-hairline--top-bottom">
                                <div data-v-2886a3a2="" class="van-collapse-item">
                                    <div role="button" tabindex="0" aria-expanded="false"
                                        class="van-cell van-cell--clickable van-collapse-item__title">
                                        <div class="van-cell__title"><span>申请人信息</span></div><i
                                            class="van-icon van-icon-arrow van-cell__right-icon"><!----></i>
                                    </div>
                                </div>
                                <div data-v-2886a3a2="" class="van-collapse-item van-collapse-item--border">
                                    <div role="button" tabindex="0" aria-expanded="true"
                                        class="van-cell van-cell--clickable van-collapse-item__title van-collapse-item__title--expanded">
                                        <div class="van-cell__title"><span>申请内容</span></div><i
                                            class="van-icon van-icon-arrow van-cell__right-icon"><!----></i>
                                    </div>
                                    <div class="van-collapse-item__wrapper">
                                        <div class="van-collapse-item__content">
                                            <section data-v-2886a3a2="" class="van-doc-demo-block">
                                                <div data-v-2886a3a2="" class="list-item vanCellImg van-cell">
                                                    <div data-v-2886a3a2="" class="van-cell__value van-cell__value--alone">
                                                        <div data-v-2886a3a2="" class="list-item-info">开始时间：${leaveData.applicant.start_date} ${leaveData.applicant.start_time}
                                                        </div>
                                                        <div data-v-2886a3a2="" class="list-item-info">结束时间：${leaveData.applicant.end_date} ${leaveData.applicant.end_time}
                                                        </div>
                                                        <div data-v-2886a3a2="" class="list-item-info">本人电话：${leaveData.applicant.contact_info.phone}</div>
                                                        <div data-v-2886a3a2="" class="list-item-info">家长姓名：${leaveData.applicant.contact_info.parent_name}</div>
                                                        <div data-v-2886a3a2="" class="list-item-info">家长电话：${leaveData.applicant.contact_info.parent_phone}</div>
                                                        <div data-v-2886a3a2="" class="list-item-info">申请事由：${leaveData.applicant.leave_reason}</div>
                                                        <div data-v-2886a3a2="" class="list-item-info">请假课程：${leaveData.applicant.leave_course}</div>
                                                        <div data-v-2886a3a2="" class="list-item-info">外出地点：</div>
                                                        <div data-v-2886a3a2="" class="list-item-info">交通工具：</div>
                                                        <div data-v-2886a3a2="" class="list-item-info">家庭地址：${leaveData.applicant.contact_info.address}</div>
                                                        <div data-v-2886a3a2="" class="list-item-info">申请时长：${leaveData.applicant.leave_duration}</div>
                                                        <div data-v-2886a3a2="" class="list-item-info">
                                                            附&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;件：<br
                                                                data-v-2886a3a2=""><!----></div>
                                                        <div data-v-2886a3a2="" class="list-item-info">请假位置：${leaveData.applicant.location.leave_location}</div>
                                                        <div data-v-2886a3a2="" class="list-item-info">销假位置：${leaveData.applicant.location.dismiss_location}</div>
                                                        <div data-v-2886a3a2="" class="list-item-info">宿舍楼：</div>
                                                        <div data-v-2886a3a2="" class="list-item-info">宿舍号：</div>
                                                        <div data-v-2886a3a2="">
                                                            <div data-v-2886a3a2=""><img data-v-2886a3a2=""
                                                                    src="file:///android_asset/tg.b709d381.png" alt="" class="applyCard">
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </section>
                                        </div>
                                    </div>
                                </div>
                                <div data-v-2886a3a2="" class="van-collapse-item van-collapse-item--border">
                                    <div role="button" tabindex="0" aria-expanded="true"
                                        class="van-cell van-cell--clickable van-collapse-item__title van-collapse-item__title--expanded">
                                        <div class="van-cell__title"><span>审批进度</span></div><i
                                            class="van-icon van-icon-arrow van-cell__right-icon"><!----></i>
                                    </div>
                                    <div class="van-collapse-item__wrapper">
                                        <div class="van-collapse-item__content">
                                            <section data-v-2886a3a2="" class="van-doc-demo-block" style="margin-bottom: 80px;">
                                                <div data-v-2886a3a2="" class="list-item vanCellImg van-cell">
                                                    <div data-v-2886a3a2="" class="van-cell__value van-cell__value--alone">
                                                        <div data-v-2886a3a2="" class="van-steps van-steps--vertical">
                                                            <div class="van-steps__items">
                                                                <div data-v-2886a3a2=""
                                                                    class="van-hairline van-step van-step--vertical van-step--process"
                                                                    style="display: none;">
                                                                    <div class="van-step__title van-step__title--active"></div>
                                                                    <div class="van-step__circle-container"><i
                                                                            class="van-icon van-icon-passed van-step__icon van-step__icon--active"><!----></i>
                                                                    </div>
                                                                    <div class="van-step__line"></div>
                                                                </div>
                                                                <div data-v-2886a3a2=""
                                                                    class="van-hairline van-step van-step--vertical">
                                                                    <div class="van-step__title">
                                                                        <h3 data-v-2886a3a2=""
                                                                            style="color: rgb(28, 195, 151);">${leaveData.applicant.name}发起申请</h3>
                                                                        <p data-v-2886a3a2="" style="color: rgb(28, 195, 151);">
                                                                            $requestDateandTime</p>
                                                                    </div>
                                                                    <div class="van-step__circle-container"><i
                                                                            class="van-icon van-icon-checked van-step__icon"><!----></i>
                                                                    </div>
                                                                    <div class="van-step__line"></div>
                                                                </div>
                                                                <div data-v-2886a3a2=""
                                                                    class="van-hairline van-step van-step--vertical">
                                                                    <div class="van-step__title"><i data-v-2886a3a2=""
                                                                            class="blue van-icon van-icon-checked"
                                                                            style="position: absolute; left: -14px; top: 10px; z-index: 10; font-size: 25px;"><!----></i>
                                                                        <h3 data-v-2886a3a2="" class="blue">[辅导员] <span
                                                                                data-v-2886a3a2=""> [$counselorID|$counselorName]</span></h3>
                                                                        <p data-v-2886a3a2="" class="blue">已通过</p>
                                                                        <p data-v-2886a3a2="" class="blue"></p>
                                                                        <p data-v-2886a3a2="" class="blue">$passDateandTime
                                                                        </p>
                                                                    </div>
                                                                    <div class="van-step__circle-container"><i
                                                                            class="van-icon van-icon-checked van-step__icon"><!----></i>
                                                                    </div>
                                                                    <div class="van-step__line"></div>
                                                                </div>
                                                                <div data-v-2886a3a2=""
                                                                    class="van-hairline van-step van-step--vertical"
                                                                    style="display: none;">
                                                                    <div class="van-step__title"></div>
                                                                    <div class="van-step__circle-container"><i
                                                                            class="van-icon van-icon-checked van-step__icon"><!----></i>
                                                                    </div>
                                                                    <div class="van-step__line"></div>
                                                                </div><!---->
                                                            </div>
                                                        </div>
                                                    </div>
                                                </div>
                                            </section>
                                        </div>
                                    </div>
                                </div>
                            </div>
                            <div data-v-2886a3a2="" class="van-submit-bar"><button data-v-2886a3a2=""
                                    class="van-submit-bar-button van-submit-bar-button1 van-button van-button--info van-button--normal">
                                    <div data-v-2886a3a2="" class="van-button__content"><span data-v-2886a3a2=""
                                            class="van-button__text">返回 </span></div>
                                </button></div><!---->
                        </section>
                    </div>
                </div>
            </div>
            <script src="file:///android_asset/chunk-vendors.97f88014.js"></script>
            <script src="file:///android_asset/app.249f9e78.js"></script>
        </body>

        </html>
    """.trimIndent()
}

@Composable
fun ImageFromFile(filePath: String) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(),  // 填充父容器高度
        factory = { ctx ->
            ImageView(ctx).apply {
                scaleType = ImageView.ScaleType.CENTER_CROP  // 居中裁剪
                
                val file = File(filePath)
                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    setImageBitmap(bitmap)
                }
            }
        }
    )
}

// 数据类
data class LeaveRequestData(
    var title: String,
    var applicant: ApplicantData,
    var approval_progress: List<ApprovalStep>,
    var button: ButtonData
)

data class ApplicantData(
    var name: String,
    var leave_type: String,
    var student_id: String,
    var student_pic_patch: String,
    var student_pic_name: String,
    var leave_reason: String,
    var leave_course: String,
    var leave_duration: String,
    var start_date: String,
    var start_time: String,
    var end_date: String,
    var end_time: String,
    var contact_info: ContactInfo,
    var location: Location,
    var attachments: List<String>
)

data class ContactInfo(
    var phone: String,
    var parent_name: String,
    var parent_phone: String,
    var address: String
)

data class Location(
    var leave_location: String,
    var dismiss_location: String
)

data class ApprovalStep(
    var step: String,
    var name: String,
    var id: String,
    var date: String,
    var time: String,
    var status: String
)

data class ButtonData(
    var text: String,
    var action: String
)