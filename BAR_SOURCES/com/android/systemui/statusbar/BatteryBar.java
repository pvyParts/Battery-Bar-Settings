package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.ContentObserver;
import android.graphics.drawable.Animatable;
import android.os.BatteryManager;
import android.os.Handler;
import android.provider.Settings;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;

public class BatteryBar extends RelativeLayout implements Animatable {

	View charge;
	RelativeLayout battbar;
	RelativeLayout chargeholder;

	private Context mContext;
	private Boolean debug = false;

	private int low_lvl = 15;
	private int high_lvl = 40;

	private int width;
	private int height;
	private int batteryLevel = 1;
	private int batteryStatus;
	private boolean mAttached;

	// Hide and Show
	private static final int STYLE_SHOW = 1;
	private static final int STYLE_DISABLE = 2;

	// Alignment Flags
	private static final int TOP_FLAG = 1;
	private static final int BOTTOM_FLAG = 2;

	private static final int INT_ANIM_DURATION = 2500; // animation speed higher
														// = slower 2500 is a
														// good normal speed
	private static final int FAST_ANIM_DURATION = 800; // animation speed higher
														// = slower 2500 is a
														// good normal speed
	private static final int SLOW_ANIM_DURATION = 3500; // animation speed
														// higher = slower 2500
														// is a good normal
														// speed

	// Amination variables
	private int anim_mode = 0;
	private int pulse_mode = 0;

	// Animation Flags
	private final int FLAG_MODE_ACLDCL = 1; // accellerate then decelerate
	private final int FLAG_MODE_ACL = 2; // accellerate only
	private final int FLAG_MODE_DCL = 3; // decelerate only
	private final int FLAG_MODE_NORM = 0; // normal curve

	// Pulse expand mode
	private final int FLAG_PULSE = 1;
	private final int FLAG_NORM = 0;

	// Amination Is Running variable
	public Boolean mRunning = false;

	private int style;

	public BatteryBar(Context context) {
		super(context);
		mContext = context;
	}

	public BatteryBar(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
		mContext = context;
	}

	public BatteryBar(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs);
		mContext = context;
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();

		battbar = new RelativeLayout(mContext);
		charge = new View(mContext);
		chargeholder = new RelativeLayout(mContext);

		// this.setOrientation(LinearLayout.HORIZONTAL); //hoizontal;

		debug = true;// Settings.getBoolean("battery_bar_debug", false);

		if (!mAttached) {
			// set and get variables
			mAttached = true;
			WindowManager screen = (WindowManager) this.getContext()
					.getApplicationContext().getSystemService("window");
			width = screen.getDefaultDisplay().getWidth();

			// battery callback handler
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_BATTERY_CHANGED);
			filter.addAction(Intent.ACTION_CONFIGURATION_CHANGED);
			getContext().registerReceiver(mIntentReceiver, filter, null,
					getHandler());

			LayoutParams LP = (LayoutParams) new LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

			charge.setLayoutParams(LP);
			charge.setBackgroundColor(getBatteryColour());
			LayoutParams LPbar1 = (LayoutParams) new LayoutParams(width,
					LayoutParams.MATCH_PARENT);
			LayoutParams LPbar = (LayoutParams) new LayoutParams(
					((batteryLevel * width) / 100), height);

			chargeholder.setLayoutParams(LPbar1);

			battbar.setLayoutParams(LPbar);

			battbar.setBackgroundColor(getBatteryColour());
			battbar.setId(battbar.hashCode());
			chargeholder.setId(chargeholder.hashCode());
			chargeholder.addView(charge);

			addView(battbar);
			addView(chargeholder);

			// chargeholder.setGravity(Gravity.LEFT);
			chargeholder.setVisibility(View.GONE);

