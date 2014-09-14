package com.ericneidhardt.dynamicsoundboard.soundsheet;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import com.ericneidhardt.dynamicsoundboard.BaseActivity;
import com.ericneidhardt.dynamicsoundboard.R;
import com.ericneidhardt.dynamicsoundboard.customview.ActionbarEditText;
import com.ericneidhardt.dynamicsoundboard.customview.DividerItemDecoration;
import com.ericneidhardt.dynamicsoundboard.customview.SlidingTabLayout;
import com.ericneidhardt.dynamicsoundboard.dao.DaoSession;
import com.ericneidhardt.dynamicsoundboard.dao.SoundSheet;
import com.ericneidhardt.dynamicsoundboard.misc.Util;
import com.ericneidhardt.dynamicsoundboard.soundcontrol.SoundManagerFragment;

import java.util.List;

/**
 * Created by eric.neidhardt on 10.09.2014.
 */
public class NavigationDrawerFragment
		extends
			Fragment
		implements
			SoundSheetAdapter.OnItemClickListener,
			SoundSheetAdapter.OnItemDeleteListener,
			ActionbarEditText.OnTextEditedListener
{
	public static final String TAG = NavigationDrawerFragment.class.getSimpleName();

	private static final String DB_SOUND_SHEETS = "com.ericneidhardt.dynamicsoundboard.SoundSheetManagerFragment.db_sound_sheets";

	private SoundSheetAdapter soundSheetAdapter;
	private DaoSession daoSession;
	private TabContentAdapter tabContentAdapter;

	private boolean hasPendingLoadSoundSheetsTask = false;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);

		this.daoSession = Util.setupDatabase(this.getActivity(), DB_SOUND_SHEETS);
		this.soundSheetAdapter = new SoundSheetAdapter();
		this.soundSheetAdapter.setOnItemClickListener(this);
		this.soundSheetAdapter.setOnItemDeleteListener(this);
		this.tabContentAdapter = new TabContentAdapter();
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);
		this.buildNavigationDrawerTabLayout();

		ActionbarEditText labelCurrentSoundSheet = (ActionbarEditText)this.getActivity().findViewById(R.id.et_set_label);
		labelCurrentSoundSheet.setOnTextEditedListener(this);
		SoundSheet currentActiveSoundSheet = this.soundSheetAdapter.getSelectedItem();
		if (currentActiveSoundSheet != null)
			labelCurrentSoundSheet.setText(currentActiveSoundSheet.getLabel());
	}

	private void buildNavigationDrawerTabLayout()
	{
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
		listSoundSheets.addItemDecoration(new DividerItemDecoration(this.getActivity(), DividerItemDecoration.VERTICAL_LIST));
		listSoundSheets.setLayoutManager(new LinearLayoutManager(this.getActivity()));
		listSoundSheets.setItemAnimator(new DefaultItemAnimator());
		listSoundSheets.setAdapter(this.soundSheetAdapter);
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
		if (this.getActivity() != null)
		{
			((BaseActivity)this.getActivity()).removeSoundFragment(data);
			((SoundManagerFragment)this.getFragmentManager().findFragmentByTag(SoundManagerFragment.TAG)).remove(data.getFragmentTag());

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

	@Override
	public void onTextEdited(String text)
	{
		SoundSheet currentActiveSoundSheet = this.soundSheetAdapter.getSelectedItem();
		if (currentActiveSoundSheet == null)
			throw new NullPointerException("sound sheet label was edited, but no sound sheet is selected");

		currentActiveSoundSheet.setLabel(text);
		this.soundSheetAdapter.notifyDataSetChanged();
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
					resId = R.id.lv_playlist;
					break;
			}
			return getActivity().findViewById(resId);
		}
	}

}