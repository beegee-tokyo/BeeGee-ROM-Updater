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

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class RecoveryManager {
	private static String TAG = "ROM Updater (Recovery Manager)";

	// Reboot into CWM recovery
	
	public static void rebootRecovery() {
		SharedData sdata = SharedData.getInstance();

		while (sdata.getRecoveryCounter() < sdata.getRecoveryOperations()) {
			while (sdata.getLockProcess())
				;
		}

		sdata.addRecoveryMessage("rm /cache/recovery/command\n");
		sdata.addRecoveryMessage("mkdir -p /sdcard/clockworkmod\n");
		sdata.addRecoveryMessage("echo 1 > /sdcard/clockworkmod/.recoverycheckpoint\n");
		sdata.addRecoveryMessage("echo start > /proc/ota\n");
		sdata.addRecoveryMessage("/system/bin/recovery_2 && /system/bin/toolbox reboot\n");

		// delete existing extendedcommand file, just in case!
		File toDelete = new File("/cache/recovery/extendedcommand");
		toDelete.delete();

		// Generate new extendedcommand file
		try {
			Process p = Runtime.getRuntime().exec("su");
			OutputStream os = p.getOutputStream();
			os.write(sdata.getRecoveryMessage().getBytes());
			os.flush();
		} catch (Exception e) {
			Log.e(TAG, "Unable to reboot into recovery");
		}
	}

	// Extended command section

	public static void setupExtendedCommand() {
		SharedData sdata = SharedData.getInstance();

		while (sdata.getLockProcess())
			;
		sdata.setLockProcess(true);

		sdata.addRecoveryMessage("mkdir -p /cache/recovery/\n");
		sdata.addRecoveryMessage("echo 'boot-recovery' >/cache/recovery/command\n");
		sdata.addRecoveryMessage("echo 'ui_print(\"BeeGees ROM Updater by elegos\");' > /cache/recovery/extendedcommand\n");

		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}

	// Install ROM
	
	public static void addUpdate(String file) {
		SharedData sdata = SharedData.getInstance();

		while (sdata.getLockProcess())
			;
		sdata.setLockProcess(true);

		sdata.addRecoveryMessage("print Installing file " + file + "\n");
		sdata.addRecoveryMessage("echo 'install_zip(\"" + file	+ "\");' >> /cache/recovery/extendedcommand\n");

		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}

	// Backup ROM
	
	public static void doBackup(Context context) {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		String backupFolder = "/sdcard/" + preferences.getString("backup_folder", "clockworkmod/backup");
		if (!backupFolder.endsWith("/"))
			backupFolder += "/";

		SharedData sdata = SharedData.getInstance();

		while (sdata.getLockProcess())
			;
		sdata.setLockProcess(true);
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH.mm");
			Date date = new Date();

			sdata.addRecoveryMessage("mkdir -p " + backupFolder + "\n");
			sdata.addRecoveryMessage("echo 'ui_print(\"BeeGees ROM Updater by elegos\\n\");' > /cache/recovery/extendedcommand\n");
			sdata.addRecoveryMessage("echo 'backup_rom(\"" + backupFolder
			+ format.format(date)
			+ "\");'" + " >> /cache/recovery/extendedcommand\n");
		} catch (Exception e) {
			Log.e(TAG, "Unable to setup environment for Nandroid backup");
			e.printStackTrace();
		} finally {
			sdata.incrementRecoveryCounter();
			sdata.setLockProcess(false);
		}
	}

	// Restore ROM
	
	public static void restoreBackup(String backupDirectory) {
		SharedData sdata = SharedData.getInstance();

		while (sdata.getLockProcess())
			;
		sdata.setLockProcess(true);

		sdata.addRecoveryMessage("echo 'ui_print(\"BeeGees ROM Updater by elegos\\n\");' > /cache/recovery/extendedcommand\n");
		sdata.addRecoveryMessage("echo 'restore_rom(\"" + backupDirectory
		+ "\", \"boot\", \"system\", \"data\", \"cache\", \"sd-ext\");' >> /cache/recovery/extendedcommand\n");

		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}

	// Wipe cache & dalvik-cache
	
	public static void wipeCache() {
		SharedData sdata = SharedData.getInstance();

		while (sdata.getLockProcess())
			;
		sdata.setLockProcess(true);

		sdata.addRecoveryMessage("echo '#!/sbin/sh\n' >> /cache/wipedalvik.sh\n");
		sdata.addRecoveryMessage("echo 'for partition in data cache system sd-ext\n' >> /cache/wipedalvik.sh\n");
		sdata.addRecoveryMessage("echo 'do' >> /cache/wipedalvik.sh\n");
		sdata.addRecoveryMessage("echo 'mount /$partition' >> /cache/wipedalvik.sh\n");
		sdata.addRecoveryMessage("echo 'rm -rf /$partition/dalvik-cache' >> /cache/wipedalvik.sh\n");
		sdata.addRecoveryMessage("echo 'echo \'Wiping \' + $partition + \'/dalvik-cache\'' >> /cache/wipedalvik.sh\n");
		sdata.addRecoveryMessage("echo 'done' >> /cache/wipedalvik.sh\n");
		sdata.addRecoveryMessage("echo 'ui_print(\"Wiping dalvik-cache\");' >> /cache/recovery/extendedcommand\n");
		sdata.addRecoveryMessage("echo 'run_program(\"/cache/wipedalvik.sh\");' >> /cache/recovery/extendedcommand\n");
		sdata.addRecoveryMessage("echo 'ui_print(\"Wiping CACHE\");' >> /cache/recovery/extendedcommand\n");
		sdata.addRecoveryMessage("echo 'format(\"/cache\");' >> /cache/recovery/extendedcommand\n");
		
		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}

	// Wipe data
	
	public static void wipeData() {
		SharedData sdata = SharedData.getInstance();
		while (sdata.getLockProcess())
			;

		sdata.setLockProcess(true);
		sdata.addRecoveryMessage("echo 'ui_print(\"Wiping USER DATA\");' >> /cache/recovery/extendedcommand\n");
		sdata.addRecoveryMessage("echo 'format(\"/data\");' >> /cache/recovery/extendedcommand\n");
		
		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}

	// Wipe SDext
	
	public static void wipeSDExt() {
		SharedData sdata = SharedData.getInstance();

		while (sdata.getLockProcess())
			;
		sdata.setLockProcess(true);

		sdata.addRecoveryMessage("echo 'ui_print(\"Wiping SD-EXT\");' >> /cache/recovery/extendedcommand\n");
		sdata.addRecoveryMessage("echo 'format(\"/sd-ext\");' >> /cache/recovery/extendedcommand\n");
		
		sdata.incrementRecoveryCounter();
		sdata.setLockProcess(false);
	}

	// Command section

	public static void setupCommand() {
		try {
			Process p = Runtime.getRuntime().exec("su");
			OutputStream os = p.getOutputStream();

			os.write("mkdir -p /cache/recovery/\n".getBytes());
			os.write("rm /cache/recovery/*\n".getBytes());
			os.write("echo 'boot-recovery' >/cache/recovery/command\n"
					.getBytes());

			os.flush();
		} catch (Exception e) {
			Log.e(TAG, "Unable to reboot into Recovery mode for wiping cache");
			e.printStackTrace();
		}
	}
}
