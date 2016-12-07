package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.model

import android.content.Context
import android.support.annotation.CheckResult
import org.greenrobot.eventbus.EventBus
import org.neidhardt.dynamicsoundboard.R
import org.neidhardt.dynamicsoundboard.SoundboardApplication
import org.neidhardt.dynamicsoundboard.dao.DaoSession
import org.neidhardt.dynamicsoundboard.dao.SoundLayout
import org.neidhardt.dynamicsoundboard.dao.SoundLayoutDao
import org.neidhardt.dynamicsoundboard.daohelper.GreenDaoHelper
import org.neidhardt.dynamicsoundboard.misc.Logger
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutAddedEvent
import org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events.SoundLayoutsRemovedEvent
import org.neidhardt.utils.letThis
import rx.Observable
import rx.schedulers.Schedulers
import java.util.*

/**
 * Created by eric.neidhardt on 06.12.2016.
 */
class SoundLayoutManager(private val context: Context) : ISoundLayoutManager {

	private val daoSession: DaoSession = GreenDaoHelper.setupDatabase(this.context, DB_SOUND_LAYOUTS)
	private val eventBus: EventBus = EventBus.getDefault()

	override val soundLayouts: MutableList<SoundLayout> = ArrayList()

	init {
		val count = this.daoSession.soundLayoutDao.count()
		if (count == 0.toLong())
			this.daoSession.soundLayoutDao.insert(this.getDefaultSoundLayout())
		this.soundLayouts.addAll(this.daoSession.soundLayoutDao.queryBuilder().list())
	}

	override fun addSoundLayout(soundLayout: SoundLayout): Observable<SoundLayout> {
		return Observable.fromCallable{
			soundLayout.letThis { item ->
				this.soundLayouts.add(item)
				this.daoSession.soundLayoutDao.insert(item)
			}
		}.doOnError { error -> Logger.e(this.toString(), error.toString()) }
		.subscribeOn(Schedulers.computation())
	}

	@CheckResult
	override fun updateSoundLayout(update: () -> SoundLayout): Observable<SoundLayout> {
		return Observable.fromCallable {
				update.invoke().letThis { item ->
					val isInDatabase = this.daoSession.soundLayoutDao.isSoundLayoutInDatabase(item)
					if (isInDatabase)
						this.daoSession.soundLayoutDao.update(item)
				}
			}.doOnError { error -> Logger.e(this.toString(), error.toString()) }
			.subscribeOn(Schedulers.computation())
	}

	override fun removeSoundLayouts(soundLayouts: List<SoundLayout>) {
		this.soundLayouts.removeAll(soundLayouts)
		this.daoSession.runInTx {
			soundLayouts.forEach { this.daoSession.soundLayoutDao.delete(it) }
		}
		if (this.soundLayouts.size == 0) {
			val defaultLayout = this.getDefaultSoundLayout()
			this.soundLayouts.add(defaultLayout)
			this.daoSession.soundLayoutDao.insert(defaultLayout)
			this.eventBus.post(SoundLayoutAddedEvent(defaultLayout))
		}
		else if (this.soundLayouts.selectedLayout == null) {
			this.soundLayouts[0].isSelected = true
			this.soundLayouts[0].updateItemInDatabaseAsync()
		}

		this.eventBus.post(SoundLayoutsRemovedEvent(soundLayouts))
	}

	override fun setSoundLayoutSelected(soundLayout: SoundLayout) {
		this.soundLayouts.forEach { layoutInList ->
			layoutInList.isSelected = layoutInList == soundLayout
			layoutInList.updateItemInDatabaseAsync()
		}
	}

	override fun getSuggestedName(): String
		= this.context.resources.getString(R.string.suggested_sound_layout_name) + this.soundLayouts.size

	private fun getDefaultSoundLayout(): SoundLayout =
			SoundLayout().apply {
				this.databaseId = DB_DEFAULT
				this.label = context.getString(R.string.suggested_sound_layout_name)
				this.isSelected = true
			}

	companion object {
		const val DB_DEFAULT: String = "org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManagerFragment.db_default"
		const val DB_SOUND_LAYOUTS = "org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManagerFragment.db_sound_layouts"

		fun getNewDatabaseIdForLabel(label: String): String {
			return Integer.toString((label + SoundboardApplication.randomNumber).hashCode())
		}
	}

	private fun SoundLayout.updateItemInDatabaseAsync() {
		val db = daoSession
		Observable.fromCallable {
			val isInDatabase = db.soundLayoutDao.isSoundLayoutInDatabase(this)
			if (isInDatabase)
				db.update(this)
		}.doOnError { error ->
			Logger.e(this.toString(), error.toString())
		}
		.subscribeOn(Schedulers.computation())
		.subscribe()
	}
}

private fun SoundLayoutDao.isSoundLayoutInDatabase(soundLayout: SoundLayout): Boolean {
	return this.queryBuilder()
			.where(SoundLayoutDao.Properties.DatabaseId.eq(soundLayout.databaseId))
			.list()
			.isNotEmpty()
}