package org.neidhardt.dynamicsoundboard.notifications;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.neidhardt.dynamicsoundboard.BaseTest;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.soundactivity.events.ActivityResumedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement_old.MusicService;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;

/**
 * File created by eric.neidhardt on 07.05.2015.
 */
public class NotificationHandlerTest extends BaseTest
{
	private NotificationHandler notificationHandler;
	private MusicService service;

	@Before
	public void setUp() throws Exception
	{
		this.service = mock(MusicService.class);
		this.notificationHandler = spy(new NotificationHandler(this.service));
	}

	@Test
	public void testOnEvent() throws Exception
	{
		// mock data
		ActivityResumedEvent event = mock(ActivityResumedEvent.class);

		PendingSoundNotification notification = mock(PendingSoundNotification.class);
		when(notification.isPlaylistNotification()).thenReturn(true);
		when(notification.getPlayerId()).thenReturn("testId");

		EnhancedMediaPlayer player = mock(EnhancedMediaPlayer.class);
		when(player.isPlaying()).thenReturn(false);

		when(this.service.searchInPlaylistForId("testId")).thenReturn(player);

		List<PendingSoundNotification> notifications = new ArrayList<>();
		notifications.add(notification);
		this.notificationHandler.setNotifications(notifications);

		// actual test
		Mockito.doNothing().when(this.notificationHandler).removePlayListNotification();
		this.notificationHandler.onEvent(event);
		verify(this.notificationHandler, atLeastOnce()).removePlayListNotification();
	}
}