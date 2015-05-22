package org.neidhardt.dynamicsoundboard.misc.progressbar;

import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.misc.longtermtask.events.LongTermTaskStateChangedEvent;

import static org.mockito.Mockito.*;

/**
 * Created by eric.neidhardt on 22.05.2015.
 */
public class ActivityProgressBarPresenterTest extends BaseTest
{
	private ActivityProgressBarPresenter presenter;
	private ActivityProgressBar mockView;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		this.mockView = mock(ActivityProgressBar.class);
		this.presenter = new ActivityProgressBarPresenter();
		this.presenter.setView(this.mockView);
	}

	@Test
	public void testOnEventMainThread() throws Exception
	{
		this.presenter.onEventMainThread(new LongTermTaskStateChangedEvent(true, 1));
		verify(this.mockView, times(1)).setVisibility(View.VISIBLE);

		this.presenter.onEventMainThread(new LongTermTaskStateChangedEvent(false, 0));
		verify(this.mockView, times(1)).setVisibility(View.GONE);

		this.presenter.onEventMainThread(new LongTermTaskStateChangedEvent(false, -1));
		verify(this.mockView, times(2)).setVisibility(View.GONE);
	}
}