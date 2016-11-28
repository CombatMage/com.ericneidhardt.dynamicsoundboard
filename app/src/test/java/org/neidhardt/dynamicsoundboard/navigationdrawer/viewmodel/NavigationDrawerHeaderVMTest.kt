package org.neidhardt.dynamicsoundboard.navigationdrawer.viewmodel

import com.sevenval.testutils.BaseRobolectricTest
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.neidhardt.dynamicsoundboard.navigationdrawer.events.OpenSoundLayoutsRequestedEvent
import kotlin.properties.Delegates

/**
 * @author eric.neidhardt on 17.06.2016.
 */
class NavigationDrawerHeaderVMTest : BaseRobolectricTest() {

	@Mock private var eventBus: EventBus? = null
	private var unitUnderTest by Delegates.notNull<NavigationDrawerHeaderVM>()

	@Before
	fun setUp() {
		MockitoAnnotations.initMocks(this)
		this.unitUnderTest = NavigationDrawerHeaderVM(this.eventBus!!, null)
	}

	@Test
	fun precondition() {
		assert(this.unitUnderTest.title == null)
	}

	@Test
	fun setAndGetTitle() {
		this.unitUnderTest.title = "test"
		assert(this.unitUnderTest.title == "test")
	}

	@Test
	fun setAndGetOpenSoundLayouts() {
		this.unitUnderTest.isSoundLayoutOpen = true
		assert(this.unitUnderTest.isSoundLayoutOpen == true)

		this.unitUnderTest.isSoundLayoutOpen = false
		assert(this.unitUnderTest.isSoundLayoutOpen == false)
	}

	@Test
	fun onChangeLayoutClicked() {
		val willOpenSoundLayout = !this.unitUnderTest.isSoundLayoutOpen
		this.unitUnderTest.onChangeLayoutClicked()
		Mockito.verify(this.eventBus, Mockito.times(1))?.post(OpenSoundLayoutsRequestedEvent(willOpenSoundLayout))
	}

}