package org.neidhardt.dynamicsoundboard.notifications

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point

/**
 * File created by eric.neidhardt on 06.01.2015.
 */
internal fun getBitmapSize(rawData: ByteArray): Point {
	val options = BitmapFactory.Options()
	options.inJustDecodeBounds = true
	BitmapFactory.decodeByteArray(rawData, 0, rawData.size(), options)
	val imageHeight = options.outHeight
	val imageWidth = options.outWidth
	return Point(imageWidth, imageHeight)
}

internal fun getBitmapSize(resources: Resources, drawableId: Int): Point {
	val options = BitmapFactory.Options()
	options.inJustDecodeBounds = true
	BitmapFactory.decodeResource(resources, drawableId, options)
	val imageHeight = options.outHeight
	val imageWidth = options.outWidth
	return Point(imageWidth, imageHeight)
}

internal fun getSampleFactor(width: Int, height: Int, requiredWidth: Int, requiredHeight: Int): Int {
	var inSampleSize = 1
	if (height > requiredHeight || width > requiredWidth) {
		val halfHeight = height / 2
		val halfWidth = width / 2

		// Calculate the largest inSampleSize value that is a power of 2 and keeps both
		// height and width larger than the requested height and width.
		while ((halfHeight / inSampleSize) >= requiredHeight && (halfWidth / inSampleSize) >= requiredWidth)
			inSampleSize *= 2
	}
	return inSampleSize
}

internal fun getBitmap(rawData: ByteArray, scaleFactor: Int): Bitmap
{
	val options = BitmapFactory.Options()
	options.inSampleSize = scaleFactor
	options.inJustDecodeBounds = false
	return BitmapFactory.decodeByteArray(rawData, 0, rawData.size(), options)
}

internal fun getBitmap(context: Context, drawableId: Int, scaleFactor: Int): Bitmap
{
	val options = BitmapFactory.Options()
	options.inSampleSize = scaleFactor
	options.inJustDecodeBounds = false
	return BitmapFactory.decodeResource(context.getResources(), drawableId, options)
}
