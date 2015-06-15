package org.neidhardt.dynamicsoundboard;

import android.app.Application;
import android.content.Context;
import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.dagger.SoundSheetsDataComponent;
import org.neidhardt.dynamicsoundboard.soundsheetmanagement.dagger.DaggerSoundSheetsDataComponent;

import java.util.Random;


@ReportsCrashes(
	formKey = "", // This is required for backward compatibility but not used
	mailTo = "eric@neidhardt-erkner.de"
)
public class DynamicSoundboardApplication extends Application {
	private static Random random;
	private static Context applicationContext;

	private static SoundSheetsDataComponent soundSheetsDataComponent;

	@Override
	public void onCreate() {
		super.onCreate();
		ACRA.init(this);

		random = new Random();
		applicationContext = this.getApplicationContext();

		soundSheetsDataComponent = DaggerSoundSheetsDataComponent.create();
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

}
