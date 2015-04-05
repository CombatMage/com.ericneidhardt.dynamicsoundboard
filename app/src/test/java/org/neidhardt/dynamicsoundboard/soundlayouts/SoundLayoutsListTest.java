package org.neidhardt.dynamicsoundboard.soundlayouts;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.NavigationDrawerFragmentTest;
import org.neidhardt.dynamicsoundboard.R;

import static org.junit.Assert.*;

/**
 * Created by eric.neidhardt on 03.04.2015.
 */
public class SoundLayoutsListTest extends NavigationDrawerFragmentTest
{

	@Test
	public void testOnDeleteSelected() throws Exception
	{
		SoundLayoutsList soundLayoutsList = (SoundLayoutsList) this.activity.findViewById(R.id.layout_select_sound_layout);
		assertNotNull(soundLayoutsList);

		// write test
	}
}