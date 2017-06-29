package org.neidhardt.dynamicsoundboard.splashactivity

import org.junit.Test

import org.junit.Assert.*
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SplashActivityModelTest {

	@Test
	fun getActivityToStart() {
		// arrange
		val unit = SplashActivityModel()

		// action
		val result = unit.getActivityToStart()

		// verify
		assertTrue(result == SoundActivity::class.java)
	}

}