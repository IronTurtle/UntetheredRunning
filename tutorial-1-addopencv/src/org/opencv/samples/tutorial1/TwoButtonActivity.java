package org.opencv.samples.tutorial1;

import org.opencv.samples.tutorial1.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class TwoButtonActivity extends Activity {

	Button startBtn;
	Button stopBtn;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_two_button);
		
		startBtn = (Button) findViewById(R.id.buttonStart);
		stopBtn = (Button)findViewById(R.id.buttonStop);
		
	}

	public void startAppOnClick(View view) {
		Intent intent = new Intent(getApplicationContext(), Sample1Java.class);
		this.startActivityForResult(intent, 200);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_two_button, menu);
		return true;
	}

}
