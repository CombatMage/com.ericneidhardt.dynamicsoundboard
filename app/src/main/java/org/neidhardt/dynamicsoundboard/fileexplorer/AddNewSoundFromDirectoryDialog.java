package org.neidhardt.dynamicsoundboard.fileexplorer;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadSoundsFromFileListTask;
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Project created by Eric Neidhardt on 30.09.2014.
 */
public class AddNewSoundFromDirectoryDialog
		extends
			FileExplorerDialog
		implements
			View.OnClickListener
{
	private static final String TAG = AddNewSoundFromDirectoryDialog.class.getName();

	protected static final String KEY_CALLING_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.fileexplorer.AddNewSoundFromDirectoryDialog.callingFragmentTag";

	private String callingFragmentTag;
	private View confirm;
	private RecyclerView directories;

	public static void showInstance(FragmentManager manager, String callingFragmentTag)
	{
		AddNewSoundFromDirectoryDialog dialog = new AddNewSoundFromDirectoryDialog();

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

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound_from_directory, null);
		this.confirm = view.findViewById(R.id.b_ok);
		this.confirm.setOnClickListener(this);
		this.confirm.setEnabled(false);

		view.findViewById(R.id.b_cancel).setOnClickListener(this);

		this.directories = (RecyclerView)view.findViewById(R.id.rv_dialog);
		this.directories.addItemDecoration(new DividerItemDecoration());
		this.directories.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		this.directories.setItemAnimator(new DefaultItemAnimator());
		this.directories.setAdapter(super.getAdapter());

		String previousPath = this.getPathFromSharedPreferences(TAG);
		if (previousPath != null)
			super.getAdapter().setParent(new File(previousPath));

		AppCompatDialog dialog = new AppCompatDialog(this.getActivity(), R.style.DialogThemeNoTitle);
		dialog.setContentView(view);

		return dialog;
	}

	@Override
	protected void onFileSelected()
	{
		this.confirm.setEnabled(true);

		int position = super.getAdapter().getFileList().indexOf(super.getAdapter().getSelectedFile());
		this.directories.scrollToPosition(position);
	}

	@Override
	protected boolean canSelectDirectory()
	{
		return true;
	}

	@Override
	protected boolean canSelectFile()
	{
		return true;
	}

	@Override
	public void onClick(@NonNull View v)
	{
		if (v.getId() == R.id.b_cancel)
			this.dismiss();
		else if (v.getId() == R.id.b_ok)
		{
			File currentDirectory = super.getAdapter().getParentFile();
			if (currentDirectory != null)
				this.storePathToSharedPreferences(TAG, currentDirectory.getPath());

			this.returnResultsToCallingFragment();
			this.dismiss();
		}
	}

	private List<File> buildResult()
	{
		List<File> files = new ArrayList<>();
		DirectoryAdapter adapter = super.getAdapter();

		if (adapter.getSelectedFile() == null)
			return files;
		else if (!adapter.getSelectedFile().isDirectory())
			files.add(adapter.getSelectedFile());
		else
		{
			File[] filesInSelectedDir = adapter.getSelectedFile().listFiles();
			if (filesInSelectedDir != null)
				Collections.addAll(files, filesInSelectedDir);
		}
		return files;
	}

	private void returnResultsToCallingFragment()
	{
		List<File> result = this.buildResult();
		LoadSoundsFromFileListTask task = new LoadSoundsFromFileListTask(result, this.callingFragmentTag, this.getSoundsDataStorage());
		task.execute();
	}
}
