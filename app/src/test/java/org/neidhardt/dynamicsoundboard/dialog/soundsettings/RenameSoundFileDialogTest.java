package org.neidhardt.dynamicsoundboard.dialog.soundsettings;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.ActivityTest;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
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

		this.service.addNewSoundToSoundsAndDatabase(TestDataGenerator.getRandomPlayerData());
		this.service.addNewSoundToSoundsAndDatabase(TestDataGenerator.getRandomPlayerData());

		MediaPlayerData data2 = TestDataGenerator.getRandomPlayerData();
		data2.setUri(data.getUri());
		this.service.addNewSoundToPlaylistAndDatabase(data2);

		MediaPlayerData data3 = TestDataGenerator.getRandomPlayerData();
		data3.setUri(data.getUri());
		this.service.addNewSoundToSoundsAndDatabase(data3);

		List<EnhancedMediaPlayer> players = dialog.getPlayersWithMatchingUri(data.getUri());
		assertThat(players.size(), equalTo(2));

		assertTrue(players.get(0).getMediaPlayerData().getUri().equals(data2.getUri()) || players.get(1).getMediaPlayerData().getUri().equals(data2.getUri()));
		assertTrue(players.get(0).getMediaPlayerData().getUri().equals(data3.getUri()) || players.get(1).getMediaPlayerData().getUri().equals(data3.getUri()));
	}
}