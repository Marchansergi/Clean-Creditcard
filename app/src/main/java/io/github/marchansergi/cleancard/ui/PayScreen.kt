package io.github.marchansergi.cleancard.ui

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import io.github.marchansergi.cleancard.NfcStatus
import io.github.marchansergi.cleancard.R
import io.github.marchansergi.cleancard.data.CardDesign

@Composable
fun PayScreen(
    card: CardDesign?,
    nfcStatus: NfcStatus,
    onClose: () -> Unit,
    onCreate: () -> Unit,
    onEnableNfc: () -> Unit,
) {
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.radialGradient(listOf(Color(0xFF1E1E22), Color(0xFF050506))))
    ) {
        if (card == null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.align(Alignment.Center).padding(32.dp),
            ) {
                Text(stringResource(R.string.pay_no_cards), textAlign = TextAlign.Center)
                Button(onClick = onCreate) { Text(stringResource(R.string.pay_create)) }
            }
        } else {
            PayContent(card, nfcStatus, onEnableNfc)
        }
        IconButton(
            onClick = onClose,
            modifier = Modifier.align(Alignment.TopEnd).safeDrawingPadding().padding(8.dp),
        ) {
            Icon(Icons.Filled.Close, contentDescription = stringResource(R.string.close), tint = Color.White)
        }
    }
}

@Composable
private fun PayContent(card: CardDesign, nfcStatus: NfcStatus, onEnableNfc: () -> Unit) {
    val tilt by rememberTilt()
    val transition = rememberInfiniteTransition(label = "pay")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(5000, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "drift",
    )
    val pulse by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulse",
    )

    BoxWithConstraints(Modifier.fillMaxSize().safeDrawingPadding()) {
        val cardWidth = min(maxWidth * 0.92f, maxHeight * 0.5f * CARD_RATIO)
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize().padding(16.dp),
        ) {
            CardFace(
                design = card,
                sheen = { Offset(0.2f + drift * 0.6f - tilt.x * 0.3f, 0.2f + tilt.y * 0.3f) },
                modifier = Modifier
                    .width(cardWidth)
                    .graphicsLayer {
                        rotationY = -tilt.x * 10f
                        rotationX = tilt.y * 8f
                        cameraDistance = 14f * density
                    },
            )
            Spacer(Modifier.height(40.dp))
            ContactlessMark(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(44.dp).graphicsLayer {
                    scaleX = pulse
                    scaleY = pulse
                },
            )
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.pay_hint), style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.pay_sub_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
            if (nfcStatus != NfcStatus.READY) {
                Spacer(Modifier.height(24.dp))
                NfcWarning(nfcStatus, onEnable = onEnableNfc)
            }
        }
    }
}

/** Device tilt relative to how the phone is being held, in -1..1 on each axis. */
@Composable
private fun rememberTilt(): State<Offset> {
    val context = LocalContext.current
    val tilt = remember { mutableStateOf(Offset.Zero) }
    DisposableEffect(Unit) {
        val manager = context.getSystemService(SensorManager::class.java)
        val sensor = manager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (manager == null || sensor == null) return@DisposableEffect onDispose {}

        var baseX = Float.NaN
        var baseY = 0f
        var x = 0f
        var y = 0f
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                val rawX = event.values[0]
                val rawY = event.values[1]
                if (baseX.isNaN()) {
                    baseX = rawX; baseY = rawY; x = rawX; y = rawY
                }
                // Fast filter follows the hand, slow filter is the "resting" position.
                x += (rawX - x) * 0.25f
                y += (rawY - y) * 0.25f
                baseX += (rawX - baseX) * 0.01f
                baseY += (rawY - baseY) * 0.01f
                tilt.value = Offset(((x - baseX) / 4f).coerceIn(-1f, 1f), ((y - baseY) / 4f).coerceIn(-1f, 1f))
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
        }
        manager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_GAME)
        onDispose { manager.unregisterListener(listener) }
    }
    return tilt
}
