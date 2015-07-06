package org.neidhardt.dynamicsoundboard.soundmanagement.dagger;

import dagger.Module;
import dagger.Provides;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataStorage;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataUtil;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsManager;

import javax.inject.Singleton;

/**
 * File created by eric.neidhardt on 10.06.2015.
 */
@Module
public class SoundsDataModule
{
	@Provides @Singleton
	SoundsManager provideSoundsManager()
	{
		return new SoundsManager();
	}

	@Provides @Singleton
	SoundsDataAccess provideSoundsDataAccess(SoundsManager manager)
	{
		return manager;
	}

	@Provides @Singleton
	SoundsDataStorage providesDataStorage(SoundsManager manager)
	{
		return manager;
	}

	@Provides @Singleton
	SoundsDataUtil providesDataUtil(SoundsManager manager)
	{
		return manager;
	}
}
