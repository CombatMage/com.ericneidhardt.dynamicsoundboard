package org.neidhardt.dynamicsoundboard.dialog;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import org.neidhardt.dynamicsoundboard.R;

/**
 * Created by eric.neidhardt on 16.02.2015.
 */
public class DeleteConfirmDialog extends BaseDialog implements View.OnClickListener
{
	private static final String TAG = DeleteConfirmDialog.class.getName();

	private TextView infoText;

	public static void showInstance(FragmentManager manager)
	{
		AddNewSoundSheetDialog dialog = new AddNewSoundSheetDialog();
		dialog.show(manager, TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound_layout, null);
		this.infoText = (TextView)view.findViewById(R.id.tv_message);

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
				// TODO confirm
				this.dismiss();
		}
	}
}
