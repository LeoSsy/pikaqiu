package com.hhkv.rustitok.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.MultiplePermissionsState
import com.google.accompanist.permissions.PermissionStatus

object DialogUtils {

    /**
     * 通用对话框
     * */
    @Composable
    fun ShowDialog(
        title:String,
        hint:String,
        cancelTitle:String = "取消",
        confirmTitle:String = "确定",
        confirmButtonClick: () -> Unit = {},
        dismissButtonClick:() -> Unit = {}
    ) {
        AlertDialog(
            title = {
                Text(text = title)
            },
            text = {
                Text(text = hint)
            },
            onDismissRequest = {
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmButtonClick()
                    }
                ) {
                    Text(confirmTitle)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        dismissButtonClick()
                    }
                ) {
                    Text(cancelTitle)
                }
            }
        )
    }

    /**
     * 提醒开启通知对话框
     * */
    @Composable
    fun ShowEnableNotificationDialog(
        context: Context,
        confirmButtonClick: () -> Unit = {},
        dismissButtonClick:() -> Unit = {}
    ) {
        AlertDialog(
            title = {
                Text(text = "启用通知权限")
            },
            text = {
                Text(text = "请在\"设置→应用通知管理\"中打开允许通知选项")
            },
            onDismissRequest = {
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmButtonClick()
                        try {
                            val intent = Intent(Settings.ACTION_MANAGE_APPLICATIONS_SETTINGS)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                ) {
                    Text("去设置")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        dismissButtonClick()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }


    /**
     * 提醒开启模拟位置对话框
     * */
    @Composable
    fun ShowEnableMockLocationDialog(
        context: Context,
        confirmButtonClick: () -> Unit = {},
        dismissButtonClick:() -> Unit = {}
    ) {
        AlertDialog(
            title = {
                Text(text = "启用位置模拟")
            },
            text = {
                Text(text = "请在\"开发者选项→选择模拟位置信息应用\"中进行设置")
            },
            onDismissRequest = {
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmButtonClick()
                        try {
                            val intent = Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }
                ) {
                    Text("设置")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        dismissButtonClick()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    /**
     * 提醒开启悬浮框对话框
     * */
    @Composable
    fun ShowEnableFloatWindowDialog(
        context: Context,
        confirmButtonClick: () -> Unit = {},
        dismissButtonClick:() -> Unit = {}
    ) {
        AlertDialog(
            title = {
                Text(text = "启用悬浮窗")
            },
            text = {
                Text(text = "为了模拟定位的稳定性，建议开启\"显示悬浮窗\"选项")
            },
            onDismissRequest = {
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmButtonClick()
                        try {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                ("package:" + context.packageName).toUri()
                            )
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                ) {
                    Text("设置")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        dismissButtonClick()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    /**
     * 显示权限被拒绝对话框
     * */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun ShowDenialPermissionDialog(
        context: Context,
        permissionsState: MultiplePermissionsState,
        confirmButtonClick: () -> Unit = {},
        dismissButtonClick:() -> Unit = {}
    ) {
        var permissions = mutableListOf<String>()
        // 显示每个权限的单独状态
        permissionsState.permissions.forEach { permission ->
            when (permission.status) {
                is PermissionStatus.Granted -> {}
                is PermissionStatus.Denied ->
                    permissions.add("${PermissionUtils.PERMISSIONS_INFOS.getValue(permission.permission)}：被拒绝")
            }
        }
        if (permissions.isEmpty()) return
        AlertDialog(
            title = {
                Text(text = "请开启以下被拒绝的权限")
            },
            text = {
                Text(text = permissions.joinToString("\n"))
            },
            onDismissRequest = {
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmButtonClick()
                        try {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                ("package:" + context.packageName).toUri()
                            )
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                ) {
                    Text("设置")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        dismissButtonClick()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }

    /**
     * 显示权限被拒绝对话框
     * */
    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    fun ShowDenialPermissionDialog(
        context: Context,
        permissions: List<String>,
        confirmButtonClick: () -> Unit = {},
        dismissButtonClick:() -> Unit = {}
    ) {
        var permissions = mutableListOf<String>()
        // 显示每个权限的单独状态
        permissions.forEach { permission ->
            permissions.add("${PermissionUtils.PERMISSIONS_INFOS.getValue(permission)}：被拒绝")
        }
        if (permissions.isEmpty()) return
        AlertDialog(
            title = {
                Text(text = "请前往应用设置中开启以下权限")
            },
            text = {
                Text(text = permissions.joinToString("\n"))
            },
            onDismissRequest = {
            },
            confirmButton = {
                Button(
                    onClick = {
                        confirmButtonClick()
                        try {
                            val intent = Intent(
                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                ("package:" + context.packageName).toUri()
                            )
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                ) {
                    Text("设置")
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        dismissButtonClick()
                    }
                ) {
                    Text("取消")
                }
            }
        )
    }


}