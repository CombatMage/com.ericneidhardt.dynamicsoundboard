package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.MediaPlayerPool;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class SoundSheetFragment extends Fragment
{
	private static final String KEY_FRAGMENT_TAG = "com.ericneidhardt.dynamicsoundboard.SoundSheetFragment.fragmentTag";
	private static final String KEY_FRAGMENT_LABEL = "com.ericneidhardt.dynamicsoundboard.SoundSheetFragment.label";

	private String fragmentTag;
	private String label;
	private MediaPlayerPool mediaPlayerPool;

	public static SoundSheetFragment getNewInstance(SoundSheet soundSheet)
	{
		SoundSheetFragment fragment = new SoundSheetFragment();
		Bundle args = new Bundle();
		args.putString(KEY_FRAGMENT_TAG, soundSheet.getFragmentTag());
		args.putString(KEY_FRAGMENT_LABEL, soundSheet.getLabel());
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.fragmentTag = this.getArguments().getString(KEY_FRAGMENT_TAG);
		this.mediaPlayerPool = new MediaPlayerPool(this.fragmentTag);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
			return null;

		View fragmentView = inflater.inflate(R.layout.fragment_soundsheet, container, false);
		ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
		fragmentView.setLayoutParams(params);

		((TextView)fragmentView.findViewById(R.id.tv_label)).setText(this.fragmentTag);

		return fragmentView;
	}

	@Override
	public void onResume()
	{
		super.onResume();

		EditText editLabel = (EditText)this.getActivity().findViewById(R.id.et_set_label);
		editLabel.setText(this.label);
	}

}
