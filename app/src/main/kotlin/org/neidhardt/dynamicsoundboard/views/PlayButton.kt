package org.neidhardt.dynamicsoundboard.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import android.widget.ImageView
import org.jetbrains.anko.*
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
		setState(State.PLAY)
	}

	fun setState(newState: State) {
		when (newState) {
			PlayButton.State.PLAY -> {
				this.icon?.setImageResource(R.drawable.ic_play)
			}
			PlayButton.State.PAUSE -> {
				this.icon?.setImageResource(R.drawable.ic_pause)
			}
			PlayButton.State.FADE -> {
				this.icon?.setImageResource(R.drawable.ic_fade_out)
			}
		}
	}
}
