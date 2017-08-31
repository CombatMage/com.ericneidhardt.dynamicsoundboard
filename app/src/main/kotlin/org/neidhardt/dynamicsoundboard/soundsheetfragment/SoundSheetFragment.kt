package org.neidhardt.dynamicsoundboard.soundsheetfragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
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
import kotlinx.android.synthetic.main.fragment_soundsheet.*
import kotlinx.android.synthetic.main.layout_fab.*
import org.neidhardt.android_utils.RxEnhancedSupportFragment
import org.neidhardt.android_utils.recyclerview_utils.decoration.DividerItemDecoration
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.base.BaseFragment
import org.neidhardt.dynamicsoundboard.dialog.GenericConfirmDialogs
import org.neidhardt.dynamicsoundboard.dialog.GenericRenameDialogs
import org.neidhardt.dynamicsoundboard.dialog.fileexplorer.AddNewSoundFromDirectoryDialog
import org.neidhardt.dynamicsoundboard.dialog.soundmanagement.SoundSettingsDialog
import org.neidhardt.dynamicsoundboard.manager.findByFragmentTag
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.misc.FileUtils
import org.neidhardt.dynamicsoundboard.misc.IntentRequest
import org.neidhardt.dynamicsoundboard.model.SoundSheet
import org.neidhardt.dynamicsoundboard.soundsheetfragment.soundlist.ItemTouchCallback
import org.neidhardt.dynamicsoundboard.soundsheetfragment.soundlist.PendingDeletionHandler
import org.neidhardt.dynamicsoundboard.soundsheetfragment.soundlist.SoundAdapter
import org.neidhardt.dynamicsoundboard.views.sound_control.PlayButton
import org.neidhardt.dynamicsoundboard.views.sound_control.ToggleLoopButton
import org.neidhardt.dynamicsoundboard.views.sound_control.TogglePlaylistButton

/**
 * Created by eric.neidhardt@gmail.com on 08.05.2017.
 */
class SoundSheetFragment : BaseFragment(), SoundSheetContract.View {

	companion object {
		internal val KEY_FRAGMENT_TAG = "SoundSheetFragment.KEY_FRAGMENT_TAG"
		fun getNewInstance(soundSheet: SoundSheet): SoundSheetFragment {
			val fragment = SoundSheetFragment()
			val args = Bundle()
			args.putString(KEY_FRAGMENT_TAG, soundSheet.fragmentTag)
			fragment.arguments = args
			return fragment
		}
	}

	private val soundSheet: SoundSheet get() =
		this.soundSheetManager.soundSheets.findByFragmentTag(this.fragmentTag)
				?: throw IllegalStateException("no match for fragmentTag found")

	private val KEY_STATE_RECYCLER_VIEW get() = "${this.fragmentTag}_recycler_view_state"

	private val preferences = SoundboardApplication.preferenceRepository
	private val soundSheetManager = SoundboardApplication.soundSheetManager
	private val soundManager = SoundboardApplication.soundManager
	private val playlistManager = SoundboardApplication.playlistManager

	private val soundAdapter by lazy {
		SoundAdapter(
				soundSheet = this.soundSheet,
				soundManager = this.soundManager,
				playlistManager = this.playlistManager
		)
	}
	private val deletionHandler by lazy {
		PendingDeletionHandler(
				soundSheet = this.soundSheet,
				adapter = this.soundAdapter,
				manager = this.soundManager,
				onItemDeletionRequested = { _, _ ->
					this.soundSheetPresenter.onUserDeletesSound()
				}
		)
	}
	private val soundSheetPresenter: SoundSheetContract.Presenter = SoundSheetPresenter(
			soundSheetView = this,
			lifecycleProvider = this.fragmentLifeCycle
	)

	private var snackbar: Snackbar? = null

