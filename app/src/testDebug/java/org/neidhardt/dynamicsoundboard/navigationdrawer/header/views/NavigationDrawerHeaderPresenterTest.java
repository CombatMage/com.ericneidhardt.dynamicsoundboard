package org.neidhardt.dynamicsoundboard.navigationdrawer.header.views;

import de.greenrobot.event.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OpenSoundLayoutsEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRemovedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRenamedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutModel;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * Created by eric.neidhardt on 27.05.2015.
 */
public class NavigationDrawerHeaderPresenterTest extends BaseTest {

	private NavigationDrawerHeaderPresenter presenter;

	private NavigationDrawerHeader view;
	private SoundLayoutModel model;
	private EventBus bus;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();

		this.bus = mock(EventBus.class);
		this.view = mock(NavigationDrawerHeader.class);

		SoundLayout activeSoundLayout = mock(SoundLayout.class);
		when(activeSoundLayout.getLabel()).thenReturn("testLabel");

		this.model = mock(SoundLayoutModel.class);
		when(this.model.getActiveSoundLayout()).thenReturn(activeSoundLayout);

		this.presenter = new NavigationDrawerHeaderPresenter(this.model);
		this.presenter.setView(this.view);
		this.presenter.setBus(this.bus);
	}

	@Test
	public void testIsEventBusSubscriber() throws Exception
	{
		assertTrue(this.presenter.isEventBusSubscriber());
	}

	@Test
	public void testSetView() throws Exception
	{
		verify(this.view, times(1)).showCurrentLayoutName(this.model.getActiveSoundLayout().getLabel());

		this.presenter.setView(null);
		verify(this.view, times(1)).showCurrentLayoutName(anyString());
	}



	@Test
	public void testOnSoundLayoutRenamedEvent() throws Exception
	{
		this.presenter.onEvent(new SoundLayoutRenamedEvent(null));
		verify(this.view, times(2)).showCurrentLayoutName(this.model.getActiveSoundLayout().getLabel());

		this.presenter.setView(null);
		this.presenter.onEvent(new SoundLayoutRenamedEvent(null));
		verify(this.view, times(2)).showCurrentLayoutName(anyString());
	}

	@Test
	public void testOnSoundLayoutRemovedEvent() throws Exception
	{
		this.presenter.onEvent(new SoundLayoutRemovedEvent());
		verify(this.view, times(2)).showCurrentLayoutName(this.model.getActiveSoundLayout().getLabel());

		this.presenter.setView(null);
		this.presenter.onEvent(new SoundLayoutRemovedEvent());
		verify(this.view, times(2)).showCurrentLayoutName(anyString());
	}

	@Test
	public void testOnSoundLayoutSelectedEvent() throws Exception
	{
		this.presenter.onEvent(new SoundLayoutSelectedEvent(null));
		verify(this.view, times(2)).showCurrentLayoutName(this.model.getActiveSoundLayout().getLabel());

		this.presenter.setView(null);
		this.presenter.onEvent(new SoundLayoutSelectedEvent(null));
		verify(this.view, times(2)).showCurrentLayoutName(anyString());
	}

	@Test
	public void testOnChangeLayoutClicked() throws Exception
	{
		this.presenter.onChangeLayoutClicked();

		verify(this.view, times(1)).animateLayoutChanges();
		verify(this.bus, times(1)).post(any(OpenSoundLayoutsEvent.class));
	}
}