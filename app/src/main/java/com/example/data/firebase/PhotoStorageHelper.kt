package com.example.data.firebase

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class PhotoStorageHelper(context: Context) {
    private val appContext = context.applicationContext
    private val photosDir: File
        get() = File(appContext.filesDir, "baby_photos").apply {
            if (!exists()) mkdirs()
        }

    fun resolvePhotoFile(fileName: String?): File? {
        if (fileName.isNullOrBlank()) return null
        val sanitized = File(fileName).name
        val file = File(photosDir, sanitized)
        return if (file.exists() && file.length() > 0L) file else null
    }

    suspend fun savePickedPhoto(uri: Uri, prefix: String = "photo"): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val resolver = appContext.contentResolver
                val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, boundsOpts)
                } ?: return@withContext Result.failure(IllegalStateException("ছবিটি পড়া যায়নি"))

                val maxDim = 1280
                var sampleSize = 1
                while (boundsOpts.outWidth / sampleSize > maxDim * 2 ||
                    boundsOpts.outHeight / sampleSize > maxDim * 2
                ) {
                    sampleSize *= 2
                }

                val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
                val decoded = resolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream, null, decodeOpts)
                } ?: return@withContext Result.failure(IllegalStateException("ছবিটি ডিকোড করা যায়নি"))

                val scaled = scaleDownIfNeeded(decoded, maxDim)
                val fileName = "${prefix}_${UUID.randomUUID().toString().take(12)}.jpg"
                val outFile = File(photosDir, fileName)
                FileOutputStream(outFile).use { out ->
                    scaled.compress(Bitmap.CompressFormat.JPEG, 84, out)
                }
                if (scaled !== decoded) {
                    scaled.recycle()
                }
                decoded.recycle()
                Result.success(fileName)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun deletePhotoFile(fileName: String?) = withContext(Dispatchers.IO) {
        if (fileName.isNullOrBlank()) return@withContext
        val file = File(photosDir, File(fileName).name)
        if (file.exists()) {
            file.delete()
        }
    }

    private fun scaleDownIfNeeded(bitmap: Bitmap, maxDim: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        if (width <= maxDim && height <= maxDim) return bitmap
        val ratio = minOf(maxDim.toFloat() / width, maxDim.toFloat() / height)
        val newW = (width * ratio).toInt().coerceAtLeast(1)
        val newH = (height * ratio).toInt().coerceAtLeast(1)
        return Bitmap.createScaledBitmap(bitmap, newW, newH, true)
    }
}
