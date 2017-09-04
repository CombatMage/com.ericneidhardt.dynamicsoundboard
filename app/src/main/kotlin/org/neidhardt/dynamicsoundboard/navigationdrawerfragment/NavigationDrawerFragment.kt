package org.neidhardt.dynamicsoundboard.navigationdrawerfragment

import android.os.Bundle
import android.support.design.widget.AppBarLayout
import android.support.design.widget.TabLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.jakewharton.rxbinding2.support.design.widget.RxTabLayout
import com.jakewharton.rxbinding2.view.RxView
import kotlinx.android.synthetic.main.fragment_navigation_drawer.view.*
import kotlinx.android.synthetic.main.layout_navigation_drawer_button_bar.view.*
import kotlinx.android.synthetic.main.layout_navigation_drawer_deletion_header.view.*
import kotlinx.android.synthetic.main.layout_navigation_drawer_header.view.*
import org.neidhardt.android_utils.animations.setOnAnimationEndedListener
import org.neidhardt.android_utils.views.NonTouchableCoordinatorLayout
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseFragment
import org.neidhardt.dynamicsoundboard.dialog.GenericAddDialogs
import org.neidhardt.dynamicsoundboard.dialog.GenericRenameDialogs
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.AddNewSoundDialog
import org.neidhardt.dynamicsoundboard.mediaplayer.PlaylistTAG
import org.neidhardt.dynamicsoundboard.model.SoundLayout
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.playlist.PlaylistAdapter
import org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.soundlayouts.SoundLayoutsAdapter
import org.neidhardt.dynamicsoundboard.navigationdrawerfragment.viewhelper.soundsheets.SoundSheetsAdapter
import org.neidhardt.dynamicsoundboard.viewhelper.recyclerview_helper.PaddingDecorator

/**
 * Created by eric.neidhardt@gmail.com on 01.09.2017.
 */
class NavigationDrawerFragment : BaseFragment(), NavigationDrawerFragmentContract.View {

	private val soundsSheetManager = SoundboardApplication.soundSheetManager

	private val adapterSoundSheets = SoundSheetsAdapter()
	private val adapterPlaylist = PlaylistAdapter()
	private val adapterSoundLayouts = SoundLayoutsAdapter()

	private lateinit var presenter: NavigationDrawerFragmentContract.Presenter

	private lateinit var coordinatorLayout: NonTouchableCoordinatorLayout
	private lateinit var appBarLayout: AppBarLayout
	private lateinit var headerLabel: TextView
	private lateinit var headerArrow: View

	private lateinit var deletionToolbar: View
	private lateinit var deletionToolbarTitle: TextView
	private lateinit var deletionToolbarSubTitle: TextView

	private lateinit var tabLayout: TabLayout
	private lateinit var recyclerView: RecyclerView

	private lateinit var buttonDeleteSelected: View

