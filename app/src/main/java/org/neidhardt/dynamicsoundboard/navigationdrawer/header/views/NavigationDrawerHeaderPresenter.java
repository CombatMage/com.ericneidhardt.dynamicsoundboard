package org.neidhardt.dynamicsoundboard.navigationdrawer.header.views;

import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRemovedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRenamedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutModel;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views.SoundLayoutSettingsDialog;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views.SoundLayoutsPresenter;
import org.neidhardt.dynamicsoundboard.presenter.BaseViewPresenter;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundDataModel;

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
	private static final String TAG = NavigationDrawerHeaderPresenter.class.getName();

	private SoundLayoutModel soundLayoutModel;
	private SoundDataModel soundDataModel;

	public void setSoundLayoutModel(SoundLayoutModel soundLayoutModel)
	{
		this.soundLayoutModel = soundLayoutModel;
	}

	public void setSoundDataModel(SoundDataModel soundDataModel)
	{
		this.soundDataModel = soundDataModel;
	}

	@Override
	protected boolean isEventBusSubscriber()
	{
		return true;
	}

	@Override
	public void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		if (this.getView() == null)
			throw new NullPointerException(TAG +": supplied view is null");

		if (this.soundLayoutModel == null || this.soundDataModel == null)
			throw new NullPointerException(TAG +": supplied model is null");

		this.getView().showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().getLabel());
		this.getView().setCurrentSoundCount(this.soundDataModel.getCurrentlyPlayingSounds().size());
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(SoundLayoutRenamedEvent event)
	{
		if (this.getView() == null || this.soundLayoutModel == null)
			return;

		this.getView().showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().getLabel());
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(SoundLayoutRemovedEvent event)
	{
		if (this.getView() == null || this.soundLayoutModel == null)
			return;

		this.getView().showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().getLabel());
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(SoundLayoutSelectedEvent event)
	{
		if (this.getView() == null || this.soundLayoutModel == null)
			return;

		this.getView().showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().getLabel());
	}

	@Override
	@SuppressWarnings("unused")
	public void onEventMainThread(MediaPlayerStateChangedEvent event)
	{
		if (this.getView() == null || this.soundDataModel == null)
			return;

		this.getView().setCurrentSoundCount(this.soundDataModel.getCurrentlyPlayingSounds().size());
		this.getView().animateHeaderAvatarRotate();
	}

	public void onChangeLayoutClicked()
	{
		this.getView().animateLayoutChanges();
		this.getEventBus().post(new OpenSoundLayoutsEvent());
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
