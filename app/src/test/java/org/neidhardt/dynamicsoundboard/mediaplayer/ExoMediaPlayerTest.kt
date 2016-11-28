package org.neidhardt.dynamicsoundboard.mediaplayer

import android.content.Context
import com.sevenval.testutils.BaseRobolectricTest
import org.greenrobot.eventbus.EventBus
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import org.robolectric.RuntimeEnvironment

/**
* Created by Eric.Neidhardt@GMail.com on 17.04.2016.
*/
class ExoMediaPlayerTest : BaseRobolectricTest()
{
	private var context: Context? = null
	@Mock private var eventBus: EventBus? = null
	@Mock private var soundsDataStorage: SoundsDataStorage? = null
	@Mock private var mediaPlayerData: MediaPlayerData? = null

	private var unitUnderTest: MediaPlayerController? = null

	@Before
	fun setUp()
	{
		MockitoAnnotations.initMocks(this)

		this.context = RuntimeEnvironment.application.applicationContext
		this.mediaPlayerData = MediaPlayerData(0).apply {
			this.fragmentTag = "testFragmentTag"
			this.uri = "testUri"
		}

		this.unitUnderTest = ExoMediaPlayer(context as Context, eventBus as EventBus, soundsDataStorage as SoundsDataStorage,
				 mediaPlayerData as MediaPlayerData)
	}

	@Test
	fun isLoopingEnabled()
	{
		this.mediaPlayerData?.isLoop = false // precondition

		this.unitUnderTest?.isLoopingEnabled = true
		assert(this.mediaPlayerData?.isLoop ?: false) // verify that underlying data was changed
		assert(this.unitUnderTest?.isLoopingEnabled ?: false)

		// constructor another player from given data should not change result
		val anotherPlayer = ExoMediaPlayer(context as Context, eventBus as EventBus, soundsDataStorage as SoundsDataStorage,
				mediaPlayerData as MediaPlayerData)

		assert(anotherPlayer.isLoopingEnabled)
	}

}