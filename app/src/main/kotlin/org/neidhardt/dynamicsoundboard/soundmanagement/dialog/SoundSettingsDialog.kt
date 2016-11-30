package org.neidhardt.dynamicsoundboard.soundmanagement.dialog

import android.annotation.SuppressLint
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.FragmentManager
import android.support.v7.app.AlertDialog
import android.view.View
import android.widget.CheckBox
import kotlinx.android.synthetic.main.dialog_sound_settings_layout.view.*
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.MediaPlayerData
import org.neidhardt.dynamicsoundboard.mediaplayer.MediaPlayerController
import org.neidhardt.dynamicsoundboard.soundmanagement.events.SoundChangedEvent
import org.neidhardt.android_utils.views.CustomEditText
import org.neidhardt.android_utils.views.SimpleSpinner
import org.neidhardt.utils.letThis
import java.util.*
import kotlin.properties.Delegates

/**
 * File created by eric.neidhardt on 23.02.2015.
 */
class SoundSettingsDialog : SoundSettingsBaseDialog() {

	private val soundSheetsDataUtil = SoundboardApplication.soundSheetsDataUtil
	private val soundsDataStorage = SoundboardApplication.soundsDataStorage
	private val soundSheetsDataAccess = SoundboardApplication.soundSheetsDataAccess
	private val soundSheetsDataStorage = SoundboardApplication.soundSheetsDataStorage

	override var fragmentTag: String by Delegates.notNull<String>()
	override var player: MediaPlayerController by Delegates.notNull<MediaPlayerController>()

	private var soundName: CustomEditText? = null
	private var soundSheetName: CustomEditText? = null
	private var soundSheetSpinner: SimpleSpinner? = null
	private var addNewSoundSheet: CheckBox? = null

	private var indexOfCurrentFragment = -1

	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
		@SuppressLint("InflateParams") val view = this.activity.layoutInflater.inflate(R.layout.dialog_sound_settings_layout, null)

		this.soundName = view.et_name_file.letThis {
			it.text = this.player.mediaPlayerData.label
		}
		this.soundSheetName = view.et_name_new_sound_sheet.letThis {
			it.text = this.soundSheetsDataUtil.getSuggestedName()
			it.visibility = View.GONE
		}
		this.soundSheetSpinner = view.s_sound_sheets?.letThis {
			this.setAvailableSoundSheets(it)
		}
		this.addNewSoundSheet = view.cb_add_new_sound_sheet.letThis {
			it.setOnCheckedChangeListener { view,isChecked ->
				this.soundSheetSpinner?.visibility = if (isChecked) View.GONE else View.VISIBLE
				this.soundSheetName?.visibility = if (isChecked) View.VISIBLE else View.GONE
			}
		}

		return AlertDialog.Builder(context).apply {
			this.setTitle(R.string.dialog_sound_settings_title)
			this.setView(view)
			this.setPositiveButton(R.string.dialog_save, { dialogInterface, i ->
				val hasLabelChanged = player.mediaPlayerData.label != soundName!!.displayedText
				deliverResult()
				dismiss()
				if (hasLabelChanged)
					RenameSoundFileDialog.show(fragmentManager, player.mediaPlayerData)
			})
			this.setNegativeButton(R.string.dialog_cancel, { dialogInterface, i -> dismiss()})
		}.create()
	}

	private fun setAvailableSoundSheets(spinner: SimpleSpinner) {
		val soundSheets = this.soundSheetsDataAccess.getSoundSheets()
		val labels = ArrayList<String>()
		for (i in soundSheets.indices) {
			if (soundSheets[i].fragmentTag == this.fragmentTag)
				this.indexOfCurrentFragment = i
			labels.add(soundSheets[i].label)
		}
		if (this.indexOfCurrentFragment == -1)
			throw IllegalStateException(TAG + " Current fragment of sound " + this.player.mediaPlayerData + " is not found in list of sound sheets " + soundSheets)

		spinner.items = labels
		spinner.setSelection(this.indexOfCurrentFragment)
	}

	private fun deliverResult() {
		val soundLabel = this.soundName!!.displayedText
		val indexOfSelectedSoundSheet = this.soundSheetSpinner!!.selectedItemPosition

		val addNewSoundSheet = this.addNewSoundSheet!!.isChecked
		val hasSoundSheetChanged = addNewSoundSheet || indexOfSelectedSoundSheet != this.indexOfCurrentFragment

		if (!hasSoundSheetChanged) {
			this.player.mediaPlayerData.label = soundLabel
			this.player.mediaPlayerData.updateItemInDatabaseAsync()
			EventBus.getDefault().post(SoundChangedEvent(this.player))
		} else {
			this.soundsDataStorage.removeSounds(listOf(this.player))

			val uri = Uri.parse(this.player.mediaPlayerData.uri)

			val mediaPlayerData: MediaPlayerData

			if (addNewSoundSheet) {
				val soundSheetName = this.soundSheetName!!.displayedText
				val soundSheet = this.soundSheetsDataUtil.getNewSoundSheet(soundSheetName)

				this.soundSheetsDataStorage.addSoundSheetToManager(soundSheet)

				val fragmentTag = soundSheet.fragmentTag
				mediaPlayerData = MediaPlayerData.getNewMediaPlayerData(fragmentTag, uri, soundLabel)
			} else {
				val fragmentTag = this.soundSheetsDataAccess.
						getSoundSheets()[indexOfSelectedSoundSheet].fragmentTag
				mediaPlayerData = MediaPlayerData.getNewMediaPlayerData(fragmentTag, uri, soundLabel)
			}

			this.soundsDataStorage.createSoundAndAddToManager(mediaPlayerData)
		}
	}

	companion object {
		private val TAG = SoundSettingsDialog::class.java.name

		fun showInstance(manager: FragmentManager, playerData: MediaPlayerData) {
			val dialog = SoundSettingsDialog()
			addArguments(dialog, playerData.playerId, playerData.fragmentTag)
			dialog.show(manager, TAG)
		}
	}

}
