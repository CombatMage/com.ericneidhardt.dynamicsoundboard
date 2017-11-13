package org.neidhardt.dynamicsoundboard.soundsheetfragment

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController

/**
 * Created by eric.neidhardt@gmail.com on 13.11.2017.
 */
class SoundSheetFragmentPresenterTest {

	@Mock private lateinit var view: SoundSheetFragmentContract.View
	@Mock private lateinit var model: SoundSheetFragmentContract.Model
	private lateinit var unit: SoundSheetFragmentContract.Presenter


	@Before
	fun setUp() {
		MockitoAnnotations.initMocks(this)
		this.unit = SoundSheetFragmentPresenter(
				this.view,
				this.model)
	}

	@Test
	fun onUserClicksFabShouldPauseSoundsIfRunning() {
		// arrange
		val testData = listOf(Mockito.mock(MediaPlayerController::class.java))
		`when`(this.model.getCurrentlyPlayingSounds()).thenReturn(testData)

		// action
		this.unit.onUserClicksFab()

		// verify
		testData.forEach { verify(it).pauseSound() }
	}
}