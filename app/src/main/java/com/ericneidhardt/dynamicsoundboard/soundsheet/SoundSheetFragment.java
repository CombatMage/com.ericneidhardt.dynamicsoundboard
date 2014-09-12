package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundDialog;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.SoundAdapter;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.SoundManagerFragment;

import java.util.List;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public class SoundSheetFragment extends Fragment implements View.OnClickListener
{
	private static final String KEY_FRAGMENT_TAG = "com.ericneidhardt.dynamicsoundboard.SoundSheetFragment.fragmentTag";

	private String fragmentTag;
	private SoundAdapter soundAdapter;

	public static SoundSheetFragment getNewInstance(SoundSheet soundSheet)
	{
		SoundSheetFragment fragment = new SoundSheetFragment();
		Bundle args = new Bundle();
		args.putString(KEY_FRAGMENT_TAG, soundSheet.getFragmentTag());
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);

		this.fragmentTag = this.getArguments().getString(KEY_FRAGMENT_TAG);
		this.soundAdapter = new SoundAdapter();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		((BaseActivity)this.getActivity()).setSoundSheetActionsEnable(true);
		this.getActivity().findViewById(R.id.action_add_sound).setOnClickListener(this);
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.action_add_sound:
				AddNewSoundDialog.showInstance(this.getFragmentManager(), this.fragmentTag);
				break;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
			return null;

		View fragmentView = inflater.inflate(R.layout.fragment_soundsheet, container, false);

		fragmentView.findViewById(R.id.b_add_sound).setOnClickListener(this);

		RecyclerView listSounds = (RecyclerView)fragmentView.findViewById(R.id.rv_sounds);
		listSounds.addItemDecoration(new DividerItemDecoration(this.getActivity(), DividerItemDecoration.VERTICAL_LIST));
		listSounds.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		listSounds.setItemAnimator(new DefaultItemAnimator());
		listSounds.setAdapter(this.soundAdapter);

		SoundManagerFragment fragment = (SoundManagerFragment)this.getFragmentManager().findFragmentByTag(SoundManagerFragment.TAG);
		this.soundAdapter.clear();
		if (fragment != null)
		{
			List<EnhancedMediaPlayer> enhancedMediaPlayers = fragment.get(this.fragmentTag);
			this.soundAdapter.addAll(enhancedMediaPlayers);
		}

		return fragmentView;
	}

	public void notifyDataSetAdded(List<EnhancedMediaPlayer> enhancedMediaPlayers)
	{
		this.soundAdapter.addAll(enhancedMediaPlayers);
	}

}
