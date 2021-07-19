package com.example.maps.utils

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.ImageView
import androidx.core.content.FileProvider
import com.example.maps.R
import kotlinx.android.synthetic.main.spot_details.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

data class Picture(val uri: Uri, val name: String)

fun getPhotoFile(context: Context, fileName: String): File {
    val directoryStorage = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
    return File(directoryStorage, fileName)
}

fun setImage(context: Context, filename: String, image: ImageView) {
    if(!fileExists(context, filename, Environment.DIRECTORY_PICTURES)) {
        image.setImageResource(R.drawable.ic_launcher_background)
        return
    }
    //File object of camera image
    val file = getPhotoFile(context, filename)

    //Uri of camera image
    val uri = FileProvider.getUriForFile(
        context,
        context.applicationContext.packageName + ".provider",
        file
    )
    if (uri != null){
        val bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), uri)
        image.setImageBitmap(bitmap)
    } else {
        image.setImageResource(R.drawable.ic_launcher_background)
    }
}
    // Checks if a volume containing external storage is available
    // for read and write.
    fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    // Checks if a volume containing external storage is available to at least read.
    fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }


    fun readExternalFile(context: Context, filename: String): String {
        var content = ""
        if (isExternalStorageReadable()) {
            val externalFile = File(context.getExternalFilesDir(null), filename)
            val externalInputStream = FileInputStream(externalFile)
            content = externalInputStream.bufferedReader().useLines { lines ->
                lines.fold("") { some, text ->
                    "$some\n$text"
                }
            }

        }
        return content
    }

    fun writeExternalFile(context: Context, filename: String, content: String): Unit {
        if (isExternalStorageWritable()) {
            val externalFile = File(context.getExternalFilesDir(null), filename)
            val externalOutputStream = FileOutputStream(externalFile)
            externalOutputStream.use {
                it.write(content.toByteArray())

            }
        }
    }
    fun fileExists(context: Context, filename: String, type: String?): Boolean {
        if(isExternalStorageReadable()) {
            val externalFile = File(context.getExternalFilesDir(type), filename)
            return externalFile.exists()
        }
        return false
    }
