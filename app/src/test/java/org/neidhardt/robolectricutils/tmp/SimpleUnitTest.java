package com.neidhardt.testutils.tmp;

import org.junit.Test;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * Created by eric.neidhardt on 02.04.2015.
 */
public class SimpleUnitTest
{

	@Test
	public void checkJUnitWork()
	{
		assertThat(true, is(true));
	}
}


