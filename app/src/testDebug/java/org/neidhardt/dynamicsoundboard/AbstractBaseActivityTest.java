package org.neidhardt.dynamicsoundboard;

import org.junit.After;
import org.junit.Before;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.MusicService;
import org.neidhardt.dynamicsoundboard.soundmanagement.service.ServiceManagerFragment;
import org.robolectric.Robolectric;

import static org.junit.Assert.*;

/**
 * File created by eric.neidhardt on 02.04.2015.
 */
public abstract class AbstractBaseActivityTest extends BaseTest
{
	protected SoundActivity activity;
	protected MusicService service;

	protected ServiceManagerFragment serviceManagerFragment;
	protected NavigationDrawerFragment navigationDrawerFragment;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		this.service = Robolectric.setupService(MusicService.class);
		this.activity = Robolectric.setupActivity(SoundActivity.class);
		this.serviceManagerFragment = this.activity.getServiceManagerFragment();
		this.navigationDrawerFragment = this.activity.getNavigationDrawerFragment();

		assertNotNull(this.activity);
		assertNotNull(this.service);
		assertNotNull(this.serviceManagerFragment);
		assertNotNull(this.navigationDrawerFragment);

		this.serviceManagerFragment.onServiceConnected(null, new MusicService.Binder(this.service));
		assertTrue(this.serviceManagerFragment.isServiceBound());
	}

	@Override
	@After
	public void tearDown() throws Exception
	{
		SoundLayoutsManager.getInstance().clear();
		this.service.deleteAllSounds();

		this.service.stopSelf();

		super.tearDown();
	}
}
