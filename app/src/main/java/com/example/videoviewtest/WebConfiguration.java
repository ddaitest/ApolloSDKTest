package com.example.videoviewtest;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import android.os.Environment;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.widget.Toast;

public final class WebConfiguration {	
    private String mConfigFile = Environment.getExternalStorageDirectory().toString() + "/test_video_urls.config";
	
	private final List<String> mLines = new ArrayList<String>();
	private boolean mValid = false;

	
	public WebConfiguration() {
        try {
        	BufferedReader bufReader = new BufferedReader(new FileReader(mConfigFile));
        	String line = bufReader.readLine();
        	while (line != null) {
        		mLines.add(line);
            	line = bufReader.readLine();
            }
        	bufReader.close();
    		mValid = true;
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
		}
	}
	
	public boolean isValid() {
		return mValid;
	}
	
	public void toast(Context context) {
		if (isValid()) {
			String msg = "Read " + mConfigFile + " - \n";
			for (String line : mLines) {
				msg += line + "\n";
			}
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(context, "Read " + mConfigFile + " failed.",
					Toast.LENGTH_SHORT).show();
		}
	}
	
	public String[] getLines() {
		return mLines.toArray(new String[mLines.size()]);
	}
}
