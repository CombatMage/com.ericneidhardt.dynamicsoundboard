package org.neidhardt.dynamicsoundboard.dialog.soundsettings;

import android.net.Uri;
import android.os.Environment;
import android.view.View;
import org.apache.tools.ant.util.FileUtils;
import org.junit.After;
import org.junit.Test;
import org.mockito.Mockito;
import org.neidhardt.dynamicsoundboard.BaseActivityTest;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.playlist.Playlist;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;
import org.robolectric.shadows.ShadowEnvironment;

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
public class RenameSoundFileDialogTest extends BaseActivityTest
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
		this.service.addNewSoundToSoundsAndDatabase(TestDataGenerator.getRandomPlayerData());
		this.service.addNewSoundToSoundsAndDatabase(TestDataGenerator.getRandomPlayerData());

		MediaPlayerData data2 = TestDataGenerator.getRandomPlayerData();
		data2.setUri(this.testData.getUri());
		this.service.addNewSoundToPlaylistAndDatabase(data2);

		MediaPlayerData data3 = TestDataGenerator.getRandomPlayerData();
		data3.setUri(this.testData.getUri());
		this.service.addNewSoundToSoundsAndDatabase(data3);

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

		this.service.addNewSoundToSoundsAndDatabase(data1);
		this.service.addNewSoundToSoundsAndDatabase(data2);

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
		String originalFileName = "testSound1.mp3";
		String newLabel = "renamedTestSound";
		File originalFile = this.createFile(originalFileName);
		assertNotNull(originalFile);
		assertTrue(originalFile.exists());

		MediaPlayerData data = mock(MediaPlayerData.class);
		when(data.getLabel()).thenReturn(originalFileName);
		when(data.getUri()).thenReturn(Uri.fromFile(originalFile).toString());

		this.dialog.setMediaPlayerData(data);
		// TODO for strange reason Uri.toString and Uri.getPath are not interchangeable
		this.dialog.deliverResult(newLabel, false);
		assertThat(originalFile.getName(), equalTo("renamedTestSound.mp3"));
	}
}