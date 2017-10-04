package org.neidhardt.dynamicsoundboard.views.sound_control

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import android.widget.ImageView
import kotlinx.android.synthetic.main.layout_toggleplaylistbutton.view.*
import org.neidhardt.dynamicsoundboard.R

/**
 * Created by eric.neidhardt@gmail.com on 30.01.2017.
 */
class TogglePlaylistButton : FrameLayout {

	enum class State {
		IN_PLAYLIST,
		NOT_IN_PLAYLIST
	}

	private lateinit var icon: ImageView

	constructor(context: Context?) : super(context) { this.init() }
	constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) { this.init() }
	constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) { this.init() }

	private fun init() {
		LayoutInflater.from(context).inflate(R.layout.layout_toggleplaylistbutton, this, true)
		this.icon = this.imageview_toggleplaylistbutton

		this.state = State.NOT_IN_PLAYLIST
	}

	var state: State = State.NOT_IN_PLAYLIST
		set(newState) {
			field = newState
			when (newState) {
				State.IN_PLAYLIST -> {
					this.icon.setImageResource(R.drawable.ic_playlist_checked)
				}
				State.NOT_IN_PLAYLIST -> {
					this.icon.setImageResource(R.drawable.ic_playlist)
				}
			}
		}
}
