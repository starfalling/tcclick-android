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
    	com.truecolor.tcclick.TCClick.onResume(this);
    	com.truecolor.tcclick.TCClick.event("测试", "测试", "测试");
    }
    
    public void onPause(){
    	super.onPause();
    	com.truecolor.tcclick.TCClick.onPause(this);
    	throw new RuntimeException("Test");
    }
    
    public void onDesstroy(){
    	super.onDestroy();
    }
}