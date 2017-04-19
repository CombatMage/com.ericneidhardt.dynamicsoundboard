package org.neidhardt.utils

import java.io.File

/**
 * Created by eric.neidhardt@gmail.com on 14.12.2016.
 */
fun File.writeString(string: String) {
	this.printWriter().use { out ->
		out.write(string)
	}
}