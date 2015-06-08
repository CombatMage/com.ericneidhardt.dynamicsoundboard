package org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views;

import org.junit.Before;
import org.junit.Test;
import org.neidhardt.dynamicsoundboard.AbstractBaseActivityTest;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerCompletedEvent;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.*;

/**
 * File created by eric.neidhardt on 20.04.2015.
 */
public class PlaylistAdapterTest extends AbstractBaseActivityTest
{
	private PlaylistAdapter adapter;

	@Override
	@Before
	public void setUp() throws Exception
	{
		super.setUp();

		PlaylistPresenter mockPresenter = mock(PlaylistPresenter.class);
		when(mockPresenter.getSoundDataModel()).thenReturn(this.serviceManagerFragment);

		this.adapter = spy(new PlaylistAdapter(mockPresenter));
	}

	@Test
	public void testGetValues() throws Exception
	{
		assertTrue(this.adapter.getValues().isEmpty());

		this.serviceManagerFragment.getPlayList().add(mock(EnhancedMediaPlayer.class));
		this.serviceManagerFragment.getPlayList().add(mock(EnhancedMediaPlayer.class));
		assertThat(adapter.getValues().size(), equalTo(2));
	}

	@Test
	public void testGetItemCount() throws Exception
	{
		assertThat(adapter.getItemCount(), equalTo(0));

		this.serviceManagerFragment.getPlayList().add(mock(EnhancedMediaPlayer.class));
		this.serviceManagerFragment.getPlayList().add(mock(EnhancedMediaPlayer.class));
		assertThat(adapter.getItemCount(), equalTo(2));
	}

	@Test
	public void testGetCurrentItemIndex() throws Exception
	{
		assertThat(adapter.getCurrentItemIndex(), equalTo(null));

		adapter.setCurrentItemIndex(1);
		assertThat(adapter.getCurrentItemIndex(), equalTo(1));
	}

	@Test(expected = IllegalStateException.class)
	public void testStartOrStopPlayList() throws Exception
	{
		EnhancedMediaPlayer playerA = mock(EnhancedMediaPlayer.class);
		adapter.startOrStopPlayList(playerA);
	}

	@Test
	public void testStartOrStopPlayList1() throws Exception
	{
		EnhancedMediaPlayer playerA = mock(EnhancedMediaPlayer.class);
		EnhancedMediaPlayer playerB = mock(EnhancedMediaPlayer.class);

		this.serviceManagerFragment.getPlayList().add(playerA);
		this.serviceManagerFragment.getPlayList().add(playerB);

		when(playerA.isPlaying()).thenReturn(true);

		adapter.startOrStopPlayList(playerA);
		verify(playerA, atLeastOnce()).pauseSound();
		verify(playerB, atLeastOnce()).stopSound();
		assertThat(adapter.getCurrentItemIndex(), equalTo(0));
	}

	@Test
	public void testStartOrStopPlayList2() throws Exception
	{
		EnhancedMediaPlayer playerA = mock(EnhancedMediaPlayer.class);
		EnhancedMediaPlayer playerB = mock(EnhancedMediaPlayer.class);

		this.serviceManagerFragment.getPlayList().add(playerA);
		this.serviceManagerFragment.getPlayList().add(playerB);

		when(playerA.isPlaying()).thenReturn(false);

		adapter.startOrStopPlayList(playerA);
		verify(playerA, atLeastOnce()).playSound();
		verify(playerB, atLeastOnce()).stopSound();
		assertThat(adapter.getCurrentItemIndex(), equalTo(0));
	}

	@Test
	public void testStartOrStopPlayList3() throws Exception
	{
		EnhancedMediaPlayer playerA = mock(EnhancedMediaPlayer.class);
		EnhancedMediaPlayer playerB = mock(EnhancedMediaPlayer.class);

		this.serviceManagerFragment.getPlayList().add(playerA);
		this.serviceManagerFragment.getPlayList().add(playerB);

		when(playerB.isPlaying()).thenReturn(true);

		adapter.startOrStopPlayList(playerB);
		verify(playerA, atLeastOnce()).stopSound();
		verify(playerB, atLeastOnce()).pauseSound();
		assertThat(adapter.getCurrentItemIndex(), equalTo(1));
	}

