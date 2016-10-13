package com.movistar.tvservices.cordova.plugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {  
	
	/*
	 ************************************************************************************************
	 * Overriden Methods 
	 ************************************************************************************************
	 */
	@Override  
	public void onReceive(Context context, Intent intent) {
		
		Intent serviceIntent = new Intent(context, TvServices.class);         
		context.startService(serviceIntent);
	} 
} 
