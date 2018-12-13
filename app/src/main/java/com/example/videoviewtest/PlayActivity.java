package com.example.videoviewtest;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.UCMobile.Apollo.AutoVideoView;
import com.UCMobile.Apollo.IVideoStatistic;
import com.UCMobile.Apollo.MediaPlayer;
import com.UCMobile.Apollo.SmartMediaPlayer;
import com.UCMobile.Apollo.SmartMediaPlayer.OnCompletionListener;
import com.UCMobile.Apollo.SmartMediaPlayer.OnErrorListener;
import com.UCMobile.Apollo.SmartMediaPlayer.PlayerType;
import com.UCMobile.Apollo.VideoView;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;

class MyStat implements IVideoStatistic
{
	@Override
	public boolean upload(HashMap<String, String> data) {
		Log.d("MyStat", ">>> MyStat upload stat: " + data);
		for (Map.Entry<String, String> entry : data.entrySet())
		{
			Log.d("MyStat", entry.getKey() + "/" + entry.getValue());
		}
		return true;
	}
	
}

public class PlayActivity extends Activity implements MediaPlayer.IRequestExternalValueListener
{
	private static final String TAG = "apolloPlayActivity";
	public static String EXTRA_KEY_COMMAND = "Command";

	public static String EXTRA_KEY_CACHE_IN_CELLULAR = "cache_in_cellular";
	
	public static String EXTRA_VALUE_COMMAND_PLAY = "Play";
	public static String EXTRA_VALUE_COMMAND_AUTO_PLAY = "AutoPlay";
	
	public static String EXTRA_KEY_FILE_PATH = "filePath"; // Play. string
	
	public static String EXTRA_KEY_FILE_LIST = "fileList"; // AutoPlay. stringArray
	
	public static String EXTRA_KEY_FORCE_SOFTWARE = "ForceSoftware"; // Force software, boolean

	public static String EXTRA_KEY_MEDIACODEC = "MediaCodec"; // enable mediacodec
	
	private Timer _autoPlayTimer = new Timer();
	private Timer _fpsTimer = new Timer();
	
	private static PlayNextController _playNextController = null;
	
	private int _currentScaleMode = AutoVideoView.VIDEO_SCALING_MODE_MIN;
	
	private static Logger _filelogger = new Logger();
	
	
	private static MyStat myStat = new MyStat();
	private static HeapChecker mhc = null;
	private static long mHeapBeforePlay = 0;
	private static long mHeapAfterPlay = 0;
	private static long mHeapAlloc = 0;
	private static long mHeapAllocAve = 0;

	private String mCurrentFilePath;
	private VideoView vv;

	private float mCurrentVolume;

	// from IRequestExernalValueListener
	public int getIntValue(int type, String key)
	{
		return 0;
	}
    public float getFloatValue(int type, String key)
    {
    	return 0;
    }
    public String getStringValue(int type, String key)
    {
    	String apollo_str = "ap_cache3=1";
    	return apollo_str;
    }

	class HeapChecker extends java.util.TimerTask {

	    private Timer mTimer = null;
	    private Context ctx = null;
	    private Handler mh = null;
	    private float heap_all = 0;
	    private float count = 0;
	    //Toast toast;

		public HeapChecker(Context c) {
			ctx = c;
			mh = new Handler(Looper.getMainLooper());
			mTimer = new Timer();
			mTimer.schedule(this, 2400,2400); 
		}

