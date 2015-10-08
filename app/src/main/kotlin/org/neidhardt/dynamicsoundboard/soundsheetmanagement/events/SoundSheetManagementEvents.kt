
package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events

import org.neidhardt.dynamicsoundboard.dao.SoundSheet

/**
 * File created by eric.neidhardt on 10.07.2015.
 */

public data class SoundSheetAddedEvent(public val soundSheet: SoundSheet)

public data class SoundSheetChangedEvent(public val soundSheet: SoundSheet)

public data class SoundSheetsRemovedEvent(public var soundSheets: List<SoundSheet>)

public class SoundSheetsInitEvent()