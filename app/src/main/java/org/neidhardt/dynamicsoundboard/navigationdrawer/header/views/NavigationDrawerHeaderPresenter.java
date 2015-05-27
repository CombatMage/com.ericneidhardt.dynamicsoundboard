package org.neidhardt.dynamicsoundboard.navigationdrawer.header.views;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OpenSoundLayoutsEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRemovedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRenamedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutModel;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views.SoundLayoutSettingsDialog;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views.SoundLayoutsPresenter;
import org.neidhardt.dynamicsoundboard.presenter.BaseViewPresenter;

/**
 * Created by eric.neidhardt on 27.05.2015.
 */
public class NavigationDrawerHeaderPresenter
		extends
			BaseViewPresenter<NavigationDrawerHeader>
		implements
			SoundLayoutSettingsDialog.OnSoundLayoutRenamedEventListener,
			SoundLayoutsPresenter.OnSoundLayoutRemovedEventListener,
			SoundLayoutsPresenter.OnSoundLayoutSelectedEventListener,
			EnhancedMediaPlayer.OnMediaPlayerStateChangedEvent
{
	private SoundLayoutModel model;

	public NavigationDrawerHeaderPresenter(SoundLayoutModel model)
	{
		super();
		this.model = model;
	}

	@Override
	protected boolean isEventBusSubscriber()
	{
		return true;
	}

	@Override
	public void setView(NavigationDrawerHeader view)
	{
		super.setView(view);
		if (view != null)
		{
			view.showCurrentLayoutName(this.model.getActiveSoundLayout().getLabel());
			view.setCurrentSoundCount(42); // TODO get sound count
		}
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(SoundLayoutRenamedEvent event)
	{
		if (this.getView() == null)
			return;

		this.getView().showCurrentLayoutName(this.model.getActiveSoundLayout().getLabel());
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(SoundLayoutRemovedEvent event)
	{
		if (this.getView() == null)
			return;

		this.getView().showCurrentLayoutName(this.model.getActiveSoundLayout().getLabel());
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(SoundLayoutSelectedEvent event)
	{
		if (this.getView() == null)
			return;

		this.getView().showCurrentLayoutName(this.model.getActiveSoundLayout().getLabel());
	}

	@Override
	@SuppressWarnings("unused")
	public void onEventMainThread(MediaPlayerStateChangedEvent event)
	{
		if (this.getView() == null)
			return;

		this.getView().setCurrentSoundCount(42); // TODO get sound count
		this.getView().animateHeaderAvatarRotate();
	}

	public void onChangeLayoutClicked()
	{
		this.getView().animateLayoutChanges();
		this.getBus().post(new OpenSoundLayoutsEvent());
	}

	public interface OnOpenSoundLayoutsEvent
	{
		/**
		 * This is called by greenRobot EventBus when the user clicks on the open SoundLayouts button in navigation drawer header.
		 * @param event delivered OpenSoundLayoutsEvent
		 */
		void onEvent(OpenSoundLayoutsEvent event);
	}
}
