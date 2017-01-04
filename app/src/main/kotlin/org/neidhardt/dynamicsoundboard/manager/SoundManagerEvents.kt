package org.neidhardt.dynamicsoundboard.manager

import org.neidhardt.dynamicsoundboard.persistance.model.NewMediaPlayerData

/**
 * File created by eric.neidhardt on 05.07.2015.
 */
data class CreatingPlayerFailedEvent(val failingPlayerData: NewMediaPlayerData)
