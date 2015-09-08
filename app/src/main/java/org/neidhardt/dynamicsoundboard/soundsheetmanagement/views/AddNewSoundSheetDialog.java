package org.neidhardt.dynamicsoundboard.soundsheetmanagement.views;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatDialog;
import android.view.View;
import android.widget.EditText;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.views.BaseDialog;
import org.neidhardt.dynamicsoundboard.views.DialogBaseLayout;

public class AddNewSoundSheetDialog extends BaseDialog implements View.OnClickListener
{
	private static final String TAG = AddNewSoundSheetDialog.class.getSimpleName();

	private static final String KEY_SUGGESTED_NAME = "org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.AddNewSoundSheetDialog.suggestedName";

	private EditText soundSheetName;
	private String suggestedName;

	public static void showInstance(FragmentManager manager, String suggestedName)
	{
		AddNewSoundSheetDialog dialog = new AddNewSoundSheetDialog();

		Bundle args = new Bundle();
		args.putString(KEY_SUGGESTED_NAME, suggestedName);
		dialog.setArguments(args);

		dialog.show(manager, TAG);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = this.getArguments();
		if (args != null)
			this.suggestedName = args.getString(KEY_SUGGESTED_NAME);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		@SuppressLint("InflateParams") View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound_sheet, null);

		this.setMainView((DialogBaseLayout) view);

		this.soundSheetName = (EditText)view.findViewById(R.id.et_name_new_sound_sheet);

		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_ok).setOnClickListener(this);

		AppCompatDialog dialog = new AppCompatDialog(this.getActivity(), R.style.DialogTheme);

		dialog.setContentView(view);
		dialog.setTitle(R.string.dialog_add_new_sound_sheet_title);

		return dialog;
	}

	@Override
	public void onClick(@NonNull View v)
	{
		switch (v.getId())
		{
			case R.id.b_cancel:
				this.dismiss();
				break;
			case R.id.b_ok:
				this.deliverResult();
				this.dismiss();
		}
	}

	private void deliverResult()
	{

		String label = this.soundSheetName.getText().toString();
		if (label.length() == 0)
			label = this.suggestedName;

		SoundSheet soundSheet = this.getSoundSheetsDataUtil().getNewSoundSheet(label);
		soundSheet.setIsSelected(true);
		this.getSoundSheetsDataStorage().addSoundSheetToManager(soundSheet);
	}

}
