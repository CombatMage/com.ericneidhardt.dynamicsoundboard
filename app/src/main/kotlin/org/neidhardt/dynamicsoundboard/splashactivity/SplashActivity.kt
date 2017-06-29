package org.neidhardt.dynamicsoundboard.splashactivity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity


/**
* Created by eric.neidhardt@sevenval.com on 10.11.2016.
*/
class SplashActivity : AppCompatActivity(), SplashActivityContract.View {

	private lateinit var presenter: SplashActivityContract.Presenter

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		this.presenter = SplashActivityPresenter(
				this,
				SplashActivityModel()
		)
		this.presenter.onCreated()
	}

	override fun openActivity(cls: Class<*>) {
		this.startActivity(Intent(this, cls))
		this.finish()
	}
}