package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.ericneidhardt.dynamicsoundboard.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric.neidhardt on 29.08.2014.
 */
public class SoundSheetAdapter extends RecyclerView.Adapter<SoundSheetAdapter.ViewHolder>
{
	private List<String> soundLayoutIds;

	public SoundSheetAdapter()
	{
		this.soundLayoutIds = new ArrayList<String>();
	}

	public void addFragmentWithId(String fragmentId) {
		this.soundLayoutIds.add(fragmentId);
		this.notifyItemInserted(this.soundLayoutIds.size());
	}

	public void removeFragmentWithId(String fragmentId) {
		int position = this.soundLayoutIds.indexOf(fragmentId);
		this.soundLayoutIds.remove(position);
		this.notifyItemRemoved(position);
	}

	public void openDialogAddNewSoundLayout(Context context)
	{
		final View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_add_new_sound_layout, null);
		((EditText)dialogView.findViewById(R.id.et_input)).setText("test" + this.soundLayoutIds.size());

		AlertDialog.Builder inputNameDialog = new AlertDialog.Builder(context);
		inputNameDialog.setView(dialogView);

		final AlertDialog dialog = inputNameDialog.create();
		dialogView.findViewById(R.id.b_cancel).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				dialog.dismiss();
			}
		});
		dialogView.findViewById(R.id.b_ok).setOnClickListener(new View.OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String newSoundLayoutId = ((EditText)dialogView.findViewById(R.id.et_input)).getText().toString();
				addFragmentWithId(newSoundLayoutId);
				dialog.dismiss();
			}
		});

		dialog.show();
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int position)
	{
		View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.navigation_drawer_item, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position)
	{
		String fragmentId = this.soundLayoutIds.get(position);
		holder.textView.setText(fragmentId);
	}

	@Override
	public int getItemCount()
	{
		return this.soundLayoutIds.size();
	}

	public static class ViewHolder extends RecyclerView.ViewHolder
	{
		private TextView textView;

		public ViewHolder(View itemView) {
			super(itemView);
			this.textView = (TextView)itemView.findViewById(R.id.tv_label);
		}
	}

	public static class ItemDivider extends RecyclerView.ItemDecoration
	{
		@Override
		public void getItemOffsets(Rect outRect, int itemPosition, RecyclerView parent)
		{
			super.getItemOffsets(outRect, itemPosition, parent);
			outRect.set(0, 1, parent.getRight(), 0);
		}
	}

}
