package com.ericneidhardt.dynamicsoundboard;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.ericneidhardt.dynamicsoundboard.customview.ActionbarEditText;
import com.ericneidhardt.dynamicsoundboard.customview.SlidingTabLayout;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.dialog.AddNewSoundSheetDialog;
import com.ericneidhardt.dynamicsoundboard.playlist.Playlist;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.SoundManagerFragment;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheets;

public class NavigationDrawerFragment extends Fragment implements View.OnClickListener
{
	public static final String TAG = NavigationDrawerFragment.class.getSimpleName();

	private static final int INDEX_SOUND_SHEETS = 0;
	private static final int INDEX_PLAYLIST = 1;

	private ViewPager tabContent;
	private TabContentAdapter tabContentAdapter;
	private Playlist playlist;
	private SoundSheets soundSheets;

	private View controls;
	private View deleteSelected;

	public Playlist getPlaylist()
	{
		return this.playlist;
	}

	public SoundSheets getSoundSheets()
	{
		return this.soundSheets;
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);

		this.tabContentAdapter = new TabContentAdapter();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		this.tabContent = (ViewPager) this.getActivity().findViewById(R.id.vp_tab_content);
		tabContent.setAdapter(this.tabContentAdapter);

		SlidingTabLayout tabBar = (SlidingTabLayout) this.getActivity().findViewById(R.id.layout_tab);
		tabBar.setViewPager(tabContent);
		tabBar.setCustomTabColorizer(new SlidingTabLayout.TabColorizer()
		{
			@Override
			public int getIndicatorColor(int position)
			{
				if (position == 0)
					return getResources().getColor(R.color.accent_200);
				else
					return getResources().getColor(R.color.primary_500);
			}

			@Override
			public int getDividerColor(int position)
			{
				return 0;
			}
		});

		this.playlist = (Playlist)this.getActivity().findViewById(R.id.playlist);
		this.playlist.onActivityCreated(this);
		this.soundSheets = (SoundSheets)this.getActivity().findViewById(R.id.sound_sheets);
		this.soundSheets.onActivityCreated(this);
		this.controls = this.getActivity().findViewById(R.id.layout_controls);
		this.deleteSelected = this.getActivity().findViewById(R.id.b_delete_selected);
		this.deleteSelected.setOnClickListener(this);

		this.getActivity().findViewById(R.id.b_delete).setOnClickListener(this);
		this.getActivity().findViewById(R.id.b_add).setOnClickListener(this);
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
				Toast.makeText(this.getActivity(), "TODO add", Toast.LENGTH_SHORT).show();
			else
			{
				SoundSheetManagerFragment fragment = (SoundSheetManagerFragment)this.getFragmentManager()
						.findFragmentByTag(SoundManagerFragment.TAG);
				AddNewSoundSheetDialog.showInstance(this.getFragmentManager(), fragment.getSuggestedSoundSheetName());
			}
		}

	}

	public void onActionModeStart()
	{
		this.deleteSelected.setVisibility(View.VISIBLE);
		this.controls.setVisibility(View.GONE);

	}

	public void onActionModeFinished()
	{
		this.controls.setVisibility(View.VISIBLE);
		this.deleteSelected.setVisibility(View.GONE);
	}

	public void openSoundSheetFragment(SoundSheet soundSheet)
	{
		BaseActivity activity = (BaseActivity)this.getActivity();
		activity.closeNavigationDrawer();
		activity.openSoundFragment(soundSheet);
		ActionbarEditText soundSheetLabel = (ActionbarEditText)activity.findViewById(R.id.et_set_label);
		soundSheetLabel.setText(soundSheet.getLabel());
	}

	private class TabContentAdapter extends PagerAdapter
	{
		@Override
		public CharSequence getPageTitle(int position)
		{
			if (position == 0)
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
