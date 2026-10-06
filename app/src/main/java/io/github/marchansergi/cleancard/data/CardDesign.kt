package io.github.marchansergi.cleancard.data

import kotlinx.serialization.Serializable
import java.util.UUID

/** Surface finish drawn on top of the card background. */
@Serializable
enum class Finish { MATTE, GLOSSY, BRUSHED_METAL, HOLOGRAPHIC, CARBON }

@Serializable
enum class TextTone { LIGHT, DARK }

/**
 * A purely visual card design. It never contains real card data: [lastDigits] is an
 * optional decorative string of at most four digits.
 */
@Serializable
data class CardDesign(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val badge: String = "",
    val holderName: String = "",
    val lastDigits: String = "",
    val colorStart: Long = 0xFF1A1A1A,
    val colorEnd: Long = 0xFF3A3A3A,
    val finish: Finish = Finish.BRUSHED_METAL,
    val textTone: TextTone = TextTone.LIGHT,
    val showChip: Boolean = true,
    val showContactless: Boolean = true,
    /** File name inside the app's private image folder, see [ImageStore]. */
    val imageFile: String? = null,
)
