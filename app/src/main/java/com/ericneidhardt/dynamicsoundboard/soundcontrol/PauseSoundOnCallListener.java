package com.ericneidhardt.dynamicsoundboard.soundcontrol;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.ericneidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;



public class PauseSoundOnCallListener extends PhoneStateListener
{
	private SoundManagerFragment soundManagerFragment;
	private List<EnhancedMediaPlayer> pauseSounds;

	public PauseSoundOnCallListener()
	{
		this.pauseSounds = new ArrayList<EnhancedMediaPlayer>();
	}

	@Override
	public void onCallStateChanged(int state, String incomingNumber)
	{
		super.onCallStateChanged(state, incomingNumber);

		if (state == TelephonyManager.CALL_STATE_RINGING)
		{
			Map<String, List<EnhancedMediaPlayer>> allPlayers = this.soundManagerFragment.getSounds();
			for (String fragmentTag : allPlayers.keySet())
			{
				for (EnhancedMediaPlayer player : allPlayers.get(fragmentTag))
				{
					if (player.isPlaying())
					{
						this.pauseSounds.add(player);
						player.pauseSound();
					}
				}
			}
			List<EnhancedMediaPlayer> playList = this.soundManagerFragment.getPlayList();
			for (EnhancedMediaPlayer player : playList)
			{
				if (player.isPlaying())
				{
					this.pauseSounds.add(player);
					player.pauseSound();
				}
			}
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

	public static void registerListener(Context context, PauseSoundOnCallListener listener, SoundManagerFragment soundManagerFragment)
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
