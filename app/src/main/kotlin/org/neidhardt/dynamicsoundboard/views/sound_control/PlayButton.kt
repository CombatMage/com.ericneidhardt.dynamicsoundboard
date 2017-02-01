package org.neidhardt.dynamicsoundboard.views.sound_control

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import org.jetbrains.anko.*
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.views.selectableItemBackgroundBorderlessResource

/**
 * Created by eric.neidhardt@gmail.com on 27.01.2017.
 */
class PlayButton : FrameLayout {

	enum class State {
		PLAY,
		PAUSE,
		FADE
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
			this.contentDescription = resources.getString(R.string.sound_control_content_description_play)
		}
		isClickable = true
		state = State.PLAY
	}

	var state: State = State.PLAY
		set(newState) {
			when (newState) {
				State.PLAY -> {
					this.icon?.setImageResource(R.drawable.ic_play)
				}
				State.PAUSE -> {
					this.icon?.setImageResource(R.drawable.ic_pause)
				}
				State.FADE -> {
					this.icon?.setImageResource(R.drawable.ic_fade_out)
				}
			}
			field = newState
		}
}
