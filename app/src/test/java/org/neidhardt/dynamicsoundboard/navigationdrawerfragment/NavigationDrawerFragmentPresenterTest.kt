package org.neidhardt.dynamicsoundboard.navigationdrawerfragment

import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.mediaplayer.PLAYLIST_TAG
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerStateChangedEvent
import org.neidhardt.dynamicsoundboard.model.MediaPlayerData
import org.robolectric.RobolectricTestRunner

/**
 * Created by eric.neidhardt@gmail.com on 13.11.2017.
 */
@RunWith(RobolectricTestRunner::class)
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
	fun changePlayerStateSoundShouldUpdateUi() {
		// arrange
		val testPlayer = Mockito.mock(MediaPlayerController::class.java)
		`when`(testPlayer.mediaPlayerData).thenReturn(
				MediaPlayerData().apply {
					this.fragmentTag = PLAYLIST_TAG
				})

		val testPauseEvent = MediaPlayerStateChangedEvent(
				testPlayer,
				true)

		var testEventEmitter: ObservableEmitter<MediaPlayerStateChangedEvent>? = null
		val testEvents = Observable.create<MediaPlayerStateChangedEvent> { subscriber ->
			testEventEmitter = subscriber
		}
		`when`(this.model.mediaPlayerStateChangedEvents).thenReturn(testEvents)

		`when`(this.model.soundSheets).thenReturn(Observable.empty())
		`when`(this.model.playList).thenReturn(Observable.empty())
		`when`(this.model.soundLayouts).thenReturn(Observable.empty())
		`when`(this.model.mediaPlayerCompletedEvents).thenReturn(Observable.empty())

		// action
		this.unit.viewCreated() // subscribe to observable
		testEventEmitter?.onNext(testPauseEvent)

		//verify
		verify(this.view).refreshDisplayedPlaylist()
	}
}