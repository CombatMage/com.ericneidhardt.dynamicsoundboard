package com.sevenval.simplestorage

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates

/**
 * Created by eric.neidhardt on 28.11.2016.
 */
@RunWith(RobolectricTestRunner::class)
class SimpleStorageTest {

	private var unitUnderTest: SimpleStorage<Int> by Delegates.notNull<SimpleStorage<Int>>()

	@Before
	fun setUp() {
		this.unitUnderTest = SimpleStorage(RuntimeEnvironment.application.applicationContext, Int::class.java)
		this.unitUnderTest.clear()
	}

	@Test
	fun precondition() {
		assertNotNull(this.unitUnderTest.storageKey)
		assertNull(this.readFirstItem())
	}

	@Test
	fun saveAndGet() {
		this.unitUnderTest.save(42).blockingSubscribe()
		assertEquals(42, this.readFirstItem())
	}

	@Test
	fun clear() {
		this.unitUnderTest.save(42).blockingSubscribe()
		assertEquals(42, this.readFirstItem())

		this.unitUnderTest.clear()
		assertNull(this.readFirstItem())
	}

	@Test
	fun saveNonPrimitive() {
		val testStorage = SimpleStorage(RuntimeEnvironment.application, TestUser::class.java)
		val testData = TestUser("user", listOf("item_1", "item_2"))

		testStorage.save(testData).blockingSubscribe()
		val result = testStorage.get().blockingIterable().first().item
		
		assertEquals(testData, result)
	}

	private fun readFirstItem(): Int? {
		val entry = this.unitUnderTest.get().blockingIterable().first()
		if (entry.isEmpty)
			return null
		return entry.item
	}

	private data class TestUser(val name: String, val inventory: List<String>)
}

