package org.neidhardt.dynamicsoundboard;

import com.neidhardt.testutils.CustomTestRunner;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.neidhardt.dynamicsoundboard.soundmanagement.MusicService;
import org.robolectric.Robolectric;

/**
 * Created by eric.neidhardt on 02.04.2015.
 */
@RunWith(CustomTestRunner.class)
public abstract class ActivityTest
{
	protected BaseActivity activity;
	protected MusicService service;

	@Before
	public void setUp() throws Exception
	{
		this.service = Robolectric.setupService(MusicService.class);
		this.activity = Robolectric.setupActivity(BaseActivity.class);
	}
}
