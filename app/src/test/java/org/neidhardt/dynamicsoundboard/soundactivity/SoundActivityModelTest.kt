package org.neidhardt.dynamicsoundboard.soundactivity

import android.content.Context
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.neidhardt.dynamicsoundboard.manager.SoundLayoutManager
import org.neidhardt.dynamicsoundboard.manager.SoundSheetManager
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.neidhardt.dynamicsoundboard.repositories.AppDataRepository
import org.robolectric.RobolectricTestRunner

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
@RunWith(RobolectricTestRunner::class)
class SoundActivityModelTest {

	@Mock private lateinit var context: Context
	@Mock private lateinit var appDataRepository: AppDataRepository
	@Mock private lateinit var soundLayoutManager: SoundLayoutManager
	@Mock private lateinit var soundSheetManager: SoundSheetManager
	private lateinit var unit: SoundActivityModel

	@Before
	fun setUp() {
		MockitoAnnotations.initMocks(this)
		this.unit = SoundActivityModel(
				this.context,
				this.appDataRepository,
				this.soundLayoutManager,
				this.soundSheetManager)
	}

	@Test
	fun startServiceToStore() {
		// action
		this.unit.saveData()
		// verify
		verify(this.appDataRepository).save(this.soundLayoutManager.soundLayouts)
	}

	// TEST Fails: because Mockito does not mock correct
	@Test
	fun getSoundSheetsReturnsDataFromStorage() {
		// arrange
		val testData = listOf(SoundSheet(), SoundSheet())
		`when`(this.soundSheetManager.soundSheets).thenReturn(testData)

		// action
		val result = this.unit.getSoundSheets()

		// verify
		assert(result.size == 2)
	}

	// TEST Fails: because Mockito does not mock correct
	@Test
	fun getNameForNewSoundSheetReturnsNameFromManager() {
		// arrange
		`when`(this.soundSheetManager.getSuggestedName(this.context)).thenReturn("test")

		// action
		val result = this.unit.getNameForNewSoundSheet()

		// verify
		assert(result == "test")
	}

}