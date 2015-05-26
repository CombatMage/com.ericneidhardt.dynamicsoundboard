package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views;

import android.util.SparseArray;
import android.view.View;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListPresenter;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRemovedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric.neidhardt on 26.05.2015.
 */
public class SoundLayoutsPresenter extends NavigationDrawerListPresenter<SoundLayoutsList>
{
	private static final String TAG = SoundLayoutsPresenter.class.getName();

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
		this.getView().getAdapter().notifyDataSetChanged();

		EventBus.getDefault().post(new SoundLayoutRemovedEvent());
	}

	public void onItemClick(View view, SoundLayout data, int position)
	{
		if (this.getView() == null)
			throw new NullPointerException(TAG + ".onItemClick failed, supplied view is null");

		if (super.isInSelectionMode)
			super.onItemSelected(view, position);
		else
		{
			this.getView().getAdapter().setSelectedItem(position);
			this.getView().toggleVisibility();
			EventBus.getDefault().post(new SoundLayoutSelectedEvent(data));
		}
	}

	public interface OnSoundLayoutRemovedEventListener
	{
		/**
		 * This is called by greenRobot EventBus in case a new SoundLayout was renamed.
		 * @param event delivered SoundLayoutRenamedEvent
		 */
		@SuppressWarnings("unused")
		void onEvent(SoundLayoutRemovedEvent event);
	}

	public interface OnSoundLayoutSelectedEventListener
	{
		/**
		 * This is called by greenRobot EventBus in case a new SoundLayout was selected.
		 * @param event delivered SoundLayoutRenamedEvent
		 */
		@SuppressWarnings("unused")
		void onEvent(SoundLayoutSelectedEvent event);
	}

}
