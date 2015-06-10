package org.neidhardt.dynamicsoundboard.navigationdrawer.soundsheets.views;

import dagger.Component;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataModule;

import javax.inject.Singleton;

/**
 * File created by eric.neidhardt on 10.06.2015.
 */
@Singleton
@Component(modules = {SoundSheetsDataModule.class})
public interface PresenterComponent
{
	SoundSheetsPresenter providePresenter();
}
