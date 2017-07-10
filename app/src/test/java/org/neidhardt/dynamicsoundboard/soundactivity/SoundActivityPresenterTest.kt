package org.neidhardt.dynamicsoundboard.soundactivity

import android.Manifest
import android.net.Uri
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations
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
	fun userClicksAddSound() {
		// action
		this.unit.userClicksAddSound()

		// verify
		verify(this.view).openAddSoundDialog()
	}

	@Test
	fun userClicksAddSounds() {
		// action
		this.unit.userClicksAddSounds()

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

	@Test
	fun onUserHasChangedPermissions() {
		// arrange
		`when`(this.view.getMissingPermissions()).thenReturn(
				asList(Manifest.permission.READ_EXTERNAL_STORAGE).toTypedArray())

		// action
		this.unit.onUserHasChangedPermissions()

		// verify
		verify(this.view).openExplainPermissionReadStorageDialog()

		// arrange
		`when`(this.view.getMissingPermissions()).thenReturn(
				asList(Manifest.permission.WRITE_EXTERNAL_STORAGE).toTypedArray())

		// action
		this.unit.onUserHasChangedPermissions()

		// verify
		verify(this.view).openExplainPermissionWriteStorageDialog()
	}

	@Test
	fun userClicksLoadLayout() {
		// action
		this.unit.userClicksLoadLayout()

		// verify
		verify(this.view).openLoadLayoutDialog()
	}

	@Test
	fun userClicksStoreLayout() {
		// action
		this.unit.userClicksStoreLayout()

		// verify
		verify(this.view).openStoreLayoutDialog()
	}

	@Test
	fun userClicksPreferences() {
		// action
		this.unit.userClicksPreferences()

		// verify
		verify(this.view).openPreferenceActivity()
	}

	@Test
	fun userClicksInfoAbout() {
		// action
		this.unit.userClicksInfoAbout()

		// verify
		verify(this.view).openInfoActivity()
	}

	@Test
	fun userClicksClearSoundSheets() {
		// action
		this.unit.userClicksClearSoundSheets()

		// verify
		verify(this.view).openConfirmClearSoundSheetsDialog()
	}

	@Test
	fun userClickClearPlaylist() {
		// action
		this.unit.userClickClearPlaylist()

		// verify
		verify(this.view).openConfirmClearPlaylistDialog()
	}

}