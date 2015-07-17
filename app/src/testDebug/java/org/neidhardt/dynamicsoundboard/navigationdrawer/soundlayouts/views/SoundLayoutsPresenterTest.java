package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views;

import android.view.View;
import de.greenrobot.event.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent;
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsManager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * File created by eric.neidhardt on 09.06.2015.
 */
public class SoundLayoutsPresenterTest extends BaseTest
{
	private SoundLayoutsPresenter presenter;
	@Mock private SoundLayouts mockView;
	@Mock private SoundLayoutsAdapter mockAdapter;
	@Mock private SoundLayoutsManager mockManager;
	@Mock private EventBus mockEventBus;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		MockitoAnnotations.initMocks(this);

		this.presenter = spy(new SoundLayoutsPresenter(this.mockManager, this.mockAdapter));
		this.presenter.setView(this.mockView);
		this.presenter.eventBus = this.mockEventBus;
	}

	@Test
	public void testIsEventBusSubscriber() throws Exception
	{
		assertFalse(this.presenter.isEventBusSubscriber());
	}

	@Test(expected = NullPointerException.class)
	public void testOnItemClickViewNull() throws Exception
	{
		this.presenter.setView(null);
		this.presenter.onItemClick(null, null, -1);
	}

	@Test
	public void testOnItemClickInSelectionMode() throws Exception
	{
		this.presenter.setIsInSelectionMode(true);
		View view = mock(View.class);
		int position = 2;
		SoundLayout data = mock(SoundLayout.class);

		this.presenter.onItemClick(view, data, position);

		verify(data, times(1)).setIsSelectedForDeletion(true);
		verify(this.mockAdapter, times(1)).notifyDataSetChanged();
	}

	@Test
	public void testOnItemClickNotInSelectionMode() throws Exception
	{
		this.presenter.setIsInSelectionMode(false);
		View view = mock(View.class);
		int position = 2;
		SoundLayout data = mock(SoundLayout.class);

		this.presenter.onItemClick(view, data, position);

		verify(this.mockManager, times(1)).setSelected(position);
		verify(this.mockView, times(1)).toggleVisibility();
		verify(this.mockEventBus, times(1)).post(any(SoundLayoutSelectedEvent.class));
		verify(this.mockAdapter, times(1)).notifyDataSetChanged();
	}
}