	init {
		RxNavi.observe(this, Event.CREATE).subscribe {
			this.retainInstance = true
			this.setHasOptionsMenu(true)
			this.fragmentTag = this.arguments.getString(KEY_FRAGMENT_TAG)
					?: throw NullPointerException(fragmentTag
					+ ": cannot create fragment, given fragmentTag is null")
		}
		RxNavi.observe(this, Event.VIEW_CREATED).subscribe {
			this.configureUi()
		}
		RxNavi.observe(this, Event.RESUME).subscribe {
			this.soundSheetPresenter.onViewResumed()
			this.bindSoundActions()
			this.bindSoundSettingsActions()
			this.bindFloatingActionButton()
		}
		RxNavi.observe(this, Event.PAUSE).subscribe {
			this.soundSheetPresenter.onViewPaused()
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
					val soundLabel = FileUtils.stripFileTypeFromName(
							FileUtils.getFileNameFromUri(this.activity, soundUri))
					this.soundSheetPresenter.onUserAddsNewPlayer(
							soundUri, soundLabel, this.soundSheet)
				}
			}
		}
	}

	private fun configureUi() {
		this.rv_fragment_sound_sheet_sounds.apply {
			this.adapter = soundAdapter
			this.layoutManager = LinearLayoutManager(this.context.applicationContext)
			this.itemAnimator = DefaultItemAnimator()
			this.addItemDecoration(DividerItemDecoration(
					this.context.applicationContext, R.color.background, R.color.divider))
		}
		val itemTouchHelper = ItemTouchHelper(
				ItemTouchCallback(
						context = this.rv_fragment_sound_sheet_sounds.context,
						oneSwipeToDelete = this.preferences.isOneSwipeToDeleteEnabled,
						deletionHandler = deletionHandler,
						adapter = this.soundAdapter,
						soundSheet = soundSheet,
						soundManager = this.soundManager
				)
		)
		itemTouchHelper.attachToRecyclerView(this.rv_fragment_sound_sheet_sounds)

		this.soundAdapter.startsReorder
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder -> itemTouchHelper.startDrag(viewHolder) }

		this.soundAdapter.startsSwipe
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder -> itemTouchHelper.startSwipe(viewHolder) }
	}

	private fun bindFloatingActionButton() {
		val fab = this.fb_layout_fab ?: return
		RxView.clicks(fab)
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe {
					this.soundSheetPresenter.onUserClicksFab()
				}
	}

	private fun bindSoundActions() {
		this.soundAdapter.clicksPlay
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder ->
					viewHolder.name.clearFocus()
					viewHolder.player?.let { player ->
						if (player.isFadingOut) {
							viewHolder.playButton.state = PlayButton.State.PLAY
						}
						else if (player.isPlayingSound) {
							viewHolder.playButton.state = PlayButton.State.FADE
						}
						else {
							viewHolder.playButton.state = PlayButton.State.PAUSE
						}
						this.soundSheetPresenter.onUserClicksPlay(player)
					}
				}
		this.soundAdapter.clicksStop
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder ->
					viewHolder.player?.let { player ->
						this.soundSheetPresenter.onUserClicksStop(player)
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
						this.soundSheetPresenter.onUserTogglePlaylist(player)
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
						this.soundSheetPresenter.onUserTogglePlayerLooping(player)
					}
				}
		this.soundAdapter.seeksToPosition
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { (viewHolder, data) ->
					viewHolder.player?.let { player ->
						this.soundSheetPresenter.onUserSeeksToPlaybackPosition(player, data)
					}
				}
	}

	private fun bindSoundSettingsActions() {
		this.soundAdapter.clicksSettings
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder ->
					viewHolder.player?.let { player ->
						SoundSettingsDialog.showInstance(
								this.fragmentManager, player.mediaPlayerData)
					}
				}
		this.soundAdapter.clicksName
				.bindToLifecycle(this.fragmentLifeCycle)
				.subscribe { viewHolder ->
					viewHolder.player?.let { player ->
						GenericRenameDialogs.showRenameSoundDialog(
								this.fragmentManager, playerData = player.mediaPlayerData)
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

	override fun showSounds() {
		this.soundAdapter.notifyDataSetChanged()
	}

	override fun showAddButton() {
		this.fb_layout_fab?.visibility = View.VISIBLE
	}

	override fun updateSound(player: MediaPlayerController) {
		this.soundAdapter.notifyItemChanged(player)
	}

	override fun openDialogForNewSound() {
		if (this.preferences.useSystemBrowserForFiles) {
			val intent = Intent(Intent.ACTION_GET_CONTENT)
			intent.type = FileUtils.MIME_AUDIO
			this.startActivityForResult(intent, IntentRequest.GET_AUDIO_FILE)
		} else {
			AddNewSoundFromDirectoryDialog.showInstance(this.fragmentManager, this.fragmentTag)
		}
	}

	override fun showSnackbarForPlayerError(player: MediaPlayerController) {
		val resources = this.context?.resources ?: return
		val message = "${resources.getString(R.string.sound_control_error_during_playback)}: " +
				player.mediaPlayerData.label

		this.snackbar?.dismiss()
		this.snackbar = Snackbar.make(
				this.cl_fragment_sound_sheet, message, Snackbar.LENGTH_INDEFINITE)
		this.snackbar?.show()
	}

	override fun showSnackbarForRestoreSound() {
		val resources = this.context?.resources ?: return
		val timeTillDeletion = this.deletionHandler.DELETION_TIMEOUT

		val count = deletionHandler.countPendingDeletions
		val message = if (count == 1)
			resources.getString(R.string.sound_control_deletion_pending_single)
		else
			resources.getString(R.string.sound_control_deletion_pending).replace("{%s0}",
					count.toString())

		this.snackbar?.dismiss()
		this.snackbar = Snackbar.make(
				this.cl_fragment_sound_sheet, message, timeTillDeletion).apply {
			this.setAction(
					R.string.sound_control_deletion_pending_undo,
					{ deletionHandler.restoreDeletedItems() }
			)
		}
		this.snackbar?.show()
	}
}