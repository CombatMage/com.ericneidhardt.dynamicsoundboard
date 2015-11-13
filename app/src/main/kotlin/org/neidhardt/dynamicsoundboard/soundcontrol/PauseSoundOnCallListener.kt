package org.neidhardt.dynamicsoundboard.soundcontrol

import android.content.Context
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import java.util.*


class PauseSoundOnCallListener : PhoneStateListener()
{
	private val pauseSounds: MutableList<MediaPlayerController> = ArrayList()
	private val soundsDataAccess = DynamicSoundboardApplication.getSoundsDataAccess()

	override fun onCallStateChanged(state: Int, incomingNumber: String?)
	{
		super.onCallStateChanged(state, incomingNumber)

		if (state == TelephonyManager.CALL_STATE_RINGING)
		{
			val currentlyPlayingSounds = this.soundsDataAccess.currentlyPlayingSounds
			if (currentlyPlayingSounds.size > 0) {
				val copyCurrentlyPlayingSounds = ArrayList<MediaPlayerController>(currentlyPlayingSounds.size) // copy to prevent concurrent modification exception
				copyCurrentlyPlayingSounds.addAll(currentlyPlayingSounds)

				for (sound in copyCurrentlyPlayingSounds)
					sound.pauseSound()
			}
			this.pauseSounds.addAll(currentlyPlayingSounds)
		} else if (state == TelephonyManager.CALL_STATE_IDLE) {
			for (player in this.pauseSounds)
				player.playSound()

			this.pauseSounds.clear()
		}
		super.onCallStateChanged(state, incomingNumber)
	}

	private fun clearReferences() {
		this.pauseSounds.clear()
	}

	companion object {

		fun registerListener(context: Context, listener: PauseSoundOnCallListener) {
			val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
			manager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE)
		}

		fun unregisterListener(context: Context, listener: PauseSoundOnCallListener) {
			listener.clearReferences()
			val manager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
			manager.listen(listener, PhoneStateListener.LISTEN_NONE)
		}
	}
}
