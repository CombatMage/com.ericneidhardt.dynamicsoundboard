package robolectricutils;

import android.os.Environment;
import org.apache.maven.artifact.ant.shaded.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.robolectric.shadows.ShadowEnvironment;

import java.io.File;

import static junit.framework.Assert.assertTrue;

/**
 * File created by eric.neidhardt on 16.04.2015.
 */
@SuppressWarnings("deprecation")
@RunWith(CustomTestRunner.class)
public abstract class BaseRobolectricTest {

	@Before
	public void setUp() throws Exception
	{
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_MOUNTED);
	}

	@After
	public void tearDown() throws Exception {
		FileUtils.cleanDirectory(Environment.getExternalStorageDirectory());
		ShadowEnvironment.setExternalStorageState(Environment.MEDIA_UNMOUNTED);
	}

	protected File createFile(String fileName) throws Exception {
		File newFile = new File(ShadowEnvironment.getExternalStorageDirectory(), fileName);
		if (!newFile.exists())
			assertTrue(newFile.createNewFile());
		return newFile;
	}

	protected File getFileFromExternalStorage(String fileName) {
		return new File(ShadowEnvironment.getExternalStorageDirectory(), fileName);
	}

}
