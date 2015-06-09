package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.DaoSession;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

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
		this.soundLayoutsManager.daoSession = this.mockSession;
	}

	@Test
	public void testGetSoundLayouts() throws Exception
	{
		assertNotNull(this.soundLayoutsManager.getSoundLayouts());
	}

	@Test
	public void testSetSelected() throws Exception
	{
		List<SoundLayout> soundLayoutList = new ArrayList<>();
		for (int i = 0; i < 3; i++)
			soundLayoutList.add(new SoundLayout());
		soundLayoutList.get(0).setIsSelected(true);
		this.soundLayoutsManager.soundLayouts = soundLayoutList;

		this.soundLayoutsManager.setSelected(1);

		assertThat(soundLayoutList.get(0).getIsSelected(), equalTo(false));
		assertThat(soundLayoutList.get(1).getIsSelected(), equalTo(true));
		assertThat(soundLayoutList.get(2).getIsSelected(), equalTo(false));

		assertSame(this.soundLayoutsManager.getActiveSoundLayout(), soundLayoutList.get(1));
	}
}