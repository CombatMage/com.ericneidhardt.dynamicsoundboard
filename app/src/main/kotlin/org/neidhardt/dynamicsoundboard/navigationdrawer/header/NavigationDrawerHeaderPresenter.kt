package org.neidhardt.dynamicsoundboard.navigationdrawer.header

import android.view.View
import android.widget.TextView
import org.neidhardt.ui_utils.presenter.ViewPresenter

/**
 * @author eric.neidhardt on 11.06.2016.
 */
interface NavigationDrawerHeader {

	var title: String

	fun animateLayoutChanges()
}

class NavigationDrawerHeaderPresenter : NavigationDrawerHeader, ViewPresenter {

	private var titleView: TextView? = null
	private var viewToAnimate: View? = null

	override var title: String = ""
		set(value) {
			field = value
			this.titleView?.text = value
		}

	fun init(titleView: TextView, viewToAnimate: View) {
		this.titleView = titleView
		this.viewToAnimate = viewToAnimate
	}

	override fun animateLayoutChanges() {
		this.viewToAnimate?.apply {
			this.animate().withLayer()
					.rotationXBy(180f)
					.setDuration(
							this.resources.getInteger(android.R.integer.config_shortAnimTime)
									.toLong()
					)
					.start()
		}
	}
}