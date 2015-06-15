package org.neidhardt.dynamicsoundboard.soundmanagement.dagger;

import dagger.Component;
import dagger.Provides;
import org.neidhardt.dynamicsoundboard.soundmanagement.model.SoundsDataAccess;
import org.neidhardt.dynamicsoundboard.soundmanagement.service.MediaPlayerService;

import javax.inject.Singleton;

/**
 * File created by eric.neidhardt on 12.06.2015.
 */
@Singleton
@Component(modules = {SoundsDataModule.class})
public interface SoundsDataComponent
{
	void inject(MediaPlayerService service);
}
