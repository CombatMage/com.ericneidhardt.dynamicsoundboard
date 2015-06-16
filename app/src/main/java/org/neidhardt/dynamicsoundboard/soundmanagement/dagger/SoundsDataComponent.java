package org.neidhardt.dynamicsoundboard.soundmanagement.dagger;

import dagger.Component;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views.SoundSheets;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.service.MediaPlayerService;
import org.neidhardt.dynamicsoundboard.views.BaseDialog;

import javax.inject.Singleton;

/**
 * File created by eric.neidhardt on 12.06.2015.
 */
@Singleton
@Component(modules = {SoundsDataModule.class})
public interface SoundsDataComponent
{
	void inject(MediaPlayerService service);

	void inject(BaseDialog dialog);

	void inject(SoundActivity activity);

	void inject(Playlist playlist);

	void inject(SoundSheets soundSheets);

	void inject(SoundSheetFragment fragment);
}
