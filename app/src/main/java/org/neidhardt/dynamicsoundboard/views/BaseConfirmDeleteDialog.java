package org.neidhardt.dynamicsoundboard.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.R;

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
		view.findViewById(R.id.b_ok).setOnClickListener(this);

		AppCompatDialog dialog = new AppCompatDialog(this.getActivity(), R.style.DialogThemeNoTitle);
		dialog.setContentView(view);
		return dialog;
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId())
		{
			case R.id.b_cancel:
				this.dismiss();
				break;
			case R.id.b_ok:
				this.delete();
				this.dismiss();
		}
	}

	protected abstract int getInfoTextResource();

	protected abstract void delete();
}
