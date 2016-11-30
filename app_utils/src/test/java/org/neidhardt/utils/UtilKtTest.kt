package org.neidhardt.utils

import com.sevenval.testutils.BaseRobolectricTest
import org.junit.Test

import org.junit.Assert.*

/**
 * Created by eric.neidhardt on 30.11.2016.
 */
class UtilKtTest : BaseRobolectricTest() {

	@Test
	fun getLongHash() {
		val testData1 = listOf("string1", "string2", "string3", "string4", "string5")
		val hashSet1 = testData1.map { it.longHash }

		val testData2 = listOf("string1", "string2", "string3", "string4", "string5")
		val hashSet2 = testData2.map { it.longHash }

		hashSet1.forEachIndexed { i, item ->
			assertEquals(item, hashSet2[i])
		}
	}

}