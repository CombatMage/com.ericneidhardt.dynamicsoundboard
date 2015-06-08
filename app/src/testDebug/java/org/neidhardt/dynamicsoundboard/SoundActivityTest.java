package org.neidhardt.dynamicsoundboard;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.events.FabClickedEvent;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * File created by eric.neidhardt on 20.04.2015.
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

}
