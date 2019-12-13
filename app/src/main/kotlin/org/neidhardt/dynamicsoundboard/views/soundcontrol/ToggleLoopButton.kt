package org.neidhardt.dynamicsoundboard.views.soundcontrol

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.android.synthetic.main.layout_toggleloopbutton.view.*
import org.neidhardt.dynamicsoundboard.R

/**
 * Created by eric.neidhardt@gmail.com on 30.01.2017.
 */
class ToggleLoopButton : FrameLayout {

	enum class State {
		LOOP_ENABLE,
		LOOP_DISABLE
	}

	private lateinit var icon: ImageView

	constructor(context: Context?) : super(context) { this.init() }
	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { this.init() }
	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { this.init() }

	private fun init() {
		LayoutInflater.from(context).inflate(R.layout.layout_toggleloopbutton, this, true)
		this.icon = this.imageview_toggleloopbutton

		this.state = ToggleLoopButton.State.LOOP_ENABLE
	}

	var state: ToggleLoopButton.State = ToggleLoopButton.State.LOOP_DISABLE
		set(newState) {
			field = newState
			when (newState) {
				State.LOOP_ENABLE -> {
					this.icon.setImageResource(R.drawable.ic_loop_checked)
				}
				State.LOOP_DISABLE -> {
					this.icon.setImageResource(R.drawable.ic_loop)
				}
			}
		}
}
