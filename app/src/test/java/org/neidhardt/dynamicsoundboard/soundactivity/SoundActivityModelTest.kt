package org.neidhardt.dynamicsoundboard.soundactivity

import android.content.Context
import android.content.Intent
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.neidhardt.dynamicsoundboard.manager.SoundSheetManager
import org.neidhardt.dynamicsoundboard.persistance.SaveDataIntentService
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.robolectric.RobolectricTestRunner

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
@RunWith(RobolectricTestRunner::class)
class SoundActivityModelTest {

	@Mock private lateinit var context: Context
	@Mock private lateinit var soundSheetManager: SoundSheetManager
	private lateinit var unit: SoundActivityModel

	@Before
	fun setUp() {
		MockitoAnnotations.initMocks(this)
		this.unit = SoundActivityModel(this.context, this.soundSheetManager)
	}

	@Test
	fun saveData() {
		// action
		this.unit.saveData()

		// verify
		verify(this.context).startService(Intent(this.context, SaveDataIntentService::class.java))
	}

	@Test
	fun getSoundSheets() {
		// arrange
		val testData = listOf(SoundSheet(), SoundSheet())
		`when`(this.soundSheetManager.soundSheets).thenReturn(testData)

		// action
		val result = this.unit.getSoundSheets()

		// verify
		assert(result.size == 2)
	}

	@Test
	fun getNameForNewSoundSheet() {
		// arrange
		`when`(this.soundSheetManager.suggestedName).thenReturn("test")

		// action
		val result = this.unit.getNameForNewSoundSheet()

		// verify
		assert(result == "test")
	}

}