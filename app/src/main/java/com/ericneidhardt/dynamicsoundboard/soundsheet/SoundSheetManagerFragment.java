package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;

/**
 * Created by Eric Neidhardt on 29.08.2014.
 */
public class SoundSheetManagerFragment extends Fragment implements View.OnClickListener
{
	public static final String TAG = SoundSheetManagerFragment.class.getSimpleName();

	private SoundSheetAdapter soundLayoutController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.soundLayoutController = new SoundSheetAdapter();
		// TODO load stored sound layout list
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		RecyclerView recyclerView = (RecyclerView) this.getActivity().findViewById(R.id.rv_navigation);

		// TODO add divider
		//recyclerView.addItemDecoration(new SoundSheetAdapter.ItemDivider());
		recyclerView.setAdapter(this.soundLayoutController);
		recyclerView.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		recyclerView.setItemAnimator(new DefaultItemAnimator());

		this.getActivity().findViewById(R.id.action_new_sound_layout).setOnClickListener(this);
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.action_new_sound_layout:
				soundLayoutController.openDialogAddNewSoundLayout(this.getActivity());
				break;
			default:
				Logger.e(TAG, "unknown item clicked " + view);
		}
	}
}
