package org.neidhardt.dynamicsoundboard.fileexplorer

import java.io.File

/**
 * File created by eric.neidhardt on 04.09.2015.
 */
interface FileResultHandler
{
	fun onFileResultsAvailable(files: List<File>)
}