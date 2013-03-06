package org.opencv.samples.facedetect;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receive extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if (intent.getAction() != null) {
			if ( intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
				Intent s = new Intent(context, FdActivity.class);
				s.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				context.startActivity(s);
			}
		}
	}
}

