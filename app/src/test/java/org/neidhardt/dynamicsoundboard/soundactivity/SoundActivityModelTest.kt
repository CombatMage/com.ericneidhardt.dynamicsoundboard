package org.neidhardt.dynamicsoundboard.soundactivity

import android.content.Context
import android.content.Intent
import org.junit.Test
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.neidhardt.dynamicsoundboard.persistance.SaveDataIntentService
import org.robolectric.RobolectricTestRunner

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
@RunWith(RobolectricTestRunner::class)
class SoundActivityModelTest {

	@Mock private lateinit var context: Context
	private lateinit var unit: SoundActivityModel

	@Before
	fun setUp() {
		MockitoAnnotations.initMocks(this)
		this.unit = SoundActivityModel(this.context)
	}

	@Test
	fun saveData() {
		// action
		this.unit.saveData()

		// verify
		verify(this.context).startService(Intent(this.context, SaveDataIntentService::class.java))
	}

}