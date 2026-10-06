package io.github.marchansergi.cleancard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.marchansergi.cleancard.data.CardRepository
import io.github.marchansergi.cleancard.data.newCardDesign
import io.github.marchansergi.cleancard.ui.CleanCardTheme
import io.github.marchansergi.cleancard.ui.EditorScreen
import io.github.marchansergi.cleancard.ui.HomeScreen

class MainActivity : ComponentActivity() {
    private var nfcStatus by mutableStateOf(NfcStatus.READY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = CardRepository.get(this)
        val openEditor = intent.getBooleanExtra(EXTRA_CREATE, false)

        setContent {
            CleanCardTheme {
                var editorOpen by rememberSaveable { mutableStateOf(openEditor) }
                var editingId by rememberSaveable { mutableStateOf<String?>(null) }

                if (editorOpen) {
                    val initial = remember(editingId) { repository.find(editingId) ?: newCardDesign() }
                    EditorScreen(
                        initial = initial,
                        isNew = repository.find(initial.id) == null,
                        onSave = {
                            repository.save(it)
                            editorOpen = false
                        },
                        onClose = { editorOpen = false },
                    )
                } else {
                    HomeScreen(
                        repository = repository,
                        nfcStatus = nfcStatus,
                        onPay = { startActivity(PayActivity.intent(this, it.id)) },
                        onEdit = {
                            editingId = it.id
                            editorOpen = true
                        },
                        onCreate = {
                            editingId = null
                            editorOpen = true
                        },
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        nfcStatus = nfcStatus()
    }

    companion object {
        private const val EXTRA_CREATE = "create"

        fun createIntent(context: Context): Intent =
            Intent(context, MainActivity::class.java).putExtra(EXTRA_CREATE, true)
    }
}
