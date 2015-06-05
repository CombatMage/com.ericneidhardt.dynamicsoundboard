package org.neidhardt.dynamicsoundboard.views;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.views.BaseDialog;

/**
 * File created by eric.neidhardt on 16.02.2015.
 */
public abstract class BaseConfirmDeleteDialog extends BaseDialog implements View.OnClickListener
{
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_confirm_delete, null);
		TextView infoText = (TextView) view.findViewById(R.id.tv_message);
		infoText.setText(this.getInfoTextResource());

		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_delete).setOnClickListener(this);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.b_cancel:
				this.dismiss();
				break;
			case R.id.b_delete:
				this.delete();
				this.dismiss();
		}
	}

	protected abstract int getInfoTextResource();

	protected abstract void delete();
}
