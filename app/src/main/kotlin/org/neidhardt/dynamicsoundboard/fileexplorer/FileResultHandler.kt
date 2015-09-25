package org.neidhardt.dynamicsoundboard.fileexplorer

import java.io.File

/**
 * File created by eric.neidhardt on 04.09.2015.
 */
public interface FileResultHandler
{
	public fun onFileResultsAvailable(files: List<File>)
}