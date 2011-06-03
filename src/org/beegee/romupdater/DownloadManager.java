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

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.util.Log;

public class DownloadManager {
	private static final String TAG = "ROM Updater (DownloadPackage.class)";
	public static final String download_path = "/sdcard/romupdater/";

	public static boolean checkHttpFile(String url) {
		try {
			HttpParams httpParameters = new BasicHttpParams();
			HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
			Log.i(TAG, "Testing " + url + "...");
			URL theUrl = new URL(url);
			HttpURLConnection connection = (HttpURLConnection) theUrl.openConnection();
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				connection.disconnect();
			} else {
				Log.i(TAG,
						"HTTP Response code: " + connection.getResponseCode());
				return false;
			}
		} catch (IOException e) {
			Log.e(TAG, e.toString());
			return false;
		}
		return true;
	}
}
