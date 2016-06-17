package org.neidhardt.dynamicsoundboard.navigationdrawer.header

import android.databinding.BaseObservable
import android.view.View

/**
* Created by Eric.Neidhardt@GMail.com on 17.06.2016.
*/
class NavigationDrawerHeaderViewModel : BaseObservable() {

	var title: String? = null

	var indicatorRotation: Int = 0

	fun onChangeLayoutClicked(view: View) {

	}
}