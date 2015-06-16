package org.neidhardt.dynamicsoundboard;

import android.app.Application;
import android.content.Context;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.neidhardt.dynamicsoundboard.soundmanagement.dagger.DaggerSoundsDataComponent;
import org.neidhardt.dynamicsoundboard.soundmanagement.dagger.SoundsDataComponent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.dagger.DaggerSoundSheetsDataComponent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.dagger.SoundSheetsDataComponent;

import java.util.Random;


@ReportsCrashes(
	formKey = "", // This is required for backward compatibility but not used
	mailTo = "eric@neidhardt-erkner.de"
)
public class DynamicSoundboardApplication extends Application {
	private static Random random;
	private static Context applicationContext;

	private static SoundSheetsDataComponent soundSheetsDataComponent;
	private static SoundsDataComponent soundsDataComponent;

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);

		random = new Random();
		applicationContext = this.getApplicationContext();

		soundSheetsDataComponent = DaggerSoundSheetsDataComponent.create();
		soundsDataComponent = DaggerSoundsDataComponent.create();
	}

	public static Context getSoundboardContext() {
		return applicationContext;
	}

	public static int getRandomNumber() {
		return random.nextInt(Integer.MAX_VALUE);
	}

	public static SoundSheetsDataComponent getSoundSheetsDataComponent() {
		return soundSheetsDataComponent;
	}

	public static SoundsDataComponent getSoundsDataComponent()
	{
		return soundsDataComponent;
	}
}
