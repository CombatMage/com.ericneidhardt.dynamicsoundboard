package com.ericneidhardt.daogenerator;

import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Schema;

public class DynamicSoundboardDaoGenerator {

    public static void main(String args[]) throws Exception {
		Schema schema = new Schema(1, "com.ericneidhardt.dynamicsoundboard");

		// TODO add entities

		new DaoGenerator().generateAll(schema, args[0]);
    }
}
