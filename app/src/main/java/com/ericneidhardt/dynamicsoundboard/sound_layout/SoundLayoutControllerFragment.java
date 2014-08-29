package com.ericneidhardt.dynamicsoundboard.sound_layout;

import android.app.Activity;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Toast;

import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;

/**
 * Created by Eric Neidhardt on 29.08.2014.
 */
public class SoundLayoutControllerFragment extends Fragment implements View.OnClickListener
{
	public static final String TAG = SoundLayoutControllerFragment.class.getSimpleName();

	private SoundLayoutController soundLayoutController;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.soundLayoutController = new SoundLayoutController();
		// TODO load stored sound layout list
	}

	@Override
	public void onAttach(Activity activity)
	{
		super.onAttach(activity);
		RecyclerView recyclerView = (RecyclerView) activity.findViewById(R.id.rv_navigation);

		// TODO add divider
		//recyclerView.addItemDecoration(new SoundLayoutController.ItemDivider());
		recyclerView.setAdapter(this.soundLayoutController);
		recyclerView.setLayoutManager(new LinearLayoutManager(activity));
		recyclerView.setItemAnimator(new DefaultItemAnimator());

		activity.findViewById(R.id.action_new_sound_layout).setOnClickListener(this);
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
