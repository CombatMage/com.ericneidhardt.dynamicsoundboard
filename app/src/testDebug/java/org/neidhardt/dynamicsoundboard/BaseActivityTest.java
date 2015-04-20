package org.neidhardt.dynamicsoundboard;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
}
