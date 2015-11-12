package org.neidhardt.dynamicsoundboard.views.progressbar;

import android.view.View;
import de.greenrobot.event.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.misc.longtermtask.events.LongTermTaskStateChangedEvent;

import static org.mockito.Mockito.*;

/**
 * File created by eric.neidhardt on 22.05.2015.
 */
public class ActivityProgressBarPresenterTest extends BaseTest
{
	@Mock private EventBus eventBus;
	@Mock private ActivityProgressBar view;

	private ActivityProgressBarPresenter presenter;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();
		MockitoAnnotations.initMocks(this);

		this.presenter = new ActivityProgressBarPresenter(this.eventBus);
		this.presenter.setView(this.view);
	}

	@Test
	public void testOnEventMainThread() throws Exception
	{
		this.presenter.onEventMainThread(new LongTermTaskStateChangedEvent(true, 1));
		verify(this.view, times(1)).setVisibility(View.VISIBLE);

		this.presenter.onEventMainThread(new LongTermTaskStateChangedEvent(false, 0));
		verify(this.view, times(1)).setVisibility(View.GONE);

		this.presenter.onEventMainThread(new LongTermTaskStateChangedEvent(false, -1));
		verify(this.view, times(2)).setVisibility(View.GONE);
	}
}