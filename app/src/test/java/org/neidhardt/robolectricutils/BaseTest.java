package org.neidhardt.robolectricutils;

import android.os.Environment;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowEnvironment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertTrue;

/**
 * Created by eric.neidhardt on 18.04.2015.
 */
@RunWith(CustomTestRunner.class)
public abstract class BaseTest
{

	@Before
	public void setUp() throws Exception
	{
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
		// nothing to be done
	}

	@After
	public void tearDown() throws Exception
	{
		File[] files = ShadowEnvironment.getExternalStorageDirectory().listFiles();
		for(File file: files)
		{
			if (file.exists())
				assertTrue("could not delete file " + file, file.delete());
		}

		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_UNMOUNTED);
		// TODO clear shared preferences
	}

	protected File createFile(String fileName) throws Exception
	{
		File newFile = new File(ShadowEnvironment.getExternalStorageDirectory(), fileName);
		if (!newFile.exists())
			assertTrue(newFile.createNewFile());
		return newFile;
	}

	protected File getFileFromExternalStorage(String fileName)
	{
		return new File(ShadowEnvironment.getExternalStorageDirectory(), fileName);
	}
}
