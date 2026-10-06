package io.github.marchansergi.cleancard.ui

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import io.github.marchansergi.cleancard.R
import io.github.marchansergi.cleancard.data.CardDesign
import io.github.marchansergi.cleancard.data.CardRepository
import io.github.marchansergi.cleancard.data.Finish
import io.github.marchansergi.cleancard.data.ImageStore
import io.github.marchansergi.cleancard.data.Preset
import io.github.marchansergi.cleancard.data.Presets
import io.github.marchansergi.cleancard.data.TextTone
import io.github.marchansergi.cleancard.data.withPreset
import kotlinx.coroutines.launch

private val CardDesignSaver = Saver<CardDesign, String>(
    save = { CardRepository.json.encodeToString(it) },
    restore = { CardRepository.json.decodeFromString<CardDesign>(it) },
)

private enum class ColorSlot { START, END }

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditorScreen(
    initial: CardDesign,
    isNew: Boolean,
    onSave: (CardDesign) -> Unit,
    onClose: () -> Unit,
) {
    var draft by rememberSaveable(stateSaver = CardDesignSaver) { mutableStateOf(initial) }
    var confirmDiscard by rememberSaveable { mutableStateOf(false) }
    var colorSlot by rememberSaveable { mutableStateOf<ColorSlot?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }
    val imageError = stringResource(R.string.editor_image_error)

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            scope.launch {
                val name = ImageStore.import(context, uri)
                if (name != null) draft = draft.copy(imageFile = name) else snackbar.showSnackbar(imageError)
            }
        }
    }

    fun requestClose() {
        if (draft != initial) confirmDiscard = true else onClose()
    }
    BackHandler { requestClose() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (isNew) R.string.editor_new else R.string.editor_edit)) },
                navigationIcon = {
                    IconButton(onClick = { requestClose() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.editor_back))
                    }
                },
                actions = {
                    TextButton(onClick = { onSave(draft.normalized()) }) { Text(stringResource(R.string.editor_save)) }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding(),
        ) {
            CardFace(
                design = draft,
                modifier = Modifier
                    .padding(horizontal = 20.dp, vertical = 12.dp)
                    .widthIn(max = 480.dp)
                    .fillMaxWidth(),
            )
            Column(
                Modifier
                    .weight(1f)
                    .widthIn(max = 640.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .navigationBarsPadding()
            ) {
                Section(stringResource(R.string.editor_section_style)) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(vertical = 4.dp),
                    ) {
                        items(Presets) { preset ->
                            PresetThumbnail(
                                preset = preset,
                                selected = draft.matches(preset),
                                onClick = { draft = draft.withPreset(preset) },
                            )
                        }
                    }
                }

                Section(stringResource(R.string.editor_section_background)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {
                            pickImage.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }) { Text(stringResource(R.string.editor_pick_image)) }
                        if (draft.imageFile != null) {
                            TextButton(onClick = { draft = draft.copy(imageFile = null) }) {
                                Text(stringResource(R.string.editor_remove_image))
                            }
                        }
                    }
                    if (draft.imageFile == null) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            ColorButton(stringResource(R.string.editor_color_start), Color(draft.colorStart)) {
                                colorSlot = ColorSlot.START
                            }
                            ColorButton(stringResource(R.string.editor_color_end), Color(draft.colorEnd)) {
                                colorSlot = ColorSlot.END
                            }
                        }
                        Text(stringResource(R.string.editor_finish), style = MaterialTheme.typography.labelLarge)
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Finish.entries.forEach { finish ->
                                FilterChip(
                                    selected = draft.finish == finish,
                                    onClick = { draft = draft.copy(finish = finish) },
                                    label = { Text(stringResource(finish.label)) },
                                )
                            }
                        }
                    }
                }

                Section(stringResource(R.string.editor_section_texts)) {
                    OutlinedTextField(
                        value = draft.title,
                        onValueChange = { draft = draft.copy(title = it.take(24)) },
                        label = { Text(stringResource(R.string.editor_title)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = draft.badge,
                        onValueChange = { draft = draft.copy(badge = it.take(12)) },
                        label = { Text(stringResource(R.string.editor_badge)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = draft.holderName,
                        onValueChange = { draft = draft.copy(holderName = it.take(26)) },
                        label = { Text(stringResource(R.string.editor_holder)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.fillMaxWidth(),
                    )
                    OutlinedTextField(
                        value = draft.lastDigits,
                        onValueChange = { value -> draft = draft.copy(lastDigits = value.filter { it.isDigit() }.take(4)) },
                        label = { Text(stringResource(R.string.editor_last_digits)) },
                        supportingText = { Text(stringResource(R.string.editor_last_digits_help)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                Section(stringResource(R.string.editor_section_details)) {
                    SwitchRow(stringResource(R.string.editor_show_chip), draft.showChip) {
                        draft = draft.copy(showChip = it)
                    }
                    SwitchRow(stringResource(R.string.editor_show_contactless), draft.showContactless) {
                        draft = draft.copy(showContactless = it)
                    }
                    Text(stringResource(R.string.editor_text_tone), style = MaterialTheme.typography.labelLarge)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = draft.textTone == TextTone.LIGHT,
                            onClick = { draft = draft.copy(textTone = TextTone.LIGHT) },
                            label = { Text(stringResource(R.string.editor_text_light)) },
                        )
                        FilterChip(
                            selected = draft.textTone == TextTone.DARK,
                            onClick = { draft = draft.copy(textTone = TextTone.DARK) },
                            label = { Text(stringResource(R.string.editor_text_dark)) },
                        )
                    }
                }
                Spacer(Modifier.height(32.dp))
            }
        }
    }

    colorSlot?.let { slot ->
        val current = if (slot == ColorSlot.START) draft.colorStart else draft.colorEnd
        ColorPickerDialog(
            initial = Color(current),
            onDismiss = { colorSlot = null },
            onPick = { picked ->
                val argb = picked.toArgb().toLong() and 0xFFFFFFFFL
                draft = if (slot == ColorSlot.START) draft.copy(colorStart = argb) else draft.copy(colorEnd = argb)
                colorSlot = null
            },
        )
    }

    if (confirmDiscard) {
        AlertDialog(
            onDismissRequest = { confirmDiscard = false },
            title = { Text(stringResource(R.string.editor_discard_title)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmDiscard = false
                    onClose()
                }) { Text(stringResource(R.string.editor_discard_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDiscard = false }) { Text(stringResource(R.string.editor_keep_editing)) }
            },
        )
    }
}

private val Finish.label
    get() = when (this) {
        Finish.MATTE -> R.string.finish_matte
        Finish.GLOSSY -> R.string.finish_glossy
        Finish.BRUSHED_METAL -> R.string.finish_brushed
        Finish.HOLOGRAPHIC -> R.string.finish_holographic
        Finish.CARBON -> R.string.finish_carbon
    }

private fun CardDesign.matches(preset: Preset) =
    imageFile == null && colorStart == preset.colorStart && colorEnd == preset.colorEnd && finish == preset.finish

private fun CardDesign.normalized() = copy(
    title = title.trim(),
    badge = badge.trim(),
    holderName = holderName.trim(),
    lastDigits = lastDigits.filter { it.isDigit() }.take(4),
)

@Composable
private fun Section(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        Text(title.uppercase(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
        content()
    }
}

@Composable
private fun PresetThumbnail(preset: Preset, selected: Boolean, onClick: () -> Unit) {
    val design = remember(preset) { CardDesign(id = preset.name, showContactless = false).withPreset(preset).copy(badge = "") }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(112.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        Box(
            if (selected) {
                Modifier.border(BorderStroke(2.dp, MaterialTheme.colorScheme.primary), RoundedCornerShape(8.dp)).padding(3.dp)
            } else {
                Modifier.padding(3.dp)
            }
        ) {
            CardFace(design, Modifier.fillMaxWidth())
        }
        Text(
            preset.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

@Composable
private fun ColorButton(label: String, color: Color, onClick: () -> Unit) {
    OutlinedButton(onClick = onClick) {
        Box(
            Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(color)
                .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
        )
        Text(label, modifier = Modifier.padding(start = 8.dp))
    }
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onChange)
            .padding(vertical = 4.dp),
    ) {
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = null)
    }
}
