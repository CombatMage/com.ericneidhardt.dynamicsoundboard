package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views;

import android.util.SparseArray;
import android.view.View;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.OpenSoundLayoutSettingsEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRemovedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.model.SoundLayoutsManager;

import java.util.ArrayList;
import java.util.List;

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
public class SoundLayoutsPresenter extends NavigationDrawerListPresenter<SoundLayoutsList> implements SoundLayoutsListAdapter.OnItemClickListener
{
	private static final String TAG = SoundLayoutsPresenter.class.getName();

	private SoundLayoutsManager soundLayoutsManager;
	private SoundLayoutsListAdapter adapter;

	EventBus eventBus;

	public SoundLayoutsPresenter(SoundLayoutsManager soundLayoutsManager, SoundLayoutsListAdapter adapter)
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
	public void onDeleteSelected(SparseArray<View> selectedItems)
	{
		if (this.getView() == null)
			throw new NullPointerException(TAG + ".onPrepareActionMode failed, supplied view is null");

		List<SoundLayout> soundLayoutsToRemove = new ArrayList<>(selectedItems.size());
		for(int i = 0; i < selectedItems.size(); i++)
		{
			int index = selectedItems.keyAt(i);
			soundLayoutsToRemove.add(this.getView().getAdapter().getValues().get(index));
		}
		SoundLayoutsManager manager = SoundLayoutsManager.getInstance();
		manager.delete(soundLayoutsToRemove);
		this.adapter.notifyDataSetChanged();

		this.eventBus.post(new SoundLayoutRemovedEvent());
	}

	@Override
	public void onItemClick(View view, SoundLayout data, int position)
	{
		if (this.getView() == null)
			throw new NullPointerException(TAG + ".onItemClick failed, supplied view is null");

		if (this.isInSelectionMode())
		{
			super.onItemSelectedForDeletion(position);
			data.setIsSelectedForDeletion(!data.isSelectedForDeletion());
		}
		else
		{
			this.soundLayoutsManager.setSelected(position);

			this.getView().toggleVisibility();
			this.eventBus.post(new SoundLayoutSelectedEvent(data));
		}
		this.adapter.notifyItemChanged(position);
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
