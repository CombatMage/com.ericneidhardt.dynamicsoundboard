package org.neidhardt.dynamicsoundboard.soundcontrol;

import android.telephony.TelephonyManager;
import org.junit.Test;
import org.neidhardt.dynamicsoundboard.AbstractBaseActivityTest;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.spy;

/**
 * Created by eric.neidhardt on 11.05.2015.
 */
public class PauseSoundOnCallListenerTest extends AbstractBaseActivityTest
{

	private PauseSoundOnCallListener listener;

	@Override
	public void setUp() throws Exception
	{
		super.setUp();

		this.listener = new PauseSoundOnCallListener();
		PauseSoundOnCallListener.registerListener(this.activity, this.listener, this.serviceManagerFragment);
	}

	@Test
	public void testOnCallStateChanged() throws Exception
	{
		// mock test data
		EnhancedMediaPlayer player = spy(new EnhancedMediaPlayer(TestDataGenerator.getRandomPlayerData()));
		this.service.getPlaylist().add(player);
		player.playSound();

		player = spy(new EnhancedMediaPlayer(TestDataGenerator.getRandomPlayerData()));
		this.service.getPlaylist().add(player);
		player.playSound();

		player = spy(new EnhancedMediaPlayer(TestDataGenerator.getRandomPlayerData()));
		this.service.getPlaylist().add(player);

		assertThat(this.service.getCurrentlyPlayingSounds().size(), equalTo(2));

		// actual test
		this.listener.onCallStateChanged(TelephonyManager.CALL_STATE_RINGING, "");
		assertThat(this.service.getCurrentlyPlayingSounds().size(), equalTo(0));
	}
}