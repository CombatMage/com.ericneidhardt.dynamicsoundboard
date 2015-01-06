package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import com.ericneidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;

import java.util.ArrayList;
import java.util.List;



public class PauseSoundOnCallListener extends PhoneStateListener
{
	private ServiceManagerFragment soundManagerFragment;
	private List<EnhancedMediaPlayer> pauseSounds;

	public PauseSoundOnCallListener()
	{
		this.pauseSounds = new ArrayList<>();
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber)
	{
		super.onCallStateChanged(state, incomingNumber);

		if (state == TelephonyManager.CALL_STATE_RINGING)
		{
			List<EnhancedMediaPlayer> currentlyPlayingSounds = soundManagerFragment.getSoundService().getCurrentlyPlayingSounds();
			if (currentlyPlayingSounds.size() > 0)
			{
				for (EnhancedMediaPlayer sound : currentlyPlayingSounds)
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
		this.soundManagerFragment = null;
	}

	public static void registerListener(Context context, PauseSoundOnCallListener listener, ServiceManagerFragment soundManagerFragment)
	{
		listener.soundManagerFragment = soundManagerFragment;
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
