package org.neihdardt.viewpagerdialog.viewhelper

import android.content.Context
import android.graphics.Color
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import org.neihdardt.viewpagerdialog.R

@Suppress("unused")
/**
 * Created by eric.neidhardt@gmail.com on 08.09.2017.
 */
class ViewPagerDialogBuilder(context: Context) : AlertDialog.Builder(context) {

	private var viewPager: ViewPager? = null

	fun setDataToDisplay(viewData: Array<String>) {
		if (this.viewPager == null) {
			this.createViewPager()
		}
		this.viewPager?.adapter = DefaultPageAdapter(viewData)
	}

	fun setCustomViewPagerAdapter(adapter: PagerAdapter) {
		if (this.viewPager == null) {
			this.createViewPager()
		}
		this.viewPager?.adapter = adapter
	}

	private fun createViewPager() {
		val view = LayoutInflater.from(this.context).inflate(
				R.layout.view_viewpagerdialog,
				null,
				false
		)
		val vp = view.findViewById(R.id.viewpager_viewpagerdialog) as ViewPager

		this.viewPager = vp
		this.setView(view)
	}

	private class DefaultPageAdapter(private val viewData: Array<String>) : PagerAdapter() {

		override fun isViewFromObject(view: View?, `object`: Any?): Boolean = view == `object`

		override fun getCount(): Int = this.viewData.size

		override fun destroyItem(container: ViewGroup?, position: Int, view: Any?) {
			if (view == null) return
			if (container == null) return

			container.removeView(view as View)
		}

		override fun instantiateItem(container: ViewGroup?, position: Int): Any {
			if (container == null) return super.instantiateItem(container, position)

			val view = TextView(container.context)
			view.height = 300
			view.text = this.viewData[position]

			container.addView(view)
			return view
		}
	}
}