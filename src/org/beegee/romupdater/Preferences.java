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

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class Preferences extends PreferenceActivity {
	private SharedPreferences preferences;
	private static final String TAG = "ROM Updater (Preferences.class)";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		preferences = PreferenceManager.getDefaultSharedPreferences(this);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		if (requestCode == 0) {
			try {
				String url = intent.getStringExtra("SCAN_RESULT");
				if (!url.endsWith("/"))
					url += "/";
				Log.i(TAG, "Repository URL found: " + url);

				Editor preferencesEditor = preferences.edit();
				preferencesEditor.putString("repository_url", url);
				preferencesEditor.commit();

				Toast t = Toast.makeText(this,
						getString(R.string.repository_changed_toast) + " ("
								+ url + ")", Toast.LENGTH_LONG);
				t.show();
			} catch (Exception e) {
				Log.e(TAG, e.toString());
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
		case R.id.menu_exit:
			finish();
			return true;
		case R.id.menu_info:
			PackageManager pm = getPackageManager();
			String version = "";
			try {
				version = pm.getPackageInfo("org.beegee.romupdater", 0).versionName;
			} catch (Exception e) {
				e.printStackTrace();
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(
					Preferences.this);
			builder.setIcon(R.drawable.ic_menu_info)
					.setTitle(getString(R.string.app_name) + " v." + version)
					.setMessage(
							SharedData.ABOUT_LICENCE
									+ "\n\nPlease donate via PayPal to giacomo.furlan@fastwebnet.it.\nThanks\n\nGiacomo 'elegos' Furlan")
					.setCancelable(false)
					.setPositiveButton(getString(R.string.OK),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,	int which) {
									dialog.dismiss();
								}
							});
			AlertDialog dialog = builder.create();
			dialog.show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
}
