package org.neidhardt.dynamicsoundboard;

import android.os.IBinder;
import org.junit.Test;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;
import org.neidhardt.robolectricutils.BaseTest;
import org.neidhardt.robolectricutils.CustomTestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.soundmanagement.MusicService;
import org.neidhardt.dynamicsoundboard.soundsheet.SoundSheetsManagerFragment;
import org.robolectric.Robolectric;
import org.robolectric.util.FragmentTestUtil;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by eric.neidhardt on 02.04.2015.
 */
public abstract class AbstractBaseActivityTest extends BaseTest
{
	protected BaseActivity activity;
	protected MusicService service;

	protected SoundSheetsManagerFragment soundSheetsManagerFragment;
	protected ServiceManagerFragment serviceManagerFragment;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		this.service = Robolectric.setupService(MusicService.class);
		this.activity = Robolectric.setupActivity(BaseActivity.class);
		this.soundSheetsManagerFragment = (SoundSheetsManagerFragment) this.activity.getFragmentManager().findFragmentByTag(SoundSheetsManagerFragment.TAG);
		this.serviceManagerFragment = (ServiceManagerFragment) this.activity.getFragmentManager().findFragmentByTag(ServiceManagerFragment.TAG);

		assertNotNull(this.activity);
		assertNotNull(this.soundSheetsManagerFragment);
		assertNotNull(this.service);
		assertNotNull(this.serviceManagerFragment);

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
