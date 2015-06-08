package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views;

import android.util.SparseArray;
import android.view.View;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundDataModel;

import java.util.ArrayList;
import java.util.List;

/**
 * File created by eric.neidhardt on 26.05.2015.
 */
public class PlaylistPresenter extends NavigationDrawerListPresenter<Playlist> implements PlaylistAdapter.OnItemClickListener
{
	private static final String TAG = PlaylistPresenter.class.getName();

	private SoundDataModel model;

	@Override
	protected boolean isEventBusSubscriber()
	{
		return false;
	}

	@Override
	public void onItemClick(View view, EnhancedMediaPlayer player, int position)
	{
		if (super.isInSelectionMode)
			super.onItemSelected(view, position);
		else
			this.getView().getAdapter().startOrStopPlayList(player);
	}

	@Override
	public void onDeleteSelected(SparseArray<View> selectedItems)
	{
		if (this.getView() == null)
			throw new NullPointerException(TAG + ".onPrepareActionMode failed, supplied view is null ");

		List<EnhancedMediaPlayer> playersToRemove = new ArrayList<>(selectedItems.size());
		for(int i = 0; i < selectedItems.size(); i++)
		{
			int index = selectedItems.keyAt(i);
			playersToRemove.add(this.getView().getAdapter().getValues().get(index));
		}

		this.model.removeSoundsFromPlaylist(playersToRemove);
		this.getView().getAdapter().notifyDataSetChanged();
	}

	SoundDataModel getSoundDataModel()
	{
		return model;
	}

	void setSoundDataModel(SoundDataModel model)
	{
		this.model = model;
	}
}
