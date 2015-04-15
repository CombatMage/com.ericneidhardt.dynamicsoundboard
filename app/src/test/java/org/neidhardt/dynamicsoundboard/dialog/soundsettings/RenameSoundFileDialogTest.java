package org.neidhardt.dynamicsoundboard.dialog.soundsettings;

import org.junit.Test;
import org.mockito.Mockito;
import org.neidhardt.dynamicsoundboard.ActivityTest;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.playlist.Playlist;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Created by eric.neidhardt on 13.04.2015.
 */
public class RenameSoundFileDialogTest extends ActivityTest
{
	private RenameSoundFileDialog dialog;
	private MediaPlayerData testData;

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		this.testData = TestDataGenerator.getRandomPlayerData();
		RenameSoundFileDialog.showInstance(this.activity.getFragmentManager(), this.testData);

		this.dialog = (RenameSoundFileDialog) this.activity.getFragmentManager().findFragmentByTag(RenameSoundFileDialog.TAG);
		assertNotNull(this.dialog);
	}

	@Test
	public void testGetPlayersWithMatchingUri() throws Exception
	{
		this.service.addNewSoundToSoundsAndDatabase(TestDataGenerator.getRandomPlayerData());
		this.service.addNewSoundToSoundsAndDatabase(TestDataGenerator.getRandomPlayerData());

		MediaPlayerData data2 = TestDataGenerator.getRandomPlayerData();
		data2.setUri(this.testData.getUri());
		this.service.addNewSoundToPlaylistAndDatabase(data2);

		MediaPlayerData data3 = TestDataGenerator.getRandomPlayerData();
		data3.setUri(this.testData.getUri());
		this.service.addNewSoundToSoundsAndDatabase(data3);

		List<EnhancedMediaPlayer> players = this.dialog.getPlayersWithMatchingUri(this.testData.getUri());
		assertThat(players.size(), equalTo(2));

		assertTrue(players.get(0).getMediaPlayerData().getUri().equals(data2.getUri()) || players.get(1).getMediaPlayerData().getUri().equals(data2.getUri()));
		assertTrue(players.get(0).getMediaPlayerData().getUri().equals(data3.getUri()) || players.get(1).getMediaPlayerData().getUri().equals(data3.getUri()));
	}

	@Test
	public void testSetUriForPlayer() throws Exception
	{
		MediaPlayerData data = TestDataGenerator.getRandomPlayerData();
		EnhancedMediaPlayer testPlayer = new EnhancedMediaPlayer(data);

		assertTrue(this.dialog.setUriForPlayer(testPlayer, this.testData.getUri()));
		assertThat(testPlayer.getMediaPlayerData().getUri(), equalTo(this.testData.getUri()));

		MediaPlayerData testPlayerData = TestDataGenerator.getRandomPlayerData();
		testPlayer = mock(EnhancedMediaPlayer.class);
		Mockito.doThrow(new IOException()).when(testPlayer).setSoundUri(any(String.class));
		Mockito.when(testPlayer.getMediaPlayerData()).thenReturn(testPlayerData);

		List<EnhancedMediaPlayer> testSounds = new ArrayList<>();
		testSounds.add(testPlayer);
		this.service.getSounds().put(testPlayer.getMediaPlayerData().getFragmentTag(), testSounds);
		assertFalse(this.dialog.setUriForPlayer(testPlayer, this.testData.getUri())); // this should remove player from service

		assertThat(this.service.getSounds().get(testPlayer.getMediaPlayerData().getFragmentTag()).size(), equalTo(0));

		testPlayer.getMediaPlayerData().setFragmentTag(Playlist.TAG);
		this.service.getPlaylist().add(testPlayer);
		assertFalse(this.dialog.setUriForPlayer(testPlayer, this.testData.getUri())); // this should remove player from playlist

		assertTrue(this.service.getPlaylist().isEmpty());
	}

	@Test
	public void testGetFileName() throws Exception
	{
		final RenameSoundFileDialog dialog = spy(new RenameSoundFileDialog());


		boolean wasExceptionThrown = false;
		try {
			dialog.appendFileTypeToNewPath(null, null);
		}
		catch (NullPointerException e) {
			wasExceptionThrown = true;
		}
		assertTrue(wasExceptionThrown);

		String newFilePath = "/dir/new";
		String oldFilePath = "/dir/old.mp3";

		assertThat(dialog.appendFileTypeToNewPath(newFilePath, oldFilePath), equalTo("/dir/new.mp3"));

		oldFilePath = "/dir/old";
		assertThat(dialog.appendFileTypeToNewPath(newFilePath, oldFilePath), equalTo("/dir/new"));

		oldFilePath = "/dir/old.old.mp3";
		assertThat(dialog.appendFileTypeToNewPath(newFilePath, oldFilePath), equalTo("/dir/new.mp3"));
	}
}