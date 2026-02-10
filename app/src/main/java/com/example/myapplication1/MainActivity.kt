package com.example.myapplication1

import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication1.ui.theme.MyApplication1Theme
import kotlinx.coroutines.delay
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplication1Theme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    WoodenFishScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

// 标识每个跳字效果的唯一ID
data class MeritPopup(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
)

@Composable
fun WoodenFishScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var soundLoaded by remember { mutableStateOf(false) }
    val soundPool = remember {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        SoundPool.Builder()
            .setMaxStreams(20)
            .setAudioAttributes(audioAttributes)
            .build()
            .apply {
                setOnLoadCompleteListener { _, _, status ->
                    if (status == 0) soundLoaded = true
                }
            }
    }
    val soundId = remember { soundPool.load(context, R.raw.voice, 1) }

    // 释放 SoundPool
    DisposableEffect(soundPool) {
        onDispose { soundPool.release() }
    }

    // 功德计数状态
    var meritCount by remember { mutableIntStateOf(0) }
    var meritName by remember { mutableStateOf("功德") }
    var meritDelta by remember { mutableIntStateOf(1) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editingName by remember { mutableStateOf(meritName) }
    var editingDelta by remember { mutableIntStateOf(meritDelta) }
    // 动画状态：是否处于被点击（缩放）状态
    var isPressed by remember { mutableStateOf(false) }
    // 跳字列表 - 使用 mutableStateListOf 维护
    val popups = remember { mutableStateListOf<MeritPopup>() }
    
    // 缩放动画
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ScaleAnimation"
    )

    val scope = rememberCoroutineScope()

    // 整个屏幕背景设置为黑色
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black), 
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "$meritName：$meritCount",
                color = Color.White,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    editingName = meritName
                    editingDelta = meritDelta
                    showEditDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(60.dp))

        // 木鱼区域（包含木鱼图片和上方的浮动文字）
        Box(contentAlignment = Alignment.Center) {
            // 木鱼图片
            Image(
                painter = painterResource(id = R.drawable.white_muyu),
                contentDescription = "Wooden Fish",
                modifier = Modifier
                    .size(200.dp)
                    .scale(scale)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        meritCount += meritDelta
                        if (soundLoaded) {
                            soundPool.play(soundId, 1f, 1f, 0, 0, 1f)
                        }
                        // 无论上一个动画是否结束，立刻添加新的跳字动画
                        val deltaText = if (meritDelta > 0) "+1" else "-1"
                        popups.add(MeritPopup(text = "$meritName$deltaText"))
                        // 触发缩放动画 (快速重置以支持快速连点视觉反馈)
                        scope.launch {
                            isPressed = true
                            delay(50) // 缩短停留时间，提升连点手感
                            isPressed = false
                        }
                    }
            )

            // 浮动文字层：遍历渲染所有的跳字
            // 使用 key 确保 Compose 能正确追踪每个独立的 popup 实例
            popups.forEach { popup ->
                key(popup.id) {
                    FloatingMeritText(
                        popup = popup,
                        onAnimationEnd = { 
                            popups.removeAll { it.id == popup.id }
                        }
                    )
                }
            }
        }
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text(text = "修改内容") },
            text = {
                Column {
                    OutlinedTextField(
                        value = editingName,
                        onValueChange = { editingName = it },
                        label = { Text(text = "功德名称") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = editingDelta == 1,
                            onClick = { editingDelta = 1 }
                        )
                        Text(text = "+1")
                        Spacer(modifier = Modifier.width(16.dp))
                        RadioButton(
                            selected = editingDelta == -1,
                            onClick = { editingDelta = -1 }
                        )
                        Text(text = "-1")
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        meritName = editingName.trim().ifBlank { "功德" }
                        meritDelta = if (editingDelta >= 0) 1 else -1
                        meritCount = 0
                        popups.clear()
                        showEditDialog = false
                    }
                ) {
                    Text(text = "保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text(text = "取消")
                }
            }
        )
    }
}

@Composable
fun FloatingMeritText(popup: MeritPopup, onAnimationEnd: () -> Unit) {
    val alpha = remember { Animatable(1f) }
    val offsetY = remember { Animatable(-100f) }
    
    LaunchedEffect(popup.id) {
        coroutineScope {
            val moveJob = launch {
                offsetY.animateTo(
                    targetValue = -250f,
                    animationSpec = tween(durationMillis = 1000)
                )
            }
            val fadeJob = launch {
                alpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 1000)
                )
            }
            joinAll(moveJob, fadeJob)
        }
        onAnimationEnd()
    }
    
    Text(
        text = popup.text,
        color = Color.White.copy(alpha = alpha.value),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .offset(y = offsetY.value.dp) 
    )
}

@Preview(showBackground = true)
@Composable
fun WoodenFishPreview() {
    MyApplication1Theme {
        WoodenFishScreen()
    }
}
