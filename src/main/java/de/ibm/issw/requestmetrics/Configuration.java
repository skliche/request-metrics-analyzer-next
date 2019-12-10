package de.ibm.issw.requestmetrics;

import java.io.File;
import java.util.prefs.Preferences;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration {

	public static final Logger LOG = LoggerFactory.getLogger(Configuration.class);

	public static final boolean ROOT_DETAILS_FIND_UNIQUE_URIS = Boolean
			.getBoolean(Configuration.class.getCanonicalName() + ".root_details_find_unique_uris");

	public static final Preferences PREFERENCES = Preferences.userRoot()
			.node("/" + Configuration.class.getCanonicalName().replace('.', '/'));

	private static final String PREF_FILE_OPEN_DIR = "file.open.dir";

	public static File getFileOpenDirectory() {
		File result = null;
		String fileOpenDir = PREFERENCES.get(PREF_FILE_OPEN_DIR, null);
		if (fileOpenDir != null && fileOpenDir.length() > 0) {
			result = new File(fileOpenDir);
			if (!result.exists()) {
				LOG.warn(
						"Preference for file open directory invalid because the directory no longer exists: "
								+ fileOpenDir);
				result = null;
			}
		}
		return result;
	}

	public static void setFileOpenDirectory(File directory) {
		LOG.info("setFileOpenDirectory " + directory.getAbsolutePath());
		PREFERENCES.put(PREF_FILE_OPEN_DIR, directory.getAbsolutePath());
	}
}
