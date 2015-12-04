package org.neidhardt.dynamicsoundboard.views.progressbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import de.greenrobot.event.EventBus
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.neidhardt.dynamicsoundboard.longtermtask.events.LongTermTaskStateChangedEvent
import org.neidhardt.dynamicsoundboard.longtermtask.events.LongTermTaskStateChangedEventListener
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.views.presenter.ViewPresenter

/**
 * File created by eric.neidhardt on 22.05.2015.
 */
interface ActivityProgressBar
{
	fun setVisibility(v: Int)
}

class ActivityProgressBarView : MaterialProgressBar, ActivityProgressBar
{
	private var presenter: ActivityProgressBarPresenter = ActivityProgressBarPresenter(EventBus.getDefault())

	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

	override fun onFinishInflate()
	{
		super.onFinishInflate()
		this.presenter.view = this
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
}

class ActivityProgressBarPresenter
(
		override val eventBus: EventBus
) : ViewPresenter<ActivityProgressBar?>, LongTermTaskStateChangedEventListener
{
	private val TAG = javaClass.name

	private var lastReceivedEvent: LongTermTaskStateChangedEvent? = null

	override val isEventBusSubscriber: Boolean = true
	override var view: ActivityProgressBar? = null
		set(value)
		{
			field = value
			if (this.lastReceivedEvent != null)
				this.onEventMainThread(this.lastReceivedEvent as LongTermTaskStateChangedEvent)
		}

	override fun onEventMainThread(event: LongTermTaskStateChangedEvent)
	{
		Logger.d(TAG, "onEvent() " + event)
		this.lastReceivedEvent = event
		val countOngoingTasks = event.nrOngoingTasks

		if (countOngoingTasks > 0)
			this.showProgressBar(true)
		else
			this.showProgressBar(false)
	}

	private fun showProgressBar(showProgressBar: Boolean)
	{
		Logger.d(TAG, "showProgressBar() " + showProgressBar)
		val progressBar = this.view ?: return

		if (showProgressBar)
			progressBar.setVisibility(View.VISIBLE)
		else
			progressBar.setVisibility(View.GONE)
	}

}