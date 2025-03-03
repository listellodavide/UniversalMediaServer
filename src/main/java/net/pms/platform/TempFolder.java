/*
 * This file is part of Universal Media Server, based on PS3 Media Server.
 *
 * This program is a free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; version 2 of the License only.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 */
package net.pms.platform;

import com.sun.jna.Platform;
import java.io.File;
import java.io.IOException;
import net.pms.util.FilePermissions;
import net.pms.util.FileUtil;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles finding a temporary directory.
 *
 * @author Tim Cox (mail@tcox.org)
 */
public class TempFolder {
	private static final Logger LOGGER = LoggerFactory.getLogger(TempFolder.class);
	private static final String DEFAULT_TEMP_FOLDER_NAME = "UMS";
	private final String userSpecifiedFolder;
	private File folder;

	/**
	 * @param userSpecifiedFolder may be null
	 */
	public TempFolder(String userSpecifiedFolder) {
		this.userSpecifiedFolder = userSpecifiedFolder;
	}

	public synchronized File getTempFolder() throws IOException {
		if (folder == null) {
			folder = getTempFolder(userSpecifiedFolder);
		}

		return folder;
	}

	private static File getTempFolder(String userSpecifiedFolder) throws IOException {
		if (userSpecifiedFolder == null) {
			return getSystemTempFolder();
		}

		try {
			return getUserSpecifiedTempFolder(userSpecifiedFolder);
		} catch (IOException e) {
			LOGGER.error("Problem with user specified temp directory - using system", e);
			return getSystemTempFolder();
		}
	}

	private static File getUserSpecifiedTempFolder(String userSpecifiedFolder) throws IOException {
		if (userSpecifiedFolder == null || userSpecifiedFolder.length() == 0) {
			throw new IOException("Temporary directory path must not be null or empty if specified");
		}

		File folderFile = new File(userSpecifiedFolder);
		folderFile.setReadable(true, false);
		folderFile.setWritable(true, false);
		FileUtils.forceMkdir(folderFile);
		assertFolderIsValid(folderFile);
		return folderFile;
	}

	private static File getSystemTempFolder() throws IOException {
		File tmp = new File(System.getProperty("java.io.tmpdir"));
		File myTMP;
		if (Platform.isWindows()) {
			myTMP = new File(tmp, DEFAULT_TEMP_FOLDER_NAME);
		} else {
			myTMP = new File(tmp, DEFAULT_TEMP_FOLDER_NAME + "-" + System.getProperty("user.name"));
		}

		myTMP.setReadable(true, false);
		myTMP.setWritable(true, false);
		FileUtils.forceMkdir(myTMP);
		assertFolderIsValid(myTMP);
		return myTMP;
	}

	private static void assertFolderIsValid(File folder) throws IOException {
		FilePermissions permission = FileUtil.getFilePermissions(folder);
		if (!permission.isFolder()) {
			throw new IOException("Temporary folder isn't a folder: " + folder.getAbsolutePath());
		}
		if (!permission.isBrowsable()) {
			throw new IOException("Temporary folder isn't browsable: " + folder.getAbsolutePath());
		}
		if (!permission.isWritable()) {
			throw new IOException("Temporary folder isn't writable: " + folder.getAbsolutePath());
		}
	}
}
