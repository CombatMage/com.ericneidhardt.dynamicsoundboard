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

import java.io.File;

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
		if (v.getId() == R.id.b_cancel)
			this.dismiss();
		else if (v.getId() == R.id.b_save)
		{
			if (super.adapter.selectedFile != null)
				this.useFile(super.adapter.selectedFile);
			else
				Toast.makeText(this.getActivity(), R.string.dialog_store_layout_no_file_info, Toast.LENGTH_SHORT).show();
		}
	}

	private void inputFileName()
	{
		this.inputFileName.setVisibility(View.VISIBLE);

		// TODO
	}

	private void useFile(File file)
	{
		// TODO

		this.dismiss();
	}
}
