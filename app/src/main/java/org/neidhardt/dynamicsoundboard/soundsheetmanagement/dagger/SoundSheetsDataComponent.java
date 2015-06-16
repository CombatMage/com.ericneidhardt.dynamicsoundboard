package org.neidhardt.dynamicsoundboard.soundsheetmanagement.dagger;

import dagger.Component;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views.SoundSheets;
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity;
import org.neidhardt.dynamicsoundboard.views.BaseDialog;

import javax.inject.Singleton;

/**
 * File created by eric.neidhardt on 12.06.2015.
 */
@Singleton
@Component(modules = {SoundSheetsDataModule.class})
public interface SoundSheetsDataComponent
{
	void inject(SoundActivity activity);

	void inject(BaseDialog dialog);

	void inject(NavigationDrawerFragment fragment);

	void inject(SoundSheets soundSheets);
}
