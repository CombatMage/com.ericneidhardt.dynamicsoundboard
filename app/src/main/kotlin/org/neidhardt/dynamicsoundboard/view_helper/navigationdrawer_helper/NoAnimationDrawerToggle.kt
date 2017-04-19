package org.neidhardt.dynamicsoundboard.view_helper.navigationdrawer_helper

import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.navigationdrawer.NavigationDrawerFragment

class NoAnimationDrawerToggle(
		activity: AppCompatActivity,
		drawerLayout: DrawerLayout?,
		toolbar: Toolbar,
		private val navigationDrawer: NavigationDrawerFragment)
: ActionBarDrawerToggle(
		activity,
		drawerLayout,
		toolbar,
		R.string.navigation_drawer_content_description_open,
		R.string.navigation_drawer_content_description_close
) {
	init { this.isDrawerIndicatorEnabled = true }

	// override onDrawerSlide and pass 0 to super disable arrow animation
	override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
		super.onDrawerSlide(drawerView, 0f)
	}

	override fun onDrawerClosed(drawerView: View?) {
		super.onDrawerClosed(drawerView)
		this.navigationDrawer.onNavigationDrawerClosed()
	}
}