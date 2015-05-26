package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views;

import android.util.SparseArray;
import android.view.View;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerListPresenter;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutChangedEvent;

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
			throw new NullPointerException(TAG + ".onPrepareActionMode failed, supplied view is null ");

		List<SoundLayout> soundLayoutsToRemove = new ArrayList<>(selectedItems.size());
		for(int i = 0; i < selectedItems.size(); i++)
		{
			int index = selectedItems.keyAt(i);
			soundLayoutsToRemove.add(this.getView().getAdapter().getValues().get(index));
		}
		SoundLayoutsManager manager = SoundLayoutsManager.getInstance();
		manager.delete(soundLayoutsToRemove);
		this.getView().getAdapter().notifyDataSetChanged();

		EventBus.getDefault().post(new SoundLayoutChangedEvent(manager.getActiveSoundLayout(), SoundLayoutChangedEvent.REQUEST.LAYOUT_REMOVED));
	}

	public void onItemClick(View view, SoundLayout data, int position)
	{
		if (this.getView() == null)
			throw new NullPointerException(TAG + ".onItemClick failed, supplied view is null ");

		if (super.isInSelectionMode)
			super.onItemSelected(view, position);
		else
		{
			this.getView().getAdapter().setSelectedItem(position);
			EventBus.getDefault().post(new SoundLayoutChangedEvent(data, SoundLayoutChangedEvent.REQUEST.CURRENT_LAYOUT_CHANGED));

			this.getView().toggleVisibility();
		}
	}
}
