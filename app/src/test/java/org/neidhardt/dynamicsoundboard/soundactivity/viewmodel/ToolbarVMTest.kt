package org.neidhardt.dynamicsoundboard.soundactivity.viewmodel

import com.sevenval.testutils.BaseRobolectricTest
import org.junit.Before
import org.junit.Test
import kotlin.properties.Delegates

/**
 * @author eric.neidhardt on 22.06.2016.
 */
class ToolbarVMTest : BaseRobolectricTest() {

	private var unitUnderTest: ToolbarVM by Delegates.notNull<ToolbarVM>()

	@Before
	fun setUp() {
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
		this.unitUnderTest.addSoundSheetClickedCallback = callback
		assert(this.unitUnderTest.addSoundSheetClickedCallback == callback)

		this.unitUnderTest.onAddSoundSheetClicked()
		assert(success)
	}

	@Test
	fun setOnAddSoundClicked() {
		var success = false
		val callback = { success = true }
		this.unitUnderTest.addSoundClickedCallback = callback
		assert(this.unitUnderTest.addSoundClickedCallback == callback)

		this.unitUnderTest.onAddSoundClicked()
		assert(success)
	}

	@Test
	fun setOnAddSoundFromDirectoryClicked() {
		var success = false
		val callback = { success = true }
		this.unitUnderTest.addSoundFromDirectoryClickedCallback = callback
		assert(this.unitUnderTest.addSoundFromDirectoryClickedCallback == callback)

		this.unitUnderTest.onAddSoundFromDirectoryClicked()
		assert(success)
	}


}