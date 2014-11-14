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
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;

import java.io.File;

/**
 * File created by eric.neidhardt on 14.11.2014.
 */
public class LoadLayoutDialog extends FileExplorerDialog implements View.OnClickListener
{
	private static final String TAG = LoadLayoutDialog.class.getName();

	public static void showInstance(FragmentManager manager)
	{
		LoadLayoutDialog dialog = new LoadLayoutDialog();

		dialog.show(manager, TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_load_sound_sheets, null);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_load).setOnClickListener(this);

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
			case R.id.b_cancel:
				this.dismiss();
				break;
			case R.id.b_load:
				if (super.adapter.selectedFile != null)
					this.useFile(super.adapter.selectedFile);
				else
					Toast.makeText(this.getActivity(), R.string.dialog_load_layout_no_file_info, Toast.LENGTH_SHORT).show();
				break;
		}
	}

	private void useFile(File file)
	{
		// TODO

	}
}
