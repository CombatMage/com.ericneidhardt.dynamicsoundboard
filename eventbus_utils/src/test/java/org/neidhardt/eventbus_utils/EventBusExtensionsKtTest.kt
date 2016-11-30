package org.neidhardt.eventbus_utils

import com.sevenval.testutils.BaseRobolectricTest
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Created by eric.neidhardt on 29.11.2016.
 */
class EventBusExtensionsKtTest : BaseRobolectricTest() {

	@Test
	fun registerIfRequired() {

		val bus = EventBus.getDefault()
		val testReceiver = TestReceiver()

		bus.register(testReceiver)
		assertTrue(bus.isRegistered(testReceiver))

		bus.registerIfRequired(testReceiver) // this will throw already registered exception if registerIfRequired is not working
	}

	private class TestReceiver {
		@Subscribe
		fun onReceive() {
			throw IllegalStateException("should not be called by this test")
		}
	}

}