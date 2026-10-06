package com.example.lesson4.utils

import android.content.Context
import android.net.Uri
import android.widget.ImageView
import androidx.core.net.toUri
import coil.load
import coil.transform.CircleCropTransformation
import com.example.lesson4.R
import java.io.File
import java.io.FileOutputStream

object ImageUtils {

    fun loadAvatar(imageView: ImageView, avatarUriString: String?) {
        if (avatarUriString.isNullOrBlank()) {
            imageView.setImageResource(R.drawable.ic_default_avatar)
            return
        }

        val dataToLoad: Any = if (avatarUriString.startsWith("http://") || avatarUriString.startsWith("https://")) {
            avatarUriString
        } else {
            avatarUriString.toUri()
        }

        imageView.load(dataToLoad) {
            crossfade(true)
            placeholder(R.drawable.ic_default_avatar)
            error(R.drawable.ic_default_avatar)
            transformations(CircleCropTransformation())
        }
    }

    fun copyUriToAppStorage(context: Context, sourceUri: Uri): String? {
        return try {
            val contentResolver = context.contentResolver
            val avatarDir = File(context.filesDir, "avatars")
            if (!avatarDir.exists()) {
                avatarDir.mkdirs()
            }

            val fileName = "avatar_${System.currentTimeMillis()}.jpg"
            val destFile = File(avatarDir, fileName)

            contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            Uri.fromFile(destFile).toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}