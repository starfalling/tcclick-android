package com.truecolor.tcclick;

import android.app.Activity;
import android.os.Bundle;

public class TCClickActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }
    
    public void onResume(){
    	super.onResume();
    	TCClick.onResume(this);
    }
    
    public void onPause(){
    	super.onPause();
    	TCClick.onPause(this);
    	throw new RuntimeException("Test");
    }
    
    public void onDesstroy(){
    	super.onDestroy();
    }
}