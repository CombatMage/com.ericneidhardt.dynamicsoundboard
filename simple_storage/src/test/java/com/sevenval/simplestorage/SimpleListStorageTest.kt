package com.sevenval.simplestorage

import com.sevenval.testutils.BaseRobolectricTest

import org.junit.Before
import org.junit.Test

import org.junit.Assert.*
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates

/**
 * Created by eric.neidhardt on 28.11.2016.
 */
class SimpleListStorageTest : BaseRobolectricTest() {

	private var unitUnderTest: SimpleListStorage<Int> by Delegates.notNull<SimpleListStorage<Int>>()

	@Before
	fun setUp() {
		this.unitUnderTest = SimpleListStorage(RuntimeEnvironment.application, Int::class.java)
		this.unitUnderTest.clear()
	}

	@Test
	fun precondition() {
		assertNotNull(this.unitUnderTest.storageKey)
		assertTrue(this.unitUnderTest.get().toBlocking().toIterable().first().isEmpty())
	}

	@Test
	fun saveAndGet() {
		val data = listOf(1,2,3,4)
		this.unitUnderTest.save(data).toBlocking().subscribe()
		assertTrue(data.containsAll(this.unitUnderTest.get().toBlocking().toIterable().first()))
	}

	@Test
	fun clear() {
		val data = listOf(1,2,3,4)
		this.unitUnderTest.save(data).toBlocking().subscribe()
		assertTrue(data.containsAll(this.unitUnderTest.get().toBlocking().toIterable().first()))

		this.unitUnderTest.clear()
		assertTrue(this.unitUnderTest.get().toBlocking().toIterable().first().isEmpty())
	}

	@Test
	fun saveNonPrimitive() {
		val testStorage = SimpleListStorage(RuntimeEnvironment.application, TestUser::class.java)

		val data = listOf(TestUser("user_1", 30), TestUser("user_2", 32))
		testStorage.save(data).toBlocking().subscribe()

		val retrievedData = testStorage.get().toBlocking().toIterable().first()
		data.forEachIndexed { i, testUser ->
			assertEquals(testUser, retrievedData[i])
		}
		assertEquals(data.size, retrievedData.size)
	}

	private data class TestUser(val name: String, val age: Int)
}
