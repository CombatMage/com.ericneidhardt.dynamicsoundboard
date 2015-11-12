package org.neidhardt.dynamicsoundboard.navigationdrawer.header;

import de.greenrobot.event.EventBus;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsRequestedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRemovedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRenamedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent;
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * File created by eric.neidhardt on 27.05.2015.
 */
public class NavigationDrawerHeaderPresenterTest extends BaseTest
{
	private NavigationDrawerHeaderPresenter presenter;

	@Mock
	private NavigationDrawerHeader view;
	@Mock
	private SoundLayoutsAccess soundLayoutModel;
	@Mock
	private EventBus bus;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();

		MockitoAnnotations.initMocks(this);

		SoundLayout activeSoundLayout = mock(SoundLayout.class);
		when(activeSoundLayout.getLabel()).thenReturn("testLabel");
		when(this.soundLayoutModel.getActiveSoundLayout()).thenReturn(activeSoundLayout);

		this.presenter = new NavigationDrawerHeaderPresenter(this.bus, this.soundLayoutModel);
		this.presenter.setView(this.view);
	}

	@Test
	public void testIsEventBusSubscriber() throws Exception
	{
		assertTrue(this.presenter.getIsEventBusSubscriber());
	}

	@Test
	public void testOnAttachedToWindow_1() throws Exception
	{
		this.presenter.onAttachedToWindow();
		verify(this.view, times(1)).showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().getLabel());
	}

	@Test(expected = NullPointerException.class)
	public void testOnAttachedToWindow_2() throws Exception
	{
		this.presenter.setView(null);
		this.presenter.onAttachedToWindow();
	}

	@Test
	public void testOnSoundLayoutRenamedEvent() throws Exception
	{
		this.presenter.onEvent(new SoundLayoutRenamedEvent(new SoundLayout()));
		verify(this.view, times(1)).showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().getLabel());

		this.presenter.setView(null);
		this.presenter.onEvent(new SoundLayoutRenamedEvent(new SoundLayout()));
		verify(this.view, times(1)).showCurrentLayoutName(anyString());
	}

	@Test
	public void testOnSoundLayoutRemovedEvent() throws Exception
	{
		this.presenter.onEvent(new SoundLayoutRemovedEvent());
		verify(this.view, times(1)).showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().getLabel());

		this.presenter.setView(null);
		this.presenter.onEvent(new SoundLayoutRemovedEvent());
		verify(this.view, times(1)).showCurrentLayoutName(anyString());
	}

	@Test
	public void testOnSoundLayoutSelectedEvent() throws Exception
	{
		this.presenter.onEvent(new SoundLayoutSelectedEvent(new SoundLayout()));
		verify(this.view, times(1)).showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().getLabel());

		this.presenter.setView(null);
		this.presenter.onEvent(new SoundLayoutSelectedEvent(new SoundLayout()));
		verify(this.view, times(1)).showCurrentLayoutName(anyString());
	}

	@Test
	public void testOnChangeLayoutClicked() throws Exception
	{
		this.presenter.onChangeLayoutClicked();

		verify(this.view, times(1)).animateLayoutChanges();
		verify(this.bus, times(1)).post(any(OpenSoundLayoutsRequestedEvent.class));
	}
}