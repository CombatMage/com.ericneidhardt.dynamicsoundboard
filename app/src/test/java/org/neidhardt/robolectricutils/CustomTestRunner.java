package org.neidhardt.robolectricutils;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.manifest.AndroidManifest;
import roboguice.util.BuildConfig;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * File created by eric.neidhardt on 02.04.2015.
 */
public class CustomTestRunner extends RobolectricTestRunner
{
	private static final int MAX_SDK_LEVEL = 21;

	public CustomTestRunner(Class<?> klass) throws InitializationError {
		super(klass);
	}

	@Override
	protected AndroidManifest getAppManifest(Config config) {
		String path = "src/main/AndroidManifest.xml";

		// android studio has a different execution root for tests than pure gradle
		// so we avoid here manual effort to get them running inside android studio
		if (!new File(path).exists())
			path = "app/" + path;

		config = overwriteConfig(config, "manifest", path);
		return super.getAppManifest(config);
	}

	private Config.Implementation overwriteConfig(Config config, String key, String value)
	{
		Properties properties = new Properties();
		properties.setProperty(key, value);
		return new Config.Implementation(config, Config.Implementation.fromProperties(properties));
	}

	@Override
	public Config getConfig(Method method) {
		Config config = super.getConfig(method);
		/*
		Fixing up the Config:
		* SDK can not be higher than 21
		* constants must point to a real BuildConfig class
		 */
		config = new Config.Implementation(ensureSdkLevel(
				config.emulateSdk()),
				config.manifest(),
				config.qualifiers(),
				config.resourceDir(),
				config.assetDir(),
				ensureSdkLevel(config.reportSdk()),
				config.shadows(),
				config.application(),
				config.libraries(),
				ensureBuildConfig(config.constants()));

		return config;
	}

	private Class<?> ensureBuildConfig(Class<?> constants) {
		if (constants == Void.class) return BuildConfig.class;
		return constants;
	}

	private int ensureSdkLevel(int sdkLevel) {
		if (sdkLevel > MAX_SDK_LEVEL) return MAX_SDK_LEVEL;
		if (sdkLevel <= 0) return MAX_SDK_LEVEL;
		return sdkLevel;
	}

}

