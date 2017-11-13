package org.neidhardt.dynamicsoundboard.navigationdrawerfragment

import io.reactivex.Observable
import org.junit.Test

import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.PLAYLIST_TAG
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent

/**
 * Created by eric.neidhardt@gmail.com on 13.11.2017.
 */
class NavigationDrawerFragmentPresenterTest {

	@Mock private lateinit var view: NavigationDrawerFragmentContract.View
	@Mock private lateinit var model: NavigationDrawerFragmentContract.Model
	private lateinit var unit: NavigationDrawerFragmentContract.Presenter

	@Before
	fun setUp() {
		MockitoAnnotations.initMocks(this)
		this.unit = NavigationDrawerFragmentPresenter(
				this.view,
				this.model)
	}

	@Test
	fun pausingSoundShouldUpdateUi() {
		// arrange
		val testPlayer = Mockito.mock(MediaPlayerController::class.java)
		val testEvent = MediaPlayerStateChangedEvent(testPlayer, true)
		`when`(testEvent.fragmentTag).thenReturn(PLAYLIST_TAG)
		`when`(this.model.mediaPlayerStateChangedEvents).thenReturn(Observable.just(testEvent))


		// action

		//verify
		verify(this.view).displayedPlaylist = emptyList()
	}
}