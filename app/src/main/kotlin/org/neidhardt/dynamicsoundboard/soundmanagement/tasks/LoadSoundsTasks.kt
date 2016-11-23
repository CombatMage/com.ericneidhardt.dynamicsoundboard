package org.neidhardt.dynamicsoundboard.soundmanagement.tasks

import android.net.Uri
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.longtermtask.LoadListTask
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage
import rx.Observable
import rx.schedulers.Schedulers
import java.io.File
import java.util.concurrent.Callable

/**
 * File created by eric.neidhardt on 10.04.2015.
 */
class LoadSoundsFromFileListTask
(
		private val filesToLoad: List<File>,
		private val fragmentTag: String,
		private val soundsDataStorage: SoundsDataStorage
) :
		LoadListTask<File>()
{
    override val TAG: String = javaClass.name

	@Throws(Exception::class)
	override fun call(): List<File>
	{
		this.filesToLoad
				.map { getMediaPlayerDataFromFile(it, this.fragmentTag) }
				.forEach { this.postUpdateToMainThread({ this.soundsDataStorage.createSoundAndAddToManager(it) }) }

		return filesToLoad
	}

	private fun getMediaPlayerDataFromFile(file: File, fragmentTag: String): MediaPlayerData
	{
		val soundUri = Uri.parse(file.absolutePath)
		val soundLabel = FileUtils.stripFileTypeFromName(
				FileUtils.getFileNameFromUri(SoundboardApplication.context, soundUri))
		return MediaPlayerData.getNewMediaPlayerData(fragmentTag, soundUri, soundLabel)
	}

}