package org.neidhardt.dynamicsoundboard.fileexplorer;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Toast;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.misc.JsonPojo;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.notifications.service.ServiceManagerFragment;
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

	public static void showInstance(FragmentManager manager)
	{
		StoreLayoutDialog dialog = new StoreLayoutDialog();

		dialog.show(manager, TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_store_sound_sheets, null);
		view.findViewById(R.id.b_add).setOnClickListener(this);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_ok).setOnClickListener(this);

		this.inputFileName = (NoUnderscoreEditText) view.findViewById(R.id.et_name_file);

		RecyclerView directories = (RecyclerView) view.findViewById(R.id.rv_directories);
		directories.addItemDecoration(new DividerItemDecoration());
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
			case R.id.b_ok:
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

		ServiceManagerFragment soundManagerFragment = this.getServiceManagerFragment();

		JsonPojo pojo = new JsonPojo();
		try
		{
			pojo.setSoundSheets(this.soundSheetsDataAccess.getSoundSheets());
			pojo.addPlayList(this.soundsDataAccess.getPlaylist());
			pojo.addSounds(this.soundsDataAccess.getSounds());

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
