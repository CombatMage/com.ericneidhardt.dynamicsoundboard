package org.neidhardt.dynamicsoundboard.soundactivity

import android.net.Uri
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SoundActivityPresenterTest {

	@Mock private lateinit var view: SoundActivityContract.View
	@Mock private lateinit var model: SoundActivityContract.Model
	private lateinit var unit: SoundActivityContract.Presenter

	@Before
	fun setUp() {
		MockitoAnnotations.initMocks(this)
		this.unit = SoundActivityPresenter(this.view, this.model)
	}

	@Test
	fun onPaused() {
		// action
		this.unit.onPaused()

		// verify
		verify(this.model).saveData()
	}

	@Test
	fun userClicksSoundSheetTitle() {
		// action
		this.unit.userClicksSoundSheetTitle()

		// verify
		verify(this.view).openRenameSoundSheetDialog()
	}

	@Test
	fun userClicksAddSoundSheet() {
		// action
		this.unit.userClicksAddSoundSheet()

		// verify
		verify(this.view).openAddSheetDialog()
	}

	@Test
	fun userClicksAddSoundDialog() {
		// action
		this.unit.userClicksAddSoundDialog()

		// verify
		verify(this.view).openAddSoundDialog()
	}

	@Test
	fun userClicksAddSoundsDialog() {
		// action
		this.unit.userClicksAddSoundsDialog()

		// verify
		verify(this.view).openAddSoundsDialog()
	}

	@Test
	fun userOpenSoundFileWithApp() {
		// arrange
		`when`(this.model.getNameForNewSoundSheet()).thenReturn("test")
		`when`(this.model.getSoundSheets()).thenReturn(emptyList())
		val testUri = Mockito.mock(Uri::class.java)

		// action
		this.unit.userOpenSoundFileWithApp(testUri)

		// verify
		verify(this.view).openAddSoundDialog(testUri, "test", emptyList())
	}

}