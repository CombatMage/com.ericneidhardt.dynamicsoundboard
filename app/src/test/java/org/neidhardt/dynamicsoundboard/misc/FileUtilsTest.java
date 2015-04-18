package org.neidhardt.dynamicsoundboard.misc;

import android.net.Uri;
import android.os.Environment;
import org.junit.Test;
import org.neidhardt.dynamicsoundboard.BaseActivityTest;

import java.io.File;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

/**
 * Created by eric.neidhardt on 14.04.2015.
 */
public class FileUtilsTest extends BaseActivityTest
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

	@SuppressWarnings("ConstantConditions")
	@Test
	public void testStripFileTypeFromName() throws Exception
	{
		String testName = null;
		boolean wasExceptionThrown = false;
		try
		{
			FileUtils.stripFileTypeFromName(testName);
		}
		catch (NullPointerException e) {
			wasExceptionThrown = true;
		}
		assertTrue(wasExceptionThrown);

		testName = "test";
		assertThat(FileUtils.stripFileTypeFromName(testName), equalTo("test"));

		testName = "test.mp3";
		assertThat(FileUtils.stripFileTypeFromName(testName), equalTo("test"));

		testName = "test.test.mp3";
		assertThat(FileUtils.stripFileTypeFromName(testName), equalTo("test.test"));
	}
}