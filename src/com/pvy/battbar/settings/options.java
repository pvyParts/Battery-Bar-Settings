
/**
 * Main Activity for TEAM battery bar "Mod Version"
 * 
 * We are NOT responsible for any damages that come about from using these sources!!
 * 
 * Version 4.0
 * For Battery Bar Version 2.2
 * 
 * options available here are
 * 		- Show/Hide
 * 		- Height
 * 		- Low / Med level % cutoffs
 *  	- Auto Magic-ally Colour / Static Colour 
 *  	- Colour Popups use the Devmil Tabbed Colour Selector Dialog (cause it is awesome)
 */

/* Change notes
 * 
 * v2.0
 * added height options.
 * 
 * v3.0
 * added new color dialogs.
 * 
 * v4.0
 * fixed default color of med level to be non-transparent
 * 
 * v5.0 
 * added new Low / Med level % cutoff options
 * 
 */
package com.pvy.battbar.settings;

import com.pvy.battbar.settings.R;
import de.devmil.common.ui.color.ColorSelectorDialog;
import de.devmil.common.ui.color.ColorSelectorDialog.OnColorChangedListener;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

public class options extends PreferenceActivity implements
		OnColorChangedListener, OnPreferenceClickListener,
		OnPreferenceChangeListener {

	private static final int BAR_STYLE_SHOW = 1; // show me the BATTERY BAR!
	private static final int BAR_STYLE_DISABLE = 2; // or hide it

	// flags for the color selector popup
	private static final int FLAG_COLOR_BATTERY = 1;
	private static final int FLAG_COLOR_AUTO_CHARGING = 2;
	private static final int FLAG_COLOR_AUTO_MEDIUM = 3;
	private static final int FLAG_COLOR_AUTO_REGULAR = 4;
	private static final int FLAG_COLOR_AUTO_LOW = 5;
	private static final int FLAG_LOW_BATTERY = 6;
	private static final int FLAG_HIGH_BATTERY = 7;

	private AlertDialog.Builder alert = null;
	private AlertDialog.Builder alert1 = null;
	private AlertDialog.Builder alert2 = null;

	private EditText input = null;
	private EditText input1 = null;
	private EditText input2 = null;

	private AlertDialog Alert = null;
	private AlertDialog Alert1 = null;
	private AlertDialog Alert2 = null;

	private int batterybarColorPickerFlag;

	private ColorSelectorDialog batteryColorPickerDialog = null;

	private Preference height;
	private Preference low;
	private Preference high;

	private int low_cut = 0;
	private int high_cut = 0;
	private int low_lvl = 15;
	private int high_lvl = 40;

	private Context mContext;

	/** Called when the activity is first created. **/
	@Override
	public void onCreate(Bundle pvymods) {
		super.onCreate(pvymods);
		addPreferencesFromResource(R.xml.options);
		mContext = this.getApplicationContext();
		
		//Create me some dialogs
		input = new EditText(this);
		input1 = new EditText(this);
		input2 = new EditText(this);

		// numbers only thanks :D
		input.setInputType(InputType.TYPE_CLASS_NUMBER);
		input1.setInputType(InputType.TYPE_CLASS_NUMBER);
		input2.setInputType(InputType.TYPE_CLASS_NUMBER);

		// set the caption values + setup the menu
		low = (Preference) findPreference("battery_bar_low_pref");
		low.setSummary(low_cut + " %");
		high = (Preference) findPreference("battery_bar_high_pref");
		high.setSummary(high_cut + " %");
		
		height = (Preference) findPreference("battery_bar_height_pref");
		Preference battery_automatically_color_pref = (Preference) findPreference("battery_bar_automatically_color_pref");
		Preference battery_bar_pref = (CheckBoxPreference) findPreference("show_battery_bar");
		height.setOnPreferenceClickListener(this);
		low.setOnPreferenceClickListener(this);
		high.setOnPreferenceClickListener(this);

		try {
			if (Settings.System.getInt(getContentResolver(),
					"battery_bar_auto_color") == 1) {
				((CheckBoxPreference) battery_automatically_color_pref)
						.setChecked(true);
				battery_automatically_color_pref.setSummary(R.string.on1);

			} else {
				((CheckBoxPreference) battery_automatically_color_pref)
						.setChecked(false);
				battery_automatically_color_pref.setSummary(R.string.off2);
			}
		} catch (SettingNotFoundException e) {
			((CheckBoxPreference) battery_automatically_color_pref)
					.setChecked(true);
			Settings.System.putInt(getContentResolver(),
					"battery_bar_auto_color", 1);
		}
		try {
			int hold = Settings.System.getInt(getContentResolver(),
					"battery_bar_height");
			height.setSummary("" + hold);
		} catch (SettingNotFoundException e) {
			Settings.System.putInt(getContentResolver(), "battery_bar_height",
					1);
			height.setSummary("1");
		}
		try {
			if (Settings.System.getInt(getContentResolver(),
					"battery_bar_style") == 1) {
				((CheckBoxPreference) battery_bar_pref).setChecked(true);
				battery_bar_pref.setSummary(R.string.on);
			} else {
				((CheckBoxPreference) battery_bar_pref).setChecked(false);
				battery_bar_pref.setSummary(R.string.off);
			}
		} catch (SettingNotFoundException e) {
			((CheckBoxPreference) battery_bar_pref).setChecked(true);
			Settings.System
					.putInt(getContentResolver(), "battery_bar_style", 1);
			battery_bar_pref.setSummary(R.string.on);
		}

		battery_bar_pref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						boolean checked = ((CheckBoxPreference) preference)
								.isChecked();
						if (checked) {
							Settings.System.putInt(getContentResolver(),
									"battery_bar_style", BAR_STYLE_SHOW);
							preference.setSummary(R.string.on);
						} else {
							Settings.System.putInt(getContentResolver(),
									"battery_bar_style", BAR_STYLE_DISABLE);
							preference.setSummary(R.string.off);
						}
						return true;
					}

				});
		battery_automatically_color_pref
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					public boolean onPreferenceClick(Preference preference) {
						boolean checked = ((CheckBoxPreference) preference)
								.isChecked();
						if (checked) {
							Settings.System.putInt(getContentResolver(),
									"battery_bar_auto_color", 1);
							preference.setSummary(R.string.on1);
						} else {
							Settings.System.putInt(getContentResolver(),
									"battery_bar_auto_color", 0);
							preference.setSummary(R.string.off2);
						}
						return true;
					}

				});

		findPreference("battery_bar_color_pref").setOnPreferenceClickListener(
				this);

		findPreference("battery_bar_color_auto_charging")
				.setOnPreferenceClickListener(this);

		findPreference("battery_bar_color_auto_regular")
				.setOnPreferenceClickListener(this);

		findPreference("battery_bar_color_auto_medium")
				.setOnPreferenceClickListener(this);

		findPreference("battery_bar_color_auto_low")
				.setOnPreferenceClickListener(this);

		batteryColorPickerDialog = new ColorSelectorDialog(this, this,
				0xffffffff);
		alert = new AlertDialog.Builder(this);
		alert1 = new AlertDialog.Builder(this);
		alert2 = new AlertDialog.Builder(this);

		alert.setTitle(height.getTitle());

		// Set an EditText view to get user input
		alert.setView(input);
		input.setText(Settings.System.getInt(getContentResolver(),
				"battery_bar_height", 1) + "");
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = "" + input.getText();
				int aInt = 0;
				try {
					aInt = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					Toast.makeText(options.this,
							"She's Dead Jim! Please Supply a Valid Number!",
							Toast.LENGTH_LONG);
				}
				if (aInt >= 50) {
					aInt = 50;
				}
				// Do something with value!
				height.setSummary("" + aInt);
				Settings.System.putInt(getContentResolver(),
						"battery_bar_height", aInt);
			}
		});
		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						// do nothing
						Alert.dismiss();

					}
				});
		Alert = alert.create();

		alert1.setTitle(height.getTitle());

		// Set an EditText view to get user input
		alert1.setView(input1);
		input1.setText(low_cut + "");

		alert1.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = "" + input1.getText();
				try {
					low_cut = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					low_cut = Settings.System.getInt(getContentResolver(),
							"battery_bar_low_cut", low_lvl);
					Toast.makeText(options.this,
							"She's Dead Jim! Please Supply a Valid Number!",
							Toast.LENGTH_LONG);
				}

				if (low_cut >= high_cut) {
					Toast.makeText(
							options.this,
							"She's Dead Jim! The Low Cutoff Must be Less than the High Cutoff!",
							Toast.LENGTH_LONG);
					low_cut = high_cut - 1;
				}

				// Do something with value!
				low.setSummary(low_cut + " %");
				Settings.System.putInt(getContentResolver(),
						"battery_bar_low_cut", low_cut);

			}
		});
		alert1.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						// do nothing.
						Alert1.dismiss();

					}
				});

		Alert1 = alert1.create();

		alert2.setTitle(height.getTitle());

		// Set an EditText view to get user input
		alert2.setView(input2);
		input2.setText(high_cut + "");

		alert2.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String value = "" + input2.getText();
				try {
					high_cut = Integer.parseInt(value);
				} catch (NumberFormatException e) {
					high_cut = Settings.System.getInt(getContentResolver(),
							"battery_bar_high_cut", 1);
					Toast.makeText(options.this,
							"She's Dead Jim! Please Supply a Valid Number!",
							Toast.LENGTH_LONG);
				}

				if (high_cut <= low_cut) {
					Toast.makeText(
							options.this,
							"She's Dead Jim! The High Cutoff Must be More than the Low Cutoff!",
							Toast.LENGTH_LONG);
					high_cut = low_cut + 1;
				}

				// Do something with value!
				high.setSummary(high_cut + " %");
				Settings.System.putInt(getContentResolver(),
						"battery_bar_high_cut", high_cut);

			}
		});
		alert2.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
						// do nothing
						Alert2.dismiss();

					}
				});
		Alert2 = alert2.create();

	}

	public boolean onPreferenceClick(Preference preference) {

		String key = preference.getKey();

		if (key.equals("battery_bar_color_auto_charging")) {
			batterybarColorPickerFlag = FLAG_COLOR_AUTO_CHARGING;

			batteryColorPickerDialog = new ColorSelectorDialog(this, this,
					Settings.System.getInt(getContentResolver(),
							"battery_bar_color_auto_charging", 0xFF00FF00));

			batteryColorPickerDialog.setTitle(R.string.batt_col_charge);
			batteryColorPickerDialog.show();

		} else if (key.equals("battery_bar_color_auto_regular")) {
			batterybarColorPickerFlag = FLAG_COLOR_AUTO_REGULAR;
			batteryColorPickerDialog = new ColorSelectorDialog(this, this,
					Settings.System.getInt(getContentResolver(),
							"battery_bar_color_auto_regular", 0xFF00FF00));
			batteryColorPickerDialog.setTitle(R.string.batt_col_reg);
			batteryColorPickerDialog.show();

		} else if (key.equals("battery_bar_color_auto_medium")) {
			batterybarColorPickerFlag = FLAG_COLOR_AUTO_MEDIUM;
			batteryColorPickerDialog = new ColorSelectorDialog(this, this,
					Settings.System.getInt(getContentResolver(),
							"battery_bar_color_auto_medium", 0xffFA8900));
			batteryColorPickerDialog.setTitle(R.string.batt_less_40_title);
			batteryColorPickerDialog.show();

		} else if (key.equals("battery_bar_color_auto_low")) {
			batterybarColorPickerFlag = FLAG_COLOR_AUTO_LOW;
			batteryColorPickerDialog = new ColorSelectorDialog(this, this,
					Settings.System.getInt(getContentResolver(),
							"battery_bar_color_auto_low", 0xFFFF0000));
			batteryColorPickerDialog.setTitle(R.string.batt_less_15_title);
			batteryColorPickerDialog.show();

		} else if (key.equals("battery_bar_color_pref")) {
			batterybarColorPickerFlag = FLAG_COLOR_BATTERY;
			batteryColorPickerDialog = new ColorSelectorDialog(this, this,
					Settings.System.getInt(getContentResolver(),
							"battery_bar_color", 0xFF00FF00));
			batteryColorPickerDialog.setTitle(R.string.batt_color);
			batteryColorPickerDialog.show();

		} else if (key.equals("battery_bar_height_pref")) {
			batterybarColorPickerFlag = FLAG_COLOR_BATTERY;
			Alert.show();
		} else if (key.equals("battery_bar_low_pref")) {
			batterybarColorPickerFlag = FLAG_LOW_BATTERY;
			Alert1.show();
		} else if (key.equals("battery_bar_high_pref")) {
			batterybarColorPickerFlag = FLAG_HIGH_BATTERY;
			Alert2.show();
		}
		return true;

	}

	public boolean onPreferenceChange(Preference preference, Object newValue) {
		if (preference == height) {
			int hold = Integer.valueOf((String) newValue);

			Settings.System.putInt(getContentResolver(), "battery_bar_height",
					hold);
			height.setSummary("" + ((String) newValue));
			return true;
		}

		return false;
	}

	@Override
	public void colorChanged(int color) {

		switch (batterybarColorPickerFlag) {

		case FLAG_COLOR_BATTERY:

			Settings.System.putInt(mContext.getContentResolver(),
					"battery_bar_color", color);

			break;

		case FLAG_COLOR_AUTO_CHARGING:

			Settings.System.putInt(mContext.getContentResolver(),
					"battery_bar_color_auto_charging", color);
			break;

		case FLAG_COLOR_AUTO_LOW:

			Settings.System.putInt(mContext.getContentResolver(),
					"battery_bar_color_auto_low", color);
			break;

		case FLAG_COLOR_AUTO_MEDIUM:

			Settings.System.putInt(mContext.getContentResolver(),
					"battery_bar_color_auto_medium", color);
			break;

		case FLAG_COLOR_AUTO_REGULAR:

			Settings.System.putInt(mContext.getContentResolver(),
					"battery_bar_color_auto_regular", color);
			break;

		}

	}

}