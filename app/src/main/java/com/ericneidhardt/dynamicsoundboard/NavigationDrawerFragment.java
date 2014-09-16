package com.ericneidhardt.dynamicsoundboard;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.ericneidhardt.dynamicsoundboard.customview.ActionbarEditText;
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import com.ericneidhardt.dynamicsoundboard.customview.SlidingTabLayout;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.SoundManagerFragment;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetAdapter;
import com.ericneidhardt.dynamicsoundboard.soundsheet.SoundSheetManagerFragment;

import java.util.List;

public class NavigationDrawerFragment
		extends
			Fragment
		implements
			SoundSheetAdapter.OnItemClickListener,
			SoundSheetAdapter.OnItemDeleteListener
{
	public static final String TAG = NavigationDrawerFragment.class.getSimpleName();

	private SoundSheetAdapter soundSheetAdapter;
	private TabContentAdapter tabContentAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);

		this.soundSheetAdapter = new SoundSheetAdapter();
		this.soundSheetAdapter.setOnItemClickListener(this);
		this.soundSheetAdapter.setOnItemDeleteListener(this);
		this.tabContentAdapter = new TabContentAdapter();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		ViewPager tabContent = (ViewPager) this.getActivity().findViewById(R.id.vp_tab_content);
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

		RecyclerView listSoundSheets = (RecyclerView)this.getActivity().findViewById(R.id.rv_sound_sheets);
		listSoundSheets.addItemDecoration(new DividerItemDecoration(this.getActivity(), DividerItemDecoration.VERTICAL_LIST, null));
		listSoundSheets.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		listSoundSheets.setItemAnimator(new DefaultItemAnimator());
		listSoundSheets.setAdapter(this.soundSheetAdapter);

		SoundSheetManagerFragment fragment = (SoundSheetManagerFragment)this.getFragmentManager()
				.findFragmentByTag(SoundSheetManagerFragment.TAG);
		this.soundSheetAdapter.clear();
		if (fragment != null)
			this.soundSheetAdapter.addAll(fragment.getAll());
		this.soundSheetAdapter.notifyDataSetChanged();
	}

	public void notifyDataSetChanged(boolean newSoundSheetsAvailable)
	{
		SoundSheetManagerFragment fragment = (SoundSheetManagerFragment)this.getFragmentManager()
				.findFragmentByTag(SoundSheetManagerFragment.TAG);

		if (newSoundSheetsAvailable)
		{
			this.soundSheetAdapter.clear();
			this.soundSheetAdapter.addAll(fragment.getAll());
			this.soundSheetAdapter.notifyDataSetChanged();
		}
		else
			this.soundSheetAdapter.notifyDataSetChanged();
	}

	@Override
	public void onItemClick(View view, SoundSheet data, int position)
	{
		if (this.getActivity() != null)
		{
			this.soundSheetAdapter.setSelectedItem(position);
			this.openSoundSheetFragment(data);
		}
	}

	@Override
	public void onItemDelete(View view, SoundSheet data, int position)
	{
		this.soundSheetAdapter.remove(data);
		this.soundSheetAdapter.notifyDataSetChanged();
		SoundSheetManagerFragment fragment = (SoundSheetManagerFragment)this.getFragmentManager()
				.findFragmentByTag(SoundSheetManagerFragment.TAG);

		fragment.remove(data);

		if (this.getActivity() != null)
		{
			((BaseActivity)this.getActivity()).removeSoundFragment(data);
			((SoundManagerFragment)this.getFragmentManager()
					.findFragmentByTag(SoundManagerFragment.TAG)).remove(data.getFragmentTag());

			if (data.getIsSelected())
			{
				List<SoundSheet> soundSheets = this.soundSheetAdapter.getValues();
				if (soundSheets.size() > 0)
				{
					int positionOfNewSelectedSoundSheet = (position > 0) ? position - 1 : 0;
					this.soundSheetAdapter.setSelectedItem(positionOfNewSelectedSoundSheet);
					this.openSoundSheetFragment(soundSheets.get(positionOfNewSelectedSoundSheet));
				}
			}
		}
	}

	private void openSoundSheetFragment(SoundSheet soundSheet)
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
			int resId = 0;
			switch (position) {
				case 0:
					resId = R.id.rv_sound_sheets;
					break;
				case 1:
					resId = R.id.playlist;
					break;
			}
			return getActivity().findViewById(resId);
		}
	}

}
