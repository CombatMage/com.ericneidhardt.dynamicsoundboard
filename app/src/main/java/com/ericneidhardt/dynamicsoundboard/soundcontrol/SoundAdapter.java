package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.CustomEditText;
import com.ericneidhardt.dynamicsoundboard.customview.DialogEditText;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric.neidhardt on 10.09.2014.
 */
public class SoundAdapter extends RecyclerView.Adapter<SoundAdapter.ViewHolder>
{
	private List<EnhancedMediaPlayer> mediaPlayers;

	public SoundAdapter()
	{
		this.mediaPlayers = new ArrayList<EnhancedMediaPlayer>();
	}

	public void add(EnhancedMediaPlayer mediaPlayer)
	{
		this.mediaPlayers.add(mediaPlayer);
		this.notifyItemInserted(this.mediaPlayers.size());
	}

	public void addAll(List<EnhancedMediaPlayer> mediaPlayers)
	{
		if (mediaPlayers == null)
			return;
		this.mediaPlayers.addAll(mediaPlayers);
		this.notifyDataSetChanged();
	}

	public void remove(EnhancedMediaPlayer mediaPlayer)
	{
		int position = this.mediaPlayers.indexOf(mediaPlayer);
		this.mediaPlayers.remove(position);
		this.notifyItemRemoved(position);
	}

	public void clear()
	{
		this.mediaPlayers.clear();
		this.notifyDataSetChanged();
	}

	public List<EnhancedMediaPlayer> getValues()
	{
		return this.mediaPlayers;
	}

	@Override
	public int getItemCount()
	{
		return this.mediaPlayers.size();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_sound_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		EnhancedMediaPlayer data = this.mediaPlayers.get(position);

		holder.name.setText(data.getMediaPlayerData().getLabel());
	}

	public class ViewHolder extends RecyclerView.ViewHolder implements CustomEditText.OnTextEditedListener
	{
		private DialogEditText name;
		private CheckBox play;
		private CheckBox loop;
		private CheckBox favorite;
		private View stop;

		public ViewHolder(View itemView)
		{
			super(itemView);

			this.name = (DialogEditText)itemView.findViewById(R.id.et_name_file);
			this.play = (CheckBox) itemView.findViewById(R.id.cb_play);
			this.loop = (CheckBox) itemView.findViewById(R.id.cb_loop);
			this.favorite = (CheckBox)itemView.findViewById(R.id.cb_add_to_playlist);
			this.stop = itemView.findViewById(R.id.b_stop);

			this.name.setOnTextEditedListener(this);
		}

		@Override
		public void onTextEdited(String text)
		{
			mediaPlayers.get(getPosition()).getMediaPlayerData().setLabel(text);
		}
	}
}
