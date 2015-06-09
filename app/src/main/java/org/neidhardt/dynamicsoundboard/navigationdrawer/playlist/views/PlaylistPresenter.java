package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views;

import android.util.SparseArray;
import android.view.View;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
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

	private PlaylistAdapter adapter;
	private SoundDataModel model;

	@Override
	protected boolean isEventBusSubscriber()
	{
		return false;
	}

	@Override
	public void onItemClick(View view, EnhancedMediaPlayer player, int position)
	{
		if (this.isInSelectionMode())
		{
			super.onItemSelectedForDeletion();
			player.getMediaPlayerData().setIsSelectedForDeletion(true);
			this.adapter.notifyItemChanged(position);
		}
		else
			this.adapter.startOrStopPlayList(player);
	}

	@Override
	public void onDeleteSelected()
	{
		List<EnhancedMediaPlayer> playersToRemove = new ArrayList<>();
		List<EnhancedMediaPlayer> existingSoundSheets = this.adapter.getValues();

		for(EnhancedMediaPlayer player : existingSoundSheets)
		{
			if (player.getMediaPlayerData().isSelectedForDeletion())
				playersToRemove.add(player);
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

	public void setAdapter(PlaylistAdapter adapter)
	{
		this.adapter = adapter;
	}
}
