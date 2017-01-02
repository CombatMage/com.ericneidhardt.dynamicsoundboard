package org.neidhardt.dynamicsoundboard.soundlayoutmanagement.events

import org.neidhardt.dynamicsoundboard.dao.SoundLayout

/**
 * File created by eric.neidhardt on 08.01.2016.
 */
data class SoundLayoutsRemovedEvent(var soundLayouts: List<SoundLayout>)

data class SoundLayoutSelectedEvent(private val selectedSoundLayout: SoundLayout)

data class SoundLayoutRenamedEvent(val renamedSoundLayout: SoundLayout)
