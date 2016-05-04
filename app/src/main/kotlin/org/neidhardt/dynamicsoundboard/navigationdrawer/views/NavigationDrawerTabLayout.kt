package org.neidhardt.dynamicsoundboard.navigationdrawer.views

import android.support.design.widget.TabLayout
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.navigationdrawer.TabMode
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OnOpenSoundLayoutsEventListener
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsRequestedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.OnSoundLayoutSelectedEventListener
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutSelectedEvent

/**
 * @author eric.neidhardt on 03.05.2016.
 */
interface NavigationDrawerTabLayout
{
	fun showDefaultTabBar()

	fun showContextualTabBar()
}

private val INDEX_SOUND_SHEETS = 0
private val INDEX_PLAYLIST = 1

class TabLayoutPresenter(private val tabLayout: TabLayout) : NavigationDrawerTabLayout,
		TabLayout.OnTabSelectedListener,
		OnOpenSoundLayoutsEventListener,
		OnSoundLayoutSelectedEventListener
{
	private var tabSoundSheets: TabLayout.Tab = tabLayout.createSoundSheetTab()
	private var tabPlayList: TabLayout.Tab = tabLayout.createPlaylistTab()
	private var tabSoundLayouts: TabLayout.Tab = tabLayout.createSoundLayoutsTab()

	private var tabMode: TabMode = TabMode.Normal

	override fun showDefaultTabBar()
	{
		this.tabMode = TabMode.Normal
		this.tabLayout.removeAllTabs()
		this.tabSoundSheets = this.tabLayout.createSoundSheetTab()
		this.tabPlayList = this.tabLayout.createPlaylistTab()

		this.tabLayout.addTab(this.tabSoundSheets, INDEX_SOUND_SHEETS)
		this.tabLayout.addTab(this.tabPlayList, INDEX_PLAYLIST)

		this.tabSoundSheets.select()
	}

	override fun showContextualTabBar()
	{
		this.tabMode = TabMode.Context
		this.tabLayout.removeAllTabs()
		this.tabSoundLayouts = this.tabLayout.createSoundLayoutsTab()

		this.tabLayout.addTab(this.tabSoundLayouts)
		this.tabSoundLayouts.select()
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: OpenSoundLayoutsRequestedEvent)
	{
		if (event.openSoundLayouts) {
			this.showContextualTabBar()
		}
		else {
			this.showDefaultTabBar()
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: SoundLayoutSelectedEvent)
	{
		this.showDefaultTabBar()
	}

}

private fun TabLayout.createSoundSheetTab(): TabLayout.Tab = this.newTab().setText(R.string.tab_sound_sheets)

private fun TabLayout.createPlaylistTab(): TabLayout.Tab = this.newTab().setText(R.string.tab_play_list)

private fun TabLayout.createSoundLayoutsTab(): TabLayout.Tab = this.newTab().setText(R.string.navigation_drawer_select_sound_layout)
