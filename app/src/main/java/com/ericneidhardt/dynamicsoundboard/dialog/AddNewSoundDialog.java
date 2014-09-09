package com.ericneidhardt.dynamicsoundboard.dialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.AddSoundListItem;
import com.ericneidhardt.dynamicsoundboard.misc.IntentRequest;
import com.ericneidhardt.dynamicsoundboard.misc.Util;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by eric.neidhardt on 08.09.2014.
 */
public class AddNewSoundDialog extends DialogFragment implements View.OnClickListener
{
	public static final String TAG = AddNewSoundDialog.class.getSimpleName();

	private static final String KEY_CALLER_FRAGMENT_TAG = "com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundDialog.callingFragmentTag";

	private ViewGroup soundsToAddLayout;
	private String callingFragmentTag;
	private List<Uri> soundsToAdd;

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

		this.soundsToAdd = new ArrayList<Uri>();
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

		this.soundsToAddLayout = (ViewGroup)view.findViewById(R.id.layout_sounds_to_add);

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
				this.startIntentForNewSound();
				break;
		}
	}

	private void startIntentForNewSound()
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType(Util.MIME_AUDIO);
		this.startActivityForResult(intent, IntentRequest.GET_AUDIO_FILE);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == IntentRequest.GET_AUDIO_FILE)
			{
				Uri soundUri = data.getData();
				this.addNewSoundToLoad(soundUri);
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	private void addNewSoundToLoad(Uri soundUri)
	{
		this.soundsToAdd.add(soundUri);
		AddSoundListItem item = new AddSoundListItem(this.getActivity());
		item.setPath(soundUri.toString());
		item.setSoundName(Util.getFileNameFromUri(soundUri));
		this.soundsToAddLayout.addView(item);
	}

	private void returnResultsToCallingFragment()
	{
		Fragment fragment = this.getFragmentManager().findFragmentByTag(this.callingFragmentTag);
		if (fragment == null)
			return;

		// TODO deliver results
		Toast.makeText(this.getActivity(), "returnResultsToCallingFragment", Toast.LENGTH_SHORT).show();
	}

}
