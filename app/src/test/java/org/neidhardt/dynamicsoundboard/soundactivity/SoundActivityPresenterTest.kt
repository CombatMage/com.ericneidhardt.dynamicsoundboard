package org.neidhardt.dynamicsoundboard.soundactivity

import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SoundActivityPresenterTest {

	@Mock private lateinit var view: SoundActivityContract.View
	@Mock private lateinit var model: SoundActivityContract.Model
	private lateinit var unit: SoundActivityContract.Presenter

	@Before
	fun setUp() {
		MockitoAnnotations.initMocks(this)
		this.unit = SoundActivityPresenter(this.view, this.model)
	}

	@Test
	fun onPaused() {
		// action
		this.unit.onPaused()

		// verify
		verify(this.model).saveData()
	}

}