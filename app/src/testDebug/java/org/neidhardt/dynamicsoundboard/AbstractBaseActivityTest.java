package org.neidhardt.dynamicsoundboard;

import org.junit.After;
import org.junit.Before;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.soundmanagement.MusicService;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.SoundSheetsManagerFragment;
import org.robolectric.Robolectric;

import static org.junit.Assert.*;

/**
 * Created by eric.neidhardt on 02.04.2015.
 */
public abstract class AbstractBaseActivityTest extends BaseTest
{
	protected SoundActivity activity;
	protected MusicService service;

	protected SoundSheetsManagerFragment soundSheetsManagerFragment;
	protected ServiceManagerFragment serviceManagerFragment;
	protected NavigationDrawerFragment navigationDrawerFragment;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		this.service = Robolectric.setupService(MusicService.class);
		this.activity = Robolectric.setupActivity(SoundActivity.class);
		this.soundSheetsManagerFragment = this.activity.getSoundSheetsManagerFragment();
		this.serviceManagerFragment = this.activity.getServiceManagerFragment();
		this.navigationDrawerFragment = this.activity.getNavigationDrawerFragment();

		assertNotNull(this.activity);
		assertNotNull(this.soundSheetsManagerFragment);
		assertNotNull(this.service);
		assertNotNull(this.serviceManagerFragment);
		assertNotNull(this.navigationDrawerFragment);

		this.serviceManagerFragment.onServiceConnected(null, new MusicService.Binder(this.service));
		assertTrue(this.serviceManagerFragment.isServiceBound());
		assertSame(this.service, this.serviceManagerFragment.getSoundService());
	}

	@Override
	@After
	public void tearDown() throws Exception
	{
		SoundLayoutsManager.getInstance().clear();
		this.soundSheetsManagerFragment.deleteAllSoundSheets();
		this.service.deleteAllSounds();

		this.service.stopSelf();
		this.activity.getFragmentManager().beginTransaction().remove(this.soundSheetsManagerFragment).commit();

		super.tearDown();
	}
}
