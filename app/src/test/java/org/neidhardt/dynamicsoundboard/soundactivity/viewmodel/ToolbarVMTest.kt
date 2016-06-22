package org.neidhardt.dynamicsoundboard.soundactivity.viewmodel

import org.junit.Before
import org.junit.Test
import robolectricutils.BaseRobolectricTest
import kotlin.properties.Delegates

/**
 * @author eric.neidhardt on 22.06.2016.
 */
class ToolbarVMTest : BaseRobolectricTest() {

	private var unitUnderTest: ToolbarVM by Delegates.notNull<ToolbarVM>()

	@Before
	override fun setUp() {
		this.unitUnderTest = ToolbarVM()
	}

	@Test
	fun precondition() {
		assert(this.unitUnderTest.isSoundSheetActionsEnable == false)
	}

	@Test
	fun setSoundSheetActionsEnable() {
		this.unitUnderTest.isSoundSheetActionsEnable = true
		assert(this.unitUnderTest.isSoundSheetActionsEnable)
	}

	@Test
	fun setOnAddSoundSheetClicked() {
		var success = false
		val callback = { success = true }
		this.unitUnderTest.onAddSoundSheetClicked = callback
		assert(this.unitUnderTest.onAddSoundSheetClicked == callback)

		this.unitUnderTest.onAddSoundSheetClicked()
		assert(success)
	}

	@Test
	fun setOnAddSoundClicked() {
		var success = false
		val callback = { success = true }
		this.unitUnderTest.onAddSoundClicked = callback
		assert(this.unitUnderTest.onAddSoundClicked == callback)

		this.unitUnderTest.onAddSoundClicked()
		assert(success)
	}

	@Test
	fun setOnAddSoundFromDirectoryClicked() {
		var success = false
		val callback = { success = true }
		this.unitUnderTest.onAddSoundFromDirectoryClicked = callback
		assert(this.unitUnderTest.onAddSoundFromDirectoryClicked == callback)

		this.unitUnderTest.onAddSoundFromDirectoryClicked()
		assert(success)
	}

}