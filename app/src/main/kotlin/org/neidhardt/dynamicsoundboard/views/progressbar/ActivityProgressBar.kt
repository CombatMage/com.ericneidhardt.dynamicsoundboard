package org.neidhardt.dynamicsoundboard.views.progressbar

import android.content.Context
import android.os.Handler
import android.util.AttributeSet
import android.view.View
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.utils.ValueHolder
import rx.Subscription
import rx.android.schedulers.AndroidSchedulers
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 22.05.2015.
 */
class ActivityProgressBarView : MaterialProgressBar {

	private var presenter: ActivityProgressBarPresenter by Delegates.notNull()

	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

	override fun onFinishInflate() {
		super.onFinishInflate()
		this.presenter = ActivityProgressBarPresenter(SoundboardApplication.taskCounter, this)
	}

	override fun onAttachedToWindow() {
		super.onAttachedToWindow()
		this.presenter.onAttached()
	}

	override fun onDetachedFromWindow() {
		this.presenter.onDetached()
		super.onDetachedFromWindow()
	}
}

class ActivityProgressBarPresenter(
		private val taskCounter: ValueHolder<Int>,
		private val view: ActivityProgressBarView
) {
	private val handler = Handler()

	private var subscription: Subscription? = null

	fun onAttached() {
		this.showProgressBar(this.taskCounter.value > 0)

		this.subscription = this.taskCounter.changes()
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { newValue -> this.showProgressBar(newValue > 0) }
	}

	fun onDetached() {
		this.subscription?.unsubscribe()
	}

	private fun showProgressBar(showProgressBar: Boolean) {
		if (showProgressBar)
			this.view.visibility = View.VISIBLE
		else
			this.handler.postDelayed( { this.view.visibility = View.GONE }, 1000)
	}

}