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
class ToggleLoopButton : FrameLayout {

	enum class State {
		LOOP_ENABLE,
		LOOP_DISABLE
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

	private var icon: ImageView? = null

	private fun init() = AnkoContext.createDelegate(this).apply {
		icon = imageView {
			this.padding = dip(12)
			this.scaleType = ImageView.ScaleType.CENTER_CROP
			this.backgroundResource = selectableItemBackgroundBorderlessResource
		}
		isClickable = true
		state = State.LOOP_ENABLE
	}

	var state: State = State.LOOP_DISABLE
		set(newState) {
			field = newState
			when (newState) {
				State.LOOP_ENABLE -> {
					this.icon?.setImageResource(R.drawable.ic_loop)
				}
				State.LOOP_DISABLE -> {
					this.icon?.setImageResource(R.drawable.ic_loop_checked)
				}
			}
		}
}