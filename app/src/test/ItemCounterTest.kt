package com.schuhtempel.shop.toolbar

import android.view.View
import android.widget.TextView
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.robolectric.RuntimeEnvironment
import robolectricutils.BaseRobolectricTest

/**
 * File created by eric.neidhardt on 28.01.2016.
 */
class ItemCounterTest : BaseRobolectricTest() {

	@Mock private var testBadge: View? = null
	@Mock private var testCounter: TextView? = null

	private var testClass: ItemCounter? = null

	@Before
	override fun setUp() {
		MockitoAnnotations.initMocks(this)

		val app = RuntimeEnvironment.application

		val context = app.applicationContext
		this.testClass = object : ItemCounter(context) {
			override var badge: View? = testBadge
			override var counter: TextView? = testCounter
		}
	}

	@Test
	fun testSetItemsInBasketCounter() {
		this.testClass?.Count = 4

		Mockito.verify(this.testBadge, Mockito.times(1))?.visibility = View.VISIBLE
		Mockito.verify(this.testCounter, Mockito.times(1))?.text = "4"
	}
}