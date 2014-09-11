package com.ericneidhardt.dynamicsoundboard.mediaplayer;

import android.media.MediaPlayer;

import java.util.List;

/**
 * Created by eric.neidhardt on 27.08.2014.
 */
public interface OnMediaPlayersRetrievedCallback {

	public void onMediaPlayersRetrieved(List<EnhancedMediaPlayer> mediaPlayers);
}
