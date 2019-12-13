package org.neidhardt.dynamicsoundboard.soundactivity

import android.Manifest
import android.net.Uri
import io.reactivex.Observable
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import java.util.Arrays.asList

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
	fun updateUiStateForStoredSoundSheets() {
		// arrange
		val testData = asList(SoundSheet())
		`when`(this.model.loadSoundSheets()).thenReturn(Observable.just(testData))

		// action
		this.unit.onResumed()

		// verify
		verify(this.view).updateUiForSoundSheets(testData)
	}

	@Test
	fun saveDataOnPause() {
		// action
		this.unit.onPaused()

		// verify
		verify(this.model).saveData()
	}

	@Test
	fun userClicksSoundSheetTitleShouldOpenRenameDialog() {
		// action
		this.unit.userClicksSoundSheetTitle()

		// verify
		verify(this.view).openRenameSoundSheetDialog()
	}

	@Test
	fun userClicksAddSoundSheetShouldOpenDialog() {
		// action
		this.unit.userClicksAddSoundSheet()

		// verify
		verify(this.view).openAddSheetDialog()
	}

	@Test
	fun userClicksAddSoundShouldOpenDialog() {
		// action
		this.unit.userClicksAddSound()

		// verify
		verify(this.view).openAddSoundDialog()
	}

	@Test
	fun userClicksAddSoundsShouldOpenDialog() {
		// action
		this.unit.userClicksAddSounds()

		// verify
		verify(this.view).openAddSoundsDialog()
	}

	@Test
	fun userOpenSoundFileWithAppShouldOpenDialog() {
		// arrange
		`when`(this.model.getNameForNewSoundSheet()).thenReturn("test")
		`when`(this.model.getSoundSheets()).thenReturn(emptyList())
		val testUri = Mockito.mock(Uri::class.java)

		// action
		this.unit.userOpenSoundFileWithApp(testUri)

		// verify
		verify(this.view).openAddSoundDialog(testUri, "test", emptyList())
	}

	@Test
	fun userClicksLoadLayoutShouldOpenDialog() {
		// action
		this.unit.userClicksLoadLayout()

		// verify
		verify(this.view).openLoadLayoutDialog()
	}

	@Test
	fun userClicksStoreLayoutShouldOpenDialog() {
		// action
		this.unit.userClicksStoreLayout()

		// verify
		verify(this.view).openStoreLayoutDialog()
	}

	@Test
	fun userClicksPreferencesOpenPreferenceActivity() {
		// action
		this.unit.userClicksPreferences()

		// verify
		verify(this.view).openPreferenceActivity()
	}

	@Test
	fun userClicksInfoAboutShouldOpenInfoActivity() {
		// action
		this.unit.userClicksInfoAbout()

		// verify
		verify(this.view).openInfoActivity()
	}

	@Test
	fun userClicksClearSoundSheetsShouldOpenConfirmDialog() {
		// action
		this.unit.userClicksClearSoundSheets()

		// verify
		verify(this.view).openConfirmClearSoundSheetsDialog()
	}

	@Test
	fun userClickClearPlaylistShouldOpenConfirmDialog() {
		// action
		this.unit.userClickClearPlaylist()

		// verify
		verify(this.view).openConfirmClearPlaylistDialog()
	}

}