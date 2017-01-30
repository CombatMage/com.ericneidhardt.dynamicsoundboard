package org.neidhardt.dynamicsoundboard.views.sound_control

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import org.jetbrains.anko.*
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.views.selectableItemBackgroundBorderlessResource

/**
 * Created by eric.neidhardt@gmail.com on 30.01.2017.
 */
class LoopButton : FrameLayout {

	enum class State {
		FINITE,
		INDEFINITE
	}

	constructor(context: Context?) : super(context) {
		init()
	}

	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
		init()
	}

	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
		init()
	}

	var isLoopEnabled = false
		private set

	private var icon: ImageView? = null

	private fun init() = AnkoContext.createDelegate(this).apply {
		icon = imageView {
			this.padding = dip(12)
			this.scaleType = ImageView.ScaleType.CENTER_CROP
			this.backgroundResource = selectableItemBackgroundBorderlessResource
		}
		isClickable = true
		setState(State.FINITE)
	}

	fun setState(newState: State) {
		when (newState) {
			State.FINITE -> {
				this.isEnabled = false
				this.icon?.setImageResource(R.drawable.ic_loop)
			}
			State.INDEFINITE -> {
				this.isEnabled = true
				this.icon?.setImageResource(R.drawable.ic_loop_checked)
			}
		}
	}
}