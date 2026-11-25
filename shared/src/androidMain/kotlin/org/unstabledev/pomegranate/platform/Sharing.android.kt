package org.unstabledev.pomegranate.platform

import android.app.Activity
import android.content.ClipData
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import java.io.File

data class SharingOptions(
    val dialogTitle: String?=null,
    val mimeType: String?=null,
    val previewData: String?=null,
)

actual class Sharing {
    actual fun text(text: String) {
        val options=SharingOptions()
        val context=androidContext ?: return

        val intent=Intent(Intent.ACTION_SEND).apply {
            setTypeAndNormalize("text/plain")
            putExtra(Intent.EXTRA_TEXT, text)

            options.dialogTitle?.let { title ->
                putExtra(Intent.EXTRA_TITLE, title)
            }

            options.previewData?.let { previewData ->
                val previewUri=resolvePreviewUri(previewData)
                clipData=ClipData.newRawUri(null, previewUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        }

        val chooser=Intent.createChooser(intent, options.dialogTitle ?: "Share").apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        context.startActivity(chooser)
    }

    private fun resolvePreviewUri(previewData: String): Uri {
        val parsed=previewData.toUri()
        return if (parsed.scheme == null || parsed.scheme == "file") {
            val file=File(parsed.path ?: previewData)
            FileProvider.getUriForFile(androidContext!!, "${androidContext!!.packageName}.fileprovider", file)
        } else parsed
    }

    actual fun file(uri: String) {
        val options=SharingOptions()
        val context=androidContext ?: return
        val formattedUri =
            if (uri.startsWith("/")) Uri.fromFile(File(uri)).toString() 
            else uri
        val file=getLocalFileFromUrl(formattedUri)

        val contentUri=FileProvider.getUriForFile(
            context, "${context.packageName}.fileprovider", file)
        val mimeType=context.contentResolver.getType(contentUri) ?: "*/*"

        val intent=Intent(Intent.ACTION_SEND).apply {
            setTypeAndNormalize(mimeType)
            putExtra(Intent.EXTRA_STREAM, contentUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            clipData=ClipData.newRawUri(options.dialogTitle, contentUri)

            options.dialogTitle?.let { title ->
                putExtra(Intent.EXTRA_TITLE, title)
            }
        }

        val chooser=Intent.createChooser(intent, options.dialogTitle ?: "Share").apply {
            if (context !is Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }

        context.startActivity(chooser)
    }

}

private fun getLocalFileFromUrl(url: String): File {
    val uri=url.toUri()
    require(uri.scheme == "file") {
        "Only local file URLs are supported (expected scheme to be 'file', got '${uri.scheme}')."
    }

    val path=uri.path
    requireNotNull(path) { "Path component of the URL to share cannot be null." }

    val file=File(path)
    require(file.exists()) { "File does not exist: $path" }

    return file
}