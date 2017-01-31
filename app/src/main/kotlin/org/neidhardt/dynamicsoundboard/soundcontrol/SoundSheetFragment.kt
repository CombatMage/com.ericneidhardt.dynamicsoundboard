package org.neidhardt.dynamicsoundboard.soundcontrol

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.helper.ItemTouchHelper
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_soundsheet.*
import kotlinx.android.synthetic.main.layout_fab.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.android_utils.recyclerview_utils.decoration.DividerItemDecoration
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseFragment
import org.neidhardt.dynamicsoundboard.dialog.GenericConfirmDialogs
import org.neidhardt.dynamicsoundboard.manager.RxNewPlaylistManager
import org.neidhardt.dynamicsoundboard.manager.RxSoundManager
import org.neidhardt.dynamicsoundboard.manager.findByFragmentTag
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerFactory
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerFailedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerFailedEventListener
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import org.neidhardt.dynamicsoundboard.soundcontrol.views.ItemTouchCallback
import org.neidhardt.dynamicsoundboard.soundcontrol.views.PendingDeletionHandler
import org.neidhardt.dynamicsoundboard.soundcontrol.views.SoundAdapter
import org.neidhardt.dynamicsoundboard.soundcontrol.views.SoundPresenter
import org.neidhardt.dynamicsoundboard.views.floatingactionbutton.AddPauseFloatingActionButtonView
import org.neidhardt.dynamicsoundboard.views.sound_control.ToggleLoopButton
import org.neidhardt.eventbus_utils.registerIfRequired
import org.neidhardt.ui_utils.helper.SnackbarPresenter
import org.neidhardt.ui_utils.helper.SnackbarView
import rx.android.schedulers.AndroidSchedulers
import rx.subscriptions.CompositeSubscription
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 02.07.2015.
 */
private val KEY_FRAGMENT_TAG = "SoundSheetFragment.KEY_FRAGMENT_TAG"
private val KEY_STATE_RECYCLER_VIEW = KEY_FRAGMENT_TAG + "_recycler_view_state"

