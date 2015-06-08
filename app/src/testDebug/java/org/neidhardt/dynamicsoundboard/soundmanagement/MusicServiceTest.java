package org.neidhardt.dynamicsoundboard.soundmanagement;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.AbstractBaseActivityTest;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.AddNewSoundEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.PlaylistLoadedEvent;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * File created by eric.neidhardt on 08.04.2015.
 */
public class MusicServiceTest extends AbstractBaseActivityTest
{
	@Test
	public void testGetDatabaseNameSounds() throws Exception
	{
		assertTrue(MusicService.getDatabaseNameSounds().equals(MusicService.DB_SOUNDS_DEFAULT));
	}

	@Test
	public void testGetDatabaseNamePlayList() throws Exception
	{
		assertTrue(MusicService.getDatabaseNamePlayList().equals(MusicService.DB_SOUNDS_PLAYLIST_DEFAULT));
	}

	@Test(expected = NullPointerException.class)
	public void testOnSoundsLoadedEventMainThread() throws Exception
	{
		this.service.onEvent(new AddNewSoundEvent(null, true));
	}

	@Test(expected = NullPointerException.class)
	public void testOnPlayListLoadedEventMainThread1() throws Exception
	{
		this.service.onEvent(new PlaylistLoadedEvent(null, true));
	}

	@Test
	public void testOnMediaPlayerStateChangedEvent() throws Exception
	{
		EnhancedMediaPlayer testPlayer = TestDataGenerator.getMockEnhancedMediaPlayer(null);
		when(testPlayer.isPlaying()).thenReturn(true);

		this.service.onEvent(new MediaPlayerStateChangedEvent(testPlayer, true));
		assertThat(this.service.getCurrentlyPlayingSounds().size(), equalTo(1));

		when(testPlayer.isPlaying()).thenReturn(false);
		this.service.onEvent(new MediaPlayerStateChangedEvent(testPlayer, true));

		assertThat(this.service.getCurrentlyPlayingSounds().size(), equalTo(0));
	}
}