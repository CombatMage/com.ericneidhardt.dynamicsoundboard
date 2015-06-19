package org.neidhardt.dynamicsoundboard.testutils;

import android.content.Context;
import android.net.Uri;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist;

import java.io.File;
import java.util.Random;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * File created by eric.neidhardt on 13.04.2015.
 */
public class TestDataGenerator
{
	private static Random random = new Random();

	public static String getRandomString()
	{
		return Integer.toString(random.nextInt(Integer.MAX_VALUE));
	}

	public static SoundSheet getRandomSoundSheet() throws Exception
	{
		SoundSheet soundSheet = new SoundSheet();
		soundSheet.setLabel(getRandomString());
		soundSheet.setFragmentTag(getRandomString());
		return soundSheet;
	}

	public static MediaPlayerData getRandomPlayerData() throws Exception
	{
		MediaPlayerData data = new MediaPlayerData();
		data.setLabel("test");
		String uri = createRandomFile();
		data.setUri(uri);
		data.setPlayerId(getRandomString());
		data.setFragmentTag(getRandomString());
		data.setIsLoop(false);
		data.setIsInPlaylist(false);
		return data;
	}

	public static MediaPlayerData getRandomPlayerDataForPlayList() throws Exception
	{
		MediaPlayerData data = new MediaPlayerData();
		data.setLabel("test");
		data.setUri(createRandomFile());
		data.setPlayerId(getRandomString());
		data.setFragmentTag(Playlist.TAG);
		data.setIsLoop(false);
		data.setIsInPlaylist(false);
		return data;
	}

	private static String createRandomFile() throws Exception
	{
		File file = BaseTest.createFile(getRandomString());
		return file.getPath();
	}

	public static MediaPlayerData getMediaPlayerData(String label, String uri)
	{
		MediaPlayerData data = new MediaPlayerData();
		data.setLabel(label);
		data.setUri(uri);
		data.setFragmentTag(Playlist.TAG);
		return data;
	}

	public static SoundLayout getRandomSoundLayout()
	{
		SoundLayout testLayout = new SoundLayout();
		testLayout.setLabel("test");
		testLayout.setDatabaseId(getRandomString());
		testLayout.setIsSelected(false);
		return testLayout;
	}

	public static EnhancedMediaPlayer getMockEnhancedMediaPlayer(MediaPlayerData data)
	{
		EnhancedMediaPlayer player = mock(EnhancedMediaPlayer.class);
		when(player.getMediaPlayerData()).thenReturn(data);
		return player;
	}
}