class SoundSheetFragment :
		BaseFragment(),
		MediaPlayerFailedEventListener
{
	companion object {
		fun getNewInstance(soundSheet: NewSoundSheet): SoundSheetFragment {
			val fragment = SoundSheetFragment()
			val args = Bundle()
			args.putString(KEY_FRAGMENT_TAG, soundSheet.fragmentTag)
			fragment.arguments = args
			return fragment
		}
	}

	override var fragmentTag: String = javaClass.name
	private val soundSheet: NewSoundSheet get() =
			this.soundSheetManager.soundSheets.findByFragmentTag(this.fragmentTag)
					?: throw IllegalStateException("no match for fragmentTag found")

	private var subscriptions = CompositeSubscription()
	private val eventBus = EventBus.getDefault()
	private val soundSheetManager = SoundboardApplication.soundSheetManager
	private val soundManager = SoundboardApplication.soundManager
	private val playlistManager = SoundboardApplication.playlistManager

	private var soundPresenter: SoundPresenter by Delegates.notNull<SoundPresenter>()
	private var itemTouchHelper: ItemTouchHelper by Delegates.notNull<ItemTouchHelper>()

	private val snackbarPresenter = SnackbarPresenter()

	private val floatingActionButton: AddPauseFloatingActionButtonView? by lazy { this.fb_layout_fab }
	private val coordinatorLayout: CoordinatorLayout by lazy { this.cl_fragment_sound_sheet }

	var soundAdapter: SoundAdapter by Delegates.notNull<SoundAdapter>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.retainInstance = true
		this.setHasOptionsMenu(true)

		val args = this.arguments
		val fragmentTag: String? = args.getString(KEY_FRAGMENT_TAG)
				?: throw NullPointerException(fragmentTag + ": cannot create fragment, given fragmentTag is null")

		this.fragmentTag = fragmentTag as String
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		if (container == null) return null
		return inflater.inflate(R.layout.fragment_soundsheet, container, false)
	}

	override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)

		this.snackbarPresenter.init(this.coordinatorLayout)

		val soundSheet = this.soundSheetManager.soundSheets.findByFragmentTag(this.fragmentTag)
				?: throw IllegalStateException("no SoundSheet for fragmentTag was found")

		val soundList = this.rv_fragment_sound_sheet_sounds

		val presenter = SoundPresenter(
				playlistManager = this.playlistManager,
				fragment = this
		)
		this.soundPresenter = presenter

		val adapter = SoundAdapter(
				soundSheet = this.soundSheet,
				soundManager = this.soundManager,
				playlistManager = this.playlistManager
		)
		this.soundAdapter = adapter

		val deletionHandler = PendingDeletionHandler(
				soundSheet = this.soundSheet,
				adapter = adapter,
				manager = this.soundManager,
				onItemDeletionRequested = { handler, time ->
					this.showSnackbarForRestore(handler, time)
				}
		)

		val itemTouchHelper = ItemTouchHelper(
				ItemTouchCallback(
						context = soundList.context,
						deletionHandler = deletionHandler,
						adapter = adapter,
						soundSheet = soundSheet,
						soundManager = this.soundManager
				)
		)
		itemTouchHelper.attachToRecyclerView(soundList)
		this.itemTouchHelper = itemTouchHelper

		soundList.apply {
			this.adapter = soundAdapter
			this.layoutManager = LinearLayoutManager(this.context.applicationContext)
			this.addItemDecoration(DividerItemDecoration(this.context.applicationContext, R.color.background, R.color.divider))
			this.itemAnimator = DefaultItemAnimator()
		}
	}

	private fun showSnackbarForRestore(deletionHandler: PendingDeletionHandler, timeTillDeletion: Int) {
		this.coordinatorLayout.context.resources.let { res ->
			val snackbarAction = SnackbarView.SnackbarAction(
					R.string.sound_control_deletion_pending_undo,
					{ deletionHandler.restoreDeletedItems() })

			val count = deletionHandler.countPendingDeletions
			val message = if (count == 1)
				res.getString(R.string.sound_control_deletion_pending_single)
			else
				res.getString(R.string.sound_control_deletion_pending).replace("{%s0}", count.toString())

			this.snackbarPresenter.showSnackbar(message, timeTillDeletion, snackbarAction)
		}
	}

	override fun onResume() {
		super.onResume()

		this.baseActivity.toolbarVM.isSoundSheetActionsEnable = true
		this.floatingActionButton?.show(true)

		this.soundPresenter.onAttachedToWindow()
		this.eventBus.registerIfRequired(this)

		this.subscriptions = CompositeSubscription()
		this.subscriptions.addAll(

				// if sounds where removed, the view becomes unscrollable and therefore the fab can not be reached
				RxSoundManager.changesSoundList(this.soundManager)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe { sounds ->
							if (sounds.isEmpty()) this.floatingActionButton?.visibility = View.VISIBLE
						},

				RxSoundManager.changesSoundList(this.soundManager)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe { this.soundAdapter.notifyDataSetChanged() },

				RxNewPlaylistManager.playlistChanges(this.playlistManager)
						.observeOn(AndroidSchedulers.mainThread())
						.subscribe { this.soundAdapter.notifyDataSetChanged() },

				this.soundAdapter.startsReorder
						.subscribe { viewHolder -> this.itemTouchHelper.startDrag(viewHolder) },

				this.soundAdapter.startsSwipe
						.subscribe { viewHolder -> this.itemTouchHelper.startSwipe(viewHolder) },

				this.soundAdapter.clicksPlay
						.subscribe { viewHolder ->
							viewHolder.name.clearFocus()
							viewHolder.player?.let { player ->
								this.soundPresenter.userTogglesPlaybackState(player)
							}
						},

				this.soundAdapter.clicksStop
						.subscribe { viewHolder ->
							viewHolder.player?.let { player ->
								this.soundPresenter.userStopsPlayback(player)
							}
						},

				this.soundAdapter.clicksTogglePlaylist
						.subscribe { viewHolder ->
							val addToPlaylist = !viewHolder.inPlaylistButton.isSelected
							viewHolder.player?.let { player ->
								this.soundPresenter.userTogglesPlaylistState(player, addToPlaylist)
							}
						},

				this.soundAdapter.clicksSettings
						.subscribe { viewHolder ->
							viewHolder.player?.let { player ->
								this.soundPresenter.userRequestPlayerSettings(player)
							}
						},

				this.soundAdapter.clicksLoopEnabled
						.subscribe { viewHolder ->
							val enable = viewHolder
									.isLoopEnabledButton.state == ToggleLoopButton.State.LOOP_DISABLE

							viewHolder.isLoopEnabledButton.state = if (enable)
										ToggleLoopButton.State.LOOP_ENABLE
									else
										ToggleLoopButton.State.LOOP_DISABLE

							viewHolder.player?.let { player ->
								this.soundPresenter.userEnablesLooping(player, enable)
							}
						},

				this.soundAdapter.changesName
						.subscribe { event ->
							event.viewHolder.name.clearFocus()
							event.viewHolder.player?.let { player ->
								this.soundPresenter.userChangesPlayerName(player, event.data)
							}
						},

				this.soundAdapter.seeksToPosition
						.subscribe { event ->
							event.viewHolder.player?.let { player ->
								this.soundPresenter.userSeeksToPlayerPosition(player, event.data)
							}
						}
		)
	}

	override fun onPause() {
		super.onPause()
		this.snackbarPresenter.stop()
		this.soundPresenter.onDetachedFromWindow()
		this.subscriptions.unsubscribe()
		this.eventBus.unregister(this)
	}

	override fun onRestoreState(savedInstanceState: Bundle) {
		super.onRestoreState(savedInstanceState)
		this.rv_fragment_sound_sheet_sounds?.layoutManager?.onRestoreInstanceState(
				savedInstanceState.getParcelable(KEY_STATE_RECYCLER_VIEW))
	}

	override fun onSaveState(outState: Bundle) {
		super.onSaveState(outState)
		this.rv_fragment_sound_sheet_sounds?.layoutManager?.let { layoutManager ->
			outState.putParcelable(KEY_STATE_RECYCLER_VIEW, layoutManager.onSaveInstanceState())
		}
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == IntentRequest.GET_AUDIO_FILE) {
				val soundUri = data!!.data
				val soundLabel = FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(this.activity, soundUri))
				val playerData = MediaPlayerFactory.getNewMediaPlayerData(this.fragmentTag, soundUri, soundLabel)
				this.soundManager.add(this.soundSheet, playerData)
				return
			}
		}
		super.onActivityResult(requestCode, resultCode, data)
	}

	override fun onOptionsItemSelected(item: MenuItem): Boolean {
		super.onOptionsItemSelected(item)
		when (item.itemId) {
			R.id.action_clear_sounds_in_sheet -> {
				GenericConfirmDialogs.showConfirmDeleteSoundsDialog(this.fragmentManager, this.soundSheet)
				return true
			}
			R.id.action_delete_sheet -> {
				GenericConfirmDialogs.showConfirmDeleteSoundSheetDialog(this.fragmentManager, this.soundSheet)
				return true
			}
			else -> return false
		}
	}

	@Subscribe(threadMode = ThreadMode.MAIN)
	override fun onEvent(event: MediaPlayerFailedEvent) {
		this.coordinatorLayout.context.resources.let { res ->
			val message = "${res.getString(R.string.sound_control_error_during_playback)}: " +
					event.player.mediaPlayerData.label
			this.snackbarPresenter.showSnackbar(message, Snackbar.LENGTH_INDEFINITE, null)
		}
	}
}
