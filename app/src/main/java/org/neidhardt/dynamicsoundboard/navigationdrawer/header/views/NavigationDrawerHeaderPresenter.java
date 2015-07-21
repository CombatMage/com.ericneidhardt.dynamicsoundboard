package org.neidhardt.dynamicsoundboard.navigationdrawer.header.views;

import android.support.annotation.NonNull;
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.*;
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.views.SoundLayoutSettingsDialog;
import org.neidhardt.dynamicsoundboard.presenter.BaseViewPresenter;
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess;

/**
 * File created by eric.neidhardt on 27.05.2015.
 */
public class NavigationDrawerHeaderPresenter
		extends
			BaseViewPresenter<NavigationDrawerHeader>
		implements
			SoundLayoutSettingsDialog.OnSoundLayoutRenamedEventListener,
			OnSoundLayoutRemovedEventListener,
			OnSoundLayoutSelectedEventListener
{
	private static final String TAG = NavigationDrawerHeaderPresenter.class.getName();

	private SoundLayoutsAccess soundLayoutModel;

	public void setSoundLayoutModel(SoundLayoutsAccess soundLayoutModel)
	{
		this.soundLayoutModel = soundLayoutModel;
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

		if (this.soundLayoutModel == null)
			throw new NullPointerException(TAG +": supplied model is null");

		this.getView().showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().getLabel());
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
	public void onEvent(@NonNull SoundLayoutRemovedEvent event)
	{
		if (this.getView() == null || this.soundLayoutModel == null)
			return;

		this.getView().showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().getLabel());
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(@NonNull SoundLayoutSelectedEvent event)
	{
		if (this.getView() == null || this.soundLayoutModel == null)
			return;

		this.getView().animateLayoutChanges();
		this.getView().showCurrentLayoutName(this.soundLayoutModel.getActiveSoundLayout().getLabel());
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
