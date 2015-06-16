package org.neidhardt.dynamicsoundboard.soundcontrol;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.service.ServiceManagerFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class PauseSoundOnCallListener extends PhoneStateListener
{
	private List<EnhancedMediaPlayer> pauseSounds;
	private SoundsDataAccess soundsDataAccess;

	public PauseSoundOnCallListener()
	{
		DynamicSoundboardApplication.getApplicationComponent().inject(this);
		this.pauseSounds = new ArrayList<>();
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber)
	{
		super.onCallStateChanged(state, incomingNumber);

		if (state == TelephonyManager.CALL_STATE_RINGING)
		{
			Set<EnhancedMediaPlayer> currentlyPlayingSounds = this.soundsDataAccess.getCurrentlyPlayingSounds();
			if (currentlyPlayingSounds.size() > 0)
			{
				List<EnhancedMediaPlayer> copyCurrentlyPlayingSounds = new ArrayList<>(currentlyPlayingSounds.size()); // copy to prevent concurrent modification exception
				copyCurrentlyPlayingSounds.addAll(currentlyPlayingSounds);

				for (EnhancedMediaPlayer sound : copyCurrentlyPlayingSounds)
					sound.pauseSound();
			}
			this.pauseSounds.addAll(currentlyPlayingSounds);
		}
		else if (state == TelephonyManager.CALL_STATE_IDLE)
		{
			for (EnhancedMediaPlayer player : this.pauseSounds)
				player.playSound();

			this.pauseSounds.clear();
		}
		super.onCallStateChanged(state, incomingNumber);
	}

	private void clearReferences()
	{
		this.pauseSounds.clear();
	}

	public static void registerListener(Context context, PauseSoundOnCallListener listener)
	{
		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (manager != null)
			manager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	public static void unregisterListener(Context context, PauseSoundOnCallListener listener)
	{
		listener.clearReferences();
		TelephonyManager manager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if (manager != null)
			manager.listen(listener, PhoneStateListener.LISTEN_NONE);
	}
}
