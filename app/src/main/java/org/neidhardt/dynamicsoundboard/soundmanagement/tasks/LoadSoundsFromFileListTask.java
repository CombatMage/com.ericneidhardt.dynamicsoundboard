package org.neidhardt.dynamicsoundboard.soundmanagement.tasks;

import android.net.Uri;
import de.greenrobot.event.EventBus;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerDataDao;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.misc.Logger;
import org.neidhardt.dynamicsoundboard.misc.longtermtask.LongTermTask;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.CreatingPlayerFailedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundLoadedEvent;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil;

import java.io.File;
import java.util.List;

/**
 * File created by eric.neidhardt on 28.04.2015.
 */
public class LoadSoundsFromFileListTask extends LongTermTask<List<File>>
{
	private static final String TAG = LoadSoundsFromFileListTask.class.getName();

	private EventBus eventBus;

	private SoundsDataAccess soundsDataAccess;
	private SoundsDataStorage soundsDataStorage;
	private SoundsDataUtil soundsDataUtil;

	private List<File> filesToLoad;
	private String fragmentTag;

	public LoadSoundsFromFileListTask(List<File> filesToLoad, String fragmentTag, SoundsDataAccess soundsDataAccess,
									  SoundsDataStorage soundsDataStorage, SoundsDataUtil soundsDataUtil)
	{
		this.eventBus = EventBus.getDefault();

		this.soundsDataAccess = soundsDataAccess;
		this.soundsDataStorage = soundsDataStorage;
		this.soundsDataUtil = soundsDataUtil;

		this.filesToLoad = filesToLoad;
		this.fragmentTag = fragmentTag;
	}

	@Override
	public List<File> call() throws Exception
	{
		for (File file : this.filesToLoad)
		{
			MediaPlayerData data = getMediaPlayerDataFromFile(file, this.fragmentTag);
			this.createSoundAndAddToManager(data);
		}

		return filesToLoad;
	}

	private static MediaPlayerData getMediaPlayerDataFromFile(File file, String fragmentTag)
	{
		Uri soundUri = Uri.parse(file.getAbsolutePath());
		String soundLabel = FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(DynamicSoundboardApplication.getSoundboardContext(), soundUri));
		return EnhancedMediaPlayer.getMediaPlayerData(fragmentTag, soundUri, soundLabel);
	}

	private void createSoundAndAddToManager(MediaPlayerData data)
	{
		if (this.soundsDataAccess.getSoundById(data.getFragmentTag(), data.getPlayerId()) != null)
		{
			Logger.d(TAG, "player: " + data + " is already loaded");
			return;
		}

		EnhancedMediaPlayer player = this.soundsDataUtil.createSound(data);
		if (player == null)
		{
			this.soundsDataStorage.removeSoundDataFromDatabase(data);
			this.eventBus.post(new CreatingPlayerFailedEvent(data));
		}
		else
			this.soundsDataStorage.addSoundToSounds(player);
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
