package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import com.ericneidhardt.dynamicsoundboard.misc.Util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Project created by Eric Neidhardt on 30.09.2014.
 */
public class AddNewSoundFromDirectory
		extends
			DialogFragment
		implements
			View.OnClickListener
{
	private static final String TAG = AddNewSoundFromDirectory.class.getSimpleName();

	private static final String KEY_CALLING_FRAGMENT_TAG = "com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundFromDirectory.callingFragmentTag";

	private DirectoryAdapter adapter;

	public static void showInstance(FragmentManager manager, String callingFragmentTag)
	{
		AddNewSoundFromDirectory dialog = new AddNewSoundFromDirectory();

		Bundle args = new Bundle();
		args.putString(KEY_CALLING_FRAGMENT_TAG, callingFragmentTag);
		dialog.setArguments(args);

		dialog.show(manager, TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
			View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound_from_directory, null);
			view.findViewById(R.id.b_ok).setOnClickListener(this);
			view.findViewById(R.id.b_cancel).setOnClickListener(this);

		RecyclerView directories = (RecyclerView)view.findViewById(R.id.rv_directories);
		directories.addItemDecoration(new DividerItemDecoration(this.getActivity(), DividerItemDecoration.VERTICAL_LIST, null));
		directories.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		directories.setItemAnimator(new DefaultItemAnimator());

		this.adapter = new DirectoryAdapter();
		directories.setAdapter(this.adapter);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.b_cancel)
			this.dismiss();
		else if (v.getId() == R.id.b_ok)
		{
			this.returnResultsToCallingFragment();
			this.dismiss();
		}
	}

	private void returnResultsToCallingFragment()
	{
		// TODO
	}

	private class DirectoryAdapter extends RecyclerView.Adapter<DirectoryEntry>
	{
		private File parent;
		private File selectedFile;
		private List<File> fileList;

		public DirectoryAdapter()
		{
			this.fileList = new ArrayList<File>();
			this.fileList = Util.getFilesInDirectory(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
			this.notifyDataSetChanged();
		}

		@Override
		public DirectoryEntry onCreateViewHolder(ViewGroup parent, int i)
		{
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.view_directory_item, parent, false);
			return new DirectoryEntry(view);
		}

		@Override
		public void onBindViewHolder(DirectoryEntry directoryEntry, int position)
		{
			File file = this.fileList.get(position);
			directoryEntry.bindData(file);
		}

		@Override
		public int getItemCount()
		{
			return this.fileList.size();
		}
	}

	private class DirectoryEntry
			extends
			RecyclerView.ViewHolder
			implements
			View.OnClickListener,
			View.OnLongClickListener
	{
		private ImageView fileType;
		private ImageView isSelected;
		private TextView fileName;

		public DirectoryEntry(View itemView)
		{
			super(itemView);
			this.fileName = (TextView) itemView.findViewById(R.id.tv_label);
			this.fileType = (ImageView) itemView.findViewById(R.id.iv_file_type);
			this.isSelected = (ImageView) itemView.findViewById(R.id.iv_selected);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		public void bindData(File file)
		{
			this.fileName.setText(file.getName());
			if (file.equals(adapter.selectedFile))
				this.isSelected.setVisibility(View.VISIBLE);
			else
				this.isSelected.setVisibility(View.INVISIBLE);
		}

		@Override
		public void onClick(View v)
		{

		}

		@Override
		public boolean onLongClick(View v)
		{
			adapter.selectedFile = adapter.fileList.get(this.getPosition());
			adapter.notifyDataSetChanged();
			return false;
		}
	}
}
