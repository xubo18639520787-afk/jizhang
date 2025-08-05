package com.personalaccounting.app.presentation.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.text.NumberFormat
import java.util.*
import kotlin.math.*

/**
 * 饼状图组件
 */
@Composable
fun PieChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    showLabels: Boolean = true,
    showPercentages: Boolean = true,
    animationDuration: Int = 1000
) {
    val total = data.sumOf { it.value.toDouble() }
    if (total == 0.0) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var animationPlayed by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(animationDuration),
        label = "pie_chart_animation"
    )

    LaunchedEffect(data) {
        animationPlayed = true
    }

    Column(modifier = modifier) {
        // 饼状图
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = minOf(size.width, size.height) / 2 * 0.8f
                
                var startAngle = -90f
                
                data.forEach { chartData ->
                    val sweepAngle = (chartData.value.toDouble() / total * 360).toFloat() * animationProgress
                    
                    drawArc(
                        color = chartData.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        topLeft = Offset(
                            center.x - radius,
                            center.y - radius
                        ),
                        size = Size(radius * 2, radius * 2)
                    )
                    
                    // 绘制标签
                    if (showLabels && animationProgress > 0.8f) {
                        val labelAngle = startAngle + sweepAngle / 2
                        val labelRadius = radius * 0.7f
                        val labelX = center.x + cos(Math.toRadians(labelAngle.toDouble())).toFloat() * labelRadius
                        val labelY = center.y + sin(Math.toRadians(labelAngle.toDouble())).toFloat() * labelRadius
                        
                        val percentage = (chartData.value.toDouble() / total * 100).toInt()
                        if (percentage >= 5) { // 只显示占比大于5%的标签
                            drawContext.canvas.nativeCanvas.drawText(
                                if (showPercentages) "$percentage%" else chartData.label,
                                labelX,
                                labelY,
                                android.graphics.Paint().apply {
                                    color = Color.White.toArgb()
                                    textSize = 12.sp.toPx()
                                    textAlign = android.graphics.Paint.Align.CENTER
                                    isFakeBoldText = true
                                }
                            )
                        }
                    }
                    
                    startAngle += sweepAngle
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // 图例
        LazyColumn {
            items(data) { chartData ->
                ChartLegendItem(
                    data = chartData,
                    percentage = (chartData.value.toDouble() / total * 100).toFloat()
                )
            }
        }
    }
}

/**
 * 柱状图组件
 */
