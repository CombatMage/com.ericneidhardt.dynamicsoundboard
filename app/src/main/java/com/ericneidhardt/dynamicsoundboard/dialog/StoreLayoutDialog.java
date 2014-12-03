package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.DialogEditText;
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.JsonPojo;
import com.ericneidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/**
 * File created by eric.neidhardt on 12.11.2014.
 */
public class StoreLayoutDialog extends FileExplorerDialog implements View.OnClickListener
{
	private static final String TAG = StoreLayoutDialog.class.getName();

	private DialogEditText inputFileName;

	public static void showInstance(FragmentManager manager)
	{
		StoreLayoutDialog dialog = new StoreLayoutDialog();

		dialog.show(manager, TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_store_sound_sheets, null);
		view.findViewById(R.id.b_save).setOnClickListener(this);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_add).setOnClickListener(this);

		this.inputFileName = (DialogEditText) view.findViewById(R.id.et_name_file);

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
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.b_add:
				this.createFileAndSelect();
				break;
			case R.id.b_cancel:
				this.dismiss();
				break;
			case R.id.b_save:
				if (super.adapter.selectedFile != null)
					this.useFile(super.adapter.selectedFile);
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

		File file = new File(super.adapter.parent, fileName);
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
			super.adapter.selectedFile = file;
			super.adapter.refreshDirectory();
			super.adapter.notifyDataSetChanged();
		}
		catch (IOException e)
		{
			Toast.makeText(this.getActivity(), R.string.dialog_store_layout_failed_create_file, Toast.LENGTH_SHORT).show();
		}
	}

	private void useFile(File file)
	{
		ObjectMapper mapper = new ObjectMapper();

		SoundSheetManagerFragment soundSheetManagerFragment = this.getSoundSheetManagerFragment();

		ServiceManagerFragment soundManagerFragment = this.getServiceManagerFragment();

		JsonPojo pojo = new JsonPojo();
		try
		{
			pojo.setSoundSheets(soundSheetManagerFragment.getAll());
			pojo.addPlayList(soundManagerFragment.getPlayList());
			pojo.addSounds(soundManagerFragment.getSounds());

			mapper.writeValue(file, pojo);

			this.dismiss();
		}
		catch (IOException e)
		{
			Logger.d(TAG, e.getMessage());
			Toast.makeText(this.getActivity(), R.string.dialog_store_layout_failed_store_layout, Toast.LENGTH_SHORT).show();
		}
	}

}
