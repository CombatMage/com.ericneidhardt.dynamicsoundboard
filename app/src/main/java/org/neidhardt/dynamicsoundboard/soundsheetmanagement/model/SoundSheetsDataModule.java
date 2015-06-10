package org.neidhardt.dynamicsoundboard.soundsheetmanagement.model;

import dagger.Module;
import dagger.Provides;

import javax.inject.Singleton;

/**
 * File created by eric.neidhardt on 10.06.2015.
 */
@Module
public class SoundSheetsDataModule
{
	@Provides @Singleton
	SoundSheetsDataAccess provideSoundSheetsDataAccess()
	{
		return new SoundSheetsManager();
	}

	@Provides
	@Singleton
	SoundSheetsDataStorage provideSoundSheetsDataStorage(){
		return new SoundSheetsManager();
	}
}
