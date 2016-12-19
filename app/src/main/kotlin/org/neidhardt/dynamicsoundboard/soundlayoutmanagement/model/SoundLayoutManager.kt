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

	override var onSoundLayoutSelectedListener: ((SoundLayout) -> Unit)? = null
	override var onSoundLayoutsChangedListener: ((List<SoundLayout>) -> Unit)? = null
	override var onSoundLayoutIsChangedListener: ((SoundLayout) -> Unit)? = null
	override var onSoundLayoutsLoadedListener: ((List<SoundLayout>) -> Unit)? = null

	override var isInitDone: Boolean = false
	override val soundLayouts: MutableList<SoundLayout> = ArrayList()

	init {
		val count = this.daoSession.soundLayoutDao.count()
		if (count == 0.toLong())
			this.daoSession.soundLayoutDao.insert(this.getDefaultSoundLayout())
		this.soundLayouts.addAll(this.daoSession.soundLayoutDao.queryBuilder().list())
		this.isInitDone = true

		this.onSoundLayoutsLoadedListener?.invoke(this.soundLayouts)
		this.onSoundLayoutsChangedListener?.invoke(this.soundLayouts)
		this.onSoundLayoutSelectedListener?.invoke(this.soundLayouts.first { it.isSelected })
	}

	@CheckResult
	override fun addSoundLayout(soundLayout: SoundLayout): Observable<SoundLayout> {
		return Observable.fromCallable {
			soundLayout.letThis { item ->
				this.soundLayouts.add(item)
				this.daoSession.soundLayoutDao.insert(item)
				this.onSoundLayoutsChangedListener?.invoke(this.soundLayouts)
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
					this.onSoundLayoutIsChangedListener?.invoke(item)
				}
			}.doOnError { error -> Logger.e(this.toString(), error.toString()) }
			.subscribeOn(Schedulers.computation())
	}

	@CheckResult
	override fun removeSoundLayouts(soundLayouts: List<SoundLayout>): Observable<List<SoundLayout>> {
		return Observable.fromCallable {
			soundLayouts.letThis { items ->
				this.soundLayouts.removeAll(soundLayouts)
				this.daoSession.runInTx {
					items.forEach { this.daoSession.soundLayoutDao.delete(it) }
				}
			}
		}.doOnError { error -> Logger.e(this.toString(), error.toString()) }
		.doOnCompleted {
			if (this.soundLayouts.size == 0) {
				val defaultLayout = this.getDefaultSoundLayout()
				this.soundLayouts.add(defaultLayout)
				this.daoSession.soundLayoutDao.insert(defaultLayout)
				this.eventBus.post(SoundLayoutAddedEvent(defaultLayout))
				this.onSoundLayoutSelectedListener?.invoke(this.soundLayouts[0])
			}
			else if (this.soundLayouts.selectedLayout == null) {
				this.soundLayouts[0].isSelected = true
				this.soundLayouts[0].updateItemInDatabase(this.daoSession.soundLayoutDao)
				this.onSoundLayoutSelectedListener?.invoke(this.soundLayouts[0])
			}
			this.onSoundLayoutsChangedListener?.invoke(this.soundLayouts)
		}
		.subscribeOn(Schedulers.computation())
	}

	override fun setSoundLayoutSelected(soundLayout: SoundLayout) {
		this.soundLayouts.forEach { layoutInList ->
			layoutInList.isSelected = layoutInList == soundLayout
			layoutInList.updateItemInDatabaseAsync()
		}
		this.onSoundLayoutSelectedListener?.invoke(soundLayout)
	}

	override fun getSuggestedName(): String {
		val count = this.soundLayouts.size
		return this.context.resources.getString(R.string.suggested_sound_layout_name) + count
	}

	private fun getDefaultSoundLayout(): SoundLayout =
			SoundLayout().apply {
				this.databaseId = DB_DEFAULT
				this.label = context.getString(R.string.suggested_sound_layout_name)
				this.isSelected = true
			}

	private fun SoundLayout.updateItemInDatabase(soundLayoutDao: SoundLayoutDao) {
		val isInDatabase = soundLayoutDao.isSoundLayoutInDatabase(this)
		if (isInDatabase)
			soundLayoutDao.update(this)
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

	companion object {
		const val DB_DEFAULT: String = "org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManagerFragment.db_default"
		const val DB_SOUND_LAYOUTS = "org.neidhardt.dynamicsoundboard.soundlayouts.SoundLayoutsManagerFragment.db_sound_layouts"

		fun getNewDatabaseIdForLabel(label: String): String {
			return Integer.toString((label + SoundboardApplication.randomNumber).hashCode())
		}
	}
}

private fun SoundLayoutDao.isSoundLayoutInDatabase(soundLayout: SoundLayout): Boolean {
	return this.queryBuilder()
			.where(SoundLayoutDao.Properties.DatabaseId.eq(soundLayout.databaseId))
			.list()
			.isNotEmpty()
}


