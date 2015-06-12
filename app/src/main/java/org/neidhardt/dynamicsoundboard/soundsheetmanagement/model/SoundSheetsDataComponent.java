package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model;

import dagger.Component;
import org.neidhardt.dynamicsoundboard.fileexplorer.LoadLayoutDialog;
import org.neidhardt.dynamicsoundboard.fileexplorer.StoreLayoutDialog;
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views.SoundSheetsPresenter;
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
}
