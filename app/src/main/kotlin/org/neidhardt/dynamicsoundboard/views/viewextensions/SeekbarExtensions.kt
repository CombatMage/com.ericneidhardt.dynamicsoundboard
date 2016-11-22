package org.neidhardt.dynamicsoundboard.views.viewextensions

import android.widget.SeekBar

/**
 * Created by eric.neidhardt@sevenval.com on 22.11.2016.
 */
fun SeekBar.setOnUserChangesListener(listener: (Int) -> Unit) {
	this.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

		override fun onProgressChanged(view: SeekBar?, progress: Int, fromUser: Boolean) {
			if (fromUser)
				listener.invoke(progress)
		}

		override fun onStartTrackingTouch(p0: SeekBar?) {}

		override fun onStopTrackingTouch(p0: SeekBar?) {}
	})
}