	override var displayedSoundSheets: List<SoundSheet>
		get() = this.adapterSoundSheets.values
		set(value) {
			this.adapterSoundSheets.setValues(value)
			this.adapterSoundSheets.notifyDataSetChanged()
		}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		this.presenter = NavigationDrawerFragmentPresenter(
				this,
				NavigationDrawerFragmentModel(this.soundsSheetManager))
	}

	override fun onCreateView(
			inflater: LayoutInflater,
			container: ViewGroup?,
			savedInstanceState: Bundle?
	): View? {
		super.onCreateView(inflater, container, savedInstanceState)

		val view = inflater.inflate(R.layout.fragment_navigation_drawer, container, false)

		this.coordinatorLayout = view.cl_navigation_drawer

		this.appBarLayout = view.abl_navigation_drawer
		this.headerLabel = view.tv_view_navigation_drawer_header_current_sound_layout_name
		this.headerArrow = view.iv_view_navigation_drawer_header_change_sound_layout_indicator

		this.deletionToolbar = view.layout_navigation_drawer_deletion_header
		this.deletionToolbarTitle = view.tv_layout_navigation_drawer_deletion_header_title
		this.deletionToolbarSubTitle = view.tv_layout_navigation_drawer_deletion_header_sub_title

		this.recyclerView = view.rv_navigation_drawer_list.apply {
			this.itemAnimator = DefaultItemAnimator()
			this.layoutManager = LinearLayoutManager(this.context)
			this.addItemDecoration(PaddingDecorator(this.context.applicationContext))
		}

		this.tabLayout = view.tl_navigation_drawer_list.apply {
			this.addTab(this.newTab().setText(R.string.tab_sound_sheets))
			this.addTab(this.newTab().setText(R.string.tab_play_list))
		}

		this.buttonDeleteSelected = view.framelayout_layoutnavigationdrawerbuttonbar_deleteselected

		RxTabLayout.selections(this.tabLayout)
				.takeUntil(RxView.detaches(this.tabLayout))
				.subscribe { tab ->
					if (tab.position == 0) {
						this.presenter.userClicksTabSoundSheets()
					} else if (tab.position == 1) {
						this.presenter.userClicksTabPlaylist()
					}
				}

		this.adapterSoundLayouts.clicksSettings
				.takeUntil(RxView.detaches(this.recyclerView))
				.map { viewHolder -> viewHolder.data }
				.filter { soundLayout -> soundLayout != null }
				.subscribe { soundLayout ->
					this.presenter.userClicksSoundLayoutSettings(soundLayout!!)
				}

		this.adapterSoundLayouts.clicksViewHolder
				.takeUntil(RxView.detaches(this.recyclerView))
				.map { viewHolder -> viewHolder.data }
				.filter { soundLayout -> soundLayout != null }
				.subscribe { soundLayout ->
					this.presenter.userClicksSoundLayoutItem(soundLayout!!)
				}

		this.adapterSoundSheets.clicksViewHolder
				.takeUntil(RxView.detaches(this.recyclerView))
				.map { viewHolder -> viewHolder.data }
				.filter { soundSheet -> soundSheet != null }
				.subscribe { soundSheet ->
					this.presenter.userClicksSoundSheet(soundSheet!!)
				}

		this.adapterPlaylist.clicksViewHolder
				.takeUntil(RxView.detaches(this.recyclerView))
				.map { viewHolder -> viewHolder.player }
				.filter { player -> player != null }
				.subscribe { player ->
					this.presenter.userClicksPlaylistSound(player!!)
				}

		view.b_layout_navigation_drawer_button_bar_add.setOnClickListener {
			this.presenter.userClicksAdd()
		}
		view.b_layout_navigation_drawer_button_bar_delete.setOnClickListener {
			this.presenter.userClicksDelete()
		}
		view.b_layout_navigation_drawer_button_bar_delete_selected.setOnClickListener {
			this.presenter.userClicksDeleteSelected()
		}
		view.b_layout_navigation_drawer_deletion_header_cancel.setOnClickListener {
			this.presenter.userClicksDeleteCancel()
		}
		view.ll_layout_navigation_drawer_deletion_header_title.setOnClickListener {
			this.presenter.userClicksSelectAll()
		}
		view.rl_view_navigation_drawer_header_change_sound_layout.setOnClickListener {
			this.presenter.userClicksHeaderSoundLayout()
		}

		return view
	}

	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		this.presenter.viewCreated()
	}

	override fun setHeaderTitle(text: String) {
		this.headerLabel.text = text
	}

	override fun animateHeaderArrow(direction: NavigationDrawerFragmentContract.View.AnimationDirection) {
		this.headerArrow.animate().cancel()

		if (direction == NavigationDrawerFragmentContract.View.AnimationDirection.UP) {
			this.headerArrow.rotationX = 0f
			this.headerArrow
					.animate()
					.withLayer()
					.rotationX(180f)
					.setOnAnimationEndedListener {
						this.headerArrow.rotationX = 180f
					}
		} else {
			this.headerArrow.rotationX = 180f
			this.headerArrow
					.animate()
					.withLayer()
					.rotationX(0f)
					.setOnAnimationEndedListener {
						this.headerArrow.rotationX = 0f
					}
		}
	}

	override fun stopDeletionMode() {
		// hide delete button and header
		this.buttonDeleteSelected.visibility = View.GONE
		this.appBarLayout.setExpanded(true, true)

		// enable scrolling
		this.recyclerView.isNestedScrollingEnabled = true
		this.coordinatorLayout.isScrollingEnabled = true

		// make sure we scroll to valid position after some items may have been deleted
		this.recyclerView.scrollToPosition(0)

		// hide deletion toolbar
		this.deletionToolbar.visibility = View.GONE
	}

	override fun showDeletionModeSoundSheets() {
		this.deletionToolbarTitle.setText(R.string.deletiontoolbar_titledeletesoundsheets)
		// TODO setSubTitle

		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		this.startDeletionMode()
	}

	override fun showDeletionModePlaylist() {
		this.deletionToolbarTitle.setText(R.string.deletiontoolbar_titledeleteplaylistsounds)
		// TODO setSubTitle

		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		this.startDeletionMode()
	}

	override fun showDeletionModeSoundLayouts() {
		this.deletionToolbarTitle.setText(R.string.deletiontoolbar_titledeletesoundlayouts)
		// TODO setSubTitle

		//TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
		this.startDeletionMode()
	}

	private fun startDeletionMode() {
		// show delete button and collapse header
		this.buttonDeleteSelected.visibility = View.VISIBLE
		this.appBarLayout.setExpanded(false, true)

		// disable scrolling
		this.recyclerView.isNestedScrollingEnabled = false
		this.coordinatorLayout.isScrollingEnabled = false

		// show deletion toolbar
		this.deletionToolbar.visibility = View.VISIBLE
	}

	override fun showSoundSheets() {
		this.recyclerView.adapter = this.adapterSoundSheets
	}

	override fun showPlaylist() {
		this.recyclerView.adapter = this.adapterPlaylist
	}

	override fun showSoundLayouts() {
		this.recyclerView.adapter = this.adapterSoundLayouts
	}

	override fun showDialogAddSoundSheet() {
		GenericAddDialogs.showAddSoundSheetDialog(this.fragmentManager)
	}

	override fun showDialogAddSoundLayout() {
		GenericAddDialogs.showAddSoundLayoutDialog(this.fragmentManager)
	}

	override fun showDialogRenameSoundLayout(soundLayout: SoundLayout) {
		GenericRenameDialogs.showRenameSoundLayoutDialog(this.fragmentManager, soundLayout)
	}

	override fun showDialogAddSoundToPlaylist() {
		AddNewSoundDialog.show(fragmentManager, PlaylistTAG)
	}

	override fun closeNavigationDrawer() {
		this.baseActivity.closeNavigationDrawer()
	}
}