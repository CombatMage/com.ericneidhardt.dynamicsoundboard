package org.neidhardt.dynamicsoundboard.soundcontrol

import org.greenrobot.eventbus.EventBus
import org.hamcrest.CoreMatchers.equalTo
import org.junit.Assert
import org.junit.Assert.assertThat
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.neidhardt.dynamicsoundboard.BaseTest
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundAddedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundsRemovedEvent
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess
import org.neidhardt.dynamicsoundboard.testutils.TestDataGenerator
import java.util.*

/**
 * File created by eric.neidhardt on 03.07.2015.
 */
public class SoundPresenterTest : BaseTest()
{
	private val FRAGMENT_TAG = javaClass.name

	private var soundPresenter: SoundPresenter? = null

	@Mock var eventBus: EventBus? = null
	@Mock var soundDataAccess: SoundsDataAccess? = null

	override fun setUp()
	{
		super.setUp()
		MockitoAnnotations.initMocks(this)

		this.soundPresenter = SoundPresenter(FRAGMENT_TAG, this.eventBus as EventBus, this.soundDataAccess as SoundsDataAccess)
	}

	@Test
	fun onSoundAddedEvent()
	{
		var event = SoundAddedEvent(TestDataGenerator.getRandomPlayer(FRAGMENT_TAG))
		this.soundPresenter!!.onEvent(event)

		event = SoundAddedEvent(TestDataGenerator.getRandomPlayer(FRAGMENT_TAG))
		this.soundPresenter!!.onEvent(event)

		event = SoundAddedEvent(TestDataGenerator.getRandomPlayer(FRAGMENT_TAG))
		this.soundPresenter!!.onEvent(event)

		assertThat(this.soundPresenter!!.values.size, equalTo(3))
		this.verifySortOrder()
	}

	@Test
	fun onSoundRemovedEvent()
	{
		// prepare test
		var event = SoundAddedEvent(TestDataGenerator.getRandomPlayer(FRAGMENT_TAG))
		this.soundPresenter!!.onEvent(event)
		event = SoundAddedEvent(TestDataGenerator.getRandomPlayer(FRAGMENT_TAG))
		this.soundPresenter!!.onEvent(event)
		event = SoundAddedEvent(TestDataGenerator.getRandomPlayer(FRAGMENT_TAG))
		this.soundPresenter!!.onEvent(event)
		event = SoundAddedEvent(TestDataGenerator.getRandomPlayer(FRAGMENT_TAG))
		this.soundPresenter!!.onEvent(event)

		val list = ArrayList<MediaPlayerController>()
		list.add(this.soundPresenter!!.values[1])
		list.add(this.soundPresenter!!.values[2])

		val removeEvent = SoundsRemovedEvent(list)
		this.soundPresenter!!.onEvent(removeEvent)

		assertThat(this.soundPresenter!!.values.size, equalTo(2))
		this.verifySortOrder()
	}

	@Test
	fun onSoundAddedWithReverseSortOrderGivenEvent()
	{
		val event0 = SoundAddedEvent(TestDataGenerator.getRandomPlayer(FRAGMENT_TAG))
		event0.player.mediaPlayerData.sortOrder = 2
		this.soundPresenter!!.onEvent(event0)

		val event1 = SoundAddedEvent(TestDataGenerator.getRandomPlayer(FRAGMENT_TAG))
		event1.player.mediaPlayerData.sortOrder = 1
		this.soundPresenter!!.onEvent(event1)

		val event2 = SoundAddedEvent(TestDataGenerator.getRandomPlayer(FRAGMENT_TAG))
		event2.player.mediaPlayerData.sortOrder = 0
		this.soundPresenter!!.onEvent(event2)

		assertThat(this.soundPresenter!!.values.size, equalTo(3))

		Assert.assertEquals(this.soundPresenter!!.values[0], event2.player)
        Assert.assertEquals(this.soundPresenter!!.values[1], event1.player)
        Assert.assertEquals(this.soundPresenter!!.values[2], event0.player)

		this.verifySortOrder()
	}


	private fun verifySortOrder()
	{
		val values = this.soundPresenter!!.values
		val count = values.size
		for (i in 0..count - 1)
		{
			val itemSortOrder = values[i].mediaPlayerData.sortOrder
            Assert.assertEquals(itemSortOrder, i)
		}
	}

}