package org.neidhardt.dynamicsoundboard.playlist;

import junit.framework.TestCase;
import org.junit.Test;
import org.neidhardt.dynamicsoundboard.AbstractBaseActivityTest;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerCompletedEvent;
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

/**
 * Created by eric.neidhardt on 20.04.2015.
 */
public class PlaylistAdapterTest extends AbstractBaseActivityTest
{

	@Test
	public void testOnEvent() throws Exception
	{
		PlaylistAdapter adapter = spy(new PlaylistAdapter());
		when(adapter.getValues()).thenReturn(this.serviceManagerFragment.getPlayList());

		final boolean[] playCalled = {false};
		MediaPlayerData data = TestDataGenerator.getMediaPlayerData("player1", "");
		EnhancedMediaPlayer player = new EnhancedMediaPlayer(data)
		{
			@Override
			public boolean playSound()
			{
				playCalled[0] = true;
				return true;
			}
		};
		this.serviceManagerFragment.getPlayList().add(player);

		assertThat(adapter.getItemCount(), equalTo(1));

		adapter.onEvent(new MediaPlayerCompletedEvent(data));
		assertTrue(playCalled[0]);
	}
}