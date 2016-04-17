package com.aral.aral.user_space.login

import com.aral.aral.navigation.NavigationRequestedEvent
import org.greenrobot.eventbus.EventBus
import org.junit.Test
import org.mockito.Mockito
import robolectricutils.BaseRobolectricTest

/**
 * File created by eric.neidhardt on 12.04.2016.
 */
class LoginActivityKtTest : BaseRobolectricTest() {

	@Test
	fun requestLoginActivity() {
		val eventBus = Mockito.spy(EventBus.getDefault())
		eventBus.requestLoginActivity()

		Mockito.verify(eventBus, Mockito.times(1)).post(NavigationRequestedEvent(LoginActivity.TAG))
	}

	@Test
	fun getRequestedLoginActivity() {
		val event = NavigationRequestedEvent(LoginActivity.TAG)
		assert(event.requestedLoginActivity)
	}

}