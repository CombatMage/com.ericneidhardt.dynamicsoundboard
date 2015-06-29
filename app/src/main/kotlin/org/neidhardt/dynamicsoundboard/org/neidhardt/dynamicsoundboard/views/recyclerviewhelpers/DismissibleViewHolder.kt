package org.neidhardt.dynamicsoundboard.org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers

import android.os.Handler
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.widget.TextView
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundProgressAdapter

/**
 * File created by eric.neidhardt on 29.06.2015.
 */
public abstract class DismissibleItemViewHolder(itemView: View)
	: RecyclerView.ViewHolder(itemView)
	, View.OnClickListener
	, ViewPager.OnPageChangeListener
{
	private val viewPager = itemView as ViewPager

	private val deleteSoundInfoLeft = itemView.findViewById(R.id.tv_delete_sound_left) as TextView
	private val deleteSoundInfoRight = itemView.findViewById(R.id.tv_delete_sound_left) as TextView

	private val handler = Handler()

	init
	{
		this.viewPager.setOffscreenPageLimit(2)
		this.viewPager.setAdapter(this.getPagerAdapter())
		this.viewPager.setOnPageChangeListener(this) // this is deprecated TODO check
		this.viewPager.setCurrentItem(this.getIndexOfContentPage())

		this.deleteSoundInfoLeft.setOnClickListener(this)
		this.deleteSoundInfoRight.setOnClickListener(this)
	}

	override fun onClick(view: View)
	{
		if (SoundboardPreferences.isOneSwipeToDeleteEnabled())
			return

		val id = view.getId()
		if (id == this.deleteSoundInfoLeft.getId() || id == this.deleteSoundInfoRight.getId())
			this.delete()
	}

	override fun onPageSelected(selectedPage: Int)
	{
		if (selectedPage != getIndexOfContentPage() && SoundboardPreferences.isOneSwipeToDeleteEnabled())
			this.handler.deleteItemDelayed()
	}

	override fun onPageScrollStateChanged(state: Int) {}

	override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}

	protected abstract fun getIndexOfContentPage(): Int

	protected abstract fun getPagerAdapter(): PagerAdapter

	protected abstract fun delete()

	fun Handler.deleteItemDelayed()
	{
		this.postDelayed(object : Runnable // delay deletion, because page is selected before scrolling has settled
		{
			override fun run() {
				delete()
			}
		}, SoundProgressAdapter.UPDATE_INTERVAL.toLong())
	}
}