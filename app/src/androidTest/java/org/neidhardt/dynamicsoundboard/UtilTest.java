package org.neidhardt.dynamicsoundboard;

import android.os.Environment;
import android.test.InstrumentationTestCase;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;

import java.io.File;

/**
 * Project created by Eric Neidhardt on 07.10.2014.
 */
public class UtilTest extends InstrumentationTestCase
{
	public void testGetFileExtension()
	{
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "test.mp3");
		String extension = FileUtils.getFileExtension(file.getAbsolutePath());
		assertEquals("mp3", extension);

		file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "test - test.mp3");
		extension = FileUtils.getFileExtension(file.getAbsolutePath());
		assertEquals("mp3", extension);
	}

	public void testGetMImeType()
	{
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "test.mp3");
		String mime = FileUtils.getMimeType(file.getAbsolutePath());
		assertTrue(mime.startsWith("audio"));
	}

	public void testIsAudioFile()
	{
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "test.mp3");
		assertTrue(FileUtils.isAudioFile(file));
	}
}
