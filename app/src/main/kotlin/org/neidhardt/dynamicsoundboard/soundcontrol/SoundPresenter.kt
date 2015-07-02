package org.neidhardt.dynamicsoundboard.soundcontrol

import android.support.v7.widget.RecyclerView
import de.greenrobot.event.EventBus
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer
import org.neidhardt.dynamicsoundboard.presenter.ViewPresenter
import org.neidhardt.dynamicsoundboard.soundmanagement.events.OnSoundsChangedEventListener
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundAddedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundChangedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundsRemovedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import java.util.ArrayList

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
public class SoundPresenter
(
		fragmentTag: String,
		eventBus: EventBus,
		soundsDataAccess: SoundsDataAccess
) : ViewPresenter<RecyclerView>, OnSoundsChangedEventListener
{
	private val eventBus = eventBus
	private val fragmentTag = fragmentTag
	private val soundsDataAccess = soundsDataAccess

	private val values = ArrayList<EnhancedMediaPlayer>()

	public var adapter: SoundAdapter? = null


	override fun onAttachedToWindow()
	{
		this.values.clear()
		this.values.addAll(this.soundsDataAccess.getSoundsInFragment(this.fragmentTag))
		this.adapter!!.notifyDataSetChanged()

		if (!this.eventBus.isRegistered(this))
			this.eventBus.register(this)
	}

	override fun onDetachedFromWindow()
	{
		this.eventBus.unregister(this)
	}

	override fun onEventMainThread(event: SoundAddedEvent)
	{
		val newPlayer = event.getPlayer()
		if (newPlayer.getMediaPlayerData().getFragmentTag().equals(this.fragmentTag))
		{
			val positionToInsert = newPlayer.getMediaPlayerData().getSortOrder()
			val count = this.values.size()
			for (i in 0..count - 1)
			{
				val existingPlayer = this.values.get(i)
				if (positionToInsert < existingPlayer.getMediaPlayerData().getSortOrder())
				{
					this.values.add(i, newPlayer)
					this.adapter?.notifyItemInserted(i)
					return
				}
			}
			this.values.add(newPlayer)
			this.adapter?.notifyItemInserted(count)
		}
	}

	override fun onEventMainThread(event: SoundsRemovedEvent)
	{
		val players = event.getPlayers()
		if (players == null)
			this.adapter?.notifyDataSetChanged()
		else
		{
			for (player in players)
				this.removePlayerAndNotifyAdapter(player)
		}
	}

	private fun removePlayerAndNotifyAdapter(player: EnhancedMediaPlayer)
	{
		val index = this.values.indexOf(player)
		if (index != -1)
		{
			this.values.remove(player)
			this.adapter.notifyItemRemoved(index)
		}
	}

	override fun onEventMainThread(event: SoundChangedEvent)
	{
		val player = event.getPlayer()
		val index = this.values.indexOf(player)
		if (index != -1)
			this.adapter?.notifyItemChanged(index)
	}

	// currently unused
	override fun getView(): RecyclerView?
	{
		throw UnsupportedOperationException()
	}

	override fun setView(view: RecyclerView?)
	{
		throw UnsupportedOperationException()
	}
}