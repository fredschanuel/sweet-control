package com.example.sweetcontrol.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun GraficoLinha(
    productionData: List<Int>,
    salesData: List<Int>,
    modifier: Modifier = Modifier
) {
    val maxValue = maxOf(productionData.maxOrNull() ?: 0, salesData.maxOrNull() ?: 0)

    Canvas(modifier = modifier) {
        val spacePerDay = size.width / (productionData.size - 1)
        val heightPerUnit = size.height / maxValue

        drawLine(
            start = Offset(0f, size.height),
            end = Offset(size.width, size.height),
            color = Color.Gray,
            strokeWidth = 2f
        )

        productionData.forEachIndexed { index, value ->
            val x = index * spacePerDay
            val y = size.height - (value * heightPerUnit)

            if (index > 0) {
                val prevX = (index - 1) * spacePerDay
                val prevY = size.height - (productionData[index - 1] * heightPerUnit)

                drawLine(
                    start = Offset(prevX, prevY),
                    end = Offset(x, y),
                    color = Color.Blue,
                    strokeWidth = 3f
                )
            }

            drawCircle(
                color = Color.Blue,
                radius = 5f,
                center = Offset(x, y)
            )
        }

        salesData.forEachIndexed { index, value ->
            val x = index * spacePerDay
            val y = size.height - (value * heightPerUnit)

            if (index > 0) {
                val prevX = (index - 1) * spacePerDay
                val prevY = size.height - (salesData[index - 1] * heightPerUnit)

                drawLine(
                    start = Offset(prevX, prevY),
                    end = Offset(x, y),
                    color = Color.Green,
                    strokeWidth = 3f
                )
            }

            drawCircle(
                color = Color.Green,
                radius = 5f,
                center = Offset(x, y)
            )
        }
    }
}