package org.neidhardt.dynamicsoundboard.views.soundcontrol

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.android.synthetic.main.layout_playbutton.view.*
import org.neidhardt.dynamicsoundboard.R

/**
 * Created by eric.neidhardt@gmail.com on 27.01.2017.
 */
class PlayButton : FrameLayout {

	enum class State {
		PLAY,
		PAUSE,
		FADE
	}

	private lateinit var icon: ImageView

	constructor(context: Context?) : super(context) { this.init() }
	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { this.init() }
	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { this.init() }

	private fun init() {
		LayoutInflater.from(context).inflate(R.layout.layout_playbutton, this, true)
		this.icon = this.imageview_playbutton

		this.state = State.PLAY
	}

	var state: State = State.PLAY
		set(newState) {
			when (newState) {
				State.PLAY -> {
					this.icon.setImageResource(R.drawable.ic_play)
				}
				State.PAUSE -> {
					this.icon.setImageResource(R.drawable.ic_pause)
				}
				State.FADE -> {
					this.icon.setImageResource(R.drawable.ic_fade_out)
				}
			}
			field = newState
		}
}
