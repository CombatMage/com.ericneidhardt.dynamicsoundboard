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
import android.widget.Toast;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.misc.JsonPojo;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.views.edittext.NoUnderscoreEditText;
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration;

import java.io.File;
import java.io.IOException;

/**
 * File created by eric.neidhardt on 12.11.2014.
 */
public class StoreLayoutDialog extends FileExplorerDialog implements View.OnClickListener
{
	private static final String TAG = StoreLayoutDialog.class.getName();

	private NoUnderscoreEditText inputFileName;
	private View confirm;
	private RecyclerView directories;

	public static void showInstance(FragmentManager manager)
	{
		StoreLayoutDialog dialog = new StoreLayoutDialog();
		dialog.show(manager, TAG);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_store_sound_sheets, null);
		view.findViewById(R.id.b_add).setOnClickListener(this);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		this.confirm = view.findViewById(R.id.b_ok);
		this.confirm.setOnClickListener(this);
		this.confirm.setEnabled(false);

		this.inputFileName = (NoUnderscoreEditText) view.findViewById(R.id.et_name_file);

		this.directories = (RecyclerView) view.findViewById(R.id.rv_dialog);
		this.directories.addItemDecoration(new DividerItemDecoration());
		this.directories.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		this.directories.setItemAnimator(new DefaultItemAnimator());
		this.directories.setAdapter(super.getAdapter());

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
		return false;
	}

	@Override
	protected boolean canSelectFile()
	{
		return true;
	}

	@Override
	public void onClick(@NonNull View v)
	{
		switch (v.getId())
		{
			case R.id.b_add:
				this.createFileAndSelect();
				break;
			case R.id.b_cancel:
				this.dismiss();
				break;
			case R.id.b_ok:
				if (super.getAdapter().getSelectedFile() != null)
					this.saveDataAndDismiss();
				else
					Toast.makeText(this.getActivity(), R.string.dialog_store_layout_no_file_info, Toast.LENGTH_SHORT).show();
				break;
		}
	}

	private void createFileAndSelect()
	{
		String fileName = this.inputFileName.getText().toString();
		if (fileName.isEmpty())
		{
			Toast.makeText(this.getActivity(), R.string.dialog_store_layout_no_file_name, Toast.LENGTH_SHORT).show();
			return;
		}

		File file = new File(super.getAdapter().getParentFile(), fileName);
		if (file.exists())
		{
			Toast.makeText(this.getActivity(), R.string.dialog_store_layout_file_exists, Toast.LENGTH_SHORT).show();
			return;
		}

		try
		{
			boolean created = file.createNewFile();
			if (!created)
			{
				Toast.makeText(this.getActivity(), R.string.dialog_store_layout_failed_create_file, Toast.LENGTH_SHORT).show();
				return;
			}

			super.getAdapter().setSelectedFile(file);
			super.getAdapter().refreshDirectory();
			super.getAdapter().notifyDataSetChanged();

			this.onFileSelected();
		}
		catch (IOException e)
		{
			Toast.makeText(this.getActivity(), R.string.dialog_store_layout_failed_create_file, Toast.LENGTH_SHORT).show();
		}
	}

	private void saveDataAndDismiss()
	{
		try
		{
			JsonPojo.writeToFile(super.getAdapter().getSelectedFile(), this.soundSheetsDataAccess.getSoundSheets(),
					this.soundsDataAccess.getPlaylist(), this.soundsDataAccess.getSounds());

			this.dismiss();
		}
		catch (IOException e)
		{
			Logger.d(TAG, e.getMessage());
			Toast.makeText(this.getActivity(), R.string.dialog_store_layout_failed_store_layout, Toast.LENGTH_SHORT).show();
		}

	}

}
