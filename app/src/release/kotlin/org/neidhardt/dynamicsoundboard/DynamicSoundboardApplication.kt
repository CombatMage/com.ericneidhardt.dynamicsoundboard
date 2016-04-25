package org.neidhardt.dynamicsoundboard;

import org.neidhardt.dynamicsoundboard.misc.Logger

class DynamicSoundboardApplication : SoundboardApplication()

private val TAG = DynamicSoundboardApplication::class.java.name

fun SoundboardApplication.Companion.reportError(error: Exception)
{
    Logger.e(TAG, error.message)
    // nothing to be done in release mode
}
