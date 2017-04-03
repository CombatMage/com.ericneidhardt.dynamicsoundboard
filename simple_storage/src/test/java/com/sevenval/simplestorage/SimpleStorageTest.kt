package com.sevenval.simplestorage

import com.sevenval.testutils.BaseRobolectricTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import kotlin.properties.Delegates

/**
 * Created by eric.neidhardt on 28.11.2016.
 */
class SimpleStorageTest : BaseRobolectricTest() {

	private var unitUnderTest: SimpleStorage<Int> by Delegates.notNull<SimpleStorage<Int>>()

	@Before
	fun setUp() {
		this.unitUnderTest = SimpleStorage(RuntimeEnvironment.application.applicationContext, Int::class.java)
		this.unitUnderTest.clear()
	}

	@Test
	fun precondition() {
		assertNotNull(this.unitUnderTest.storageKey)
		assertTrue(this.unitUnderTest.get().blockingIterable().none())
	}

	@Test
	fun saveAndGet() {
		this.unitUnderTest.save(42).blockingSubscribe()
		assertEquals(42, firstItem)
	}

	@Test
	fun clear() {
		this.unitUnderTest.save(42).blockingSubscribe()
		assertEquals(42, firstItem)

		this.unitUnderTest.clear()
		assertTrue(this.unitUnderTest.get().blockingIterable().none())
	}

	@Test
	fun saveNonPrimitive() {
		val testStorage = SimpleStorage(RuntimeEnvironment.application, TestUser::class.java)
		val testData = TestUser("user", listOf("item_1", "item_2"))

		testStorage.save(testData).blockingSubscribe()
		val retrievedData = testStorage.get().blockingIterable().first()
		assertEquals(testData, retrievedData)
	}

	private val firstItem: Int? get() = this.unitUnderTest.get().blockingIterable().first()

	private data class TestUser(val name: String, val inventory: List<String>)
}

