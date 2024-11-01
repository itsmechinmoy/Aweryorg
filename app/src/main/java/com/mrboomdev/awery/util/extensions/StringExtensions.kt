package com.mrboomdev.awery.util.extensions

import android.net.Uri
import android.provider.OpenableColumns
import androidx.media3.common.MimeTypes
import com.mrboomdev.awery.app.AweryLifecycle.getAppContext
import java.io.File
import java.net.MalformedURLException
import java.net.URISyntaxException
import java.net.URL
import kotlin.math.min

private val REMOVE_LAST_URL_CHARS = arrayOf("/", "?", "#", "&", " ")
private const val FILE_SIZE_BORDER = 1000 / 1024

fun Long.formatFileSize(): String {
	val kb = this / 1024
	val mb = kb / 1024
	val gb = mb / 1024

	if(gb > FILE_SIZE_BORDER) return "$gb gb"
	if(mb > FILE_SIZE_BORDER) return "$mb mb"
	if(kb > FILE_SIZE_BORDER) return "$kb kb"

	return "$gb b"
}

fun Uri.parseMimeType(): String {
	if(scheme == "content") {
		getAppContext().contentResolver.query(
			this, null, null, null, null
		)?.use { cursor ->
			return cursor.getString(cursor.getColumnIndexOrThrow(
				OpenableColumns.DISPLAY_NAME
			)).parseMimeType(true)
		}
	}

	return (lastPathSegment ?: toString().let {
		it.substring(it.lastIndexOf("/") + 1)
	}).parseMimeType(true)
}

fun File.parseMimeType(): String {
	return name.parseMimeType(true)
}

fun String.parseMimeType(): String {
	return parseMimeType(false)
}

private fun String.parseMimeType(isName: Boolean): String {
	var fileName = if(isName) this
	else File(this).name

	if(fileName.contains("#")) {
		fileName = fileName.substring(0, fileName.indexOf("#"))
	}

	if(fileName.contains("?")) {
		fileName = fileName.substring(0, fileName.indexOf("?"))
	}

	if(fileName.contains("/")) {
		fileName = fileName.substring(0, fileName.indexOf("/"))
	}

	return when(val ext = fileName.substring(fileName.lastIndexOf(".") + 1)) {
		"vtt" -> MimeTypes.TEXT_VTT
		"srt" -> MimeTypes.APPLICATION_SUBRIP
		"scc" -> MimeTypes.APPLICATION_CEA708
		"ts" -> MimeTypes.APPLICATION_DVBSUBS
		"mka" -> MimeTypes.APPLICATION_MATROSKA
		"wvtt" -> MimeTypes.APPLICATION_MP4VTT
		"pgs" -> MimeTypes.APPLICATION_PGS
		"rtsp" -> MimeTypes.APPLICATION_RTSP
		"ass", "ssa" -> MimeTypes.APPLICATION_SS
		"ttml", "xml", "dfxp" -> MimeTypes.APPLICATION_TTML
		"tx3g" -> MimeTypes.APPLICATION_TX3G
		"idx", "sub" -> MimeTypes.APPLICATION_VOBSUB
		else -> throw IllegalArgumentException("Unknown mime type! $ext")
	}
}

fun String.isValidUrl(): Boolean {
	if(isBlank()) {
		return false
	}

	try {
		URL(this).toURI()
		return true
	} catch(e: URISyntaxException) {
		return false
	} catch(e: MalformedURLException) {
		return false
	}
}

fun String.cleanUrl(): String {
	var url = this

	loop@ while(true) {
		for(character in REMOVE_LAST_URL_CHARS) {
			if(url.endsWith(character)) {
				url = url.substring(0, url.length - 1)
				continue@loop
			}
		}

		break
	}

	return url
}

fun String.removeIndent(): String {
	val builder = StringBuilder()
	val iterator = lines().iterator()

	while(iterator.hasNext()) {
		builder.append(iterator.next().trim())

		if(iterator.hasNext()) {
			builder.append("\n")
		}
	}

	return builder.toString()
}