@Composable
fun BarChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    showValues: Boolean = true,
    animationDuration: Int = 1000
) {
    val maxValue = data.maxOfOrNull { it.value.toDouble() } ?: 0.0
    if (maxValue == 0.0) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var animationPlayed by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(animationDuration),
        label = "bar_chart_animation"
    )

    LaunchedEffect(data) {
        animationPlayed = true
    }

    Column(modifier = modifier) {
        // 柱状图
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            val barWidth = size.width / data.size * 0.6f
            val barSpacing = size.width / data.size * 0.4f
            val chartHeight = size.height * 0.8f
            
            data.forEachIndexed { index, chartData ->
                val barHeight = (chartData.value.toDouble() / maxValue * chartHeight).toFloat() * animationProgress
                val x = index * (barWidth + barSpacing) + barSpacing / 2
                val y = size.height - barHeight
                
                // 绘制柱子
                drawRect(
                    color = chartData.color,
                    topLeft = Offset(x, y),
                    size = Size(barWidth, barHeight)
                )
                
                // 绘制数值
                if (showValues && animationProgress > 0.8f) {
                    val valueText = NumberFormat.getCurrencyInstance(Locale.CHINA)
                        .format(chartData.value.toDouble())
                    
                    drawContext.canvas.nativeCanvas.drawText(
                        valueText,
                        x + barWidth / 2,
                        y - 10,
                        android.graphics.Paint().apply {
                            color = MaterialTheme.colorScheme.onSurface.toArgb()
                            textSize = 10.sp.toPx()
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                    )
                }
                
                // 绘制标签
                drawContext.canvas.nativeCanvas.drawText(
                    chartData.label,
                    x + barWidth / 2,
                    size.height - 5,
                    android.graphics.Paint().apply {
                        color = MaterialTheme.colorScheme.onSurface.toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
    }
}

/**
 * 折线图组件
 */
@Composable
fun LineChart(
    data: List<ChartData>,
    modifier: Modifier = Modifier,
    showPoints: Boolean = true,
    showValues: Boolean = false,
    animationDuration: Int = 1000
) {
    val maxValue = data.maxOfOrNull { it.value.toDouble() } ?: 0.0
    val minValue = data.minOfOrNull { it.value.toDouble() } ?: 0.0
    val valueRange = maxValue - minValue
    
    if (valueRange == 0.0 || data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无数据",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var animationPlayed by remember { mutableStateOf(false) }
    val animationProgress by animateFloatAsState(
        targetValue = if (animationPlayed) 1f else 0f,
        animationSpec = tween(animationDuration),
        label = "line_chart_animation"
    )

    LaunchedEffect(data) {
        animationPlayed = true
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val chartHeight = size.height * 0.8f
        val chartWidth = size.width * 0.9f
        val startX = size.width * 0.05f
        val startY = size.height * 0.1f
        
        val points = data.mapIndexed { index, chartData ->
            val x = startX + (index.toFloat() / (data.size - 1)) * chartWidth
            val normalizedValue = (chartData.value.toDouble() - minValue) / valueRange
            val y = startY + chartHeight - (normalizedValue * chartHeight).toFloat()
            Offset(x, y)
        }
        
        // 绘制折线
        if (points.size > 1) {
            for (i in 0 until (points.size - 1)) {
                val progress = minOf(animationProgress * points.size - i, 1f).coerceAtLeast(0f)
                if (progress > 0) {
                    val start = points[i]
                    val end = points[i + 1]
                    val animatedEnd = Offset(
                        start.x + (end.x - start.x) * progress,
                        start.y + (end.y - start.y) * progress
                    )
                    
                    drawLine(
                        color = data[i].color,
                        start = start,
                        end = animatedEnd,
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }
        }
        
        // 绘制数据点
        if (showPoints) {
            points.forEachIndexed { index, point ->
                val progress = minOf(animationProgress * points.size - index, 1f).coerceAtLeast(0f)
                if (progress > 0) {
                    drawCircle(
                        color = data[index].color,
                        radius = 4.dp.toPx() * progress,
                        center = point
                    )
                    
                    drawCircle(
                        color = Color.White,
                        radius = 2.dp.toPx() * progress,
                        center = point
                    )
                }
            }
        }
        
        // 绘制数值
        if (showValues && animationProgress > 0.8f) {
            points.forEachIndexed { index, point ->
                val valueText = NumberFormat.getCurrencyInstance(Locale.CHINA)
                    .format(data[index].value.toDouble())
                
                drawContext.canvas.nativeCanvas.drawText(
                    valueText,
                    point.x,
                    point.y - 15,
                    android.graphics.Paint().apply {
                        color = MaterialTheme.colorScheme.onSurface.toArgb()
                        textSize = 10.sp.toPx()
                        textAlign = android.graphics.Paint.Align.CENTER
                    }
                )
            }
        }
        
        // 绘制X轴标签
        data.forEachIndexed { index, chartData ->
            val x = startX + (index.toFloat() / (data.size - 1)) * chartWidth
            val y = size.height - 10
            
            drawContext.canvas.nativeCanvas.drawText(
                chartData.label,
                x,
                y,
                android.graphics.Paint().apply {
                    color = MaterialTheme.colorScheme.onSurface.toArgb()
                    textSize = 10.sp.toPx()
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }
    }
}

/**
 * 图例项组件
 */
@Composable
private fun ChartLegendItem(
    data: ChartData,
    percentage: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(data.color)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data.label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = NumberFormat.getCurrencyInstance(Locale.CHINA).format(data.value.toDouble()),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Text(
            text = "${percentage.toInt()}%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = data.color
        )
    }
}

/**
 * 统计卡片组件
 */
@Composable
fun StatisticsCard(
    title: String,
    value: String,
    subtitle: String? = null,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                textAlign = TextAlign.Center
            )
            
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * 图表数据类
 */
data class ChartData(
    val label: String,
    val value: BigDecimal,
    val color: Color
)