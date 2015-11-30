package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import android.content.Context
import android.util.AttributeSet
import android.view.View
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.misc.AnimationUtils

/**
 * File created by Eric Neidhardt on 12.11.2014.
 */
private val PAUSE_STATE = intArrayOf(R.attr.state_pause)

public class AddPauseFloatingActionButton : com.melnykov.fab.FloatingActionButton, View.OnClickListener
{
	private val eventBus = EventBus.getDefault()
	private var presenter: AddPauseFloatingActionButtonPresenter? = null

	@SuppressWarnings("unused")
	public constructor(context: Context) : super(context) { this.init() }

	@SuppressWarnings("unused")
	public constructor(context: Context, attrs: AttributeSet) : super(context, attrs) { this.init() }

	@SuppressWarnings("unused")
	public constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) { this.init() }

	private fun init()
	{
		this.presenter = AddPauseFloatingActionButtonPresenter(this.eventBus, SoundboardApplication.getSoundsDataAccess())
	}

	override fun onFinishInflate()
	{
		super.onFinishInflate()
		this.setOnClickListener(this)
		this.presenter?.view = this
	}

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		this.presenter?.onAttachedToWindow()
	}

	override fun onDetachedFromWindow()
	{
		this.presenter?.onDetachedFromWindow()
		super.onDetachedFromWindow()
	}

	override fun onClick(v: View) {
		this.presenter?.onFabClicked()
	}

	override fun onCreateDrawableState(extraSpace: Int): IntArray
	{
		val state = super.onCreateDrawableState(extraSpace + PAUSE_STATE.size)
		if (this.presenter?.isStatePause ?: false)
			View.mergeDrawableStates(state, PAUSE_STATE)
		return state
	}

	fun animateUiChanges()
	{
		this.post { AnimationUtils.createCircularReveal(this,
				width,
				height,
				0f,
				(height * 2).toFloat())?.apply { start() } }
	}
}
