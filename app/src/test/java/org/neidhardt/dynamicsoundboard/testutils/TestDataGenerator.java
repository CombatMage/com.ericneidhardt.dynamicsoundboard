package org.neidhardt.dynamicsoundboard.testutils;

import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.io.File;
import java.util.Random;

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

	public static EnhancedMediaPlayer getRandomPlayer(String fragmentTag) throws Exception
	{
		EnhancedMediaPlayer player = TestDataGenerator.getMockEnhancedMediaPlayer(TestDataGenerator.getRandomPlayerData());
		player.getMediaPlayerData().setFragmentTag(fragmentTag);
		return player;
	}

	private static String createRandomFile() throws Exception
	{
		File file = BaseTest.createFile(getRandomString());
		return file.getPath();
	}

	public static EnhancedMediaPlayer getMockEnhancedMediaPlayer(MediaPlayerData data)
	{
		EnhancedMediaPlayer player = mock(EnhancedMediaPlayer.class);
		when(player.getMediaPlayerData()).thenReturn(data);

		return player;
	}
}
