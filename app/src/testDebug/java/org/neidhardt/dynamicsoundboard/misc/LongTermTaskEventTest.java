package org.neidhardt.dynamicsoundboard.misc;

import org.junit.Test;
import org.neidhardt.dynamicsoundboard.misc.progressbar.LongTermTaskEvent;
import org.neidhardt.robolectricutils.BaseTest;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Created by eric.neidhardt on 28.04.2015.
 */
public class LongTermTaskEventTest extends BaseTest
{

	@Test
	public void testIsTaskFinished() throws Exception
	{
		LongTermTaskEvent event = new LongTermTaskEvent(false);
		assertFalse(event.isTaskStarted());

		event = new LongTermTaskEvent(true);
		assertTrue(event.isTaskStarted());
	}
}