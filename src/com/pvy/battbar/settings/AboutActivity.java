/**
 * About Activity for TEAM Battery Bar "Mod Version"
 * 
 * We are NOT responsible for any damages that come about from using these sources!!
 */
/* Change Notes:
 * 
 * v5
 * updated the TEAM site to our new home
 * 
 */
package com.pvy.battbar.settings;

import com.pvy.battbar.settings.R;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceClickListener;

public class AboutActivity extends PreferenceActivity {

	public void onCreate(Bundle pvymods) {
		super.onCreate(pvymods);
		addPreferencesFromResource(R.xml.about_prefs);
		
		/*********
		 * Please dont remove these links feel free to add more to the list tho.
		 *********/
		
		findPreference("pvy_web").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent browserIntent = new Intent(
								"android.intent.action.VIEW", Uri
										.parse("http://team-devs.com"));
						startActivity(browserIntent);
						return true;
					}
				});		
		findPreference("thread_link").setOnPreferenceClickListener(
				new OnPreferenceClickListener() {
					public boolean onPreferenceClick(Preference preference) {
						Intent browserIntent = new Intent(
								"android.intent.action.VIEW", Uri
										.parse("http://forum.xda-developers.com/showthread.php?t=1371330")); 
						startActivity(browserIntent);
						return true;
					}
				});		
	}
}