		public void cancelTimer() {
			if (mTimer != null) {
				mTimer.cancel();
				mTimer = null;
			}
		}
		@Override
		public void run() {
			mh.post(new Runnable() {
				public void run() {
					long ha = Debug.getNativeHeapAllocatedSize();
					mHeapAlloc = ha/1024;
					heap_all += mHeapAlloc;
					count ++;
					mHeapAllocAve = (int)(heap_all / count);
				}
			});
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, Menu.FIRST+0, 0, "调用清空cache");
        menu.add(Menu.NONE, Menu.FIRST+1, 0, "从实例方法调用清空cache");
        menu.add(Menu.NONE, Menu.FIRST+2, 0, "删除n分钟前的cache");
        menu.add(Menu.NONE, Menu.FIRST+3, 0, "删除到剩余nMB");
        menu.add(Menu.NONE, Menu.FIRST+4, 0, "设置播放器内部的删除缓存过期时间");
        menu.add(Menu.NONE, Menu.FIRST+5, 0, "读取播放器内部的删除缓存过期时间");
        menu.add(Menu.NONE, Menu.FIRST+6, 0, "设置播放器内部的删除缓存后尽可能保留的系统剩余空间");
        menu.add(Menu.NONE, Menu.FIRST+7, 0, "读取播放器内部的删除缓存后尽可能保留的系统剩余空间");
        menu.add(Menu.NONE, Menu.FIRST+8, 0, "还没做：获取当前实例的所有options");
        menu.add(Menu.NONE, Menu.FIRST+9, 0, "还没做：设置当前实例多个options");
        menu.add(Menu.NONE, Menu.FIRST+10, 0, "还没做：获取全局所有options");
        menu.add(Menu.NONE, Menu.FIRST+11, 0, "还没做：设置全局多个options");
        menu.add(Menu.NONE, Menu.FIRST+12, 0, "改变蜂窝网络打开cache的设置");
        menu.add(Menu.NONE, Menu.FIRST+13, 0, "获取当前cache文件大小");
        return true;
	}

