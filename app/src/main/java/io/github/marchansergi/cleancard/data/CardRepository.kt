package io.github.marchansergi.cleancard.data

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json

/** Stores card designs locally in SharedPreferences. Nothing leaves the device. */
class CardRepository private constructor(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("cards", Context.MODE_PRIVATE)

    private val _cards = MutableStateFlow(load())
    val cards: StateFlow<List<CardDesign>> = _cards.asStateFlow()

    private val _defaultId = MutableStateFlow(prefs.getString(KEY_DEFAULT, null))
    val defaultId: StateFlow<String?> = _defaultId.asStateFlow()

    init {
        // Drops images left behind by an editor session that was never saved.
        cleanImages()
    }

    fun find(id: String?): CardDesign? = id?.let { wanted -> _cards.value.firstOrNull { it.id == wanted } }

    fun defaultCard(): CardDesign? = find(_defaultId.value) ?: _cards.value.firstOrNull()

    fun save(card: CardDesign) {
        val current = _cards.value
        _cards.value = if (current.any { it.id == card.id }) {
            current.map { if (it.id == card.id) card else it }
        } else {
            current + card
        }
        if (find(_defaultId.value) == null) setDefault(card.id)
        persist()
        cleanImages()
    }

    fun delete(id: String) {
        _cards.value = _cards.value.filterNot { it.id == id }
        if (_defaultId.value == id) setDefault(_cards.value.firstOrNull()?.id)
        persist()
        cleanImages()
    }

    fun setDefault(id: String?) {
        _defaultId.value = id
        prefs.edit().putString(KEY_DEFAULT, id).apply()
    }

    private fun load(): List<CardDesign> {
        val raw = prefs.getString(KEY_CARDS, null) ?: return emptyList()
        return runCatching { json.decodeFromString<List<CardDesign>>(raw) }.getOrDefault(emptyList())
    }

    private fun persist() {
        prefs.edit().putString(KEY_CARDS, json.encodeToString(_cards.value)).apply()
    }

    private fun cleanImages() {
        ImageStore.deleteUnused(appContext, _cards.value.mapNotNull { it.imageFile }.toSet())
    }

    companion object {
        private const val KEY_CARDS = "cards"
        private const val KEY_DEFAULT = "default_id"

        val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        @Volatile
        private var instance: CardRepository? = null

        fun get(context: Context): CardRepository =
            instance ?: synchronized(this) {
                instance ?: CardRepository(context).also { instance = it }
            }
    }
}
