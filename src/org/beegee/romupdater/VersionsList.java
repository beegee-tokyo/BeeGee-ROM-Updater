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

import java.util.Iterator;
import java.util.Vector;
import org.beegee.romupdater.types.ROMVersion;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class VersionsList extends ROMSuperActivity {
	private static final String TAG = "ROM Updater (VersionsList.class)";

	private SharedPreferences preferences;
	private SharedData shared;

	private JSONParser myParser = new JSONParser();
	private ListView versionsListView;

	private Vector<ROMVersion> modVersions;

	private String checkLocalVersion;
	private String checkModVersion;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.versions_list);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
		shared = SharedData.getInstance();

		String prefRepo = preferences.getString("repository_url", "");
		if (!prefRepo.equals(""))
			shared.setRepositoryUrl(prefRepo);
		else {
			String buildRepo = BuildParser
					.parseString("ro.romupdater.repository");
			shared.setRepositoryUrl(buildRepo);
		}

		// repository not set
		if (shared.getRepositoryUrl().equals("http://")) {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setMessage(getString(R.string.error_repository_not_set))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.OK),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									Intent settings = new Intent(
											VersionsList.this,
											Preferences.class);
									startActivity(settings);
									finish();
								}
							});
			AlertDialog alert = dialog.create();
			alert.show();
			return;
		}

		versionsListView = (ListView) this.findViewById(R.id.versionsList);
		Toast t = Toast.makeText(this, getString(R.string.changelog_toast),
				Toast.LENGTH_LONG);
		t.show();
		setMainView();

		versionsListView
				.setOnItemLongClickListener(new OnItemLongClickListener() {
					public boolean onItemLongClick(AdapterView<?> parent,
							View view, int position, long id) {
						String selectedItem = parent
								.getItemAtPosition(position).toString();
						Log.i(TAG, "ITEM: " + selectedItem);

//						String version = selectedItem.substring(selectedItem.lastIndexOf(" ") + 1);

						String ver = shared.getRepositoryROMName() + " ";
						
//						String version = selectedItem.substring(ver.length(),selectedItem.length()-7));
						String version = selectedItem.substring(ver.length(),selectedItem.length()-7);

						String changelog = "";
						ROMVersion currentVersion = new ROMVersion();
						Iterator<ROMVersion> iVersion = modVersions.iterator();

						while (iVersion.hasNext()) {
							currentVersion = iVersion.next();
							if (currentVersion.getVersion().equals(version)) {
								changelog = currentVersion.getChangelog();
								break;
							}
						}

						AlertDialog.Builder dialog = new AlertDialog.Builder(
								VersionsList.this);
						dialog.setMessage(
								selectedItem + " changelog:\n\n" + changelog)
								.setCancelable(false)
								.setPositiveButton(getString(R.string.OK),
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.dismiss();
											}
										});
						AlertDialog alert = dialog.create();
						alert.show();

						return true;
					}
				});

		versionsListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				String selectedVersion = arg0.getItemAtPosition(arg2).toString();
				Log.i(TAG, "Item selected: " + selectedVersion);
				String ver = shared.getRepositoryROMName() + " ";
				
				shared.setDownloadVersion(selectedVersion.substring(ver.length(),selectedVersion.length()-7));

				Intent selector = new Intent(VersionsList.this, VersionSelector.class);
				selector.putExtra("org.beegee.romupdater.VersionSelector.versionUri", myParser.getROMVersionUri(shared.getDownloadVersion()));
				startActivity(selector);
			}
		});
	}

	private void setMainView() {
		String repositoryUrl = shared.getRepositoryUrl();

		// repository URL is void -> must set it before, finish
		if (repositoryUrl.equals("")) {
			AlertDialog.Builder error = new AlertDialog.Builder(VersionsList.this);
			error.setMessage(getString(R.string.error_repository_not_set))
					.setCancelable(false)
					.setPositiveButton(getString(R.string.settings), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									Intent settings = new Intent(VersionsList.this, Preferences.class);
									startActivity(settings);
								}
							});
			error.create().show();
			finish();
		}

		new DownloadJSON().execute(repositoryUrl + "main.json");
	}

	@Override
	void onJSONDataDownloaded(Boolean success) {
		super.onJSONDataDownloaded(success);

		// download failed
		// activity ends from superclass
		if (!success) {
			return;
		}

		modVersions = myParser.getROMVersions();

		// JSON parse failed, alert and return
		if (myParser.failed) {
			AlertDialog.Builder error = new AlertDialog.Builder(VersionsList.this);
			error.setMessage(getString(R.string.error_json_download))
					.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
			error.create().show();
			finish();
		}

		/* Global variables */
		shared.setRespositoryModel(myParser.parsedVersions.getPhoneModel());
		shared.setRepositoryROMName(myParser.parsedVersions.getName());

		// the repository is not for the current model
		if (!shared.getRepositoryModel().equals(SharedData.LOCAL_MODEL)) {
			AlertDialog.Builder modelAlert = new AlertDialog.Builder(this);
			modelAlert
					.setCancelable(false)
					.setMessage(getString(R.string.error_model_mismatch))
					.setPositiveButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									finish();
								}
							});
			modelAlert.create().show();
			return;
		}

		// Vector of versions, one per line
		Vector<String> versionsList = new Vector<String>();

		Iterator<ROMVersion> versionsIterator = modVersions.iterator();
		// 2. insert the versions in a vector
		String iteratorVersion = "";
		// iterate through the JSON result
		while (versionsIterator.hasNext()) {
			iteratorVersion = versionsIterator.next().getVersion();
			// if same ROM name and greater version
			// or different ROM name, add the update to the vector
			checkLocalVersion = SharedData.LOCAL_VERSION;
			checkModVersion = iteratorVersion;
			
			checkLocalVersion  = checkLocalVersion.replace('a','1');
			checkLocalVersion  = checkLocalVersion.replace('b','2');
			checkLocalVersion  = checkLocalVersion.replace('c','3');
			checkLocalVersion  = checkLocalVersion.replace('d','4');
			checkLocalVersion  = checkLocalVersion.replace('e','5');
			checkLocalVersion  = checkLocalVersion.replace('f','6');
			checkLocalVersion  = checkLocalVersion.replace('g','7');
			checkLocalVersion  = checkLocalVersion.replace('h','8');
			checkLocalVersion  = checkLocalVersion.replace('i','9');
			checkLocalVersion  = checkLocalVersion.replace('.','0');
			
			checkModVersion  = checkModVersion.replace('a','1');
			checkModVersion  = checkModVersion.replace('b','2');
			checkModVersion  = checkModVersion.replace('c','3');
			checkModVersion  = checkModVersion.replace('d','4');
			checkModVersion  = checkModVersion.replace('e','5');
			checkModVersion  = checkModVersion.replace('f','6');
			checkModVersion  = checkModVersion.replace('g','7');
			checkModVersion  = checkModVersion.replace('h','8');
			checkModVersion  = checkModVersion.replace('i','9');
			checkModVersion  = checkModVersion.replace('.','0');
			
			if (checkModVersion.length()<=5){
				for (int b=1;b==5-checkModVersion.length();b++){
					checkModVersion = checkModVersion + "0";}
			}
			if (checkLocalVersion.length()<=5){
				for (int a=1;a==5-checkLocalVersion.length();a++){
					checkLocalVersion = checkLocalVersion + "0";}
			}
			
			if (!SharedData.LOCAL_ROMNAME.equals(shared.getRepositoryROMName())
					|| (SharedData.LOCAL_ROMNAME.equals(shared.getRepositoryROMName()) 
							))
				if (Integer.parseInt(checkLocalVersion) == Integer.parseInt(checkModVersion))
					versionsList.add(myParser.modName + " " + iteratorVersion + " <= cur");
				if (Integer.parseInt(checkLocalVersion) > Integer.parseInt(checkModVersion))
					versionsList.add(myParser.modName + " " + iteratorVersion + " <= old");
				if (Integer.parseInt(checkLocalVersion) < Integer.parseInt(checkModVersion))
					versionsList.add(myParser.modName + " " + iteratorVersion + " <= new");
		}

		// ROM name differs between the local and the remote one
		// alert the user he'll only be able to download FULL versions
		if (!SharedData.LOCAL_ROMNAME.equals(shared.getRepositoryROMName())) {
			AlertDialog.Builder builder = new AlertDialog.Builder(VersionsList.this);
			builder.setCancelable(true)
					.setMessage(getString(R.string.modname_mismatch))
					.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
		}

		// if the vector is empty, alert and finish
		if (versionsList.isEmpty()) {
			AlertDialog.Builder updatedBuilder = new AlertDialog.Builder(
					VersionsList.this);
			updatedBuilder
					.setCancelable(true)
					.setMessage(getString(R.string.rom_is_updated))
					.setPositiveButton(getString(R.string.OK), new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int which) {
									dialog.dismiss();
									finish();
								}
							});
			AlertDialog upToDateDialog = updatedBuilder.create();
			upToDateDialog.show();
			return;
		}

		// 3. set the versions list
		ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, versionsList);
		versionsListView.setAdapter(adapter);
	}

}
