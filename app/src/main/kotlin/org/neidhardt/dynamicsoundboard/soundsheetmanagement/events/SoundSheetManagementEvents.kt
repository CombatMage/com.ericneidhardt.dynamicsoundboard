
package org.neidhardt.dynamicsoundboard.soundsheetmanagement.events

import org.neidhardt.dynamicsoundboard.dao.SoundSheet
import org.neidhardt.dynamicsoundboard.mediaplayer.EnhancedMediaPlayer

/**
 * File created by eric.neidhardt on 10.07.2015.
 */
data
public class SoundSheetAddedEvent(public val soundSheet: SoundSheet)

data
public class SoundSheetChangedEvent(public val soundSheet: SoundSheet)

data
public class SoundSheetsRemovedEvent(public var soundSheets: List<SoundSheet>)

data
public class SoundSheetsInitEvent()