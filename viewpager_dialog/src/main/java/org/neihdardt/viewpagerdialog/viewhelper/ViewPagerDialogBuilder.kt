package org.neihdardt.viewpagerdialog.viewhelper

import android.content.Context
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
	private var counter: TextView? = null

	fun setDataToDisplay(viewData: Array<String>) {
		if (this.viewPager == null) {
			this.createViewPager()
		}
		this.initViewPager(DefaultPageAdapter(viewData))
	}

	fun setCustomViewPagerAdapter(adapter: PagerAdapter) {
		if (this.viewPager == null) {
			this.createViewPager()
		}
		this.initViewPager(adapter)
	}

	private fun createViewPager() {
		val view = LayoutInflater.from(this.context).inflate(
				R.layout.view_viewpagerdialog,
				null,
				false
		)
		this.viewPager = view.findViewById(R.id.viewpager_viewpagerdialog_messages) as ViewPager
		this.counter = view.findViewById(R.id.textview_viewpagerdialog_messagecount) as TextView
		this.setView(view)
	}

	private fun initViewPager(adapter: PagerAdapter) {
		val count = adapter.count
		if (count > 0) {
			this.counter?.text = "1 / $count"
			this.viewPager?.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
				override fun onPageScrollStateChanged(state: Int) {}
				override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {}
				override fun onPageSelected(position: Int) {
					counter?.text = "${position + 1} / $count"
				}
			})

			this.counter?.visibility = View.VISIBLE
		} else {
			this.counter?.visibility = View.GONE
		}
		this.viewPager?.adapter = adapter
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
			view.text = this.viewData[position]

			container.addView(view)
			return view
		}
	}
}