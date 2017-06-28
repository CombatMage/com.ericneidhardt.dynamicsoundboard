package org.neidhardt.dynamicsoundboard.splashactivity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity


/**
* Created by eric.neidhardt@sevenval.com on 10.11.2016.
*/
class SplashActivity : AppCompatActivity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		this.startActivity(Intent(this, SoundActivity::class.java))
		this.finish()
	}
}