package io.github.marchansergi.cleancard

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.marchansergi.cleancard.data.CardRepository
import io.github.marchansergi.cleancard.ui.CleanCardTheme
import io.github.marchansergi.cleancard.ui.PayScreen

/**
 * Shows a card design full screen. It does not take part in the payment: the default NFC
 * payment app (e.g. Google Wallet) answers the terminal in the background as usual.
 */
class PayActivity : ComponentActivity() {
    private var cardId by mutableStateOf<String?>(null)
    private var nfcStatus by mutableStateOf(NfcStatus.READY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        window.attributes = window.attributes.apply {
            screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_FULL
        }
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        cardId = intent.getStringExtra(EXTRA_CARD_ID)

        val repository = CardRepository.get(this)
        setContent {
            CleanCardTheme {
                val cards by repository.cards.collectAsStateWithLifecycle()
                val defaultId by repository.defaultId.collectAsStateWithLifecycle()
                val card = cards.firstOrNull { it.id == cardId }
                    ?: cards.firstOrNull { it.id == defaultId }
                    ?: cards.firstOrNull()
                PayScreen(
                    card = card,
                    nfcStatus = nfcStatus,
                    onClose = { finish() },
                    onCreate = {
                        startActivity(MainActivity.createIntent(this))
                        finish()
                    },
                    onEnableNfc = { openNfcSettings() },
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        cardId = intent.getStringExtra(EXTRA_CARD_ID)
    }

    override fun onResume() {
        super.onResume()
        nfcStatus = nfcStatus()
    }

    companion object {
        private const val EXTRA_CARD_ID = "card_id"

        fun intent(context: Context, cardId: String?): Intent =
            Intent(context, PayActivity::class.java).putExtra(EXTRA_CARD_ID, cardId)
    }
}
