package org.neidhardt.dynamicsoundboard;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.customview.navigationdrawer.SlidingTabLayout;
import org.neidhardt.dynamicsoundboard.dialog.AddNewSoundSheetDialog;
import org.neidhardt.dynamicsoundboard.dialog.addnewsound.AddNewSoundDialog;
import org.neidhardt.dynamicsoundboard.playlist.Playlist;
import org.neidhardt.dynamicsoundboard.playlist.PlaylistAdapter;
import org.neidhardt.dynamicsoundboard.soundsheet.SoundSheets;
import org.neidhardt.dynamicsoundboard.soundsheet.SoundSheetsAdapter;
import org.neidhardt.dynamicsoundboard.soundsheet.SoundSheetsManagerFragment;

public class NavigationDrawerFragment
		extends
			BaseFragment
		implements
			View.OnClickListener,
			ViewPager.OnPageChangeListener
{
	public static final String TAG = NavigationDrawerFragment.class.getName();

	private static final int INDEX_SOUND_SHEETS = 0;
	private static final int INDEX_PLAYLIST = 1;

	private ViewPager tabContent;
	private TabContentAdapter tabContentAdapter;
	private Playlist playlist;
	private PlaylistAdapter playlistAdapter;
	private SoundSheets soundSheets;
	private SoundSheetsAdapter soundSheetsAdapter;

	private View contextualActionContainer;
	private View deleteSelected;

	public Playlist getPlaylist()
	{
		return this.playlist;
	}

	public SoundSheetsAdapter getSoundSheetsAdapter()
	{
		return this.soundSheetsAdapter;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);

		this.tabContentAdapter = new TabContentAdapter();
		this.playlistAdapter = new PlaylistAdapter();
		this.soundSheetsAdapter = new SoundSheetsAdapter();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		this.tabContent = (ViewPager) this.getActivity().findViewById(R.id.vp_tab_content);
		this.tabContent.setAdapter(this.tabContentAdapter);
		this.tabContent.setOnPageChangeListener(this);

		SlidingTabLayout tabBar = (SlidingTabLayout) this.getActivity().findViewById(R.id.layout_tab);
		tabBar.setOnPageChangeListener(this);
		tabBar.setViewPager(tabContent);
		tabBar.setCustomTabColorizer(new SlidingTabLayout.TabColorizer()
		{
			@Override
			public int getIndicatorColor(int position)
			{
				return getResources().getColor(R.color.accent_200);
			}

			@Override
			public int getDividerColor(int position)
			{
				return 0;
			}
		});

		this.playlist = (Playlist)this.getActivity().findViewById(R.id.playlist);
		this.soundSheets = (SoundSheets)this.getActivity().findViewById(R.id.sound_sheets);

		this.contextualActionContainer = this.getActivity().findViewById(R.id.layout_contextual_controls);
		this.deleteSelected = this.getActivity().findViewById(R.id.b_delete_selected);
		this.deleteSelected.setOnClickListener(this);

		this.playlist.setAdapter(this.playlistAdapter);
		this.soundSheets.setAdapter(this.soundSheetsAdapter);
		this.initSoundSheetsAndAdapter();
		this.initPlayListAndAdapter();

		this.getActivity().findViewById(R.id.b_delete).setOnClickListener(this);
		this.getActivity().findViewById(R.id.b_add).setOnClickListener(this);
	}

	@Override
	public void onResume()
	{
		super.onResume();

		this.initSoundSheetsAndAdapter();
		this.initPlayListAndAdapter();
	}

	private void initSoundSheetsAndAdapter()
	{
		this.soundSheets.setParentFragment(this);
		this.soundSheetsAdapter.setParentFragment(this);
		this.soundSheetsAdapter.notifyDataSetChanged();
	}

	private void initPlayListAndAdapter()
	{
		this.playlist.setParentFragment(this);
		this.playlistAdapter.setServiceManagerFragment(this.getServiceManagerFragment());
		this.playlistAdapter.startProgressUpdateTimer();
		if (EventBus.getDefault().isRegistered(this.playlistAdapter))
			EventBus.getDefault().register(this.playlistAdapter);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		EventBus.getDefault().unregister(this.playlistAdapter);
		this.playlistAdapter.stopProgressUpdateTimer();
		this.playlist.setParentFragment(null);

		this.soundSheets.setParentFragment(null);
	}

	@Override
	public void onClick(View v)
	{
		if (v.getId() == R.id.b_delete)
		{
			if (this.tabContent.getCurrentItem() == INDEX_PLAYLIST)
				this.playlist.prepareItemDeletion();
			else
				this.soundSheets.prepareItemDeletion();
		}
		else if (v.getId() == R.id.b_delete_selected)
		{
			if (this.tabContent.getCurrentItem() == INDEX_PLAYLIST)
				this.playlist.deleteSelected();
			else
				this.soundSheets.deleteSelected();
		}
		else if (v.getId() == R.id.b_add)
		{
			if (this.tabContent.getCurrentItem() == INDEX_PLAYLIST)
				AddNewSoundDialog.showInstance(this.getFragmentManager(), Playlist.TAG);
			else
			{
				SoundSheetsManagerFragment fragment = this.getSoundSheetManagerFragment();
				AddNewSoundSheetDialog.showInstance(this.getFragmentManager(), fragment.getSuggestedSoundSheetName());
			}
		}

	}

	public void onActionModeStart()
	{
		this.deleteSelected.setVisibility(View.VISIBLE);
		int distance = this.contextualActionContainer.getWidth();

		this.deleteSelected.setTranslationX(-distance);
		this.deleteSelected.animate().
				translationX(0).
				setDuration(this.getResources().getInteger(android.R.integer.config_shortAnimTime)).
				setInterpolator(new DecelerateInterpolator()).
				start();
	}

	public void onActionModeFinished()
	{
		this.deleteSelected.setVisibility(View.GONE);
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

	@Override
	public void onPageSelected(int position)
	{
		if (!this.getBaseActivity().isActionModeActive())
			return;
		if (position == INDEX_SOUND_SHEETS)
			this.soundSheets.prepareItemDeletion();
		else if (position == INDEX_PLAYLIST)
			this.playlist.prepareItemDeletion();
	}

	@Override
	public void onPageScrollStateChanged(int state) {}

	private class TabContentAdapter extends PagerAdapter
	{
		@Override
		public CharSequence getPageTitle(int position)
		{
			if (position == INDEX_SOUND_SHEETS)
				return getResources().getString(R.string.tab_sound_sheets);
			else
				return getResources().getString(R.string.tab_play_list);
		}

		@Override
		public int getCount()
		{
			return 2;
		}

		@Override
		public boolean isViewFromObject(View view, Object object)
		{
			return view.equals(object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position)
		{
			switch (position)
			{
				case INDEX_SOUND_SHEETS:
					return soundSheets;
				case INDEX_PLAYLIST:
					return playlist;
				default:
					throw new NullPointerException("instantiateItem: no view for position " + position + " is available");
			}
		}
	}

}
