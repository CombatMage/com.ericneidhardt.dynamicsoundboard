package org.neidhardt.dynamicsoundboard.splashactivity

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SplashActivityPresenterTest {

	@Mock private lateinit var view: SplashActivityContract.View
	private lateinit var unit: SplashActivityPresenter

	@Before
	fun setUp() {
		MockitoAnnotations.initMocks(this)
		this.unit = SplashActivityPresenter(this.view)
	}

	@Test
	fun onCreated() {
		// action
		this.unit.onCreated()

		// verify
		verify(this.view).openActivity(SoundActivity::class.java)
	}



}