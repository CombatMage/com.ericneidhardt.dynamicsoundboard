package org.neidhardt.dynamicsoundboard.soundsheetmanagement.dagger;

import dagger.Module;
import dagger.Provides;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataAccess;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataStorage;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsDataUtil;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.model.SoundSheetsManager;

import javax.inject.Singleton;

/**
 * File created by eric.neidhardt on 10.06.2015.
 */
@Module
public class SoundSheetsDataModule
{
	@Provides @Singleton
	SoundSheetsManager provideSoundSheetsManager()
	{
		return new SoundSheetsManager();
	}

	@Provides @Singleton
	SoundSheetsDataAccess provideSoundSheetsDataAccess(SoundSheetsManager manager)
	{
		return manager;
	}

	@Provides @Singleton
	SoundSheetsDataStorage provideSoundSheetsDataStorage(SoundSheetsManager manager)
	{
		return manager;
	}

	@Provides @Singleton
	SoundSheetsDataUtil provideSoundSheetsDataUtil(SoundSheetsManager manager)
	{
		return manager;
	}
}