	@Test
	public void testStartOrStopPlayList4() throws Exception
	{
		EnhancedMediaPlayer playerA = mock(EnhancedMediaPlayer.class);
		EnhancedMediaPlayer playerB = mock(EnhancedMediaPlayer.class);

		this.serviceManagerFragment.getPlayList().add(playerA);
		this.serviceManagerFragment.getPlayList().add(playerB);

		when(playerB.isPlaying()).thenReturn(false);

		adapter.startOrStopPlayList(playerB);
		verify(playerA, atLeastOnce()).stopSound();
		verify(playerB, atLeastOnce()).playSound();
		assertThat(adapter.getCurrentItemIndex(), equalTo(1));
	}

	@Test
	public void testStartOrStopPlayList5() throws Exception
	{
		EnhancedMediaPlayer playerA = mock(EnhancedMediaPlayer.class);
		EnhancedMediaPlayer playerB = mock(EnhancedMediaPlayer.class);

		this.serviceManagerFragment.getPlayList().add(playerA);
		this.serviceManagerFragment.getPlayList().add(playerB);

		when(playerB.isPlaying()).thenReturn(false);

		adapter.setCurrentItemIndex(1);
		adapter.startOrStopPlayList(playerA);
		verify(playerA, atLeastOnce()).playSound();
		verify(playerB, atLeastOnce()).stopSound();
		assertThat(adapter.getCurrentItemIndex(), equalTo(0));
	}

	@Test
	public void testStartOrStopPlayList6() throws Exception
	{
		EnhancedMediaPlayer playerA = mock(EnhancedMediaPlayer.class);

		this.serviceManagerFragment.getPlayList().add(playerA);

		when(playerA.isPlaying()).thenReturn(false);

		adapter.setCurrentItemIndex(1);
		adapter.startOrStopPlayList(playerA);
		verify(playerA, atLeastOnce()).playSound();
		assertThat(adapter.getCurrentItemIndex(), equalTo(0));

		when(playerA.isPlaying()).thenReturn(true);

		adapter.setCurrentItemIndex(1);
		adapter.startOrStopPlayList(playerA);
		verify(playerA, atLeastOnce()).pauseSound();
		assertThat(adapter.getCurrentItemIndex(), equalTo(0));
	}

	@Test
	public void testOnEvent() throws Exception
	{
		MediaPlayerData dataA = mock(MediaPlayerData.class);

		EnhancedMediaPlayer playerA = mock(EnhancedMediaPlayer.class);
		EnhancedMediaPlayer playerB = mock(EnhancedMediaPlayer.class);

		when(playerA.isPlaying()).thenReturn(true);
		when(playerA.getMediaPlayerData()).thenReturn(dataA);

		this.serviceManagerFragment.getPlayList().add(playerA);
		this.serviceManagerFragment.getPlayList().add(playerB);

		adapter.setCurrentItemIndex(0);
		adapter.onEvent(new MediaPlayerCompletedEvent(dataA));

		assertThat(adapter.getCurrentItemIndex(), equalTo(1));
		verify(playerB, atLeastOnce()).playSound();
	}

	@Test
	public void testOnEvent1() throws Exception
	{
		MediaPlayerData dataB = mock(MediaPlayerData.class);

		EnhancedMediaPlayer playerA = mock(EnhancedMediaPlayer.class);
		EnhancedMediaPlayer playerB = mock(EnhancedMediaPlayer.class);

		when(playerB.isPlaying()).thenReturn(true);
		when(playerB.getMediaPlayerData()).thenReturn(dataB);

		this.serviceManagerFragment.getPlayList().add(playerA);
		this.serviceManagerFragment.getPlayList().add(playerB);

		adapter.setCurrentItemIndex(1);
		adapter.onEvent(new MediaPlayerCompletedEvent(dataB));

		assertThat(adapter.getCurrentItemIndex(), equalTo(0));
		verify(playerA, atLeastOnce()).playSound();
	}

	@Test
	public void testOnEvent2() throws Exception
	{
		MediaPlayerData data = mock(MediaPlayerData.class);

		EnhancedMediaPlayer player = mock(EnhancedMediaPlayer.class);

		when(player.isPlaying()).thenReturn(true);
		when(player.getMediaPlayerData()).thenReturn(data);

		this.serviceManagerFragment.getPlayList().add(player);

		adapter.setCurrentItemIndex(0);
		adapter.onEvent(new MediaPlayerCompletedEvent(data));

		assertThat(adapter.getCurrentItemIndex(), equalTo(0));
		verify(player, atLeastOnce()).playSound();
	}

}