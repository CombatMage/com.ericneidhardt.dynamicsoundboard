package org.neidhardt.dynamicsoundboard;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.customview.floatingactionbutton.events.FabClickedEvent;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by eric.neidhardt on 20.04.2015.
 */
public class SoundActivityTest extends AbstractBaseActivityTest
{
	@Test
	public void testRemoveSoundFragment() throws Exception
	{
		SoundSheetFragment fragment = new SoundSheetFragment();
		this.activity.getFragmentManager().beginTransaction().add(fragment, "testTag").commit();
		assertNotNull(this.activity.getFragmentManager().findFragmentByTag("testTag"));

		SoundSheet soundSheet = mock(SoundSheet.class);
		when(soundSheet.getFragmentTag()).thenReturn("testTag");
		this.activity.removeSoundFragment(soundSheet);
		assertNull(this.activity.getFragmentManager().findFragmentByTag("testTag"));
	}

	@Test
	public void testServiceManagerFragmentAccess() throws Exception
	{
		assertSame(this.serviceManagerFragment, this.serviceManagerFragment.getServiceManagerFragment());
	}

	@Test
	public void testNavigationDrawerFragmentAccess() throws Exception
	{
		assertSame(this.navigationDrawerFragment, this.navigationDrawerFragment.getNavigationDrawerFragment());
	}

	@Test
	public void testSoundSheetManagerFragmentAccess() throws Exception
	{
		assertSame(this.soundSheetsManagerFragment, this.soundSheetsManagerFragment.getSoundSheetManagerFragment());
	}

	@Test
	public void testOnEvent() throws Exception
	{
		// mock test data
		EnhancedMediaPlayer player = spy(new EnhancedMediaPlayer(TestDataGenerator.getRandomPlayerData()));
		this.service.getPlaylist().add(player);
		player.playSound();

		player = spy(new EnhancedMediaPlayer(TestDataGenerator.getRandomPlayerData()));
		this.service.getPlaylist().add(player);
		player.playSound();

		player = spy(new EnhancedMediaPlayer(TestDataGenerator.getRandomPlayerData()));
		this.service.getPlaylist().add(player);

		assertThat(this.service.getCurrentlyPlayingSounds().size(), equalTo(2));

		// actual test
		this.activity.onEvent(new FabClickedEvent());
		assertThat(this.service.getCurrentlyPlayingSounds().size(), equalTo(0));
	}
}
