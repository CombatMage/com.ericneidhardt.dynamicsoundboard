package org.neidhardt.dynamicsoundboard.dialog.soundsettings;

import android.net.Uri;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.neidhardt.dynamicsoundboard.AbstractBaseActivityTest;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.AddNewSoundEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.views.RenameSoundFileDialog;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Created by eric.neidhardt on 13.04.2015.
 */
public class RenameSoundFileDialogTest extends AbstractBaseActivityTest
{
	private RenameSoundFileDialog dialog;
	private MediaPlayerData testData;

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		this.testData = TestDataGenerator.getRandomPlayerDataForPlayList();
		this.service.onEvent(new PlaylistLoadedEvent(this.testData, false));

		RenameSoundFileDialog.showInstance(this.activity.getFragmentManager(), this.testData);
		this.activity.getFragmentManager().executePendingTransactions();
		this.dialog = (RenameSoundFileDialog) this.activity.getFragmentManager().findFragmentByTag(RenameSoundFileDialog.TAG);

		assertNotNull(this.dialog);
		assertNotNull(this.dialog.getActivity());
		assertSame(activity, this.dialog.getActivity());
	}

	@Override
	@After
	public void tearDown() throws Exception
	{
		this.dialog.dismiss();
		super.tearDown();
	}

	@Test
	public void testGetPlayersWithMatchingUri() throws Exception
	{
		this.service.onEvent(new AddNewSoundEvent(TestDataGenerator.getRandomPlayerData(), false));
		this.service.onEvent(new AddNewSoundEvent(TestDataGenerator.getRandomPlayerData(), false));

		MediaPlayerData data2 = TestDataGenerator.getRandomPlayerData();
		data2.setUri(this.testData.getUri());
		this.service.onEvent(new PlaylistLoadedEvent(data2, false));

		MediaPlayerData data3 = TestDataGenerator.getRandomPlayerData();
		data3.setUri(this.testData.getUri());
		this.service.onEvent(new AddNewSoundEvent(data3, false));

		List<EnhancedMediaPlayer> players = this.dialog.getPlayersWithMatchingUri(this.testData.getUri());
		assertThat(players.size(), equalTo(3)); // this.testData + data2 + data3

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
		when(testPlayer.getMediaPlayerData()).thenReturn(testPlayerData);

		List<EnhancedMediaPlayer> testSounds = new ArrayList<>();
		testSounds.add(testPlayer);
		this.service.getSounds().put(testPlayer.getMediaPlayerData().getFragmentTag(), testSounds);
		assertFalse(this.dialog.setUriForPlayer(testPlayer, this.testData.getUri())); // this should remove player from service

		assertThat(this.service.getSounds().get(testPlayer.getMediaPlayerData().getFragmentTag()).size(), equalTo(0));

		testPlayer.getMediaPlayerData().setFragmentTag(Playlist.TAG);
		this.service.getPlaylist().add(testPlayer);
		assertFalse(this.dialog.setUriForPlayer(testPlayer, this.testData.getUri())); // this should remove player from playlist

		assertThat(this.service.getPlaylist().size(), equalTo(1));
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

	@Test
	public void testDeliverResult() throws Exception
	{
		// create files and verify they exists
		String originalFileName = "testSound1.mp3";
		String newLabel = "renamedTestSound";
		File originalFile = this.createFile(originalFileName);
		assertNotNull(originalFile);
		assertTrue(originalFile.exists());

		// mock test data
		MediaPlayerData data = TestDataGenerator.getMediaPlayerData(originalFileName, Uri.fromFile(originalFile).toString());
		EnhancedMediaPlayer player = TestDataGenerator.getMockEnhancedMediaPlayer(data);

		// prepare test
		Uri originalFileUri = Uri.fromFile(originalFile);
		this.service.getPlaylist().add(player);
		assertThat(this.dialog.getPlayersWithMatchingUri(originalFileUri.toString()).size(), equalTo(1));
		this.dialog.setMediaPlayerData(data);

		// actual test
		this.dialog.deliverResult(Uri.fromFile(originalFile), newLabel, false);
		assertTrue(this.getFileFromExternalStorage("renamedTestSound.mp3").exists());
	}
}