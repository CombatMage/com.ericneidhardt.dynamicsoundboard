package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views;

import android.view.View;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * File created by eric.neidhardt on 09.06.2015.
 */
public class SoundLayoutsPresenterTest extends BaseTest
{
	private SoundLayoutsPresenter presenter;
	@Mock private SoundLayoutsList mockList;
	@Mock private SoundLayoutsListAdapter mockAdapter;
	@Mock private SoundLayoutsManager mockManager;

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		MockitoAnnotations.initMocks(this);

		this.presenter = spy(new SoundLayoutsPresenter(this.mockManager, this.mockAdapter));
		this.presenter.setView(this.mockList);
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
		verify(this.mockAdapter, times(1)).notifyItemChanged(position);
	}
}