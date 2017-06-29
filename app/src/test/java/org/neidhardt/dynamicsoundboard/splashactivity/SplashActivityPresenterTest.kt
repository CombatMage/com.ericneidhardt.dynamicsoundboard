package org.neidhardt.dynamicsoundboard.splashactivity

import org.junit.Test

import org.junit.Assert.*
import org.junit.Before
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SplashActivityPresenterTest {

	@Mock private lateinit var view: SplashActivityContract.View
	@Mock private lateinit var model: SplashActivityContract.Model
	private lateinit var unit: SplashActivityPresenter

	@Before
	fun setUp() {
		MockitoAnnotations.initMocks(this)
		this.unit = SplashActivityPresenter(this.view, this.model)
	}

	@Test
	fun onCreated() {
		// arange
		`when`(this.model.getActivityToStart()).thenReturn(SplashActivityPresenterTest::class.java)

		// action
		this.unit.onCreated()

		// verify
		verify(this.view).openActivity(SplashActivityPresenterTest::class.java)
	}

}