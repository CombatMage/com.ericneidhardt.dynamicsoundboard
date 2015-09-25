package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts

import android.content.Context
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerList
import org.neidhardt.dynamicsoundboard.navigationdrawer.views.NavigationDrawerListPresenter
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsAccess
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model.SoundLayoutsStorage
import org.neidhardt.dynamicsoundboard.views.recyclerviewhelpers.DividerItemDecoration

/**
 * File created by eric.neidhardt on 08.03.2015.
 */
public class SoundLayouts : NavigationDrawerList
{
	private val eventBus: EventBus = EventBus.getDefault()

	private val soundLayoutsAccess: SoundLayoutsAccess = DynamicSoundboardApplication.getSoundLayoutsAccess()
	private val soundLayoutsStorage: SoundLayoutsStorage = DynamicSoundboardApplication.getSoundLayoutsStorage()

	public var presenter: SoundLayoutsPresenter = SoundLayoutsPresenter(this.soundLayoutsAccess, this.soundLayoutsStorage)
	public var adapter: SoundLayoutsAdapter = SoundLayoutsAdapter(this.presenter, this.eventBus)

	@SuppressWarnings("unused")
	public constructor(context: Context) : super(context)
	{
		this.init(context)
	}

	@SuppressWarnings("unused")
	public constructor(context: Context, attrs: AttributeSet) : super(context, attrs)
	{
		this.init(context)
	}

	@SuppressWarnings("unused")
	public constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)
	{
		this.init(context)
	}

	private fun init(context: Context)
	{
		this.presenter.adapter = this.adapter

		LayoutInflater.from(context).inflate(R.layout.view_sound_layout_list, this, true)

		val soundLayouts = this.findViewById(R.id.rv_sound_layouts_list) as RecyclerView
		if (!this.isInEditMode)
		{
			soundLayouts.addItemDecoration(DividerItemDecoration())
			soundLayouts.layoutManager = LinearLayoutManager(context)
			soundLayouts.itemAnimator = DefaultItemAnimator()
		}
		soundLayouts.adapter = this.adapter
	}

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		this.presenter.onAttachedToWindow()
	}

	override fun onDetachedFromWindow()
	{
		this.presenter.onDetachedFromWindow()
		super.onDetachedFromWindow()
	}

	override fun onFinishInflate() {
		super.onFinishInflate()
		this.presenter.view = this
	}

	override fun getItemCount(): Int
	{
		return presenter.values.size()
	}

	override fun getActionModeTitle(): Int
	{
		return R.string.cab_title_delete_sound_layouts
	}

	public fun isActive(): Boolean
	{
		return this.visibility == View.VISIBLE
	}

	public fun toggleVisibility()
	{
		if (this.visibility == View.VISIBLE)
			this.hideSelectSoundLayout()
		else
			this.showSelectSoundLayoutOverlay()
	}

	private fun showSelectSoundLayoutOverlay()
	{
		this.visibility = View.VISIBLE
	}

	private fun hideSelectSoundLayout()
	{
		this.visibility = View.INVISIBLE
	}

	override fun getPresenter(): NavigationDrawerListPresenter<*>?
	{
		return this.presenter
	}
}
