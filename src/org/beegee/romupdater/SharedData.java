/*
 * This file is part of ROMUpdater.

 * ROMUpdater is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.

 * ROMUpdater is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with ROMUpdater.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beegee.romupdater;

import java.io.InputStream;

public class SharedData {
	static final Object mLock = new Object();
	static SharedData mInstance;

	public static final String LOCAL_MODEL = android.os.Build.MODEL;
	public static String LOCAL_ROMNAME = BuildParser.parseString("ro.beegee.romname");
	public static String LOCAL_VERSION = BuildParser.parseString("ro.beegee.romversion");

	public static final String ABOUT_LICENCE = "BeeGee ROM Updater Copyright (C) 2011 by beegee_tokyo\nbased on\nROM Updater Copyright (C) 2011 by elegos\nThis program comes with ABSOLUTELY NO WARRANTY;\nThis is free software, and you are welcome to redistribute it under certain conditions;";

	private String repositoryModel;
	private String repositoryROMName;
	private String downloadVersion;

	private String repositoryUrl;
	private String downloadedFile;

	private Boolean lockProcess; // synchronous operations
	private Integer recoveryCounter;
	private Integer recoveryOperations;
	private String recoveryMessage;

	private InputStream data;

	private SharedData() {
		setRepositoryROMName("");
		setRespositoryModel("");
		setDownloadVersion("");

		setLockProcess(false);
		setRecoveryCounter(0);
		setRecoveryOperations(0);
		setRecoveryMessage("");
	}

	//
	// SETTERS
	//

	public static SharedData getInstance() {
		synchronized (mLock) {
			if (mInstance == null) {
				mInstance = new SharedData();
			}
			return mInstance;
		}
	}

	public void setRespositoryModel(String repository) {
		repositoryModel = repository;
	}

	public void setRepositoryROMName(String name) {
		repositoryROMName = name;
	}

	public void setDownloadVersion(String version) {
		downloadVersion = version;
	}

	public void setRepositoryUrl(String url) {
		if (!url.startsWith("http://"))
			url = "http://" + url;
		if (!url.endsWith("/"))
			url += "/";
		repositoryUrl = url;
	}

	public void setDownloadedFile(String file) {
		downloadedFile = file;
	}

	public void setLockProcess(Boolean b) {
		lockProcess = b;
	}

	private void setRecoveryCounter(Integer i) {
		recoveryCounter = i;
	}

	public void incrementRecoveryCounter() {
		recoveryCounter++;
	}

	public void decrementRecoveryCounter() {
		recoveryCounter--;
	}

	public void setRecoveryOperations(Integer i) {
		recoveryOperations = i;
	}

	public void setRecoveryMessage(String s) {
		recoveryMessage = s;
	}

	public void addRecoveryMessage(String s) {
		recoveryMessage += s;
	}

	public void setInputStreamData(InputStream is) {
		data = is;
	}

	//
	// GETTERS
	//

	public String getRepositoryModel() {
		return repositoryModel;
	}

	public String getRepositoryROMName() {
		return repositoryROMName;
	}

	public String getDownloadVersion() {
		return downloadVersion;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}

	public String getDownloadedFile() {
		return downloadedFile;
	}

	public Boolean getLockProcess() {
		return lockProcess;
	}

	public Integer getRecoveryCounter() {
		return recoveryCounter;
	}

	public Integer getRecoveryOperations() {
		return recoveryOperations;
	}

	public String getRecoveryMessage() {
		return recoveryMessage;
	}

	public InputStream getInputStreamData() {
		return data;
	}
}
