package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.AddSoundListItem;

/**
 * Created by eric.neidhardt on 08.09.2014.
 */
public class AddNewSoundDialog extends DialogFragment implements View.OnClickListener
{
	public static final String TAG = AddNewSoundDialog.class.getSimpleName();

	private static final String KEY_CALLER_FRAGMENT_TAG = "com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundDialog.callingFragmentTag";

	private ViewGroup soundsToAdd;
	private String callingFragmentTag;

	public static void showInstance(FragmentManager manager, String callingFragmentTag)
	{
		AddNewSoundDialog dialog = new AddNewSoundDialog();

		Bundle args = new Bundle();
		args.putString(KEY_CALLER_FRAGMENT_TAG, callingFragmentTag);
		dialog.setArguments(args);

		dialog.show(manager, TAG);
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);

		Bundle args = this.getArguments();
		if (args != null)
			this.callingFragmentTag = args.getString(KEY_CALLER_FRAGMENT_TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState)
	{
		View view = this.getActivity().getLayoutInflater().inflate(R.layout.dialog_add_new_sound, null);

		view.findViewById(R.id.b_ok).setOnClickListener(this);
		view.findViewById(R.id.b_cancel).setOnClickListener(this);
		view.findViewById(R.id.b_add_another_sound).setOnClickListener(this);

		this.soundsToAdd = (ViewGroup)view.findViewById(R.id.layout_sounds_to_add);

		AlertDialog.Builder builder = new AlertDialog.Builder(this.getActivity());
		builder.setView(view);

		return builder.create();
	}

	@Override
	public void onClick(View v)
	{
		switch (v.getId()) {
			case R.id.b_ok:
				this.returnResultsToCallingFragment();
				this.dismiss();
				break;
			case R.id.b_cancel:
				this.dismiss();
				break;
			case R.id.b_add_another_sound:
				// TODO start activity for result
				this.addNewSoundToLoad();
				break;
		}
	}

	private void returnResultsToCallingFragment()
	{
		Fragment fragment = this.getFragmentManager().findFragmentByTag(this.callingFragmentTag);
		if (fragment == null)
			return;

		// TODO deliver results
		Toast.makeText(this.getActivity(), "returnResultsToCallingFragment", Toast.LENGTH_SHORT).show();
	}

	private void addNewSoundToLoad()
	{
		AddSoundListItem item = new AddSoundListItem(this.getActivity());
		item.setPath("file//test wie das aussieht");
		item.setSoundName("ein neuer name.mp4");
		this.soundsToAdd.addView(item);
	}

}
