package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model;

import dagger.Component;
import org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views.SoundSheetsPresenter;

import javax.inject.Singleton;

/**
 * File created by eric.neidhardt on 12.06.2015.
 */
@Singleton
@Component(modules = {SoundSheetsDataModule.class})
public interface SoundSheetsDataComponent
{
	SoundSheetsDataStorage provideSoundSheetsDataStorage();

	SoundSheetsDataAccess provideSoundSheetsDataAccess();

	SoundSheetsDataUtil provideSoundSheetsDataUtil();
}
