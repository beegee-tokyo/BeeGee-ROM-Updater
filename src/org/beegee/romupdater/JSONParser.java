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
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URI;
import java.util.Vector;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.beegee.romupdater.types.AvailableVersion;
import org.beegee.romupdater.types.AvailableVersions;
import org.beegee.romupdater.types.ROMVersion;
import org.beegee.romupdater.types.ROMVersions;
import org.beegee.romupdater.types.RepoList;

import com.google.gson.Gson;

import android.util.Log;

public class JSONParser {
	private static final String TAG = "ROM Updater (JSONParser.class)";
	public String modName = "";
	public AvailableVersions parsedAvailableVersions;
	public ROMVersions parsedVersions;
	public Boolean failed = false;

	public static boolean checkRepository(String repository_url) {
		if (!repository_url.startsWith("http://"))
			repository_url = "http://" + repository_url;
		if (!repository_url.endsWith("/"))
			repository_url += "/";
		repository_url += "main.json";
		return DownloadManager.checkHttpFile(repository_url);
	}

	public static InputStream getJSONData(String url) throws Exception {
		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
		DefaultHttpClient httpClient = new DefaultHttpClient();
		URI uri;
		InputStream data = null;
		try {
			uri = new URI(url);
			HttpGet method = new HttpGet(uri);
			HttpResponse response = httpClient.execute(method);
			data = response.getEntity().getContent();
		} catch (Exception e) {
			Log.e(TAG, "Unable to download file: " + e);
			throw e;
		}
		return data;
	}

	public String getROMVersionUri(String version) {
		for (ROMVersion rv : parsedVersions.getVersions()) {
			if (rv.getVersion().equals(version))
				return rv.getUri();
		}
		return "";
	}

	public Vector<ROMVersion> getROMVersions() {
		failed = false;

		Vector<ROMVersion> versions = new Vector<ROMVersion>();
		Gson gson = new Gson();
		Reader r;
		try {
			SharedData shared = SharedData.getInstance();
			r = new InputStreamReader(shared.getInputStreamData());

			parsedVersions = new ROMVersions();
			parsedVersions = gson.fromJson(r, ROMVersions.class);

			shared.setRepositoryROMName(parsedVersions.getName());
			shared.setRespositoryModel(parsedVersions.getPhoneModel());
		} catch (Exception e) {
			e.printStackTrace();
			failed = true;
			return new Vector<ROMVersion>();
		}

		modName = parsedVersions.getName();
		for (ROMVersion rv : parsedVersions.getVersions()) {
			Log.i(TAG, "Version: " + rv.getVersion() + " - " + rv.getUri());
			Log.i(TAG, rv.getChangelog());
			versions.add(rv);
			rv.getUri();
		}

		return versions;
	}

	public Vector<AvailableVersion> getAvailableVersions() {
		failed = false;

		parsedAvailableVersions = new AvailableVersions();
		Vector<AvailableVersion> versions = new Vector<AvailableVersion>();
		Gson gson = new Gson();
		SharedData shared = SharedData.getInstance();

		// InputStream is given from the async task
		Reader r = new InputStreamReader(shared.getInputStreamData());
		try {
			parsedAvailableVersions = gson.fromJson(r, AvailableVersions.class);
		} catch (Exception e) {
			e.printStackTrace();
			failed = true;
			return new Vector<AvailableVersion>();
		}

		for (AvailableVersion av : parsedAvailableVersions
				.getAvailableVersions()) {
			Log.i(TAG, "Version: " + av.getVersion() + " (" + av.getUri() + ")");
			versions.add(av);
		}

		return versions;
	}

	public RepoList[] getRepositoriesFromJSON() {
		failed = false;

		// get the json input stream from the async task
		SharedData shared = SharedData.getInstance();
		Reader r = new InputStreamReader(shared.getInputStreamData());

		RepoList[] theList;
		Gson gson = new Gson();
		try {
			theList = gson.fromJson(r, RepoList[].class);
			return theList;
		} catch (Exception e) {
			e.printStackTrace();
			failed = true;
			return new RepoList[0];
		}
	}

	public String getUrlForVersion(String version) {
		for (AvailableVersion av : parsedAvailableVersions.getAvailableVersions()) {
			if (Integer.parseInt(av.getVersion()) == Integer.parseInt(version))
				return av.getUri();
		}
		return "";
	}
}
