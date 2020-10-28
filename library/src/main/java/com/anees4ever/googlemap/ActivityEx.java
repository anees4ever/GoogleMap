package com.anees4ever.googlemap;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

public class ActivityEx extends Activity {
	public static Context appContext;
	
	public Context context;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		appContext= appContext==null?getApplicationContext():appContext;
		context= this;
	}

	public static void ToastIt(String message) {
		ActivityEx.ToastIt(appContext, message);
	}

	public static void ToastIt(Context context, String message) {
		try {
			Toast.makeText(context, message, Toast.LENGTH_LONG).show();
		} catch(Exception e1) {
			Log.d("ToastIt", e1.toString());
			try {
				Log.d("ToastIt", "Trying Method2");
				Toast.makeText(ActivityEx.appContext, message, Toast.LENGTH_LONG).show();
			} catch(Exception e2) {
				Log.d("ToastIt", e2.toString());
				Log.d("ToastIt:Message", message);
			}
		}
	}

	public static boolean BlockOtherCalls= false;
	public void showFocus(View view) {
		try {
			showFocus(context, view);
		} catch(Exception e) {
			Log.e("showFocus", e.toString());
		}
	}
	public static void showFocus(Context context, View view) {
		if(BlockOtherCalls) {
			BlockOtherCalls= false;
			return;
		}
		try {
			view.requestFocus();
			InputMethodManager ipmm= (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			ipmm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
		} catch(Exception e) {
			Log.e("showFocus", e.toString());
		}
	}

	public void hideFocus(View view) {
		try {
			hideFocus(context, view);
		} catch(Exception e) {
			Log.e("hideFocus", e.toString());
		}
	}
	public static void hideFocus(Context context, View view) {
		if(BlockOtherCalls) {
			BlockOtherCalls= false;
			return;
		}
		try {
			IBinder token= ((Activity) context).getCurrentFocus()==null?null:((Activity) context).getCurrentFocus().getWindowToken();
			hideFocus(context, token);
		} catch(Exception e) {
			Log.e("hideFocus", e.toString());
		}
		try {
			hideFocus(context, view==null?null:view.getWindowToken());
		} catch(Exception e) {
			Log.e("hideFocus", e.toString());
		}
	}
	public static void hideFocus(Context context, IBinder token) {
		try {
			if(token==null) {
				return;
			}
			InputMethodManager ipmm= (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
			ipmm.hideSoftInputFromWindow(token, 0);
		} catch(Exception e) {
			Log.e("hideFocus", e.toString());
		}
	}
}
