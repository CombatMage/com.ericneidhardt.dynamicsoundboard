package org.neidhardt.dynamicsoundboard.misc.progressbar;

import android.view.View;
import android.widget.ProgressBar;
import org.junit.Before;
import org.junit.Test;
import org.neidhardt.robolectricutils.BaseTest;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by eric.neidhardt on 28.04.2015.
 */
public class ProgressbarHandlerTest extends BaseTest
{
	private ProgressBar progressBar;
	private ProgressbarHandler progressbarHandler;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		this.progressBar = mock(ProgressBar.class);
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
		LongTermTaskEvent event = mock(LongTermTaskEvent.class);
		when(event.isTaskStarted()).thenReturn(false);

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
		LongTermTaskEvent event = mock(LongTermTaskEvent.class);
		when(event.isTaskStarted()).thenReturn(true);

		this.progressbarHandler.onEvent(event);
		this.progressbarHandler.onEvent(event);
		this.progressbarHandler.onEvent(event);

		assertThat(this.progressbarHandler.getPendingEventCounter(), equalTo(3));
		verify(this.progressBar, never()).setVisibility(View.GONE);
		verify(this.progressBar, atLeastOnce()).setVisibility(View.VISIBLE);
	}

	@Test
	public void testOnEvent2() throws Exception
	{
		LongTermTaskEvent event = mock(LongTermTaskEvent.class);
		when(event.isTaskStarted()).thenReturn(true);

		this.progressbarHandler.onEvent(event);

		assertThat(this.progressbarHandler.getPendingEventCounter(), equalTo(1));
		verify(this.progressBar, never()).setVisibility(View.GONE);
		verify(this.progressBar, atLeastOnce()).setVisibility(View.VISIBLE);

		when(event.isTaskStarted()).thenReturn(false);
		this.progressbarHandler.onEvent(event);

		assertThat(this.progressbarHandler.getPendingEventCounter(), equalTo(0));
	}
}