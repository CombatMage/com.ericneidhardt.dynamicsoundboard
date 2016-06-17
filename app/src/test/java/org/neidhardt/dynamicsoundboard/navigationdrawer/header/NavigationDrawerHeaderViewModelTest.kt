package org.neidhardt.dynamicsoundboard.navigationdrawer.header

import android.view.View
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.neidhardt.dynamicsoundboard.navigationdrawer.header.events.OpenSoundLayoutsRequestedEvent
import robolectricutils.BaseRobolectricTest
import kotlin.properties.Delegates

/**
 * @author eric.neidhardt on 17.06.2016.
 */
class NavigationDrawerHeaderViewModelTest : BaseRobolectricTest() {

	@Mock private var eventBus: EventBus? = null
	private var unitUnderTest by Delegates.notNull<NavigationDrawerHeaderViewModel>()

	@Before
	override fun setUp() {
		MockitoAnnotations.initMocks(this)
		this.unitUnderTest = NavigationDrawerHeaderViewModel(this.eventBus!!, null)
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
	fun setAndGetIndicatorRotation() {
		this.unitUnderTest.indicatorRotation = 4
		assert(this.unitUnderTest.indicatorRotation == 4)
	}

	@Test
	fun setAndGetOpenSoundLayouts() {
		this.unitUnderTest.openSoundLayouts = true
		assert(this.unitUnderTest.openSoundLayouts == true)

		this.unitUnderTest.openSoundLayouts = false
		assert(this.unitUnderTest.openSoundLayouts == false)
	}

	@Test
	fun onChangeLayoutClicked() {
		this.unitUnderTest.onChangeLayoutClicked(Mockito.mock(View::class.java))
		Mockito.verify(this.eventBus, Mockito.times(1))?.post(OpenSoundLayoutsRequestedEvent(Mockito.anyBoolean()))
	}

}