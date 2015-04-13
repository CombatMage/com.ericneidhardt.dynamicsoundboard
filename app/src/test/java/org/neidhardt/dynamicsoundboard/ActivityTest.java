package org.neidhardt.dynamicsoundboard;

import android.os.IBinder;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.robolectricutils.CustomTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.soundmanagement.MusicService;
import org.neidhardt.dynamicsoundboard.soundsheet.SoundSheetsManagerFragment;
import org.robolectric.Robolectric;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by eric.neidhardt on 02.04.2015.
 */
@RunWith(CustomTestRunner.class)
public abstract class ActivityTest
{
	protected BaseActivity activity;
	protected MusicService service;

	protected SoundSheetsManagerFragment soundSheetsManagerFragment;

	@Before
	public void setUp() throws Exception
	{
		this.service = Robolectric.setupService(MusicService.class);
		this.activity = Robolectric.setupActivity(BaseActivity.class);
		this.soundSheetsManagerFragment = (SoundSheetsManagerFragment) this.activity.getFragmentManager().findFragmentByTag(SoundSheetsManagerFragment.TAG);

		assertNotNull(this.activity);
		assertNotNull(this.soundSheetsManagerFragment);
		assertNotNull(this.service);

		ServiceManagerFragment fragment = (ServiceManagerFragment) this.activity.getFragmentManager().findFragmentByTag(ServiceManagerFragment.TAG);
		fragment.onServiceConnected(null, new MusicService.Binder(this.service));

		assertTrue(fragment.isServiceBound());
	}

	@After
	public void tearDown() throws Exception
	{
		SoundLayoutsManager.getInstance().clear();
		this.soundSheetsManagerFragment.deleteAllSoundSheets();
		this.service.deleteAllSounds();
	}
}
