package org.neidhardt.dynamicsoundboard;

import com.neidhardt.testutils.CustomTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

import static org.junit.Assert.*;

/**
 * Created by eric.neidhardt on 02.04.2015.
 */
@RunWith(CustomTestRunner.class)
public class BaseActivityTest extends ActivityTest
{
	private ServiceManagerFragment fragment;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
	}

	@Test
	public void testServiceConnected() throws Exception
	{
		this.fragment = (ServiceManagerFragment) this.activity.getFragmentManager().findFragmentByTag(ServiceManagerFragment.TAG);
		assertNotNull(this.fragment);
		assertNotNull(this.fragment.getSoundService());
	}
}