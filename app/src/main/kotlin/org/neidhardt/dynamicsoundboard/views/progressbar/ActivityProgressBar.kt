package org.neidhardt.dynamicsoundboard.views.progressbar

import android.content.Context
import android.util.AttributeSet
import android.view.View
import me.zhanghai.android.materialprogressbar.MaterialProgressBar
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.dynamicsoundboard.longtermtask.events.LongTermTaskStateChangedEvent
import org.neidhardt.dynamicsoundboard.longtermtask.events.LongTermTaskStateChangedEventListener
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.misc.registerIfRequired
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 22.05.2015.
 */
class ActivityProgressBarView : MaterialProgressBar
{
	private var presenter: ActivityProgressBarPresenter by Delegates.notNull()

	constructor(context: Context) : super(context)

	constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

	constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

	override fun onFinishInflate()
	{
		super.onFinishInflate()
		this.presenter = ActivityProgressBarPresenter(EventBus.getDefault(), this)
	}

	override fun onAttachedToWindow()
	{
		super.onAttachedToWindow()
		this.presenter.onAttached()
	}

	override fun onDetachedFromWindow()
	{
		this.presenter.onDetached()
		super.onDetachedFromWindow()
	}
}

class ActivityProgressBarPresenter
(
		private val eventBus: EventBus,
		private val view: ActivityProgressBarView

) : LongTermTaskStateChangedEventListener
{
	private val TAG = javaClass.name

	private var lastReceivedEvent: LongTermTaskStateChangedEvent? = null

	fun onAttached()
	{
		this.eventBus.registerIfRequired(this)
		if (this.lastReceivedEvent != null)
			this.onEvent(this.lastReceivedEvent as LongTermTaskStateChangedEvent)
	}

	fun onDetached()
	{
		this.eventBus.unregister(this)
	}

	@Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
	override fun onEvent(event: LongTermTaskStateChangedEvent)
	{
		Logger.d(TAG, "onEvent() " + event)
		this.lastReceivedEvent = event
		event.nrOngoingTasks.let { count -> this.showProgressBar(count > 0) }
	}

	private fun showProgressBar(showProgressBar: Boolean)
	{
		Logger.d(TAG, "showProgressBar() " + showProgressBar)
		this.view.let { it.visibility = if (showProgressBar) View.VISIBLE else View.GONE }
	}

}