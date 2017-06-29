package org.neidhardt.dynamicsoundboard.splashactivity

import android.support.v7.app.AppCompatActivity
import org.neidhardt.dynamicsoundboard.soundactivity.SoundActivity

/**
 * Created by eric.neidhardt@gmail.com on 29.06.2017.
 */
class SplashActivityModel : SplashActivityContract.Model {

	override fun getActivityToStart(): Class<*> {
		return SoundActivity::class.java
	}

}