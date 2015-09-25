package org.neidhardt.dynamicsoundboard.misc;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

/**
 * File created by eric.neidhardt on 19.06.2015.
 */
public class JsonPojoTest extends BaseTest
{
	private List<SoundSheet> sheets;
	private List<EnhancedMediaPlayer> playlist;
	private Map<String, List<EnhancedMediaPlayer>> sounds;

	private File file;

	@Override
	public void setUp() throws Exception
	{
		super.setUp();
		this.sheets = new ArrayList<>();
		this.sheets.add(TestDataGenerator.getRandomSoundSheet());

		this.playlist = new ArrayList<>();
		this.playlist.add(mock(EnhancedMediaPlayer.class));

		List<EnhancedMediaPlayer> soundList = new ArrayList<>();
		soundList.add(mock(EnhancedMediaPlayer.class));
		this.sounds = new HashMap<>();
		this.sounds.put("test", soundList);

		this.file = BaseTest.createFile("JsonPojoTest.data");
	}

	@Test
	public void testWriteAndReadFromFile() throws Exception
	{
		JsonPojo.writeToFile(this.file, this.sheets, this.playlist, this.sounds);

		JsonPojo pojo = JsonPojo.readFromFile(this.file);
		assertThat(pojo.getSoundSheets().size(), equalTo(1));
		assertThat(pojo.getPlayList().size(), equalTo(1));
		assertThat(pojo.getSounds().get("test").size(), equalTo(1));
	}
}