package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;

import static org.junit.Assert.assertNotNull;

/**
 * File created by eric.neidhardt on 09.06.2015.
 */
public class SoundLayoutsManagerTest extends BaseTest
{
	private SoundLayoutsManager soundLayoutsManager;

	@Mock private DaoSession mockSession;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		MockitoAnnotations.initMocks(this);

		this.soundLayoutsManager = new SoundLayoutsManager();
	}

	@Test
	public void testGetSoundLayouts() throws Exception
	{
		assertNotNull(this.soundLayoutsManager.getSoundLayouts());
	}
}