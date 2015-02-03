package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
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
import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.BaseFragment;
import com.ericneidhardt.dynamicsoundboard.NavigationDrawerFragment;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.AddPauseFloatingActionButton;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundDialog;
import com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundFromDirectory;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.misc.IntentRequest;
import com.ericneidhardt.dynamicsoundboard.misc.Logger;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

import static java.util.Arrays.asList;


public class SoundSheetFragment
		extends
			BaseFragment
		implements
			View.OnClickListener,
			SoundAdapter.OnItemDeleteListener,
			DragSortRecycler.OnDragStateChangedListener,
			DragSortRecycler.OnItemMovedListener
{
	private static final String KEY_FRAGMENT_TAG = "com.ericneidhardt.dynamicsoundboard.SoundSheetFragment.fragmentTag";

	private static final float FLOATING_ITEM_ALPHA = 0.4f;
	private static final int FLOATING_ITEM_BG_COLOR_ID = R.color.accent_200;
	private static final float AUTO_SCROLL_SPEED = 0.3f;
	private static final float AUTO_SCROLL_WINDOW = 0.1f;

	private String fragmentTag;
	private SoundAdapter soundAdapter;
	private RecyclerView soundLayout;

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
		return fragmentTag;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);
		this.setHasOptionsMenu(true);

		this.fragmentTag = this.getArguments().getString(KEY_FRAGMENT_TAG);
		this.soundAdapter = new SoundAdapter(this);
		this.soundAdapter.setOnItemDeleteListener(this);
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

		this.soundAdapter.onParentResume(this);
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
		this.soundAdapter.onParentPause();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		super.onOptionsItemSelected(item);
		switch (item.getItemId())
		{
			case R.id.action_delete_sheet:
				this.removeAllSounds();
				SoundSheet soundSheet = this.getSoundSheetManagerFragment().get(this.fragmentTag);
				this.getSoundSheetManagerFragment().remove(this.fragmentTag);
				this.getBaseActivity().removeSoundFragment(soundSheet);
				return true;
			case R.id.action_clear_sounds_in_sheet:
				this.removeAllSounds();
				this.notifySoundSheetList();
				this.soundAdapter.notifyDataSetChanged();
				return true;
			default:
				return false;
		}
	}

	private void removeAllSounds()
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
				String soundLabel = Util.getFileNameFromUri(this.getActivity(), soundUri);
				ServiceManagerFragment fragment = this.getServiceManagerFragment();
				fragment.getSoundService().addNewSoundToServiceAndDatabase(EnhancedMediaPlayer.getMediaPlayerData(this.fragmentTag, soundUri, soundLabel));
				fragment.notifySoundSheetFragments();
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

		DragSortRecycler dragSortRecycler = new SoundSortRecycler();
		dragSortRecycler.setOnItemMovedListener(this);
		dragSortRecycler.setOnDragStateChangedListener(this);

		this.soundLayout.addItemDecoration(dragSortRecycler);
		this.soundLayout.addOnItemTouchListener(dragSortRecycler);
		this.soundLayout.setOnScrollListener(dragSortRecycler.getScrollListener());

		this.soundAdapter.notifyDataSetChanged();

		return fragmentView;
	}

	@Override
	public void onDragStart()
	{
		Logger.d(fragmentTag, "onDragStart");
		this.soundLayout.setItemAnimator(null); // drag does not work with default animator
		this.soundAdapter.stopProgressUpdateTimer();
	}

	@Override
	public void onDragStop()
	{
		Logger.d(fragmentTag, "onDragStop");
		this.soundLayout.setItemAnimator(new DefaultItemAnimator()); // add animator for delete animation
		this.soundAdapter.scheduleProgressUpdateTimer();
	}

	@Override
	public void onItemMoved(int from, int to)
	{
		Logger.d(fragmentTag, "onItemMoved " + from + " to " + to);
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
		fragment.getSoundService().removeSounds(asList(player));
		fragment.notifyPlaylist();

		this.notifySoundSheetList();

		AddPauseFloatingActionButton fab = (AddPauseFloatingActionButton) this.getActivity().findViewById(R.id.fab_add);
		if (fab != null)
			fab.show(true);
	}

	private void notifySoundSheetList()
	{
		NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment)this.getFragmentManager()
				.findFragmentByTag(NavigationDrawerFragment.TAG);
		navigationDrawerFragment.getSoundSheets().notifyDataSetChanged(false); // updates sound count in sound sheet list
	}

	public void notifyDataSetChanged()
	{
		this.soundAdapter.notifyDataSetChanged();
	}

	private class SoundSortRecycler extends DragSortRecycler
	{
		public SoundSortRecycler()
		{
			this.setViewHandleId(R.id.b_reorder);
			this.setFloatingAlpha(FLOATING_ITEM_ALPHA);
			this.setFloatingBgColor(getResources().getColor(FLOATING_ITEM_BG_COLOR_ID));
			this.setAutoScrollSpeed(AUTO_SCROLL_SPEED);
			this.setAutoScrollWindow(AUTO_SCROLL_WINDOW);
		}
	}

	private class DividerItemDecoration extends RecyclerView.ItemDecoration
	{
		private int heightDivider = getResources().getDimensionPixelSize(R.dimen.stroke);

		@Override
		public void onDraw(Canvas canvas, RecyclerView parent, RecyclerView.State state)
		{
			final int childCount = parent.getChildCount();
			if (childCount == 0)
				return;

			for (int i = 0; i < childCount; i++)
			{
				if (i < childCount - 1) // do not draw divider after last item
				{
					View child = parent.getChildAt(i);
					final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();
					final int left = parent.getPaddingLeft();
					final int right = parent.getWidth() - parent.getPaddingRight();
					final int top = child.getBottom() + params.bottomMargin;
					final int bottom = top + this.heightDivider;

					this.drawDividerBackground(canvas, left, top, right, bottom);
					this.drawDivider(canvas, left, top, right, bottom);
				}
			}
		}

		private void drawDividerBackground(Canvas canvas, int left, int top, int right, int bottom)
		{
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);paint.setColor(getResources().getColor(R.color.background));
			canvas.drawRect(left, top, right, bottom, paint);
		}

		private void drawDivider(Canvas canvas, int left, int top, int right, int bottom)
		{
			Paint paint = new Paint();
			paint.setStyle(Paint.Style.FILL);paint.setColor(getResources().getColor(R.color.divider));
			canvas.drawRect(left, top, right, bottom, paint);
		}

		@Override
		public void getItemOffsets(Rect outRect, View childView, RecyclerView parent, RecyclerView.State state)
		{
			int topOffset = 0;
			int bottomOffset = this.heightDivider;
			int rightOffset =  0;
			int leftOffset =  0;
			outRect.set(leftOffset, topOffset, rightOffset, bottomOffset);
		}
	}
}
