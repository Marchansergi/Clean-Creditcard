package io.github.marchansergi.cleancard.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/** Copies user picked images into private storage, downscaled, so no URI permission is kept. */
object ImageStore {
    private const val MAX_SIZE = 1600

    fun dir(context: Context) = File(context.filesDir, "card_images").apply { mkdirs() }

    fun file(context: Context, name: String) = File(dir(context), name)

    suspend fun import(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        runCatching {
            val resolver = context.contentResolver
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@runCatching null

            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_SIZE) sample *= 2
            val options = BitmapFactory.Options().apply { inSampleSize = sample }
            val decoded = resolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return@runCatching null

            val rotation = resolver.openInputStream(uri)?.use {
                when (ExifInterface(it).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90f
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180f
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270f
                    else -> 0f
                }
            } ?: 0f
            val bitmap = if (rotation == 0f) decoded else {
                Bitmap.createBitmap(decoded, 0, 0, decoded.width, decoded.height, Matrix().apply { postRotate(rotation) }, true)
            }

            val name = "${UUID.randomUUID()}.jpg"
            file(context, name).outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
            name
        }.getOrNull()
    }

    /** Deletes images that no saved card references any more. */
    fun deleteUnused(context: Context, used: Set<String>) {
        dir(context).listFiles()?.filter { it.name !in used }?.forEach { it.delete() }
    }
}
