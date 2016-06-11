package org.neidhardt.dynamicsoundboard.views.floatingactionbutton

import org.neidhardt.ui_utils.presenter.ViewPresenter

/**
 * File created by eric.neidhardt on 21.05.2015.
 */
interface AddPauseFloatingAction {

	var state: AddPauseFloatingAction.State

	enum class State { PLAY, ADD }
}

class AddPauseFloatingActionButtonPresenter : AddPauseFloatingAction, ViewPresenter{

	private var fab: AddPauseFloatingActionButtonView? = null

	fun init(fab: AddPauseFloatingActionButtonView) {
		this.fab = fab
		this.refreshFab()
	}

	override var state = AddPauseFloatingAction.State.ADD
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
