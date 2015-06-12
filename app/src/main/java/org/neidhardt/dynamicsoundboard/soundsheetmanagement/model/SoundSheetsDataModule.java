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
	@Provides @Singleton SoundSheetsManager provideSoundSheetsManager()
	{
		return new SoundSheetsManager();
	}

	@Provides @Singleton SoundSheetsDataAccess provideSoundSheetsDataAccess(SoundSheetsManager manager)
	{
		return manager;
	}

	@Provides @Singleton SoundSheetsDataStorage provideSoundSheetsDataStorage(SoundSheetsManager manager)
	{
		return manager;
	}

	@Provides @Singleton SoundSheetsDataUtil provideSoundSheetsDataUtil(SoundSheetsManager manager)
	{
		return manager;
	}
}
