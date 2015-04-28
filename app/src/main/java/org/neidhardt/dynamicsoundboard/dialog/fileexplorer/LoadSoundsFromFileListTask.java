package org.neidhardt.dynamicsoundboard.dialog.fileexplorer;

import android.net.Uri;
import org.neidhardt.dynamicsoundboard.DynamicSoundboardApplication;
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData;
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer;
import org.neidhardt.dynamicsoundboard.misc.FileUtils;
import org.neidhardt.dynamicsoundboard.soundmanagement.ServiceManagerFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.tasks.LoadTask;

import java.io.File;
import java.util.List;

/**
 * Created by eric.neidhardt on 28.04.2015.
 */
public class LoadSoundsFromFileListTask extends LoadTask<File>
{
	private List<File> filesToLoad;
	private String fragmentTag;
	private ServiceManagerFragment serviceManagerFragment;

	public LoadSoundsFromFileListTask(List<File> filesToLoad, String fragmentTag, ServiceManagerFragment serviceManagerFragment)
	{
		this.filesToLoad = filesToLoad;
		this.fragmentTag = fragmentTag;
		this.serviceManagerFragment = serviceManagerFragment;
	}

	@Override
	public List<File> call() throws Exception
	{

		for (File file : this.filesToLoad)
		{
			Uri soundUri = Uri.parse(file.getAbsolutePath());
			String soundLabel = FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(DynamicSoundboardApplication.getSoundboardContext(), soundUri));
			MediaPlayerData playerData = EnhancedMediaPlayer.getMediaPlayerData(this.fragmentTag, soundUri, soundLabel);

			this.serviceManagerFragment.getSoundService().addNewSoundToSoundsAndDatabase(playerData);
		}

		return filesToLoad;
	}

	@Override
	protected void onSuccess(List<File> files) throws Exception
	{
		super.onSuccess(files);
		this.serviceManagerFragment.notifyFragment(this.fragmentTag);
	}
}
