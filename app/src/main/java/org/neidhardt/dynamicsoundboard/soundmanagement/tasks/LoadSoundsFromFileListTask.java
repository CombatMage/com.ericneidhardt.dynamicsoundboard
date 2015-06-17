package org.neidhardt.dynamicsoundboard.soundmanagement.tasks;

import android.net.Uri;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.misc.longtermtask.LongTermTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.*;

import java.io.File;
import java.util.List;

/**
 * File created by eric.neidhardt on 28.04.2015.
 */
public class LoadSoundsFromFileListTask extends LongTermTask<List<File>>
{
	private static final String TAG = LoadSoundsFromFileListTask.class.getName();

	private SoundsDataStorage soundsDataStorage;

	private List<File> filesToLoad;
	private String fragmentTag;

	public LoadSoundsFromFileListTask(List<File> filesToLoad, String fragmentTag, SoundsDataStorage soundsDataStorage)
	{
		this.soundsDataStorage = soundsDataStorage;

		this.filesToLoad = filesToLoad;
		this.fragmentTag = fragmentTag;
	}

	@Override
	public List<File> call() throws Exception
	{
		for (File file : this.filesToLoad)
		{
			MediaPlayerData data = getMediaPlayerDataFromFile(file, this.fragmentTag);
			this.soundsDataStorage.createSoundAndAddToManager(data);
		}

		return filesToLoad;
	}

	private static MediaPlayerData getMediaPlayerDataFromFile(File file, String fragmentTag)
	{
		Uri soundUri = Uri.parse(file.getAbsolutePath());
		String soundLabel = FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(DynamicSoundboardApplication.getSoundboardContext(), soundUri));
		return EnhancedMediaPlayer.getMediaPlayerData(fragmentTag, soundUri, soundLabel);
	}

	@Override
	protected void onSuccess(List<File> files) throws Exception
	{
		super.onSuccess(files);
	}

	@Override
	protected String getTag()
	{
		return TAG;
	}
}
