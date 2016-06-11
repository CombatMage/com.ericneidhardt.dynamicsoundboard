package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import org.neidhardt.ui_utils.presenter.ViewPresenter

/**
 * File created by eric.neidhardt on 21.05.2015.
 */
interface AddPauseFloatingActionView {

	var state: AddPauseFloatingActionView.State

	enum class State { PLAY, ADD }
}

class AddPauseFloatingActionButtonPresenter : AddPauseFloatingActionView, ViewPresenter{

	private var fab: AddPauseFloatingActionButton? = null

	fun init(fab: AddPauseFloatingActionButton) {
		this.fab = fab
		this.refreshFab()
	}

	override var state = AddPauseFloatingActionView.State.ADD
		set(value) {
			if (value != field) {
				field = value
				this.refreshFab()
			}
		}

	private fun refreshFab() {
		this.fab?.let {
			it.refreshDrawableState()
			it.animateUiChanges()
		}
	}

	override fun stop() {
		super.stop()
		this.fab = null
	}
}
