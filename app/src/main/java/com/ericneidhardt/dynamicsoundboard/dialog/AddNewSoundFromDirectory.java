package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.net.Uri;
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
import com.ericneidhardt.dynamicsoundboard.dao.MediaPlayerData;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.storage.SoundManagerFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
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
	private String callingFragmentTag;

	public static void showInstance(FragmentManager manager, String callingFragmentTag)
	{
		AddNewSoundFromDirectory dialog = new AddNewSoundFromDirectory();

		Bundle args = new Bundle();
		args.putString(KEY_CALLING_FRAGMENT_TAG, callingFragmentTag);
		dialog.setArguments(args);

		dialog.show(manager, TAG);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = this.getArguments();
		if (args != null)
			this.callingFragmentTag = args.getString(KEY_CALLING_FRAGMENT_TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound_from_directory, null);
		view.findViewById(R.id.b_ok).setOnClickListener(this);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);

		RecyclerView directories = (RecyclerView)view.findViewById(R.id.rv_directories);
		directories.addItemDecoration(new DividerItemDecoration(this.getActivity(), null));
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

	private List<File> buildResult()
	{
		List<File> files = new ArrayList<File>();
		if (this.adapter.selectedFile == null)
			return files;
		else if (!this.adapter.selectedFile.isDirectory())
			files.add(this.adapter.selectedFile);
		else
		{
			File[] filesInSelectedDir = this.adapter.selectedFile.listFiles();
			if (filesInSelectedDir != null)
				Collections.addAll(files, filesInSelectedDir);
		}
		return files;
	}

	private void returnResultsToCallingFragment()
	{
		List<File> result = this.buildResult();
		SoundManagerFragment fragment = (SoundManagerFragment)this.getFragmentManager().findFragmentByTag(SoundManagerFragment.TAG);

		for (File file : result)
		{
			Uri soundUri = Uri.parse(file.getAbsolutePath());
			String soundLabel = Util.getFileNameFromUri(this.getActivity(), soundUri);
			MediaPlayerData playerData = EnhancedMediaPlayer.getMediaPlayerData(this.callingFragmentTag, soundUri, soundLabel);

			fragment.addSound(playerData);
		}
		fragment.notifyFragment(this.callingFragmentTag);
	}

	private class DirectoryAdapter extends RecyclerView.Adapter<DirectoryEntry>
	{
		private File parent;
		private File selectedFile;
		private List<File> fileList;

		public DirectoryAdapter()
		{
			this.setParent(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));
			this.notifyDataSetChanged();
		}

		public void setParent(File parent)
		{
			this.parent = parent;
			this.fileList = Util.getFilesInDirectory(this.parent);
			if (this.parent.getParentFile() != null)
				this.fileList.add(0, this.parent.getParentFile());
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
		private ImageView selectionIndicator;
		private TextView fileName;

		public DirectoryEntry(View itemView)
		{
			super(itemView);
			this.fileName = (TextView) itemView.findViewById(R.id.tv_label);
			this.fileType = (ImageView) itemView.findViewById(R.id.iv_file_type);
			this.selectionIndicator = (ImageView) itemView.findViewById(R.id.iv_selected);

			itemView.setOnClickListener(this);
			itemView.setOnLongClickListener(this);
		}

		public void bindData(File file)
		{
			if (file.equals(adapter.parent.getParentFile()))
				this.bindParentDirectory();
			else
			{
				this.fileName.setText(file.getName());
				if (file.isDirectory())
					this.bindDirectory(file);
				else
					this.bindFile(file);

				if (file.equals(adapter.selectedFile))
				{
					this.selectionIndicator.setVisibility(View.VISIBLE);
					this.fileType.setSelected(true);
				}
				else
				{
					this.selectionIndicator.setVisibility(View.INVISIBLE);
					this.fileType.setSelected(false);
				}
			}
		}

		private void bindFile(File file)
		{
			if (Util.isAudioFile(file))
				this.fileType.setImageResource(R.drawable.selector_ic_file_sound);
			else
				this.fileType.setImageResource(R.drawable.selector_ic_file);
		}

		private void bindDirectory(File file)
		{
			if (Util.containsAudioFiles(file))
				this.fileType.setImageResource(R.drawable.selector_ic_folder_sound);
			else
				this.fileType.setImageResource(R.drawable.selector_ic_folder);
		}

		private void bindParentDirectory()
		{
			this.fileName.setText("..");
			this.fileType.setImageResource(R.drawable.selector_ic_parent_directory);
			this.selectionIndicator.setVisibility(View.GONE);
		}

		@Override
		public void onClick(View v)
		{
			File file = adapter.fileList.get(this.getPosition());
			if (!file.isDirectory())
				return;
			adapter.setParent(file);
			adapter.notifyDataSetChanged();
		}

		@Override
		public boolean onLongClick(View v)
		{
			File file = adapter.fileList.get(this.getPosition());
			if (file.equals(adapter.parent.getParentFile()))
				return false;
			adapter.selectedFile = file;
			adapter.notifyDataSetChanged();
			return false;
		}
	}
}
