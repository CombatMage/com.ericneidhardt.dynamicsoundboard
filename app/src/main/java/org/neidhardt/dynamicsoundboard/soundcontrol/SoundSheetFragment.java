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
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.SoundSheet;
import org.neidhardt.dynamicsoundboard.fileexplorer.AddNewSoundFromDirectory;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.misc.IntentRequest;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.soundactivity.BaseFragment;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OnOpenSoundDialogEventListener;
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundRenameEvent;
import org.neidhardt.dynamicsoundboard.soundcontrol.events.OpenSoundSettingsEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.OnSoundsChangedEventListener;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundAddedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundChangedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundsRemovedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil;
import org.neidhardt.dynamicsoundboard.soundmanagement.views.AddNewSoundDialog;
import org.neidhardt.dynamicsoundboard.soundmanagement.views.ConfirmDeleteSoundsDialog;
import org.neidhardt.dynamicsoundboard.soundmanagement.views.RenameSoundFileDialog;
import org.neidhardt.dynamicsoundboard.soundmanagement.views.SoundSettingsDialog;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.views.ConfirmDeleteSoundSheetDialog;
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.AddPauseFloatingActionButton;
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;


public class SoundSheetFragment
		extends
			BaseFragment
		implements
			View.OnClickListener,
			SoundAdapter.OnItemDeleteListener,
			DragSortRecycler.OnDragStateChangedListener,
			DragSortRecycler.OnItemMovedListener,
			OnOpenSoundDialogEventListener,
			OnSoundsChangedEventListener
{
	private static final String KEY_FRAGMENT_TAG = "org.neidhardt.dynamicsoundboard.SoundSheetFragment.fragmentTag";
	private static final String LOG_TAG = SoundSheetFragment.class.getName();

	private String fragmentTag;
	private SoundAdapter soundAdapter;
	private RecyclerView soundLayout;
	private SoundDragSortRecycler dragSortRecycler;
	private SoundSheetScrollListener scrollListener;

	@Inject SoundsDataStorage soundsDataStorage;
	@Inject SoundsDataAccess soundsDataAccess;
	@Inject SoundsDataUtil soundsDataUtil;

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
		DynamicSoundboardApplication.getApplicationComponent().inject(this);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);

		Bundle args = this.getArguments();
		if (args != null)
			this.fragmentTag = args.getString(KEY_FRAGMENT_TAG);
		this.soundAdapter = new SoundAdapter(this, this.soundsDataAccess, this.soundsDataStorage);
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

		SoundActivity activity = this.getBaseActivity();
		activity.setSoundSheetActionsEnable(true);
		activity.findViewById(R.id.action_add_sound).setOnClickListener(this);
		activity.findViewById(R.id.action_add_sound_dir).setOnClickListener(this);

		this.attachScrollViewToFab();
		this.soundAdapter.notifyDataSetChanged();
		this.soundAdapter.startProgressUpdateTimer();
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
	}

	@Override
	public void onStart()
	{
		super.onStart();
		EventBus eventBus = EventBus.getDefault();
		if (!eventBus.isRegistered(this))
			eventBus.register(this);
		if (!eventBus.isRegistered(this.soundAdapter))
			eventBus.register(this.soundAdapter);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		EventBus eventBus = EventBus.getDefault();
		eventBus.unregister(this.soundAdapter);
		eventBus.unregister(this);
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

				this.soundsDataStorage.createSoundAndAddToManager(playerData);
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
				ConfirmDeleteSoundsDialog.showInstance(this.getFragmentManager(), this.fragmentTag);
				return true;
			case R.id.action_delete_sheet:
				ConfirmDeleteSoundSheetDialog.showInstance(this.getFragmentManager(), this.fragmentTag);
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
		this.soundsDataStorage.moveSoundInFragment(fragmentTag, from, to);
		this.soundAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemDelete(EnhancedMediaPlayer player, int position)
	{
		this.soundsDataStorage.removeSounds(Collections.singletonList(player));

		AddPauseFloatingActionButton fab = (AddPauseFloatingActionButton) this.getActivity().findViewById(R.id.fab_add);
		if (fab != null)
			fab.show(true);
	}

	@Override
	public void onEvent(OpenSoundRenameEvent event)
	{
		RenameSoundFileDialog.showInstance(this.getFragmentManager(), event.getData());
	}

	@Override
	public void onEvent(OpenSoundSettingsEvent event)
	{
		SoundSettingsDialog.showInstance(this.getFragmentManager(), event.getData());
	}

	@Override
	public void onEventMainThread(SoundAddedEvent event)
	{
		this.soundAdapter.notifyDataSetChanged();
	}

	@Override
	public void onEventMainThread(SoundsRemovedEvent event)
	{
		List<EnhancedMediaPlayer> playersToRemove = event.getPlayers();
		if (playersToRemove == null)
			this.soundAdapter.notifyDataSetChanged();
		else if (playersToRemove.size() == 1) // if there is only 1 item removed, the adapter is only notified once, to ensure nice animation
		{
			MediaPlayerData data = playersToRemove.get(0).getMediaPlayerData();
			if (data.getFragmentTag().equals(this.fragmentTag))
			{
				int position = data.getSortOrder();
				this.soundAdapter.notifyItemRemoved(position);
				if (position > 0)
					this.soundAdapter.notifyItemChanged(position - 1);
			}
		}
		else
			this.soundAdapter.notifyDataSetChanged();

		if (this.soundAdapter.getValues().size() == 0)
		{
			AddPauseFloatingActionButton fab = (AddPauseFloatingActionButton) this.getActivity().findViewById(R.id.fab_add);
			if (fab != null)
				fab.show();
		}
	}

	@Override
	public void onEventMainThread(SoundChangedEvent event)
	{
		if (event.getPlayer().getMediaPlayerData().getFragmentTag().equals(this.fragmentTag))
			this.soundAdapter.notifyItemChanged(event.getPlayer());
	}
}