			updateBatteryBar();
		}
		//

		//

	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mAttached) {
			getContext().unregisterReceiver(mIntentReceiver);
			mAttached = false;
		}
	}

	/**
	 * Handles changes ins battery level and charger connection
	 */
	private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals(Intent.ACTION_BATTERY_CHANGED)) {
				batteryStatus = intent.getIntExtra(BatteryManager.EXTRA_STATUS,
						BatteryManager.BATTERY_STATUS_UNKNOWN);
				batteryLevel = intent.getIntExtra("level", 100);
				updateBatteryBar();
			}

			if (action.equals(Intent.ACTION_CONFIGURATION_CHANGED)) {
				updateBatteryBar();

			}
		}
	};

	private Integer getBatteryColour() {

		boolean autoColorBatteryText = Settings.System.getInt(getContext()
				.getContentResolver(), "battery_bar_auto_color", 1) == 1 ? true
				: false;

		if (autoColorBatteryText) {
			// battery Charging or full
			if (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING
					|| batteryStatus == BatteryManager.BATTERY_STATUS_FULL) {

				return Settings.System.getInt(
						getContext().getContentResolver(),
						"battery_bar_color_auto_charging", 0xFF93D500);

			} else {
				// high & low cut-offs for auto colours
				int level_low = Settings.System.getInt(getContext()
						.getContentResolver(), "battery_bar_low_cut", 15);

				int level_high = Settings.System.getInt(getContext()
						.getContentResolver(), "battery_bar_high_cut", 40);

				if (batteryLevel < level_low) {
					// Low Battery
					return Settings.System.getInt(getContext()
							.getContentResolver(),
							"battery_bar_color_auto_low", 0xFFD54B00);

				} else if (batteryLevel < level_high) {
					// Medium Battery
					return Settings.System.getInt(getContext()
							.getContentResolver(),
							"battery_bar_color_auto_medium", 0xFFD5A300);

				} else {
					// Regular Battery
					return Settings.System.getInt(getContext()
							.getContentResolver(),
							"battery_bar_color_auto_regular", 0xFFFFFFFF);

				}
			}
		} else {

			return Settings.System.getInt(getContext().getContentResolver(),
					"battery_bar_color", 0xFFFFFFFF);

		}

	}

	final void updateBatteryBar() {
		int colour = getBatteryColour();

		style = Settings.System.getInt(getContext().getContentResolver(),
				"battery_bar_style", STYLE_DISABLE);
		int settingsHeight = Settings.System.getInt(getContext()
				.getContentResolver(), "battery_bar_height", 1);

		if (style == STYLE_SHOW) {
			this.setVisibility(View.VISIBLE);

			WindowManager screen = (WindowManager) this.getContext()
					.getApplicationContext().getSystemService("window");
			
			//ViewGroup.LayoutParams baseparam1 = battbar.getLayoutParams();
			//RelativeLayout.LayoutParams baseparam1a = new RelativeLayout.LayoutParams(
					//baseparam1);
			ViewGroup.LayoutParams baseparam = battbar.getLayoutParams();

			width = screen.getDefaultDisplay().getWidth();
			height = settingsHeight;
			
			//baseparam1a.width = ViewGroup.LayoutParams.WRAP_CONTENT;
			//baseparam1a.height = ViewGroup.LayoutParams.WRAP_CONTENT;

			baseparam.width = (batteryLevel * width) / 100;
			baseparam.height = height;
			
			battbar.setLayoutParams(baseparam);
			battbar.setBackgroundColor(colour);
			//this.setLayoutParams(baseparam1a);
			
			ViewGroup.LayoutParams baseparamc = chargeholder.getLayoutParams();
			Boolean pulse = true;
			// Bullet / Pulse Animation
			// Leaves artifacts behind.... so true for now.
			if (pulse) {
				RelativeLayout.LayoutParams align = new RelativeLayout.LayoutParams(
						baseparamc);
				align.height = height;
				align.width = width - ((batteryLevel * width) / 100);
				Log.d("battbar", "" + battbar.getId());
				align.addRule(RelativeLayout.RIGHT_OF, battbar.getId());
				chargeholder.setLayoutParams(align);
				charge.setBackgroundColor(colour);
			} else if (!pulse) {
				RelativeLayout.LayoutParams align = new RelativeLayout.LayoutParams(
						baseparamc);
				align.height = height;
				align.width = 5;
				align.addRule(RelativeLayout.ALIGN_LEFT);
				chargeholder.setLayoutParams(align);
				// setDisplayedChild(indexOfChild(chargeholder));
				// bringChildToFront(chargeholder);
				charge.setBackgroundColor(0xaFFFFFFF);
			}

			// animate? stop animation?
			if (batteryStatus == BatteryManager.BATTERY_STATUS_CHARGING
					|| batteryStatus == BatteryManager.BATTERY_STATUS_FULL) {
				if (!mRunning) {
					start();
				}
			} else {
				if (mRunning) {
					stop();
				}
			}
		} else {
			this.setVisibility(View.GONE);
		}
		// was having issues with alignment
		// battbar.setGravity(Gravity.LEFT | Gravity.TOP);
		// charge.setGravity(Gravity.LEFT | Gravity.TOP);
	}

	@Override
	public boolean isRunning() {
		return mRunning;
	}

	@Override
	public void start() {

		if (Settings.System.getInt(getContext().getContentResolver(),
				"battery_bar_anim_on", 0) == 1) {
			Boolean pulsing = true;
			if (pulsing) {
				mRunning = true;
				anim_mode = Settings.System.getInt(getContext()
						.getContentResolver(), "anim_type", FLAG_MODE_ACLDCL);
				pulse_mode = Settings.System.getInt(getContext()
						.getContentResolver(), "anim_pulse_type", FLAG_PULSE);
				ScaleAnimation anim = new ScaleAnimation(0.0f, 1.0f, 1.0f, 1.0f);
				anim.setDuration(INT_ANIM_DURATION);
				if (anim_mode == FLAG_MODE_ACLDCL) {
					anim.setInterpolator(new AccelerateDecelerateInterpolator());
				} else if (anim_mode == FLAG_MODE_ACL) {
					anim.setInterpolator(new AccelerateInterpolator());
				} else if (anim_mode == FLAG_MODE_DCL) {
					anim.setInterpolator(new DecelerateInterpolator());
				} else {
					anim.setInterpolator(new LinearInterpolator());
				}
				if (pulse_mode == FLAG_PULSE) {
					anim.setRepeatMode(Animation.REVERSE);
				} else {
					// anim.setRepeatMode(Animation.REVERSE);
				}
				Log.d("battbar", "start anim");
				anim.setRepeatCount(Animation.INFINITE);

				chargeholder.setVisibility(View.VISIBLE);
				chargeholder.startAnimation(anim);
			} else if (!pulsing) {
				mRunning = true;
				anim_mode = Settings.System.getInt(getContext()
						.getContentResolver(), "anim_type", FLAG_MODE_ACLDCL);
				pulse_mode = Settings.System.getInt(getContext()
						.getContentResolver(), "anim_pulse_type", FLAG_PULSE);
				TranslateAnimation anim = new TranslateAnimation(0,
						((batteryLevel * width) / 100) - 5, 0, 0);
				anim.setDuration(INT_ANIM_DURATION);

				if (anim_mode == FLAG_MODE_ACLDCL) {
					anim.setInterpolator(new AccelerateDecelerateInterpolator());
				} else if (anim_mode == FLAG_MODE_ACL) {
					anim.setInterpolator(new AccelerateInterpolator());
				} else if (anim_mode == FLAG_MODE_DCL) {
					anim.setInterpolator(new DecelerateInterpolator());
				} else {
					anim.setInterpolator(new LinearInterpolator());
				}

				if (pulse_mode == FLAG_PULSE) {
					anim.setRepeatMode(Animation.REVERSE);
				} else {
					// anim.setRepeatMode(Animation.REVERSE);
				}
				Log.d("battbar", "start anim");
				anim.setRepeatCount(Animation.INFINITE);

				chargeholder.setVisibility(View.VISIBLE);
				chargeholder.startAnimation(anim);
			}
		}
	}

	@Override
	public void stop() {
		mRunning = false;
		// stop the animation.
		chargeholder.clearAnimation();
		Log.d("battbar", "stop anim");
		// hide the layout in-case it is a bad size after stopping.
		chargeholder.setVisibility(View.GONE);
	}

	class SettingsObserver extends ContentObserver {
		public SettingsObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub

			Log.d("pvymods", "batbar observe");

		}

		void observe() {
			Log.d("pvymods", "batbar observe");

			Context mContext = getContext();

			ContentResolver localContentResolver = mContext
					.getContentResolver();

			localContentResolver
					.registerContentObserver(
							Settings.System.getUriFor("battery_bar_style"),
							false, this);

			localContentResolver.registerContentObserver(
					Settings.System.getUriFor("battery_bar_height"), false,
					this);

			localContentResolver
					.registerContentObserver(
							Settings.System.getUriFor("battery_bar_color"),
							false, this);

			localContentResolver.registerContentObserver(
					Settings.System.getUriFor("battery_bar_color_auto_low"),
					false, this);

			localContentResolver.registerContentObserver(
					Settings.System.getUriFor("battery_bar_color_auto_medium"),
					false, this);

			localContentResolver
					.registerContentObserver(Settings.System
							.getUriFor("battery_bar_color_auto_regular"),
							false, this);

			localContentResolver.registerContentObserver(
					Settings.System.getUriFor("battery_bar_color_auto_full"),
					false, this);

			localContentResolver.registerContentObserver(Settings.System
					.getUriFor("battery_bar_color_auto_charging"), false, this);

			localContentResolver.registerContentObserver(
					Settings.System.getUriFor("battery_bar_auto_color"), false,
					this);

			localContentResolver.registerContentObserver(
					Settings.System.getUriFor("anim_type"), false, this);

			localContentResolver.registerContentObserver(
					Settings.System.getUriFor("anim_pulse_type"), false, this);

			localContentResolver.registerContentObserver(
					Settings.System.getUriFor("battery_bar_anim_on"), false,
					this);

		}

		public void onChange(boolean paramBoolean) {

			updateBatteryBar();
			Log.d("pvymods", "batterybar observed change done");

		}

	}

}
