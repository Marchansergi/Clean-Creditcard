package io.github.marchansergi.cleancard.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.marchansergi.cleancard.R

private val Swatches = listOf(
    0xFF0C0C0C, 0xFF2B2B2E, 0xFF5A5D63, 0xFFC9CDD2, 0xFFF4F4F2,
    0xFF7A5A1E, 0xFFB08D57, 0xFFE3C477, 0xFFF6E7B5, 0xFFB76E79,
    0xFF0B1D51, 0xFF1F4FA8, 0xFF2C5364, 0xFF0B3D2E, 0xFF1E7F5C,
    0xFF5B1A2E, 0xFFB3122E, 0xFFDD2476, 0xFFFF7A3D, 0xFF4B2A7B,
).map { Color(it) }

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ColorPickerDialog(initial: Color, onDismiss: () -> Unit, onPick: (Color) -> Unit) {
    val hsv = remember(initial) { FloatArray(3).also { android.graphics.Color.colorToHSV(initial.toArgb(), it) } }
    var hue by remember(initial) { mutableFloatStateOf(hsv[0]) }
    var saturation by remember(initial) { mutableFloatStateOf(hsv[1]) }
    var brightness by remember(initial) { mutableFloatStateOf(hsv[2]) }
    val color = Color.hsv(hue, saturation, brightness)

    fun select(swatch: Color) {
        val values = FloatArray(3)
        android.graphics.Color.colorToHSV(swatch.toArgb(), values)
        hue = values[0]
        saturation = values[1]
        brightness = values[2]
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.color_title)) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.verticalScroll(rememberScrollState()),
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color)
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Swatches.forEach { swatch ->
                        val label = "#%06X".format(swatch.toArgb() and 0xFFFFFF)
                        Box(
                            Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(swatch)
                                .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                .clickable { select(swatch) }
                                .semantics { contentDescription = label }
                        )
                    }
                }
                LabeledSlider(stringResource(R.string.color_hue), hue, 0f..360f) { hue = it }
                LabeledSlider(stringResource(R.string.color_saturation), saturation, 0f..1f) { saturation = it }
                LabeledSlider(stringResource(R.string.color_brightness), brightness, 0f..1f) { brightness = it }
            }
        },
        confirmButton = {
            TextButton(onClick = { onPick(color) }) { Text(stringResource(R.string.ok)) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onChange: (Float) -> Unit,
) {
    Column {
        Text(label, style = MaterialTheme.typography.labelMedium)
        Slider(value = value, onValueChange = onChange, valueRange = range)
    }
}
