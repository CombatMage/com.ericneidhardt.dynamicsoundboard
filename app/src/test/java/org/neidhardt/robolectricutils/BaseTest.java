package org.neidhardt.robolectricutils;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by eric.neidhardt on 18.04.2015.
 */
@RunWith(CustomTestRunner.class)
public abstract class BaseTest
{
	@Before
	public void setUp() throws Exception
	{
		// nothing to be done
	}

	@After
	public void tearDown() throws Exception
	{
		// TODO clear shared preferences
	}
}
