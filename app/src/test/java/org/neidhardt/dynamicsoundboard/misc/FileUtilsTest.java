package org.neidhardt.dynamicsoundboard.misc;

import android.net.Uri;
import android.os.Environment;
import org.junit.Test;
import org.neidhardt.dynamicsoundboard.ActivityTest;

import java.io.File;

import static org.junit.Assert.*;

/**
 * Created by eric.neidhardt on 14.04.2015.
 */
public class FileUtilsTest extends ActivityTest
{

	@Test
	public void testGetFileForUri() throws Exception
	{
		File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC), "test.mp3");
		assertTrue(!file.exists());
		File testFile = FileUtils.getFileForUri(this.activity, Uri.fromFile(file));
		assertNull(testFile);

		assertTrue(file.createNewFile());

		testFile = FileUtils.getFileForUri(this.activity, Uri.fromFile(file));
		assertNotNull(testFile);
		assertEquals(file.getAbsolutePath(), testFile.getAbsolutePath());
		assertEquals(file, testFile);
	}

	@Test
	public void testStripFileTypeFromName() throws Exception
	{
		// TODO write test
	}
}