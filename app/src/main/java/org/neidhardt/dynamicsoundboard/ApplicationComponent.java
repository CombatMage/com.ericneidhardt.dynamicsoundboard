package org.neidhardt.dynamicsoundboard;

import dagger.Component;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.navigationdrawer.playlist.views.Playlist;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views.SoundSheets;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.soundcontrol.PauseSoundOnCallListener;
import org.neidhardt.dynamicsoundboard.soundcontrol.SoundSheetFragment;
import org.neidhardt.dynamicsoundboard.soundmanagement.dagger.SoundsDataModule;
import org.neidhardt.dynamicsoundboard.notifications.service.NotificationService;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.dagger.SoundSheetsDataModule;
import org.neidhardt.dynamicsoundboard.views.BaseDialog;

import javax.inject.Singleton;

/**
 * File created by eric.neidhardt on 16.06.2015.
 */
@Singleton
@Component(modules = {SoundsDataModule.class, SoundSheetsDataModule.class})
public interface ApplicationComponent
{
	void inject(NotificationService service);

	void inject(BaseDialog dialog);

	void inject(SoundActivity activity);

	void inject(Playlist playlist);

	void inject(SoundSheets soundSheets);

	void inject(SoundSheetFragment fragment);

	void inject(NavigationDrawerFragment fragment);

	void inject(PauseSoundOnCallListener listener);

	SoundsDataAccess provideSoundsDataAccess();
}
