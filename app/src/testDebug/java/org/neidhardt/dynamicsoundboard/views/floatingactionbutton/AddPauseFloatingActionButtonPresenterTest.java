package org.neidhardt.dynamicsoundboard.views.floatingactionbutton;

import de.greenrobot.event.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivitySoundsStateChangedEvent;
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.events.FabClickedEvent;

import static org.mockito.Mockito.*;

/**
 * Created by eric.neidhardt on 21.05.2015.
 */
public class AddPauseFloatingActionButtonPresenterTest extends BaseTest {

	private AddPauseFloatingActionButtonViewPresenter fabPresenter;
	private AddPauseFloatingActionButton mockView;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.mockView = mock(AddPauseFloatingActionButton.class);
		this.fabPresenter = new AddPauseFloatingActionButtonViewPresenter();
		this.fabPresenter.setView(this.mockView);
	}

	@Test
	public void testOnFabClicked() throws Exception
	{
		// test setup
		EventBus mockBus = mock(EventBus.class);
		this.fabPresenter.setEventBus(mockBus);
		// trigger test
		this.fabPresenter.onFabClicked();
		// check
		verify(mockBus, times(1)).post(Matchers.any(FabClickedEvent.class));
	}

	@Test
	public void testOnActivitySoundsStateChangedEvent() throws Exception
	{
		this.fabPresenter.isStatePause = false;
		this.fabPresenter.onEventMainThread(new ActivitySoundsStateChangedEvent(true));
		verify(this.mockView, times(1)).refreshDrawableState();
		verify(this.mockView, times(1)).animateUiChanges();

		this.fabPresenter.onEventMainThread(new ActivitySoundsStateChangedEvent(false));
		verify(this.mockView, times(2)).refreshDrawableState();
		verify(this.mockView, times(2)).animateUiChanges();
	}

	@Test
	public void testOnActivitySoundsStateChangedEvent_1() throws Exception
	{
		this.fabPresenter.isStatePause = false;
		this.fabPresenter.onEventMainThread(new ActivitySoundsStateChangedEvent(false));
		verify(this.mockView, never()).refreshDrawableState();
		verify(this.mockView, never()).animateUiChanges();

		this.fabPresenter.isStatePause = true;
		this.fabPresenter.onEventMainThread(new ActivitySoundsStateChangedEvent(true));
		verify(this.mockView, never()).refreshDrawableState();
		verify(this.mockView, never()).animateUiChanges();
	}
}