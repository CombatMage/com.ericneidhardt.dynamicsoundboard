package org.neidhardt.dynamicsoundboard.soundsheetfragment.viewhelper

import android.os.Bundle
import android.support.v7.widget.RecyclerView
import org.neidhardt.androidutils.EnhancedSupportFragment

/**
 * Created by eric.neidhardt@gmail.com on 13.11.2017.
 */
abstract class RecyclerViewFragment : EnhancedSupportFragment() {

	private val keyStateRecyclerView get() = "${this.fragmentTag}_recycler_view_state"

	abstract var recyclerView: RecyclerView

	override fun onRestoreState(savedInstanceState: Bundle) {
		super.onRestoreState(savedInstanceState)

		val layoutManager = this.recyclerView.layoutManager
		if (layoutManager != null) {
			savedInstanceState.putParcelable(
					keyStateRecyclerView,
					layoutManager.onSaveInstanceState())
		}
	}

	override fun onSaveState(outState: Bundle) {
		super.onSaveState(outState)

		val layoutManager = this.recyclerView.layoutManager
		layoutManager?.onRestoreInstanceState(outState.getParcelable(keyStateRecyclerView))
	}
}