package org.neidhardt.dynamicsoundboard.misc.progressbar;

import android.view.View;
import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;
import org.junit.Before;
import org.junit.Test;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LongTermTaskStartedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LongTermTaskStoppedEvent;
import org.neidhardt.robolectricutils.BaseTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by eric.neidhardt on 28.04.2015.
 */
public class ProgressbarHandlerTest extends BaseTest
{
	private SmoothProgressBar progressBar;
	private ProgressbarHandler progressbarHandler;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		this.progressBar = mock(SmoothProgressBar.class);
		this.progressbarHandler = new ProgressbarHandler(this.progressBar);
	}

	@Test
	public void testShowProgressBar() throws Exception
	{
		this.progressbarHandler.showProgressBar(false);
		verify(this.progressBar, atLeastOnce()).setVisibility(View.GONE);
	}

	@Test
	public void testShowProgressBar1() throws Exception
	{
		this.progressbarHandler.showProgressBar(true);
		verify(this.progressBar, atLeastOnce()).setVisibility(View.VISIBLE);
	}

	@Test
	public void testOnEvent() throws Exception
	{
		LongTermTaskStoppedEvent event = mock(LongTermTaskStoppedEvent.class);

		this.progressbarHandler.onEvent(event);
		this.progressbarHandler.onEvent(event);
		this.progressbarHandler.onEvent(event);

		assertThat(this.progressbarHandler.getPendingEventCounter(), equalTo(0));
		verify(this.progressBar, atLeastOnce()).setVisibility(View.GONE);
		verify(this.progressBar, never()).setVisibility(View.VISIBLE);
	}

	@Test
	public void testOnEvent1() throws Exception
	{
		LongTermTaskStartedEvent event = mock(LongTermTaskStartedEvent.class);

		this.progressbarHandler.onEvent(event);
		this.progressbarHandler.onEvent(event);
		this.progressbarHandler.onEvent(event);

		assertThat(this.progressbarHandler.getPendingEventCounter(), equalTo(3));
		verify(this.progressBar, atMost(1)).setVisibility(View.GONE);
		verify(this.progressBar, atLeastOnce()).setVisibility(View.VISIBLE);
	}

	@Test
	public void testOnEvent2() throws Exception
	{
		LongTermTaskStartedEvent startedEvent = mock(LongTermTaskStartedEvent.class);

		this.progressbarHandler.onEvent(startedEvent);

		assertThat(this.progressbarHandler.getPendingEventCounter(), equalTo(1));
		verify(this.progressBar, atMost(1)).setVisibility(View.GONE);
		verify(this.progressBar, atLeastOnce()).setVisibility(View.VISIBLE);

		LongTermTaskStoppedEvent stoppedEvent = mock(LongTermTaskStoppedEvent.class);
		this.progressbarHandler.onEvent(stoppedEvent);

		assertThat(this.progressbarHandler.getPendingEventCounter(), equalTo(0));
	}
}