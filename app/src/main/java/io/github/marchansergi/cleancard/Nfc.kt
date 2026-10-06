package io.github.marchansergi.cleancard

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.provider.Settings

enum class NfcStatus { READY, DISABLED, MISSING }

fun Context.nfcStatus(): NfcStatus {
    val adapter = NfcAdapter.getDefaultAdapter(this) ?: return NfcStatus.MISSING
    return if (adapter.isEnabled) NfcStatus.READY else NfcStatus.DISABLED
}

/** Opens the first settings screen of [actions] that exists on this device. */
fun Context.openSettings(vararg actions: String) {
    for (action in listOf(*actions, Settings.ACTION_SETTINGS)) {
        try {
            startActivity(Intent(action))
            return
        } catch (e: ActivityNotFoundException) {
            // Try the next one: some vendors remove specific settings screens.
        }
    }
}

fun Context.openNfcSettings() = openSettings(Settings.ACTION_NFC_SETTINGS)

/** Screen where the user picks the default contactless payment app (e.g. Google Wallet). */
fun Context.openPaymentSettings() = openSettings(Settings.ACTION_NFC_PAYMENT_SETTINGS, Settings.ACTION_NFC_SETTINGS)
