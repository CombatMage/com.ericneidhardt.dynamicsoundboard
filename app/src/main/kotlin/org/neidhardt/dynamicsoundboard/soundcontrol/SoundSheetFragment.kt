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
import com.jakewharton.rxbinding2.view.RxView
import com.trello.navi2.Event
import com.trello.navi2.rx.RxNavi
import com.trello.rxlifecycle2.kotlin.bindToLifecycle
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.android.synthetic.main.fragment_soundsheet.*
import kotlinx.android.synthetic.main.layout_fab.*
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.neidhardt.android_utils.RxEnhancedSupportFragment
import org.neidhardt.android_utils.recyclerview_utils.decoration.DividerItemDecoration
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseFragment
import org.neidhardt.dynamicsoundboard.dialog.GenericConfirmDialogs
import org.neidhardt.dynamicsoundboard.dialog.GenericRenameDialogs
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.AddNewSoundFromDirectoryDialog
import org.neidhardt.dynamicsoundboard.manager.RxNewPlaylistManager
import org.neidhardt.dynamicsoundboard.manager.RxSoundManager
import org.neidhardt.dynamicsoundboard.manager.findByFragmentTag
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerFactory
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerFailedEvent
import org.neidhardt.dynamicsoundboard.mediaplayer.events.MediaPlayerFailedEventListener
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.misc.registerIfRequired
import org.neidhardt.dynamicsoundboard.persistance.model.NewSoundSheet
import org.neidhardt.dynamicsoundboard.preferences.SoundboardPreferences
import org.neidhardt.dynamicsoundboard.soundcontrol.views.ItemTouchCallback
import org.neidhardt.dynamicsoundboard.soundcontrol.views.PendingDeletionHandler
import org.neidhardt.dynamicsoundboard.soundcontrol.views.SoundAdapter
import org.neidhardt.dynamicsoundboard.soundcontrol.views.SoundPresenter
import org.neidhardt.dynamicsoundboard.views.sound_control.ToggleLoopButton
import org.neidhardt.dynamicsoundboard.views.sound_control.TogglePlaylistButton
import org.neidhardt.ui_utils.helper.SnackbarPresenter
import org.neidhardt.ui_utils.helper.SnackbarView
import org.neidhardt.utils.getCopyList
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

	private val eventBus = EventBus.getDefault()
	private val soundSheetManager = SoundboardApplication.soundSheetManager
	private val soundManager = SoundboardApplication.soundManager
	private val soundLayoutManager = SoundboardApplication.soundLayoutManager
	private val playlistManager = SoundboardApplication.playlistManager
	private val coordinatorLayout: CoordinatorLayout by lazy { this.cl_fragment_sound_sheet }
	private val soundAdapter = SoundAdapter(
			soundSheet = this.soundSheet,
			soundManager = this.soundManager,
			playlistManager = this.playlistManager
	)
	private val snackbarPresenter = SnackbarPresenter()
	private var soundPresenter: SoundPresenter by Delegates.notNull<SoundPresenter>()
	private var itemTouchHelper: ItemTouchHelper by Delegates.notNull<ItemTouchHelper>()

	init {
		RxNavi.observe(this, Event.CREATE).subscribe {
			this.retainInstance = true
			this.setHasOptionsMenu(true)

			this.fragmentTag = this.arguments.getString(KEY_FRAGMENT_TAG)
					?: throw NullPointerException(fragmentTag + ": cannot create fragment, given fragmentTag is null")
		}

		RxNavi.observe(this, Event.VIEW_CREATED).subscribe {
			this.snackbarPresenter.init(this.coordinatorLayout)

			val soundSheet = this.soundSheetManager.soundSheets.findByFragmentTag(this.fragmentTag)
					?: throw IllegalStateException("no SoundSheet for fragmentTag was found")

			val soundList = this.rv_fragment_sound_sheet_sounds
			val presenter = SoundPresenter(
					playlistManager = this.playlistManager,
					fragment = this,
					adapter = this.soundAdapter
			)

			this.soundPresenter = presenter

			val deletionHandler = PendingDeletionHandler(
					soundSheet = this.soundSheet,
					adapter = this.soundAdapter,
					manager = this.soundManager,
					onItemDeletionRequested = { handler, time ->
						this.showSnackbarForRestore(handler, time)
					}
			)
			val itemTouchHelper = ItemTouchHelper(
					ItemTouchCallback(
							context = soundList.context,
							deletionHandler = deletionHandler,
							adapter = this.soundAdapter,
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

			this.fb_layout_fab?.let {
				RxView.clicks(it)
						.bindToLifecycle(this.fragmentLifeCycle)
						.subscribe {
							this.onFabClickedEvent()
						}
			}
		}

		RxNavi.observe(this, Event.RESUME).subscribe {
			this.baseActivity.onSoundSheetFragmentResumed()
			this.fb_layout_fab?.show(true)

			this.soundPresenter.onAttachedToWindow()
			this.eventBus.registerIfRequired(this)

			this.connectStorageEvents()

			this.connectSoundActions()
		}

		RxNavi.observe(this, Event.PAUSE).subscribe {
			this.snackbarPresenter.stop()
			this.soundPresenter.onDetachedFromWindow()
			this.eventBus.unregister(this)
		}

		RxEnhancedSupportFragment.restoresState(this)
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { state ->
					this.rv_fragment_sound_sheet_sounds?.layoutManager?.let { layoutManager ->
						state.putParcelable(KEY_STATE_RECYCLER_VIEW, layoutManager.onSaveInstanceState())
					}
				}

		RxEnhancedSupportFragment.savesState(this)
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { state ->
					this.rv_fragment_sound_sheet_sounds?.layoutManager?.onRestoreInstanceState(
							state.getParcelable(KEY_STATE_RECYCLER_VIEW))
				}

		RxNavi.observe(this, Event.ACTIVITY_RESULT).subscribe { result ->
			val resultCode = result.resultCode()
			val requestCode = result.requestCode()
			val data = result.data()
			if (resultCode == Activity.RESULT_OK) {
				if (requestCode == IntentRequest.GET_AUDIO_FILE) {
					val soundUri = data!!.data
					val soundLabel = FileUtils.stripFileTypeFromName(FileUtils.getFileNameFromUri(this.activity, soundUri))
					val playerData = MediaPlayerFactory.getNewMediaPlayerData(this.fragmentTag, soundUri, soundLabel)
					this.soundManager.add(this.soundSheet, playerData)
				}
			}
		}
	}

	private fun connectStorageEvents() {
		// if sounds where removed, the view may becomes unscrollable and therefore the fab can not be reached
		RxSoundManager.changesSoundList(this.soundManager)
				.bindToLifecycle(this.fragmentLifeCycle)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe { sounds ->
					if (sounds.isEmpty()) this.fb_layout_fab?.visibility = View.VISIBLE
				}

		RxSoundManager.changesSoundList(this.soundManager)
				.bindToLifecycle(this.fragmentLifeCycle)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe {
					this.soundAdapter.notifyDataSetChanged()
				}

		RxNewPlaylistManager.playlistChanges(this.playlistManager)
				.bindToLifecycle(this.fragmentLifeCycle)
				.observeOn(AndroidSchedulers.mainThread())
				.subscribe {
					this.soundAdapter.notifyDataSetChanged()
				}
	}

	private fun connectSoundActions() {
		this.soundAdapter.startsReorder
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder -> this.itemTouchHelper.startDrag(viewHolder) }

		this.soundAdapter.startsSwipe
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder -> this.itemTouchHelper.startSwipe(viewHolder) }

		this.soundAdapter.clicksPlay
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder ->
					viewHolder.name.clearFocus()
					viewHolder.player?.let { player ->
						this.soundPresenter.userTogglesPlaybackState(player, viewHolder.playButton)
					}
				}

		this.soundAdapter.clicksStop
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder ->
					viewHolder.player?.let { player ->
						this.soundPresenter.userStopsPlayback(player)
					}
				}

		this.soundAdapter.clicksTogglePlaylist
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder ->
					val addToPlaylist = viewHolder
							.inPlaylistButton.state == TogglePlaylistButton.State.NOT_IN_PLAYLIST

					viewHolder.inPlaylistButton.state = if (addToPlaylist)
						TogglePlaylistButton.State.IN_PLAYLIST
					else
						TogglePlaylistButton.State.NOT_IN_PLAYLIST

					viewHolder.player?.let { player ->
						this.soundPresenter.userTogglesPlaylistState(player, addToPlaylist)
					}
				}

		this.soundAdapter.clicksSettings
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder ->
					viewHolder.player?.let { player ->
						this.soundPresenter.userRequestPlayerSettings(player)
					}
				}

		this.soundAdapter.clicksLoopEnabled
				.bindToLifecycle(this.fragmentLifeCycle)
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
				}

		this.soundAdapter.clicksName
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder ->
					viewHolder.player?.let { player ->
						GenericRenameDialogs.showRenameSoundDialog(
								fragmentManager = this.fragmentManager,
								playerData = player.mediaPlayerData)
					}
				}

		this.soundAdapter.seeksToPosition
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { event ->
					event.viewHolder.player?.let { player ->
						this.soundPresenter.userSeeksToPlayerPosition(player, event.data)
					}
				}
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

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		super.onCreateView(inflater, container, savedInstanceState)
		if (container == null) return null
		return inflater.inflate(R.layout.fragment_soundsheet, container, false)
	}

	fun onFabClickedEvent() {
		val currentlyPlayingSounds = this.soundLayoutManager.currentlyPlayingSounds
		if (currentlyPlayingSounds.isNotEmpty()) {
			val copyCurrentlyPlayingSounds = currentlyPlayingSounds.getCopyList()
			for (sound in copyCurrentlyPlayingSounds)
				sound.pauseSound()
		}
		else {
			if (SoundboardPreferences.useSystemBrowserForFiles()) {
				val intent = Intent(Intent.ACTION_GET_CONTENT)
				intent.type = FileUtils.MIME_AUDIO
				this.startActivityForResult(intent, IntentRequest.GET_AUDIO_FILE)
			} else {
				AddNewSoundFromDirectoryDialog.showInstance(this.fragmentManager, this.fragmentTag)
			}
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
}
