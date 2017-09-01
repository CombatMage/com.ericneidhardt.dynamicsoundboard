package org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.viewmodel

import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.robolectric.RobolectricTestRunner
import kotlin.properties.Delegates

/**
 * @author eric.neidhardt on 21.06.2016.
 */
@RunWith(RobolectricTestRunner::class)
class NavigationDrawerButtonBarVMTest {

	@Mock private var eventBus: EventBus? = null
	private var unitUnderTest by Delegates.notNull<NavigationDrawerButtonBarVM>()

	@Before
	fun setUp() {
		MockitoAnnotations.initMocks(this)
		this.unitUnderTest = NavigationDrawerButtonBarVM()
	}

	@Test
	fun setEnableDeleteSelected() {
		this.unitUnderTest.enableDeleteSelected = true
		assert(this.unitUnderTest.enableDeleteSelected)
	}

	@Test
	fun setOnDeleteClicked() {
		var success = false
		val callback = { success = true }
		this.unitUnderTest.deleteClickedCallback = callback
		assert(this.unitUnderTest.deleteClickedCallback == callback)

		this.unitUnderTest.onDeleteClicked()
		assert(success)
	}

	@Test
	fun setOnAddClicked() {
		var success = false
		val callback = { success = true }
		this.unitUnderTest.addClickedCallback = callback
		assert(this.unitUnderTest.addClickedCallback == callback)

		this.unitUnderTest.onAddClicked()
		assert(success)
	}

	@Test
	fun setOnDeleteSelectedClicked() {
		var success = false
		val callback = { success = true }
		this.unitUnderTest.deleteSelectedClickedCallback = callback
		assert(this.unitUnderTest.deleteSelectedClickedCallback == callback)

		this.unitUnderTest.onDeleteSelectedClicked()
		assert(success)
	}

}