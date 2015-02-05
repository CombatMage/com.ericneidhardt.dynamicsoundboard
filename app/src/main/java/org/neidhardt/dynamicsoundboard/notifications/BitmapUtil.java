package org.neidhardt.dynamicsoundboard.notifications;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

/**
 * Created by eric.neidhardt on 06.01.2015.
 */
class BitmapUtil
{

	static Point getBitmapSize(byte [] rawData)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(rawData, 0, rawData.length, options);
		int imageHeight = options.outHeight;
		int imageWidth = options.outWidth;
		return new Point(imageWidth, imageHeight);
	}

	static Point getBitmapSize(Resources resources, int drawableId)
	{
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(resources, drawableId, options);
		int imageHeight = options.outHeight;
		int imageWidth = options.outWidth;
		return new Point(imageWidth, imageHeight);
	}

	static int getSampleFactor(int width, int height, int requiredWidth, int requiredHeight)
	{
		int inSampleSize = 1;
		if (height > requiredHeight || width > requiredWidth)
		{
			final int halfHeight = height / 2;
			final int halfWidth = width / 2;

			// Calculate the largest inSampleSize value that is a power of 2 and keeps both
			// height and width larger than the requested height and width.
			while ((halfHeight / inSampleSize) >= requiredHeight
					&& (halfWidth / inSampleSize) >= requiredWidth)
				inSampleSize *= 2;
		}
		return inSampleSize;
	}

	static Bitmap getBitmap(byte [] rawData, int scaleFactor)
	{
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = scaleFactor;
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeByteArray(rawData, 0, rawData.length, options);
	}

	static Bitmap getBitmap(Context context, int drawableId, int scaleFactor)
	{
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = scaleFactor;
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeResource(context.getResources(), drawableId, options);
	}
}