	private EditText mPromptTxtInput = null;
	private void prompt(String title, String message, String hint, String okLabel, String cancelLabel, DialogInterface.OnClickListener okListener) {
		mPromptTxtInput = new EditText(this);
		// Set the default text to a link of the Queen
		mPromptTxtInput.setHint(hint);

		new AlertDialog.Builder(this)
			.setTitle(title)
			.setMessage(message)
			.setView(mPromptTxtInput)
			.setPositiveButton(okLabel, okListener)
			.setNegativeButton(cancelLabel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			})
			.show(); 
	}

	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        AutoVideoView videoView = (AutoVideoView)findViewById(R.id.playVideoView1);
        VideoView vv = videoView.getVideoView();

        int ret = 0;

        switch(item.getItemId())
        {
        case Menu.FIRST+0:
        	ret = VideoView.setGlobalOption("rw.global.prune_cache", "true");
        	Toast.makeText(this, "set rw.global.prune_cache=true, return " + ret, Toast.LENGTH_SHORT).show();
            break;
        case Menu.FIRST+1:
        	if (vv != null) {
        		ret = vv.setGlobalOption("rw.global.prune_cache", "true");
        		Toast.makeText(this, "set rw.global.prune_cache=true, return " + ret, Toast.LENGTH_SHORT).show();
        	} else {
        		Toast.makeText(this, "实例为空", Toast.LENGTH_SHORT).show();
        	}
        	
            break;
        case Menu.FIRST+2:
        	prompt("输入框", "要删除几分钟前的cache?", "5", "删除", "取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = mPromptTxtInput.getText().toString();

					int ret = VideoView.setGlobalOption("rw.global.prune_cache_expired", value);
        			Toast.makeText(PlayActivity.this, "set rw.global.prune_cache_expired="+ value +", return " + ret, Toast.LENGTH_SHORT).show();
				}
			});
            break;
        case Menu.FIRST+3:
        	prompt("输入框", "要删除到剩余几MB磁盘空间?", "200", "删除", "取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = mPromptTxtInput.getText().toString();

					int ret = VideoView.setGlobalOption("rw.global.prune_cache_to_free", value);
        			Toast.makeText(PlayActivity.this, "set rw.global.prune_cache_to_free="+ value +", return " + ret, Toast.LENGTH_SHORT).show();
				}
			});
            break;
        case Menu.FIRST+4:
        	prompt("输入框", "要设置内部删除过期时间为几分钟?", "5", "设置", "取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = mPromptTxtInput.getText().toString();

					AutoVideoView videoView = (AutoVideoView)findViewById(R.id.playVideoView1);
		        	VideoView vv = videoView.getVideoView();
		        	if (vv != null) {
		        		int ret = vv.setOption("rw.instance.cache_expire_time", value);
        				Toast.makeText(PlayActivity.this, "set rw.global.cache_expire_time="+ value +", return " + ret, Toast.LENGTH_SHORT).show();
		        	} else {
		        		Toast.makeText(PlayActivity.this, "vv is null", Toast.LENGTH_SHORT).show();
		        	}
				}
			});
            break;
        case Menu.FIRST+5:
        	if (vv != null) {
        		String value = vv.getOption("rw.instance.cache_expire_time");
	        	Toast.makeText(PlayActivity.this, "get rw.instance.cache_expire_time returns " + value, Toast.LENGTH_SHORT).show();
        	} else {
        		Toast.makeText(PlayActivity.this, "vv is null", Toast.LENGTH_SHORT).show();
        	}
            break;
        case Menu.FIRST+6:
        	prompt("输入框", "要设置内部删除到剩余多少MB磁盘空间?", "200", "设置", "取消", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					String value = mPromptTxtInput.getText().toString();

					AutoVideoView videoView = (AutoVideoView)findViewById(R.id.playVideoView1);
		        	VideoView vv = videoView.getVideoView();
		        	if (vv != null) {
		        		int ret = vv.setOption("rw.instance.instance_cache_min_free", value);
        				Toast.makeText(PlayActivity.this, "set rw.global.instance_cache_min_free="+ value +", return " + ret, Toast.LENGTH_SHORT).show();
		        	} else {
		        		Toast.makeText(PlayActivity.this, "vv is null", Toast.LENGTH_SHORT).show();
		        	}
				}
			});
            break;
        case Menu.FIRST+7:
        	if (vv != null) {
        		String value = vv.getOption("rw.instance.instance_cache_min_free");
	        	Toast.makeText(PlayActivity.this, "get rw.instance.instance_cache_min_free returns " + value, Toast.LENGTH_SHORT).show();
        	} else {
        		Toast.makeText(PlayActivity.this, "vv is null", Toast.LENGTH_SHORT).show();
        	}
            break;
        case Menu.FIRST+8:
        	if (vv != null) {
        		Map<String, String> options = vv.getOptions();
	        	if (options != null) {
	        		Toast.makeText(PlayActivity.this, "vv.getOptions returns "+ options.toString(), Toast.LENGTH_SHORT).show();
	        	} else {
	        		Toast.makeText(PlayActivity.this, "vv.getOptions returns null", Toast.LENGTH_SHORT).show();
	        	}
        	} else {
        		Toast.makeText(PlayActivity.this, "vv is null", Toast.LENGTH_SHORT).show();
        	}
            break;
        case Menu.FIRST+9:
	        {
	        	Map<String, String> options = new HashMap<String, String>();
	        	options.put("rw.instance.foo", "bar");
	        	options.put("rw.instance.foo1", "bar1");
	        	if (vv != null) {
	        		ret = vv.setOptions(options);
	        		Toast.makeText(PlayActivity.this, "vv.setOptions returns " + ret, Toast.LENGTH_SHORT).show();
	        	}
	            break;
	        }	        	
        case Menu.FIRST+10:
	        {
	        	Map<String, String> options = VideoView.getGlobalOptions();
	        	if (options != null) {
	        		Toast.makeText(PlayActivity.this, "VideoView.getGlobalOptions returns "+ options.toString(), Toast.LENGTH_SHORT).show();
	        	} else {
	        		Toast.makeText(PlayActivity.this, "VideoView.getGlobalOptions returns null", Toast.LENGTH_SHORT).show();
	        	}
	            break;
	        }
        case Menu.FIRST+11:
	        {
	        	Map<String, String> options = new HashMap<String, String>();
	        	options.put("rw.global.foo", "bar");
	        	options.put("rw.global.foo1", "bar1");
	        	ret = VideoView.setGlobalOptions(options);
	        	Toast.makeText(PlayActivity.this, "VideoView.setOptions returns " + ret, Toast.LENGTH_SHORT).show();
	            break;
	        }
        case Menu.FIRST+12:
	        {
	        	prompt("输入框", "蜂窝网络下是否打开cache?1或者0", "0", "设置", "取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						AutoVideoView videoView = (AutoVideoView)findViewById(R.id.playVideoView1);
		        		VideoView vv = videoView.getVideoView();
						String value = mPromptTxtInput.getText().toString();
						set_cache_in_cellular(vv, value.equals("1"));
					}
				});
				break;
	        }
	    case Menu.FIRST+13:
	        {
	        	String size = VideoView.getGlobalOption("ro.global.cached_file_size=" + mCurrentFilePath);
	        	Toast.makeText(PlayActivity.this, "current file cache size " + size, Toast.LENGTH_SHORT).show();
				break;
	        }
        }

        
        return true;
    }

    private void set_cache_in_cellular(VideoView vv, boolean cache_in_cellular)
    {
    	vv.setOption("rw.instance.cache_in_cellular", cache_in_cellular ? "true" : "false");
		String ret_str = vv.getOption("rw.instance.cache_in_cellular");
		Toast.makeText(PlayActivity.this, "rw.instance.cache_in_cellular returns " + ret_str, Toast.LENGTH_SHORT).show();
    }

	private void set_mediacodec_enable(VideoView vv, boolean enable)
	{
		vv.setOption("rw.instance.ap_hwa_enable", enable ? "1" : "0");
		String ret_str = vv.getOption("rw.instance.ap_hwa_enable");
		Toast.makeText(PlayActivity.this, "rw.instance.ap_hwa_enable returns " + ret_str, Toast.LENGTH_SHORT).show();
	}

	private boolean mSubtitleIsOn = false;
	private View mSubtitleView = null;
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void startSubtitle() {
//		FrameLayout rootLayout = (FrameLayout)findViewById(android.R.id.content);
		FrameLayout play_frame = (FrameLayout)findViewById(R.id.play_frame);

		Map<String, String> v = new HashMap<String, String>();

		// way one: use file path
		String default_file_path = "/sdcard/subtitle.vtt";
		File file = new File(default_file_path);
		if(file.exists()) {
			 v.put("filepath", default_file_path);
		} else {
			// way two: use file content
			String content = "WEBVTT\n" + "Kind: captions\n" + "Language: en\n" + "\n" + "00:00:00.000 --> 00:00:04.270\n" + "内嵌中文0-4.27s\n" + "\n" + "00:00:04.270 --> 00:00:11.680\n" + "内嵌<b>中文</b>4.27s-11.68s\n" + "\n" + "00:00:11.680 --> 00:00:20.990\n" + "内嵌中文11.68s-20.99s\n" + "\n" + "00:00:20.990 --> 00:00:26.980\n" + "中文20.99s-26.98s\n" + "\n" + "00:00:26.980 --> 00:00:33.050\n" + "people realized he was a genius. And Mark\n" + "Rothko did this and people said, My kid could";
			v.put("content", content);
		}
//		v.put("preset_style", "awesome");
		v.put("outline_color", "red");
		v.put("has_outline", "true");
		v.put("foreground_color", "yellow");
		// Supported formats are: #RRGGBB #AARRGGBB or one of the following names: 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'darkgray', 'grey', 'lightgrey', 'darkgrey', 'aqua', 'fuchsia', 'lime', 'maroon', 'navy', 'olive', 'purple', 'silver', 'teal'.
		// if want transparent, use #00000000
		v.put("background_color", "#00000000");
		mSubtitleView = (View)vv.setGeneralOption("ro.instance.start_subtitle", v);

		if (mSubtitleView != null) {
			RelativeLayout.LayoutParams subtitle_params = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.FILL_PARENT, ViewGroup.LayoutParams.FILL_PARENT);
			play_frame.addView(mSubtitleView, subtitle_params);
			mSubtitleIsOn = true;
			CheckBox cbSubtitle = (CheckBox) findViewById(R.id.cb_subtitle);
			cbSubtitle.setChecked(true);
		} else {
			Log.w(TAG, "subtitleView is null");
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void stopSubtitle() {
		Map<String, String> v = new HashMap<String, String>();
		vv.setGeneralOption("ro.instance.stop_subtitle", v);
		mSubtitleIsOn = false;
		CheckBox cbSubtitle = (CheckBox) findViewById(R.id.cb_subtitle);
		cbSubtitle.setChecked(false);
		CheckBox cbSubtitlePause = (CheckBox)findViewById(R.id.cb_subtitle_pause);
		cbSubtitlePause.setChecked(false);

		FrameLayout play_frame = (FrameLayout)findViewById(R.id.play_frame);
		play_frame.removeView(mSubtitleView);
		mSubtitleView = null;
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void pauseSubtitle(boolean is_pause) {
		if (!mSubtitleIsOn) {
			CheckBox cbSubtitlePause = (CheckBox) findViewById(R.id.cb_subtitle_pause);
			cbSubtitlePause.setChecked(false);
			return;
		}
		Map<String, String> v = new HashMap<String, String>();
		v.put("start", is_pause ? "false" : "true");
		vv.setGeneralOption("ro.instance.pause_subtitle", v);
	}

    private void volUp() {
    	if (vv != null) {
    		mCurrentVolume += .1f;
    		if (mCurrentVolume > 1) {
    			mCurrentVolume = 1f;
    		}
			vv.setVolume(mCurrentVolume, mCurrentVolume);
		}
    }

    private void volDown() {
		if (vv != null) {
			mCurrentVolume -= .1f;
    		if (mCurrentVolume < 0f) {
    			mCurrentVolume = 0f;
    		}
			vv.setVolume(mCurrentVolume, mCurrentVolume);
		}
    }

    private void mute() {
		if (vv != null) {
			mCurrentVolume = 0f;
			// vv.setVolume(0f, 0f);
			// alterative
			vv.setGeneralOption("rw.instance.mute", null);
		}
    }

    private void unmute() {
		if (vv != null) {
			mCurrentVolume = 1f;
			// vv.setVolume(1f, 1f);
			// alternative
			Map<String,Float> m = new HashMap<String, Float>();
			m.put("left", Float.parseFloat("1.0"));
			m.put("right", Float.parseFloat("1.0"));
			vv.setGeneralOption("rw.instance.setvolume", m);
		}
    }

	@Override
	protected void onCreate(Bundle savedInstance)
	{
		super.onCreate(savedInstance);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.video_activity);

		mCurrentVolume = 1f;

		// Set VideoView
		AutoVideoView videoView = (AutoVideoView)findViewById(R.id.playVideoView1);
		MediaController mediaController = new MediaController(this);

		registerForContextMenu(findViewById(R.id.tvmenu));
		
		vv = videoView.getVideoView();
		if (vv != null) {
			vv.setExternalValueListener(this);
		}

		Log.d("PlayActivity apollo", "version string " + videoView.getVersionString());

		mediaController.setAnchorView(videoView);
		videoView.setMediaController(mediaController);

        ////// for subtitle
		startSubtitle();

		CheckBox cbSubtitle = (CheckBox) findViewById(R.id.cb_subtitle);
		cbSubtitle.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					startSubtitle();
				} else {
					stopSubtitle();
				}
			}
		});

		CheckBox cbSubtitlePause = (CheckBox) findViewById(R.id.cb_subtitle_pause);
		cbSubtitlePause.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				pauseSubtitle(isChecked);
			}
		});

		_playNextController = null;
		// Set scale switch button
		Button switchScaleButton = (Button)findViewById(R.id.scale_switch_button);
		switchScaleButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) 
			{
			     if ( _currentScaleMode < AutoVideoView.VIDEO_SCALING_MODE_MAX )
			    	_currentScaleMode++;
			     else
			    	_currentScaleMode = AutoVideoView.VIDEO_SCALING_MODE_MIN;
			     
			     _setScaleMode(_currentScaleMode);
			}
		});

		((Button)findViewById(R.id.volup_btn)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v)
			{
				Log.d(TAG, "volup pressed");
				volUp();
			}
		});

		((Button)findViewById(R.id.voldown_btn)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v)
			{
				Log.d(TAG, "voldown pressed");
				volDown();
			}
		});

		((Button)findViewById(R.id.mute_btn)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v)
			{
				Log.d(TAG, "mute pressed");
				mute();
			}
		});

		((Button)findViewById(R.id.unmute_btn)).setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v)
			{
				Log.d(TAG, "unmute pressed");
				unmute();
			}
		});
		
		_setScaleMode(_currentScaleMode);

		//  
		Intent invokingIntent = getIntent();

		boolean cache_in_cellular = invokingIntent.getBooleanExtra(EXTRA_KEY_CACHE_IN_CELLULAR, false);
		Log.e("PlayActivity", "cache_in_cellular from mainactivity is " + cache_in_cellular);

		boolean mediacodec_enable = invokingIntent.getBooleanExtra(EXTRA_KEY_MEDIACODEC, true);
		Log.e("PlayActivity", "mediacodec_enable from mainactivity is " + mediacodec_enable);
		if (vv != null) {
			set_cache_in_cellular(vv, cache_in_cellular);
			set_mediacodec_enable(vv, mediacodec_enable);
		}


		if (invokingIntent.getStringExtra(EXTRA_KEY_COMMAND) != null) {
			if ( invokingIntent.getStringExtra(EXTRA_KEY_COMMAND).equals(EXTRA_VALUE_COMMAND_PLAY) ) {
			   _playFile(invokingIntent.getStringExtra(EXTRA_KEY_FILE_PATH), invokingIntent.getBooleanExtra(EXTRA_KEY_FORCE_SOFTWARE, false) ? PlayerType.R2_PLAYER : PlayerType.NONE);
			}
			else {
			   _autoPlay(invokingIntent.getStringArrayExtra(EXTRA_KEY_FILE_LIST), invokingIntent.getBooleanExtra(EXTRA_KEY_FORCE_SOFTWARE, false) ? PlayerType.R2_PLAYER : PlayerType.NONE);
			}
		} else {
			String action = invokingIntent.getAction();
    		String type = invokingIntent.getType();
    		Bundle extras = getIntent().getExtras();
    		Uri uri = getIntent().getData();
			Log.d(TAG, "common intent, action = " + action + ", type = " + type + ", uri = " + uri);

			if (extras != null) {
				for (String key : extras.keySet()) {
					Object value = extras.get(key);
					Log.d(TAG, String.format("intent extra %s %s (%s)", key,
							value.toString(), value.getClass().getName()));
				}
			}

			if (type != null && type.startsWith("video/")) {
				if (false) {
					/*
					try{
						InputStream is = getContentResolver().openInputStream(uri);
						int avaible = is.available();
						Log.d(TAG, "content file available bytes  " + avaible);

						long skipped = is.skip(100);
						Log.d(TAG, "skipped " + skipped + " markSupported() " + is.markSupported());
						is.reset();
					} catch (FileNotFoundException e) {
						Log.w(TAG, "file not found for content " + e.toString());
					} catch (IOException e) {
						Log.w(TAG, "IOException " + e.toString());
					}
					*/
					try {
						AssetFileDescriptor afd = this.getContentResolver().openAssetFileDescriptor(uri, "r");
					    Log.d(TAG, "content afd is " + afd);

					    // method 1
					    /*InputStream is = afd.createInputStream();
						int avaible = is.available();
						Log.d(TAG, "content file available bytes  " + avaible);

						long skipped = is.skip(100);
						Log.d(TAG, "skipped " + skipped + " markSupported() " + is.markSupported());
						is.reset();*/

						// method 2
						FileDescriptor fd = afd.getFileDescriptor();
						Log.d(TAG, "content fd is " + fd);
						MediaPlayer.testFileDescriptor(fd);

					    afd.close();
					} catch (FileNotFoundException e) {
						Log.w(TAG, "FileNotFoundException " + e.toString());
					} catch (IOException e) {
						Log.w(TAG, "IOException " + e.toString());
					}
					
				} else {
					_playFile(uri.toString(), PlayerType.NONE);  // evernote passes only uri and a extra(is_evernote_premium:true)
				}
			} else {
				Log.w(TAG, "not handled intent");
			}
		}
	    
	}
	
	@Override
	protected void onDestroy()
	{
		Log.d(TAG, "onDestroy playActivity");
		stopSubtitle();
  		AutoVideoView videoView = (AutoVideoView)findViewById(R.id.playVideoView1);
  		videoView.stopPlayback();
  		videoView.suspend();

		_autoPlayTimer.cancel();
		if (_fpsTimer != null) {
			_fpsTimer.cancel();
			_fpsTimer = null;
		}
		if (_fpsTimerHandler != null) _fpsTimerHandler = null;

		vv = null;

		super.onDestroy();

		mHeapAfterPlay = Debug.getNativeHeapAllocatedSize();	
		mHeapAlloc = 0;
		mHeapAllocAve = 0;
		Toast.makeText(getApplicationContext(), "heap alloc after play "+ mHeapAfterPlay/1024+" KB" , Toast.LENGTH_LONG).show();

		Runtime.getRuntime().gc();

		if(mhc != null)
			mhc.cancelTimer();
		mhc=null;
		if(mHeapAfterPlay-mHeapBeforePlay > 1024*1024*2 ){	//2M
			Toast.makeText(getApplicationContext(), "!!! 这个视频可能产生了内存泄漏 ！！！" , Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.setHeaderTitle("Context Menu");
		menu.add(0, menu.FIRST, Menu.NONE, "获取当前httpheader");//.setIcon(R.drawable.menu_item);
		menu.add(0, menu.FIRST+1, Menu.NONE, "获取当前视频的码率");
		menu.add(0, menu.FIRST+2, Menu.NONE, "获取当前视频的格式");
		menu.add(0, menu.FIRST+10, Menu.NONE, "空菜单，可勾选").setCheckable(true);
		menu.add(0, menu.FIRST+11, Menu.NONE, "空菜单").setShortcut('3', '3');
		SubMenu sub = menu.addSubMenu("子菜单");
		sub.add("子菜单项");
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		super.onContextItemSelected(item);
		String s = "null";
		switch(item.getItemId()) {
			case Menu.FIRST:
				s = vv.getOption("ro.instance.info_http_request_headers");
				break;
			case Menu.FIRST + 1:
				s = vv.getOption("ro.instance.info_media_bitrate");
				break;
			case Menu.FIRST + 2:
				s = vv.getOption("ro.instance.info_media_format");
				break;
		}
		Log.d(TAG, "onContextItemSelected " + item.getTitle() + "(len" + s.length() + "): " + s);
		return false;
	}

	private void _playFile(String filePath, PlayerType t)
	{
		if ( filePath == null )
		   return;

		mCurrentFilePath = filePath;
	
		Log.d("PlayActivity", "Playing " + filePath + " now.");
		
		_filelogger.addRecordToLog("Playing " + filePath + " now.");
		
  		AutoVideoView videoView = (AutoVideoView)findViewById(R.id.playVideoView1);
  		videoView.stopPlayback();

		videoView.setPlayerType(t);

		videoView.setStatisticHelper(myStat);
		
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.0.3; ko-kr; LG-L160L Build/IML74K) AppleWebkit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
		headers.put("Cookie", "key=xxx");
		
		mHeapBeforePlay = Debug.getNativeHeapAllocatedSize();
		mHeapAlloc = 0;
		mHeapAllocAve = 0;
		mhc = new HeapChecker(getApplicationContext());
		Toast.makeText(getApplicationContext(), "heap alloc before play "+ mHeapBeforePlay/1024+" KB" , Toast.LENGTH_SHORT).show();
		
		if ( filePath.startsWith("http") )
		   videoView.setVideoURI(Uri.parse(filePath), headers);
		else
		   videoView.setVideoPath(filePath);
		
		videoView.setOnCompletionListener(new OnCompletionListener() {
			public void onCompletion(SmartMediaPlayer mp)
			{
				// _fpsTimer.cancel();
				Toast.makeText(PlayActivity.this, "onCompletion", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "onCompletion");
				if(mhc != null)
					mhc.cancelTimer();
				mhc=null;
				PlayActivity.this._filelogger.addRecordToLog("onCompletion");
				if (PlayActivity.this._playNextController != null) PlayActivity.this._playNextController.playNext();
				//finish();
			}
		});
		
		videoView.setOnErrorListener(new OnErrorListener() {
			
			@Override
			public boolean onError(SmartMediaPlayer smp, int what, int extra) {
				// TODO Auto-generated method stub
				Toast.makeText(PlayActivity.this, "onError " + what + "," + extra, Toast.LENGTH_SHORT).show();
				Log.d(TAG, "onError " + what + "," + extra);
				PlayActivity.this._filelogger.addRecordToLog("onError " + what);
				if (PlayActivity.this._playNextController != null) PlayActivity.this._playNextController.playNext();
				//finish();
				return false;
			}
		});

  		VideoView vv = videoView.getVideoView();
  		if (vv != null) {
  			Map<String, String> options = new HashMap<String, String>();
  			options.put("rw.global.ap_seek_buf", "1000");
  			options.put("rw.instance.abc", "test");
  			// options.put("rw.instance.ap_cache3", "0"); // diable cache.
  			vv.setOptions(options);
  		}
		
		videoView.start();
		
		Log.d("TEST", vv.getOption("rw.global.ap_seek_buf"));
		Log.d("TEST", vv.getOption("rw.instance.abc"));
		
		_fpsTimer.schedule(new FPSTimerTask(), 0, 1000);
		
	}
	
	private void _autoPlay(String [] filePaths, PlayerType t)
	{
		if ( filePaths == null || filePaths.length < 1 )
		   return;
	
		//_autoPlayTimer.schedule(new AutoPlayTimerTask(filePaths, forceSoftware), 0, 10000);
		
		_playNextController = new PlayNextController(filePaths, t);
		_playNextController.playNext();
	}
	
	class PlayMessage {
		public String[] _filePaths;
		public PlayerType _playerType;
	}

	class PlayNextController {
		
		public PlayNextController(String [] filePaths, PlayerType t)
		{
			   _filePaths = filePaths;
			   _currentIndex = 0;
			   _playerType = t;
		}
		
		public void playNext()
		{
			Message msg = PlayActivity.this._autoPlayHandler.obtainMessage();
			msg.arg1 = _currentIndex++;

			PlayMessage pm = new PlayMessage();
			pm._filePaths = _filePaths;
			pm._playerType = _playerType;

			msg.obj = pm;
			
			if ( _currentIndex >= _filePaths.length )
			   _currentIndex = 0;
				
			PlayActivity.this._autoPlayHandler.sendMessage(msg);
		}
		
		private String [] _filePaths = null;
		private int _currentIndex = 0;
		private PlayerType _playerType = PlayerType.NONE;
		
	} // class PlayNextController

	
	private Handler _autoPlayHandler = new Handler() {
		public void handleMessage(Message msg) {
			PlayMessage pm = (PlayMessage)msg.obj;
			   
			   // Switch VideoView here
			   // _updateVideoView();
			   
			   Toast.makeText(PlayActivity.this, "play file " + pm._filePaths[msg.arg1], Toast.LENGTH_SHORT).show();
			   Log.d("VideoViewTest", "play file " + pm._filePaths[msg.arg1]);
			   // Play
			   _playFile(pm._filePaths[msg.arg1], pm._playerType);
		}
	}; 
	
	class AutoPlayTimerTask extends TimerTask {
	
		public AutoPlayTimerTask(String [] filePaths, boolean forceSoftware)
		{
			   _filePaths = filePaths;
			   _currentIndex = 0;
			   _forceSoftware = forceSoftware;
		}
		
		@Override
		public void run()
		{
			Message msg = PlayActivity.this._autoPlayHandler.obtainMessage();
			msg.arg1 = _currentIndex++;
			msg.arg2 = _forceSoftware ? 1 : 0;
			msg.obj = _filePaths;
			
			if ( _currentIndex >= _filePaths.length )
			   _currentIndex = 0;
				
			PlayActivity.this._autoPlayHandler.sendMessage(msg);
		}
		
		private String [] _filePaths = null;
		private int _currentIndex = 0;
		private boolean _forceSoftware = false;
		
	} // class AutoPlayTimerTask

	
	private void _setScaleMode(int scaleMode)
	{
		AutoVideoView videoView = (AutoVideoView)findViewById(R.id.playVideoView1);
		Log.d("PlayActivity apollo", "version string " + videoView.getVersionString());
		videoView.setVideoScalingMode(scaleMode);
	}
	
	float maxFps;
	
	private void _updateFPS()
	{
		TextView fpsTextView = (TextView)findViewById(R.id.fps_text);
		AutoVideoView videoView = (AutoVideoView)findViewById(R.id.playVideoView1);
		
		float fps = videoView.getFPS();
		float averFps = videoView.getAverageFPS();
		if(fps > maxFps)
			maxFps = fps;
		
		fpsTextView.setTextSize(10);
		fpsTextView.setText(
				"fps:" + Float.valueOf(fps).toString() + "\n"
				+ "ave_fps:" + Float.valueOf(averFps).toString() + "\n"
				+ "maxFps:" + Float.valueOf(maxFps).toString() + "\n"
				+ "heap:" + mHeapAlloc+ " KB" + "\n"
				+ "heap_ave:" + mHeapAllocAve +" KB"
				);
	}
	
	class FPSTimerHandler extends Handler {
	
		private PlayActivity _playActivity = null;
		
		public FPSTimerHandler(PlayActivity playActivity)
		{
			_playActivity = playActivity;
		}
		
		public void handleMessage(Message msg)
		{
			_playActivity._updateFPS();
		}
	} // class FPSTimerHandler
	
	private FPSTimerHandler _fpsTimerHandler = new FPSTimerHandler(this);
	
	// class FpsTimerTask
	class FPSTimerTask extends TimerTask {
		
		@Override
		public void run()
		{
			Message msg = PlayActivity.this._fpsTimerHandler.obtainMessage();
			PlayActivity.this._fpsTimerHandler.sendMessage(msg);
		}
		
	} // class FPSTimerTaskfinal 
}
