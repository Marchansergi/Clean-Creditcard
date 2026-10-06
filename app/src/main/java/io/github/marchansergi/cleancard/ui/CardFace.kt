package io.github.marchansergi.cleancard.ui

import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.marchansergi.cleancard.data.CardDesign
import io.github.marchansergi.cleancard.data.Finish
import io.github.marchansergi.cleancard.data.ImageStore
import io.github.marchansergi.cleancard.data.TextTone
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.ceil
import kotlin.random.Random

/** ISO/IEC 7810 ID-1 card proportions (85.60 × 53.98 mm). */
const val CARD_RATIO = 85.60f / 53.98f

private val DefaultSheen = Offset(0.3f, 0.2f)

/**
 * Draws a card design. Every size is relative to the card width, so it looks the same as a
 * small thumbnail or full screen.
 *
 * @param sheen normalized position (0..1) of the light reflection; read at draw time only, so
 * animating it does not recompose the card.
 */
@Composable
fun CardFace(
    design: CardDesign,
    modifier: Modifier = Modifier,
    sheen: () -> Offset = { DefaultSheen },
) {
    val image = rememberCardImage(design.imageFile)
    val light = design.textTone == TextTone.LIGHT
    val textColor = if (light) Color(0xFFF7F7F5) else Color(0xFF1C1C1C)
    val textShadow = if (light) {
        Shadow(Color.Black.copy(alpha = 0.45f), Offset(0f, 2f), 3f)
    } else {
        Shadow(Color.White.copy(alpha = 0.35f), Offset(0f, 1.5f), 1f)
    }

    BoxWithConstraints(modifier.aspectRatio(CARD_RATIO)) {
        val w = maxWidth
        val density = LocalDensity.current
        fun fontSize(fraction: Float) = with(density) { (w * fraction).toSp() }
        val shape = RoundedCornerShape(w * 0.045f)

        Box(
            Modifier
                .fillMaxSize()
                .shadow(w * 0.03f, shape)
                .clip(shape)
                .border(1.dp, Color.White.copy(alpha = 0.14f), shape)
        ) {
            if (image != null) {
                Image(
                    bitmap = image,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Box(
                Modifier
                    .fillMaxSize()
                    .cardBackground(design, hasImage = image != null)
                    .drawBehind {
                        val s = sheen()
                        drawRect(
                            Brush.radialGradient(
                                listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
                                center = Offset(size.width * s.x, size.height * s.y),
                                radius = size.width * 0.65f,
                            )
                        )
                    }
            )

            Column(Modifier.fillMaxSize().padding(w * 0.06f)) {
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = design.title,
                        color = textColor,
                        fontSize = fontSize(0.062f),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(shadow = textShadow),
                        modifier = Modifier.weight(1f),
                    )
                    if (design.badge.isNotBlank()) {
                        Text(
                            text = design.badge.uppercase(),
                            color = textColor,
                            fontSize = fontSize(0.038f),
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.18.em,
                            maxLines = 1,
                            style = TextStyle(shadow = textShadow),
                            modifier = Modifier.padding(start = w * 0.03f),
                        )
                    }
                }
                Spacer(Modifier.weight(1f))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (design.showChip) {
                        ChipMark(Modifier.size(width = w * 0.135f, height = w * 0.105f))
                        Spacer(Modifier.width(w * 0.035f))
                    }
                    if (design.showContactless) {
                        ContactlessMark(textColor, Modifier.size(w * 0.075f))
                    }
                }
                Spacer(Modifier.weight(1f))
                if (design.lastDigits.isNotBlank()) {
                    Text(
                        text = "••••  ••••  ••••  ${design.lastDigits}",
                        color = textColor,
                        fontSize = fontSize(0.055f),
                        fontFamily = FontFamily.Monospace,
                        letterSpacing = 0.05.em,
                        maxLines = 1,
                        style = TextStyle(shadow = textShadow),
                    )
                    Spacer(Modifier.height(w * 0.02f))
                }
                if (design.holderName.isNotBlank()) {
                    Text(
                        text = design.holderName.uppercase(),
                        color = textColor,
                        fontSize = fontSize(0.042f),
                        letterSpacing = 0.1.em,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(shadow = textShadow),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

/** Gradient plus finish texture. Everything is built once per size, not on every frame. */
private fun Modifier.cardBackground(design: CardDesign, hasImage: Boolean) = drawWithCache {
    val start = Color(design.colorStart)
    val end = Color(design.colorEnd)
    val gradient = Brush.linearGradient(listOf(start, end), Offset.Zero, Offset(size.width, size.height))
    val scrim = if (design.textTone == TextTone.LIGHT) Color.Black else Color.White
    val imageScrim = Brush.verticalGradient(
        0f to scrim.copy(alpha = 0.25f),
        0.3f to Color.Transparent,
        0.6f to Color.Transparent,
        1f to scrim.copy(alpha = 0.45f),
    )

    val brushedLines = if (design.finish == Finish.BRUSHED_METAL) {
        val random = Random(7)
        val step = 1.5.dp.toPx()
        List(ceil(size.height / step).toInt()) { i ->
            val tone = if (random.nextBoolean()) Color.White else Color.Black
            Triple(i * step, tone.copy(alpha = 0.02f + random.nextFloat() * 0.06f), step * 0.6f)
        }
    } else emptyList()
    val metalBands = Brush.verticalGradient(
        0f to Color.White.copy(alpha = 0f),
        0.32f to Color.White.copy(alpha = 0.14f),
        0.5f to Color.White.copy(alpha = 0f),
        0.82f to Color.Black.copy(alpha = 0.10f),
        1f to Color.White.copy(alpha = 0f),
    )
    val gloss = Brush.linearGradient(
        0f to Color.White.copy(alpha = 0f),
        0.42f to Color.White.copy(alpha = 0.16f),
        0.5f to Color.White.copy(alpha = 0.04f),
        0.58f to Color.White.copy(alpha = 0f),
        start = Offset.Zero,
        end = Offset(size.width, size.height),
    )
    val holo = Brush.linearGradient(
        listOf(0xFFFF9AD5, 0xFF9AD0FF, 0xFFA6FFCB, 0xFFFFF59A, 0xFFD3A6FF).map { Color(it).copy(alpha = 0.45f) },
        start = Offset.Zero,
        end = Offset(size.width * 0.6f, size.height * 0.6f),
        tileMode = TileMode.Mirror,
    )
    val carbonCell = size.width / 48f
    val carbonTiles = if (design.finish == Finish.CARBON) {
        val cols = ceil(size.width / carbonCell).toInt()
        val rows = ceil(size.height / carbonCell).toInt()
        List(cols * rows) { index ->
            val col = index % cols
            val row = index / cols
            val topLeft = Offset(col * carbonCell, row * carbonCell)
            val colors = listOf(Color.White.copy(alpha = 0.08f), Color.Black.copy(alpha = 0.18f))
            val brush = if ((col + row) % 2 == 0) {
                Brush.verticalGradient(colors, startY = topLeft.y, endY = topLeft.y + carbonCell)
            } else {
                Brush.horizontalGradient(colors, startX = topLeft.x, endX = topLeft.x + carbonCell)
            }
            topLeft to brush
        }
    } else emptyList()

    onDrawBehind {
        if (hasImage) {
            drawRect(imageScrim)
            drawRect(gloss)
            return@onDrawBehind
        }
        drawRect(gradient)
        when (design.finish) {
            Finish.MATTE -> drawRect(Color.Black.copy(alpha = 0.05f))
            Finish.GLOSSY -> drawRect(gloss)
            Finish.BRUSHED_METAL -> {
                brushedLines.forEach { (y, color, stroke) ->
                    drawLine(color, Offset(0f, y), Offset(size.width, y), stroke)
                }
                drawRect(metalBands)
            }
            Finish.HOLOGRAPHIC -> {
                drawRect(holo)
                drawRect(gloss)
            }
            Finish.CARBON -> {
                carbonTiles.forEach { (topLeft, brush) ->
                    drawRect(brush, topLeft, Size(carbonCell, carbonCell))
                }
                drawRect(gloss)
            }
        }
    }
}

/** Generic EMV-style contact chip. */
@Composable
private fun ChipMark(modifier: Modifier) {
    Canvas(modifier) {
        val corner = CornerRadius(size.minDimension * 0.18f)
        drawRoundRect(
            Brush.linearGradient(
                listOf(Color(0xFFE9D8A6), Color(0xFFC9A54C), Color(0xFFF3E3B3), Color(0xFFB08A3A)),
                start = Offset.Zero,
                end = Offset(size.width, size.height),
            ),
            cornerRadius = corner,
        )
        val line = Color(0xFF6B5420).copy(alpha = 0.55f)
        val stroke = size.minDimension * 0.045f
        val w = size.width
        val h = size.height
        listOf(h / 3f, h * 2f / 3f).forEach { y ->
            drawLine(line, Offset(0f, y), Offset(w * 0.36f, y), stroke)
            drawLine(line, Offset(w * 0.64f, y), Offset(w, y), stroke)
        }
        drawLine(line, Offset(w / 2f, 0f), Offset(w / 2f, h * 0.3f), stroke)
        drawLine(line, Offset(w / 2f, h * 0.7f), Offset(w / 2f, h), stroke)
        drawRoundRect(
            line,
            topLeft = Offset(w * 0.36f, h * 0.3f),
            size = Size(w * 0.28f, h * 0.4f),
            cornerRadius = CornerRadius(stroke * 2f),
            style = Stroke(stroke),
        )
        drawRoundRect(line, cornerRadius = corner, style = Stroke(stroke))
    }
}

/** Generic contactless symbol: four arcs opening to the right. */
@Composable
fun ContactlessMark(color: Color, modifier: Modifier = Modifier) {
    Canvas(modifier) {
        val s = size.minDimension
        val center = Offset(s * 0.104f, size.height / 2f)
        val stroke = Stroke(width = s * 0.085f, cap = StrokeCap.Round)
        listOf(0.208f, 0.354f, 0.5f, 0.646f).forEach { fraction ->
            val r = s * fraction
            drawArc(
                color = color,
                startAngle = -40f,
                sweepAngle = 80f,
                useCenter = false,
                topLeft = Offset(center.x - r, center.y - r),
                size = Size(r * 2f, r * 2f),
                style = stroke,
            )
        }
    }
}

private val imageCache = LruCache<String, ImageBitmap>(6)

@Composable
private fun rememberCardImage(fileName: String?): ImageBitmap? {
    val context = LocalContext.current
    val bitmap by produceState(initialValue = fileName?.let { imageCache.get(it) }, fileName) {
        value = fileName?.let { name ->
            imageCache.get(name) ?: withContext(Dispatchers.IO) {
                runCatching {
                    BitmapFactory.decodeFile(ImageStore.file(context, name).path)?.asImageBitmap()
                }.getOrNull()
            }?.also { imageCache.put(name, it) }
        }
    }
    return bitmap
}
