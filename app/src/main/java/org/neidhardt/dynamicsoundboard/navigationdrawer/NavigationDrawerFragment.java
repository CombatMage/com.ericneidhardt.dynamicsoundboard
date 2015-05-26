package org.neidhardt.dynamicsoundboard.navigationdrawer;

import android.animation.Animator;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.R;
import org.neidhardt.dynamicsoundboard.dao.SoundLayout;
import org.neidhardt.dynamicsoundboard.dialog.AddNewSoundSheetDialog;
import org.neidhardt.dynamicsoundboard.dialog.addnewsound.AddNewSoundDialog;
import org.neidhardt.dynamicsoundboard.misc.AnimationUtils;
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.ActionModeEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.SoundLayoutsManager;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRemovedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutRenamedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events.SoundLayoutSelectedEvent;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views.AddNewSoundLayoutDialog;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views.SoundLayoutSettingsDialog;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views.SoundLayoutsList;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.views.SoundLayoutsPresenter;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views.SoundSheets;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views.SoundSheetsAdapter;
import org.neidhardt.dynamicsoundboard.soundactivity.BaseFragment;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.SoundSheetsManagerFragment;

public class NavigationDrawerFragment
		extends
			BaseFragment
		implements
			View.OnClickListener,
			ViewPager.OnPageChangeListener,
			NavigationDrawerListPresenter.OnNavigationDrawerListEventListener,
			SoundLayoutSettingsDialog.OnSoundLayoutRenamedEventListener,
			SoundLayoutsPresenter.OnSoundLayoutRemovedEventListener,
			SoundLayoutsPresenter.OnSoundLayoutSelectedEventListener
{
	public static final String TAG = NavigationDrawerFragment.class.getName();

	private static final int INDEX_SOUND_SHEETS = 0;
	private static final int INDEX_PLAYLIST = 1;

	private EventBus bus;

	private SlidingTabLayout tabBar;
	private ViewPager tabContent;
	private TabContentAdapter tabContentAdapter;

	private ViewGroup listContainer;
	private SoundLayoutsList soundLayoutList;
	private Playlist playlist;
	private SoundSheets soundSheets;

	private TextView currentLayoutName;
	private View contextualActionContainer;
	private View deleteSelected;

	private ViewPagerContentObserver listObserver;

	private int minHeightOfListContent = 0;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.setRetainInstance(true);

		this.listObserver = new ViewPagerContentObserver();
		this.tabContentAdapter = new TabContentAdapter();

		this.bus = EventBus.getDefault();
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View fragmentView = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);

		this.contextualActionContainer = fragmentView.findViewById(R.id.layout_contextual_controls);
		this.listContainer = (ViewGroup) fragmentView.findViewById(R.id.layout_navigation_drawer_list_content);

		this.deleteSelected = fragmentView.findViewById(R.id.b_delete_selected);
		this.deleteSelected.setOnClickListener(this);

		fragmentView.findViewById(R.id.b_delete).setOnClickListener(this);
		fragmentView.findViewById(R.id.b_ok).setOnClickListener(this);
		fragmentView.findViewById(R.id.layout_change_sound_layout).setOnClickListener(this);

		this.tabContent = (ViewPager) fragmentView.findViewById(R.id.vp_tab_content);
		this.tabContent.setAdapter(this.tabContentAdapter);
		this.tabContent.setOnPageChangeListener(this);

		this.tabBar = (SlidingTabLayout) fragmentView.findViewById(R.id.layout_tab);
		this.tabBar.setOnPageChangeListener(this);
		this.tabBar.setViewPager(tabContent);
		this.tabBar.setCustomTabColorizer(new NavigationDrawerTabColorizer());

		this.soundLayoutList = (SoundLayoutsList) fragmentView.findViewById(R.id.layout_select_sound_layout);
		this.playlist = (Playlist) fragmentView.findViewById(R.id.playlist);
		this.soundSheets = (SoundSheets) fragmentView.findViewById(R.id.sound_sheets);

		this.initPlayListAndAdapter();
		this.initSoundSheetsAndAdapter();

		return fragmentView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState)
	{
		super.onActivityCreated(savedInstanceState);

		this.currentLayoutName = (TextView) this.getActivity().findViewById(R.id.tv_current_sound_layout_name);
		this.currentLayoutName.setText(SoundLayoutsManager.getInstance().getActiveSoundLayout().getLabel());
	}

	@Override
	public void onResume()
	{
		super.onResume();

		this.initSoundLayoutsAndAdapter();
		this.initSoundSheetsAndAdapter();
		this.initPlayListAndAdapter();

		this.calculateMinHeightOfListContent();
		this.adjustViewPagerToContent();

		this.playlist.getAdapter().registerAdapterDataObserver(this.listObserver);
		this.soundSheets.getAdapter().registerAdapterDataObserver(this.listObserver);
	}

	/**
	 * Calculates the minimum require height of the viewpager's content (this is the height used if the content is smaller than the
	 * screens height). Recalculation is require every time the screen's metric changes (ie. switch from/to full immersive mode).
	 */
	public void calculateMinHeightOfListContent()
	{
		this.minHeightOfListContent = this.contextualActionContainer.getTop() - listContainer.getTop();  // this is the minimal height required to fill the screen properly
	}

	private void initSoundLayoutsAndAdapter()
	{
		this.soundLayoutList.getAdapter().setNavigationDrawerFragment(this);
	}

	private void initSoundSheetsAndAdapter()
	{
		this.soundSheets.getAdapter().setNavigationDrawerFragment(this);
	}

	private void initPlayListAndAdapter()
	{
		this.playlist.getAdapter().setServiceManagerFragment(this.getServiceManagerFragment());
	}

	@Override
	public void onStart()
	{
		super.onStart();
		if (!this.bus.isRegistered(this))
			this.bus.register(this);
	}

	@Override
	public void onStop()
	{
		super.onStop();
		this.bus.unregister(this);
	}

	@Override
	public void onPause()
	{
		super.onPause();

		this.playlist.getAdapter().unregisterAdapterDataObserver(this.listObserver);
		this.soundSheets.getAdapter().unregisterAdapterDataObserver(this.listObserver);
	}

	@Override
	public void onClick(View v)
	{
		int id = v.getId();
		if (id == R.id.b_delete)
		{
			if (this.soundLayoutList.isActive())
				this.soundLayoutList.getPresenter().prepareItemDeletion();
			else if (this.tabContent.getCurrentItem() == INDEX_PLAYLIST)
				this.playlist.getPresenter().prepareItemDeletion();
			else
				this.soundSheets.getPresenter().prepareItemDeletion();
		}
		else if (id == R.id.b_delete_selected)
		{
			if (this.soundLayoutList.isActive())
				this.soundLayoutList.getPresenter().deleteSelected();
			else if (this.tabContent.getCurrentItem() == INDEX_PLAYLIST)
				this.playlist.getPresenter().deleteSelected();
			else
				this.soundSheets.getPresenter().deleteSelected();
		}
		else if (id  == R.id.b_ok)
		{
			if (this.soundLayoutList.isActive())
				AddNewSoundLayoutDialog.showInstance(this.getFragmentManager(), SoundLayoutsManager.getInstance().getSuggestedSoundLayoutName());
			else if (this.tabContent.getCurrentItem() == INDEX_PLAYLIST)
				AddNewSoundDialog.showInstance(this.getFragmentManager(), Playlist.TAG);
			else
			{
				SoundSheetsManagerFragment fragment = this.getSoundSheetManagerFragment();
				AddNewSoundSheetDialog.showInstance(this.getFragmentManager(), fragment.getSuggestedSoundSheetName());
			}
		}
		else if (id == R.id.layout_change_sound_layout)
		{
			this.animateSoundLayoutsListAppear();
			this.soundLayoutList.toggleVisibility();
			if (this.getBaseActivity().isActionModeActive() && this.soundLayoutList.isActive())
				this.soundLayoutList.getPresenter().prepareItemDeletion();
		}
	}

	private void animateSoundLayoutsListAppear()
	{
		View indicator = this.getActivity().findViewById(R.id.iv_change_sound_layout_indicator);
		indicator.animate()
				.rotationXBy(180)
				.setDuration(this.getResources().getInteger(android.R.integer.config_shortAnimTime))
				.start();

		final View viewToAnimate = this.getActivity().findViewById(R.id.v_reveal_shadow);
		Animator animator = AnimationUtils.createSlowCircularReveal(viewToAnimate,
				this.listContainer.getWidth(), 0,
				0, 2 * this.listContainer.getHeight());

		if (animator != null)
			animator.start();
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(ActionModeEvent event)
	{
		ActionModeEvent.REQUEST requestedAction = event.getRequestedAction();
		switch (requestedAction)
		{
			case START:
				this.onActionModeStart();
				return;
			case STOPPED:
				this.onActionModeFinished();
		}
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(SoundLayoutRenamedEvent event)
	{
		SoundLayout renamedLayout = event.getRenamedSoundLayout();
		SoundLayoutsManager manager = SoundLayoutsManager.getInstance();

		if (renamedLayout.equals(manager.getActiveSoundLayout()))
			this.setLayoutName(renamedLayout.getLabel());
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(SoundLayoutRemovedEvent event)
	{
		SoundLayoutsManager manager = SoundLayoutsManager.getInstance();
		this.setLayoutName(manager.getActiveSoundLayout().getLabel());
	}

	@Override
	@SuppressWarnings("unused")
	public void onEvent(SoundLayoutSelectedEvent event)
	{
		SoundLayoutsManager manager = SoundLayoutsManager.getInstance();
		this.setLayoutName(manager.getActiveSoundLayout().getLabel());
	}

	private void onActionModeStart()
	{
		this.deleteSelected.setVisibility(View.VISIBLE);
		int distance = this.contextualActionContainer.getWidth();

		this.deleteSelected.setTranslationX(-distance);
		this.deleteSelected.animate().
				translationX(0).
				setDuration(this.getResources().getInteger(android.R.integer.config_mediumAnimTime)).
				setInterpolator(new DecelerateInterpolator()).
				start();
	}

	private void onActionModeFinished()
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
			this.soundSheets.getPresenter().prepareItemDeletion();
		else if (position == INDEX_PLAYLIST)
			this.playlist.getPresenter().prepareItemDeletion();
	}

	@Override
	public void onPageScrollStateChanged(int state) {}

	public void setLayoutName(String layoutName)
	{
		if (this.currentLayoutName != null)
			this.currentLayoutName.setText(layoutName);
	}

	/**
	 * This function resize the view pagers height to its content. It is necessary, because the viewpager can not
	 * have layout parameter wrap_content.
	 */
	public void adjustViewPagerToContent()
	{
		Resources resources = DynamicSoundboardApplication.getSoundboardContext().getResources();
		int childHeight = resources.getDimensionPixelSize(R.dimen.height_list_item);
		int dividerHeight = resources.getDimensionPixelSize(R.dimen.stroke);
		int padding = resources.getDimensionPixelSize(R.dimen.margin_small);

		int soundSheetCount = this.getSoundSheetManagerFragment().getSoundSheets().size();
		int playListCount = this.playlist.getAdapter().getItemCount();

		int heightSoundSheetChildren = soundSheetCount * childHeight;
		int heightDividerSoundSheet = soundSheetCount > 1 ? (soundSheetCount - 1) * dividerHeight : 0;
		int heightSoundSheet = heightSoundSheetChildren + heightDividerSoundSheet + padding + this.tabBar.getHeight();

		int heightPlayListChildren = playListCount * childHeight;
		int heightDividerPlayList = playListCount > 1 ? (playListCount - 1) * dividerHeight : 0;
		int heightPlayList = heightPlayListChildren + heightDividerPlayList + padding + this.tabBar.getHeight();

		int largestList = Math.max(heightSoundSheet, heightPlayList);
		if (this.minHeightOfListContent == 0) // 0 means the current height was not measured, remeasure
			this.minHeightOfListContent = this.contextualActionContainer.getTop() - listContainer.getTop();

		this.listContainer.getLayoutParams().height = Math.max(largestList, minHeightOfListContent);
	}

	public SoundSheetsAdapter getSoundSheetsAdapter()
	{
		return this.soundSheets.getAdapter();
	}

	public Playlist getPlaylist()
	{
		return this.playlist;
	}

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

	private class ViewPagerContentObserver extends RecyclerView.AdapterDataObserver
	{
		@Override
		public void onChanged()
		{
			super.onChanged();
			adjustViewPagerToContent();
		}
	}

	private class NavigationDrawerTabColorizer implements SlidingTabLayout.TabColorizer
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
	}
}
