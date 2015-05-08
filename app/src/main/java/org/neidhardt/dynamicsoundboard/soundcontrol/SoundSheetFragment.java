package org.neidhardt.dynamicsoundboard.soundcontrol;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.emtronics.dragsortrecycler.DragSortRecycler;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.BaseActivity;
import org.neidhardt.dynamicsoundboard.BaseFragment;
import org.neidhardt.dynamicsoundboard.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.customview.AddPauseFloatingActionButton;
import org.neidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.dialog.addnewsound.AddNewSoundDialog;
import org.neidhardt.dynamicsoundboard.dialog.deleteconfirmdialog.ConfirmDeleteSoundsDialog;
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.AddNewSoundFromDirectory;
import org.neidhardt.dynamicsoundboard.events.SoundLoadedEvent;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.misc.IntentRequest;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

import java.util.Collections;


public class SoundSheetFragment
		extends
			BaseFragment
		implements
			View.OnClickListener,
			SoundAdapter.OnItemDeleteListener,
			DragSortRecycler.OnDragStateChangedListener,
			DragSortRecycler.OnItemMovedListener
{
	private static final String KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.SoundSheetFragment.fragmentTag";
	private static final String LOG_TAG = SoundSheetFragment.class.getName();

	private String fragmentTag;
	private SoundAdapter soundAdapter;
	private RecyclerView soundLayout;
	private SoundDragSortRecycler dragSortRecycler;
	private SoundSheetScrollListener scrollListener;

	public static SoundSheetFragment getNewInstance(SoundSheet soundSheet)
	{
		SoundSheetFragment fragment = new SoundSheetFragment();
		Bundle args = new Bundle();
		args.putString(KEY_FRAGMENT_TAG, soundSheet.getFragmentTag());
		fragment.setArguments(args);
		return fragment;
	}

	public String getFragmentTag()
	{
		return this.fragmentTag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);

		Bundle args = this.getArguments();
		if (args != null)
			this.fragmentTag = args.getString(KEY_FRAGMENT_TAG);
		this.soundAdapter = new SoundAdapter(this);
		this.soundAdapter.setOnItemDeleteListener(this);

		this.dragSortRecycler = new SoundDragSortRecycler(this.getResources(), R.id.b_reorder);
		this.dragSortRecycler.setOnItemMovedListener(this);
		this.dragSortRecycler.setOnDragStateChangedListener(this);
		this.scrollListener = new SoundSheetScrollListener(this.dragSortRecycler);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		BaseActivity activity = this.getBaseActivity();
		activity.setSoundSheetActionsEnable(true);
		activity.findViewById(R.id.action_add_sound).setOnClickListener(this);
		activity.findViewById(R.id.action_add_sound_dir).setOnClickListener(this);

		this.attachScrollViewToFab();

		this.soundAdapter.setServiceManagerFragment(this.getServiceManagerFragment());
		this.soundAdapter.startProgressUpdateTimer();

		EventBus.getDefault().register(this.soundAdapter);
		EventBus.getDefault().register(this);
	}

	private void attachScrollViewToFab()
	{
		AddPauseFloatingActionButton fab = (AddPauseFloatingActionButton) this.getActivity().findViewById(R.id.fab_add);
		if (fab == null || this.soundLayout == null)
			return;

		fab.attachToRecyclerView(this.soundLayout);
		fab.show(false);
	}

	@Override
	public void onPause()
	{
		super.onPause();
		this.soundAdapter.stopProgressUpdateTimer();
		EventBus.getDefault().unregister(this.soundAdapter);
		EventBus.getDefault().unregister(this);
	}

	public void deleteAllSoundsInSoundSheet()
	{
		this.removeAllSounds();
		this.notifySoundSheetList();
		this.soundAdapter.notifyDataSetChanged();
	}

	public void removeAllSounds()
	{
		this.getServiceManagerFragment().getSoundService().removeSounds(this.fragmentTag);
		AddPauseFloatingActionButton fab = (AddPauseFloatingActionButton) this.getActivity().findViewById(R.id.fab_add);
		if (fab != null)
			fab.show();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		if (resultCode == Activity.RESULT_OK)
		{
			if (requestCode == IntentRequest.GET_AUDIO_FILE)
			{
				Uri soundUri = data.getData();
				String soundLabel = FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(this.getActivity(), soundUri));
				MediaPlayerData playerData = EnhancedMediaPlayer.getMediaPlayerData(this.fragmentTag, soundUri, soundLabel);
				EventBus.getDefault().postSticky(new SoundLoadedEvent(playerData, false));
				return;
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onClick(View view)
	{
		switch (view.getId())
		{
			case R.id.action_add_sound:
				AddNewSoundDialog.showInstance(this.getFragmentManager(), this.fragmentTag);
				break;
			case R.id.action_add_sound_dir:
				AddNewSoundFromDirectory.showInstance(this.getFragmentManager(), this.fragmentTag);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		switch (item.getItemId())
		{
			case R.id.action_clear_sounds_in_sheet:
				ConfirmDeleteSoundsDialog.showInstance(this.getFragmentManager());
				return true;
			default:
				return false;
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (container == null)
			return null;

		View fragmentView = inflater.inflate(R.layout.fragment_soundsheet, container, false);

		this.soundLayout = (RecyclerView)fragmentView.findViewById(R.id.rv_sounds);
		this.soundLayout.setAdapter(this.soundAdapter);
		this.soundLayout.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		this.soundLayout.setItemAnimator(new DefaultItemAnimator());
		this.soundLayout.addItemDecoration(new DividerItemDecoration());

		this.soundLayout.addItemDecoration(this.dragSortRecycler);
		this.soundLayout.addOnItemTouchListener(this.dragSortRecycler);
		this.soundLayout.addOnScrollListener(this.scrollListener);
		this.soundLayout.addOnScrollListener(this.dragSortRecycler.getScrollListener());

		this.soundAdapter.setRecyclerView(this.soundLayout);
		this.soundAdapter.notifyDataSetChanged();

		return fragmentView;
	}

	@Override
	public void onDragStart()
	{
		Logger.d(LOG_TAG, "onDragStart");
		this.soundLayout.setItemAnimator(null); // drag does not work with default animator
		this.soundAdapter.stopProgressUpdateTimer();
	}

	@Override
	public void onDragStop()
	{
		Logger.d(LOG_TAG, "onDragStop");
		this.soundLayout.invalidateItemDecorations();
		this.soundAdapter.notifyDataSetChanged();
		this.soundLayout.setItemAnimator(new DefaultItemAnimator()); // add animator for delete animation
		this.soundAdapter.startProgressUpdateTimer();
	}

	@Override
	public void onItemMoved(int from, int to)
	{
		this.getServiceManagerFragment().getSoundService().moveSoundInFragment(fragmentTag, from, to);
		this.soundAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemDelete(EnhancedMediaPlayer player, int position)
	{
		this.soundAdapter.notifyItemRemoved(position);
		if (position > 0)
			this.soundAdapter.notifyItemChanged(position - 1);

		ServiceManagerFragment fragment = this.getServiceManagerFragment();
		fragment.getSoundService().removeSounds(Collections.singletonList(player));
		fragment.notifyPlaylist();

		this.notifySoundSheetList();

		AddPauseFloatingActionButton fab = (AddPauseFloatingActionButton) this.getActivity().findViewById(R.id.fab_add);
		if (fab != null)
			fab.show(true);
	}

	/**
	 * This is called by greenDao EventBus in case sound loading from MusicService has finished
	 * @param event delivered SoundLoadedEvent
	 */
	@SuppressWarnings("unused")
	public void onEventMainThread(SoundLoadedEvent event)
	{
		MediaPlayerData data = event.getLoadedSoundData();
		if (data != null && this.getFragmentTag().equals(data.getFragmentTag()))
			this.soundAdapter.notifyDataSetChanged();
	}

	private void notifySoundSheetList()
	{
		NavigationDrawerFragment navigationDrawerFragment = this.getNavigationDrawerFragment();
		navigationDrawerFragment.getSoundSheetsAdapter().notifyDataSetChanged(); // updates sound count in sound sheet list
	}

	public void notifyDataSetChanged()
	{
		this.soundAdapter.notifyDataSetChanged();
	}

}
