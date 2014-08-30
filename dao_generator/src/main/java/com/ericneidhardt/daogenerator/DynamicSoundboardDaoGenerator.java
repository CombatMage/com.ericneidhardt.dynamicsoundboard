package com.ericneidhardt.daogenerator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class DynamicSoundboardDaoGenerator
{

    public static void main(String args[]) throws Exception
	{
		Schema schema = new Schema(4, "com.ericneidhardt.dynamicsoundboard.dao");

		addMediaPlayerEntity(schema);
		addSoundSheetEntity(schema);

		new DaoGenerator().generateAll(schema, args[0]);
    }

	private static void addMediaPlayerEntity(Schema schema)
	{
		Entity sound = schema.addEntity("MediaPlayerData");
		sound.addIdProperty();
		sound.addStringProperty("hash").unique();
	}

	private static void addSoundSheetEntity(Schema schema)
	{
		Entity soundSheet = schema.addEntity("SoundSheet");
		soundSheet.addIdProperty();
		soundSheet.addStringProperty("fragmentTag");
		soundSheet.addStringProperty("label");
	}
}
