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
class TogglePlaylistButton : FrameLayout {

	enum class State {
		IN_PLAYLIST,
		NOT_IN_PLAYLIST
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
			this.imageResource = R.drawable.selector_ic_playlist
			this.contentDescription = resources.getString(R.string.sound_control_content_description_playlist)
		}
		isClickable = true
		state = State.NOT_IN_PLAYLIST
	}

	var state: State = State.NOT_IN_PLAYLIST
		set(newState) {
			field = newState
			when (newState) {
				State.IN_PLAYLIST -> {
					this.icon?.isSelected = true
				}
				State.NOT_IN_PLAYLIST -> {
					this.icon?.isSelected = false
				}
			}
		}
}