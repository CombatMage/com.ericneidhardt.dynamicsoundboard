package org.neidhardt.dynamicsoundboard.dialog.soundsettings;

import android.net.Uri;
import android.view.View;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.neidhardt.dynamicsoundboard.AbstractBaseActivityTest;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.playlist.Playlist;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundLoadedEvent;
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
		this.service.addNewSoundToPlaylistAndDatabase(this.testData);

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
		this.service.onEvent(new SoundLoadedEvent(TestDataGenerator.getRandomPlayerData(), false));
		this.service.onEvent(new SoundLoadedEvent(TestDataGenerator.getRandomPlayerData(), false));

		MediaPlayerData data2 = TestDataGenerator.getRandomPlayerData();
		data2.setUri(this.testData.getUri());
		this.service.addNewSoundToPlaylistAndDatabase(data2);

		MediaPlayerData data3 = TestDataGenerator.getRandomPlayerData();
		data3.setUri(this.testData.getUri());
		this.service.onEvent(new SoundLoadedEvent(data3, false));

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
	public void testSetMediaPlayerData() throws Exception
	{
		MediaPlayerData data1 = TestDataGenerator.getRandomPlayerData();
		MediaPlayerData data2 = TestDataGenerator.getRandomPlayerData();
		data1.setUri(this.testData.getUri());
		data2.setUri(this.testData.getUri());

		this.service.onEvent(new SoundLoadedEvent(data1, false));
		this.service.onEvent(new SoundLoadedEvent(data2, false));

		assertThat(this.dialog.getPlayersWithMatchingUri(this.testData.getUri()).size(), equalTo(3));  // this.testData + data2 + data3

		this.dialog.setMediaPlayerData(data1);

		View dialogView = this.dialog.getMainView();
		assertNotNull(dialogView);

		View renameCheckBox = dialogView.findViewById(R.id.cb_rename_all_occurrences);
		assertNotNull(renameCheckBox);
		assertThat(renameCheckBox.getVisibility(), equalTo(View.VISIBLE));
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

	@Test
	public void testDeliverResult1() throws Exception
	{
		// create files and verify they exists
		String originalFileName = "testSound1.mp3";
		String newLabel = "renamedTestSound";
		File originalFile = this.createFile(originalFileName);

		MediaPlayerData data1 = spy(TestDataGenerator.getMediaPlayerData(originalFileName, Uri.fromFile(originalFile).toString()));
		EnhancedMediaPlayer player1 = TestDataGenerator.getMockEnhancedMediaPlayer(data1);

		MediaPlayerData data2 = spy(TestDataGenerator.getMediaPlayerData("data2", Uri.fromFile(originalFile).toString()));
		EnhancedMediaPlayer player2 = TestDataGenerator.getMockEnhancedMediaPlayer(data2);

		MediaPlayerData data3 = spy(TestDataGenerator.getMediaPlayerData("data3", Uri.fromFile(originalFile).toString()));
		EnhancedMediaPlayer player3 = TestDataGenerator.getMockEnhancedMediaPlayer(data3);

		// prepare test
		Uri originalFileUri = Uri.fromFile(originalFile);
		this.service.getPlaylist().add(player1);
		this.service.getPlaylist().add(player2);
		this.service.getPlaylist().add(player3);
		this.dialog.setMediaPlayerData(data1);
		assertThat(this.dialog.getPlayersWithMatchingUri(originalFileUri.toString()).size(), equalTo(3));

		// actual test
		this.dialog.deliverResult(Uri.fromFile(originalFile), newLabel, true);
		assertTrue(this.getFileFromExternalStorage("renamedTestSound.mp3").exists());
		assertThat(this.service.getPlaylist().get(1).getMediaPlayerData().getLabel(), equalTo("renamedTestSound"));
		assertThat(this.service.getPlaylist().get(2).getMediaPlayerData().getLabel(), equalTo("renamedTestSound"));

		verify(this.service.getPlaylist().get(1).getMediaPlayerData(), atLeastOnce()).setItemWasAltered();
		verify(this.service.getPlaylist().get(2).getMediaPlayerData(), atLeastOnce()).setItemWasAltered();
	}
}