package org.neidhardt.dynamicsoundboard.dialog.soundsettings;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.ActivityTest;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;

import static org.junit.Assert.*;

/**
 * Created by eric.neidhardt on 13.04.2015.
 */
public class RenameSoundFileDialogTest extends ActivityTest
{

	@Test
	public void testGetPlayersWithMatchingUri() throws Exception
	{
		MediaPlayerData data = TestDataGenerator.getRandomPlayerData();
		RenameSoundFileDialog.showInstance(this.activity.getFragmentManager(), data);

		RenameSoundFileDialog dialog = (RenameSoundFileDialog) this.activity.getFragmentManager().findFragmentByTag(RenameSoundFileDialog.TAG);
		assertNotNull(dialog);

		//TODO write test
	}
}