package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import android.animation.Animator
import android.content.Context
import android.util.AttributeSet
import android.view.View
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.misc.AnimationUtils

/**
 * File created by Eric Neidhardt on 12.11.2014.
 */
public class AddPauseFloatingActionButton : com.melnykov.fab.FloatingActionButton, Runnable, View.OnClickListener {

	private var presenter: AddPauseFloatingActionButtonPresenter? = null

	@SuppressWarnings("unused")
	public constructor(context: Context) : super(context) {
		this.init()
	}

	@SuppressWarnings("unused")
	public constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
		this.init()
	}

	@SuppressWarnings("unused")
	public constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
		this.init()
	}

	private fun init() {
		this.presenter = AddPauseFloatingActionButtonPresenter(DynamicSoundboardApplication.getSoundsDataAccess())
	}

	override fun onFinishInflate() {
		super.onFinishInflate()
		this.setOnClickListener(this)
		this.presenter!!.view = this
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		this.presenter!!.onAttachedToWindow()
	}

	override fun onDetachedFromWindow() {
		this.presenter!!.onDetachedFromWindow()
		super.onDetachedFromWindow()
	}

	override fun onClick(v: View) {
		this.presenter!!.onFabClicked()
	}

	override fun onCreateDrawableState(extraSpace: Int): IntArray {
		val state = super.onCreateDrawableState(extraSpace + PAUSE_STATE.size())
		if (this.presenter != null && this.presenter!!.isStatePause)
			View.mergeDrawableStates(state, PAUSE_STATE)

		return state
	}

	fun animateUiChanges() {
		this.post(this)
	}

	override fun run() {
		val animator = AnimationUtils.createCircularReveal(this@AddPauseFloatingActionButton,
				width,
				height,
				0f,
				(height * 2).toFloat())

		animator?.start()
	}

	companion object {
		private val PAUSE_STATE = intArrayOf(R.attr.state_pause)
	}
}
