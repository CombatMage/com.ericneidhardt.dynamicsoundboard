package org.neidhardt.dynamicsoundboard;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by eric.neidhardt on 20.04.2015.
 */
public class BaseActivityTest extends AbstractBaseActivityTest
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
	public void testOnFabClicked() throws Exception
	{
		// TODO test
	}
}
