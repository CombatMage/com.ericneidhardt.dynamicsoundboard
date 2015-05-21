package org.neidhardt.dynamicsoundboard.customview.floatingactionbutton;

import de.greenrobot.event.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.neidhardt.dynamicsoundboard.AbstractBaseActivityTest;
import org.neidhardt.dynamicsoundboard.events.FabClickedEvent;

import static org.mockito.Mockito.*;

/**
 * Created by eric.neidhardt on 21.05.2015.
 */
public class AddPauseFloatingActionButtonPresenterTest extends AbstractBaseActivityTest  {

	private AddPauseFloatingActionButtonPresenter fabPresenter;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		this.fabPresenter = new AddPauseFloatingActionButtonPresenter();
	}

	@Test
	public void testOnFabClicked() throws Exception
	{
		// test setup
		EventBus mockBus = mock(EventBus.class);
		this.fabPresenter.bus = mockBus;
		// trigger test
		this.fabPresenter.onFabClicked();
		// check
		verify(mockBus, times(1)).post(Matchers.any(FabClickedEvent.class));
	}
}