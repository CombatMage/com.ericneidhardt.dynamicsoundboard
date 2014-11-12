package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
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
			FileExplorerDialog
		implements
			View.OnClickListener
{
	private static final String TAG = AddNewSoundFromDirectory.class.getName();

	private static final String KEY_CALLING_FRAGMENT_TAG = "com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundFromDirectory.callingFragmentTag";

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
		view.findViewById(R.id.b_add).setOnClickListener(this);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);

		RecyclerView directories = (RecyclerView)view.findViewById(R.id.rv_directories);
		directories.addItemDecoration(new DividerItemDecoration(this.getActivity(), Color.TRANSPARENT));
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
		else if (v.getId() == R.id.b_add)
		{
			this.returnResultsToCallingFragment();
			this.dismiss();
		}
	}

	private List<File> buildResult()
	{
		List<File> files = new ArrayList<File>();
		if (super.adapter.selectedFile == null)
			return files;
		else if (!super.adapter.selectedFile.isDirectory())
			files.add(super.adapter.selectedFile);
		else
		{
			File[] filesInSelectedDir = super.adapter.selectedFile.listFiles();
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
}
