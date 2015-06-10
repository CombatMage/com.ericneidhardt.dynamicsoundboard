package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views;

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
			player.getMediaPlayerData().setIsSelectedForDeletion(!player.getMediaPlayerData().isSelectedForDeletion());
			this.adapter.notifyItemChanged(position);
			super.onItemSelectedForDeletion();
		}
		else
			this.adapter.startOrStopPlayList(player);
	}

	@Override
	public void deleteSelectedItems()
	{
		List<EnhancedMediaPlayer> playersToRemove = this.getPlayersSelectedForDeletion();

		this.model.removeSoundsFromPlaylist(playersToRemove);
		this.adapter.notifyDataSetChanged();

		super.onSelectedItemsDeleted();
	}

	@Override
	protected int getNumberOfItemsSelectedForDeletion()
	{
		return this.getPlayersSelectedForDeletion().size();
	}

	@Override
	protected void deselectAllItemsSelectedForDeletion()
	{
		List<EnhancedMediaPlayer> selectedPlayers = this.getPlayersSelectedForDeletion();
		for (EnhancedMediaPlayer player : selectedPlayers)
		{
			player.getMediaPlayerData().setIsSelectedForDeletion(false);
			this.adapter.notifyItemChanged(player);
		}
	}

	private List<EnhancedMediaPlayer> getPlayersSelectedForDeletion()
	{
		List<EnhancedMediaPlayer> selectedPlayers = new ArrayList<>();
		List<EnhancedMediaPlayer> existingPlayers = this.adapter.getValues();
		for(EnhancedMediaPlayer players : existingPlayers)
		{
			if (players.getMediaPlayerData().isSelectedForDeletion())
				selectedPlayers.add(players);
		}
		return selectedPlayers;
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
