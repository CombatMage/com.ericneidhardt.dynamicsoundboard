package org.neidhardt.dynamicsoundboard.navigationdrawer.soundlayouts.events

import org.neidhardt.dynamicsoundboard.dao.SoundLayout

/**
 * File created by eric.neidhardt on 11.11.2015.
 */
data class SoundLayoutSelectedEvent(private val selectedSoundLayout: SoundLayout)

data class SoundLayoutRenamedEvent(val renamedSoundLayout: SoundLayout)

data class SoundLayoutAddedEvent(val data: SoundLayout)

data class OpenSoundLayoutSettingsEvent(val soundLayout: SoundLayout)

class SoundLayoutRemovedEvent