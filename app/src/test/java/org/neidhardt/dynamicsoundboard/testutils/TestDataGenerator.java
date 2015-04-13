package org.neidhardt.dynamicsoundboard.testutils;

import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;

import java.util.Random;

/**
 * Created by eric.neidhardt on 13.04.2015.
 */
public class TestDataGenerator
{
	private static Random random = new Random();

	public static String getRandomString()
	{
		return Integer.toString(random.nextInt(Integer.MAX_VALUE));
	}

	public static MediaPlayerData getRandomPlayerData()
	{
		MediaPlayerData data = new MediaPlayerData();
		data.setLabel("test");
		data.setUri(getRandomString());
		data.setPlayerId(getRandomString());
		data.setFragmentTag(getRandomString());
		data.setIsLoop(false);
		data.setIsInPlaylist(false);
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
}
