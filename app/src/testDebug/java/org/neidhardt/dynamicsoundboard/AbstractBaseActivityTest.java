package org.neidhardt.dynamicsoundboard;

import org.junit.After;
import org.junit.Before;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.notifications.service.ServiceManagerFragment;
import org.robolectric.Robolectric;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * File created by eric.neidhardt on 02.04.2015.
 */
public abstract class AbstractBaseActivityTest extends BaseTest
{
	protected SoundActivity activity;

	protected ServiceManagerFragment serviceManagerFragment;
	protected NavigationDrawerFragment navigationDrawerFragment;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		this.activity = Robolectric.setupActivity(SoundActivity.class);
		this.serviceManagerFragment = this.activity.getServiceManagerFragment();
		this.navigationDrawerFragment = this.activity.getNavigationDrawerFragment();

		assertNotNull(this.activity);
		assertNotNull(this.serviceManagerFragment);
		assertNotNull(this.navigationDrawerFragment);

		assertTrue(this.serviceManagerFragment.isServiceBound());
	}

	@Override
	@After
	public void tearDown() throws Exception
	{
		SoundLayoutsManager.getInstance().clear();
		super.tearDown();
	}
}
