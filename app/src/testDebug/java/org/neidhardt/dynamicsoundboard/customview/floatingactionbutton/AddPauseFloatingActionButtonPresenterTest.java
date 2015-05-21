package org.neidhardt.dynamicsoundboard.customview.floatingactionbutton;

import de.greenrobot.event.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.neidhardt.dynamicsoundboard.AbstractBaseActivityTest;
import org.neidhardt.dynamicsoundboard.customview.floatingactionbutton.events.FabClickedEvent;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivitySoundsStateChangedEvent;

import static org.mockito.Mockito.*;

/**
 * Created by eric.neidhardt on 21.05.2015.
 */
public class AddPauseFloatingActionButtonPresenterTest extends AbstractBaseActivityTest  {

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
		this.fabPresenter.setBus(mockBus);
		// trigger test
		this.fabPresenter.onFabClicked();
		// check
		verify(mockBus, times(1)).post(Matchers.any(FabClickedEvent.class));
	}

	@Test
	public void testOnActivitySoundsStateChangedEvent() throws Exception
	{
		this.fabPresenter.onEvent(new ActivitySoundsStateChangedEvent(true));
		verify(this.mockView, times(1)).setPauseState();
	}

	@Test
	public void testOnActivitySoundsStateChangedEvent_1() throws Exception
	{
		this.fabPresenter.onEvent(new ActivitySoundsStateChangedEvent(false));
		verify(this.mockView, times(1)).setAddState();
	}
}