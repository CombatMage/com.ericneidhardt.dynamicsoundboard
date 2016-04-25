
package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events

import org.neidhardt.dynamicsoundboard.dao.SoundSheet

/**
 * File created by eric.neidhardt on 10.07.2015.
 */

data class SoundSheetAddedEvent(val soundSheet: SoundSheet)

data class SoundSheetChangedEvent(val soundSheet: SoundSheet)

data class SoundSheetsRemovedEvent(var soundSheets: List<SoundSheet>)

class SoundSheetsInitEvent()