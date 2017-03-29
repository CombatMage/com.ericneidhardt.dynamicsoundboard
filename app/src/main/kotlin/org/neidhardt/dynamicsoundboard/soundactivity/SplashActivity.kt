package org.neidhardt.dynamicsoundboard.soundactivity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity


/**
* Created by eric.neidhardt@sevenval.com on 10.11.2016.
*/
class SplashActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		this.startActivity(android.content.Intent(this, SoundActivity::class.java))
		this.finish()
	}
}