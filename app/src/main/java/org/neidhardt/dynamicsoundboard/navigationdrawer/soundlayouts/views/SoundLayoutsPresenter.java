package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views;

import android.view.View;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.SoundLayoutsAdapter;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OpenSoundLayoutSettingsEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRemovedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent;
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter;

import java.util.ArrayList;
import java.util.List;

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
public class SoundLayoutsPresenter extends NavigationDrawerListPresenter<SoundLayouts> implements SoundLayoutsAdapter.OnItemClickListener
{
	private SoundLayoutsManager soundLayoutsManager;
	private SoundLayoutsAdapter adapter;

	EventBus eventBus;

	public SoundLayoutsPresenter(SoundLayoutsManager soundLayoutsManager, SoundLayoutsAdapter adapter)
	{
		this.soundLayoutsManager = soundLayoutsManager;
		this.adapter = adapter;
		this.eventBus = EventBus.getDefault();
	}

	@Override
	protected boolean isEventBusSubscriber()
	{
		return false;
	}

	@Override
	public void deleteSelectedItems()
	{
		List<SoundLayout> soundLayoutsToRemove = this.getSoundLayoutsSelectedForDeletion();

		SoundLayoutsManager manager = SoundLayoutsManager.Companion.getInstance();
		manager.delete(soundLayoutsToRemove);
		this.adapter.notifyDataSetChanged();
		this.eventBus.post(new SoundLayoutRemovedEvent());

		super.onSelectedItemsDeleted();
	}

	@Override
	public void onItemClick(View view, SoundLayout data, int position)
	{
		if (this.isInSelectionMode())
		{
			data.setIsSelectedForDeletion(!data.isSelectedForDeletion());
			super.onItemSelectedForDeletion();
		}
		else
		{
			this.soundLayoutsManager.setSelected(position);

			this.getView().toggleVisibility();
			this.eventBus.post(new SoundLayoutSelectedEvent(data));
		}
		this.adapter.notifyDataSetChanged();
	}

	@Override
	protected int getNumberOfItemsSelectedForDeletion()
	{
		return this.getSoundLayoutsSelectedForDeletion().size();
	}

	@Override
	protected void deselectAllItemsSelectedForDeletion()
	{
		List<SoundLayout> selectedSoundLayouts = this.getSoundLayoutsSelectedForDeletion();
		for (SoundLayout soundLayout : selectedSoundLayouts)
		{
			soundLayout.setIsSelectedForDeletion(false);
			this.adapter.notifyItemChanged(soundLayout);
		}
	}

	private List<SoundLayout> getSoundLayoutsSelectedForDeletion()
	{
		List<SoundLayout> selectedSoundLayouts = new ArrayList<>();
		List<SoundLayout> existingSoundLayouts = this.adapter.getValues();
		for(SoundLayout soundLayout : existingSoundLayouts)
		{
			if (soundLayout.isSelectedForDeletion())
				selectedSoundLayouts.add(soundLayout);
		}
		return selectedSoundLayouts;
	}

	@Override
	public void onItemSettingsClicked(SoundLayout data)
	{
		this.eventBus.post(new OpenSoundLayoutSettingsEvent(data));
	}

	public interface OnSoundLayoutRemovedEventListener
	{
		/**
		 * This is called by greenRobot EventBus in case a new SoundLayout was renamed.
		 * @param event delivered SoundLayoutRenamedEvent
		 */
		void onEvent(SoundLayoutRemovedEvent event);
	}

	public interface OnSoundLayoutSelectedEventListener
	{
		/**
		 * This is called by greenRobot EventBus in case a new SoundLayout was selected.
		 * @param event delivered SoundLayoutRenamedEvent
		 */
		void onEvent(SoundLayoutSelectedEvent event);
	}

	public interface OnOpenSoundLayoutSettingsEvent
	{
		/**
		 * This is called by greenRobot EventBus when the settings dialog for a certain SoundLayout is requested.
		 * @param event Delivered OpenSoundLayoutSettingsEvent
		 */
		void onEvent(OpenSoundLayoutSettingsEvent event);
	}
}
