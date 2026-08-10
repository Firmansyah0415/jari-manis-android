package com.jarimanis.jarimanis.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream

// Fungsi pembantu untuk menyalin gambar dari Galeri (Uri) menjadi File sementara (Cache) agar bisa dikirim
fun uriToFile(context: Context, uri: Uri): File? {
    return try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null
        // Membuat file sementara dengan format .jpg
        val tempFile = File.createTempFile("profil_", ".jpg", context.cacheDir)
        val outputStream = FileOutputStream(tempFile)

        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()
        tempFile
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}