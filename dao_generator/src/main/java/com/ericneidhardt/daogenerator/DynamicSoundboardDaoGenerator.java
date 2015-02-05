package com.ericneidhardt.daogenerator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class DynamicSoundboardDaoGenerator
{

    public static void main(String args[]) throws Exception
	{
		Schema schema = new Schema(9, "org.neidhardt.dynamicsoundboard.dao");

		addMediaPlayerEntity(schema);
		addSoundSheetEntity(schema);

		new DaoGenerator().generateAll(schema, args[0]);
    }

	private static void addMediaPlayerEntity(Schema schema)
	{
		Entity sound = schema.addEntity("MediaPlayerData");
		sound.addIdProperty();
		sound.addStringProperty("playerId").unique().notNull();
		sound.addStringProperty("fragmentTag").notNull();
		sound.addStringProperty("label").notNull();
		sound.addStringProperty("uri").notNull();
		sound.addBooleanProperty("isLoop").notNull();
		sound.addBooleanProperty("isInPlaylist").notNull();
		sound.addLongProperty("timePosition");
		sound.addIntProperty("sortOrder");
	}

	private static void addSoundSheetEntity(Schema schema)
	{
		Entity soundSheet = schema.addEntity("SoundSheet");
		soundSheet.addIdProperty();
		soundSheet.addStringProperty("fragmentTag").unique().notNull();
		soundSheet.addStringProperty("label").notNull();
		soundSheet.addBooleanProperty("isSelected").notNull();
	}
}
