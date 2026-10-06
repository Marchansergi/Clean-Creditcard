package io.github.marchansergi.cleancard.data

/**
 * Generic premium-looking styles. They are deliberately not copies of any real bank or
 * card network design (logos and trade dress of real brands are trademarks).
 */
data class Preset(
    val name: String,
    val colorStart: Long,
    val colorEnd: Long,
    val finish: Finish,
    val textTone: TextTone,
    val badge: String,
)

val Presets = listOf(
    Preset("Oro metálico", 0xFF9C7A3C, 0xFFF1D88F, Finish.BRUSHED_METAL, TextTone.DARK, "GOLD"),
    Preset("Negro titanio", 0xFF111111, 0xFF3B3B3D, Finish.BRUSHED_METAL, TextTone.LIGHT, "BLACK"),
    Preset("Platino", 0xFF8E9399, 0xFFE8EAEC, Finish.BRUSHED_METAL, TextTone.DARK, "PLATINUM"),
    Preset("Oro rosa", 0xFFB76E79, 0xFFF2C9BC, Finish.BRUSHED_METAL, TextTone.DARK, "ROSE"),
    Preset("Holográfico", 0xFFB9C6F0, 0xFFF0E6F6, Finish.HOLOGRAPHIC, TextTone.DARK, "HOLO"),
    Preset("Fibra de carbono", 0xFF0C0C0C, 0xFF2A2A2A, Finish.CARBON, TextTone.LIGHT, "CARBON"),
    Preset("Zafiro", 0xFF0B1D51, 0xFF3358A8, Finish.GLOSSY, TextTone.LIGHT, "SAPPHIRE"),
    Preset("Esmeralda", 0xFF0B3D2E, 0xFF1E7F5C, Finish.BRUSHED_METAL, TextTone.LIGHT, "EMERALD"),
    Preset("Medianoche", 0xFF0F2027, 0xFF2C5364, Finish.GLOSSY, TextTone.LIGHT, ""),
    Preset("Atardecer", 0xFFDD2476, 0xFFFF7A3D, Finish.GLOSSY, TextTone.LIGHT, ""),
)

fun CardDesign.withPreset(preset: Preset) = copy(
    colorStart = preset.colorStart,
    colorEnd = preset.colorEnd,
    finish = preset.finish,
    textTone = preset.textTone,
    badge = preset.badge,
    imageFile = null,
)

fun newCardDesign() = CardDesign(title = "Mi tarjeta").withPreset(Presets.first())
