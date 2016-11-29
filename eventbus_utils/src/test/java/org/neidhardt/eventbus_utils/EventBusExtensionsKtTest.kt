package org.neidhardt.eventbus_utils

import com.sevenval.testutils.BaseRobolectricTest
import org.greenrobot.eventbus.EventBus
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by eric.neidhardt on 29.11.2016.
 */
class EventBusExtensionsKtTest : BaseRobolectricTest() {

	@Test
	fun registerIfRequired() {

		val unitUnderTest = EventBus.getDefault()
		val testReceiver = object {}

		unitUnderTest.register(testReceiver)
		assertTrue(unitUnderTest.isRegistered(testReceiver))

		unitUnderTest.registerIfRequired(testReceiver) // this will throw already registered exception if registerIfRequired is not working
	}

}