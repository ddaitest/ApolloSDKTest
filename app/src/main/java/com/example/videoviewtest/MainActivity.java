package com.example.videoviewtest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.lang.Float;

// import com.UCMobile.Apollo.Apollo;
import com.UCMobile.Apollo.MediaPlayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.ContextWrapper;

import java.lang.reflect.Field;

public class MainActivity extends Activity {

    private static final String TAG = "apollo_test";
    private boolean mOptionCacheInCellular;

    private TestServiceClient mTestServiceClient;

    private void startIntentByParam(Uri uri) {

        Log.i(TAG, uri.toString());

        String extraSoftware = getIntent().getStringExtra("software");
        Boolean isSoftWare = true;
        if (extraSoftware != null)
            isSoftWare = extraSoftware.equals("true");
        Log.i(TAG, "" + isSoftWare);
        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
        intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
        intent.putExtra(PlayActivity.EXTRA_KEY_COMMAND,
                PlayActivity.EXTRA_VALUE_COMMAND_PLAY);
        intent.putExtra(PlayActivity.EXTRA_KEY_FILE_PATH, uri.toString().trim());
        intent.putExtra(PlayActivity.EXTRA_KEY_FORCE_SOFTWARE, isSoftWare);
        startActivity(intent);

    }

    private boolean shouldUseTextView() {
        //return false;
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(Menu.NONE, Menu.FIRST + 1, 0, "设置在蜂窝网络下的cache");
        menu.add(Menu.NONE, Menu.FIRST + 2, 0, "清空cache");
        menu.add(Menu.NONE, Menu.FIRST + 3, 0, "设置私有目录（影响配置文件和cacheidx目录）");
        menu.add(Menu.NONE, Menu.FIRST + 4, 0, "设置cache dir");
        menu.add(Menu.NONE, Menu.FIRST + 5, 0, "VIDEO S3");
        menu.add(Menu.NONE, Menu.FIRST + 6, 0, "VIDEO imgaws");
        menu.add(Menu.NONE, Menu.FIRST + 7, 0, "VIDEO img");
        menu.add(Menu.NONE, Menu.FIRST + 8, 0, "VIDEO img-01");
        menu.add(Menu.NONE, Menu.FIRST + 9, 0, "VIDEO img-03");
        // menu.add(Menu.NONE, Menu.FIRST+5, 0, "查看当前预设值");
        // menu.add(Menu.NONE, Menu.FIRST+6, 0, "预设音量");
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

    private Field getField(String class_sig, String field_name) {
		/*try {
			Class<?> clazz = Class.forName(class_sig);  
			Field[] declaredFields = clazz.getDeclaredFields();
			List<Field> staticFields = new ArrayList<Field>();
			for (Field field : declaredFields) {
			    if (java.lang.reflect.Modifier.isStatic(field.getModifiers())) {
			        staticFields.add(field);
			        Log.d(TAG, "static field " + field.getName());
			    }
			}
		} catch (ClassNotFoundException e) {  
			// TODO Auto-generated catch block
			Log.d(TAG, "exception " + e.toString());
			e.printStackTrace();  
		} catch (IllegalArgumentException e) {  
			// TODO Auto-generated catch block
			Log.d(TAG, "exception " + e.toString());
			e.printStackTrace();  
		}*/

        try {
            Class<?> clazz = Class.forName(class_sig);
            Field field = clazz.getDeclaredField(field_name);
            field.setAccessible(true);
            return field;
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "exception " + e.toString());
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            Log.d(TAG, "exception " + e.toString());
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            Log.d(TAG, "exception " + e.toString());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        Toast.makeText(MainActivity.this, "onOptionsItemSelected " + item.getItemId(), Toast.LENGTH_SHORT).show();

        switch (item.getItemId()) {
            case Menu.FIRST + 5:
                urlEditText.setText("https://s3.ap-south-1.amazonaws.com/img-welike-in/mark_480P_16494645.m3u8.mp4");
                break;
            case Menu.FIRST + 6:
                urlEditText.setText("https://imgaws.welike.in/mark_480P_16494645.m3u8.mp4");
                break;
            case Menu.FIRST + 7:
                urlEditText.setText("https://img.welike.in/video-34f462ae45274b6189278138b35a5ae0.mp4");
                break;
            case Menu.FIRST + 8:
                urlEditText.setText("https://img-01.welike.in/video-34f462ae45274b6189278138b35a5ae0.mp4");
                break;
            case Menu.FIRST + 9:
                urlEditText.setText("https://img-03.welike.in/video-34f462ae45274b6189278138b35a5ae0.mp4");
                break;
            case Menu.FIRST + 1:
                prompt("输入框", "蜂窝网络下是否打开cache?1或者0", "0", "设置", "取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = mPromptTxtInput.getText().toString();
                        if (value.equals("1")) {
                            mOptionCacheInCellular = true;
                        } else {
                            mOptionCacheInCellular = false;
                        }
                    }
                });
                break;
            case Menu.FIRST + 2: {
                int ret = MediaPlayer.setGlobalOption("rw.global.prune_cache", "true");
                Toast.makeText(this, "set rw.global.prune_cache=true, return " + ret, Toast.LENGTH_SHORT).show();
                break;
            }
            case Menu.FIRST + 3: {
                prompt("输入框", "设置私有目录(目前要求存在)", "/sdcard/private", "设置", "取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = mPromptTxtInput.getText().toString();
                        ContextWrapper c = new ContextWrapper(MainActivity.this);
                        try {
                            MediaPlayer._nativeSetContextAndFilesPath(MainActivity.this, value);
                        } catch (UnsatisfiedLinkError ufe) {
                            Log.w(TAG, "_nativeSetContextAndFilesPath method not found." + ufe);
                        }
                    }
                });
                break;
            }
            case Menu.FIRST + 4: {
                prompt("输入框", "设置cache dir", "/sdcard/apolloCache", "设置", "取消", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String value = mPromptTxtInput.getText().toString();
                        String key = "rw.global.cache_dir";
                        MediaPlayer.setGlobalOption(key, value);
                    }
                });
                break;
            }
	        /*case Menu.FIRST+5:
	        {
	        	Field leftVolumeField = getField("com.UCMobile.Apollo.MediaPlayer", "_pendingLeftVolume");
				float leftValue = -1;
				float rightValue = -1;
				if (leftVolumeField != null) {
					try {
						leftValue = leftVolumeField.getFloat(null);
					} catch (IllegalAccessException e) {
						Log.w(TAG, "exception " + e.toString());
					}
				}
				Field rightVolumeField = getField("com.UCMobile.Apollo.MediaPlayer", "_pendingRightVolume");
				if (rightVolumeField != null) {
					try {
						rightValue = rightVolumeField.getFloat(null);
					} catch (IllegalAccessException e) {
						Log.w(TAG, "exception " + e.toString());
					}
				}
				Toast.makeText(MainActivity.this, "get preset volume left:" + leftValue + " right:" + rightValue, Toast.LENGTH_SHORT).show();
	            break;
	        }
	        case Menu.FIRST+6:
	        {
	        	prompt("输入框", "预设音量(下次播放生效,0静音,1全开)", "0", "设置", "取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Float value = Float.parseFloat(mPromptTxtInput.getText().toString());
						Log.d(TAG, "get preset volume value " + value);
						Field leftVolumeField = getField("com.UCMobile.Apollo.MediaPlayer", "_pendingLeftVolume");
						if (leftVolumeField != null) {
							try {
								Log.d(TAG, "leftVolumeField value before " + leftVolumeField.getFloat(null));
								leftVolumeField.setFloat(null, value);
								Log.d(TAG, "leftVolumeField value after " + leftVolumeField.getFloat(null));
							} catch (IllegalAccessException e) {
								Log.w(TAG, "exception " + e.toString());
							}
						}
						Field rightVolumeField = getField("com.UCMobile.Apollo.MediaPlayer", "_pendingRightVolume");
						if (rightVolumeField != null) {
							try {
								Log.d(TAG, "rightVolumeField value before " + rightVolumeField.getFloat(null));
								rightVolumeField.setFloat(null, value);
								Log.d(TAG, "rightVolumeField value after " + rightVolumeField.getFloat(null));
							} catch (IllegalAccessException e) {
								Log.w(TAG, "exception " + e.toString());
							}
						}
					}
				});
	            break;
	        }*/
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        //mTestServiceClient.doUnbindService();
        //DownloaderServiceClient.getInstance(this).unBindService();
        super.onDestroy();
    }


    private int verToNum(int x, int y, int z) {
        return ((x & 0x7) << 28) | ((y & 0x3ff) << 18) | (z & 0x3ffff);
    }

    private int verToNum(String ver) {
        String[] parts = ver.split("\\.");
        if (parts.length != 3) {
            Log.d(TAG, "ver " + ver + " not compose with 3 component. fail");
            return 0;
        }
        int x = Integer.parseInt(parts[0]);
        int y = Integer.parseInt(parts[1]);
        int z = Integer.parseInt(parts[2]);
        return verToNum(x, y, z);
    }

    private void testVersion() {
        String ver1 = "2.8.5";
        int num_ver1 = verToNum(ver1);
        Log.d(TAG, "ver1 " + ver1 + " num_ver1 is " + num_ver1);

        String ver2 = "2.9.5";
        int num_ver2 = verToNum(ver2);
        Log.d(TAG, "ver2 " + ver2 + " num_ver2 is " + num_ver2);

        assert (num_ver2 > num_ver1);
    }

    private void testCacheDir() {
        String key = "rw.global.cache_dir";
        String value = MediaPlayer.getGlobalOption(key);
        Log.d(TAG, "testCacheDir getGlobalOption key " + key + " value " + value);

        MediaPlayer mp = new MediaPlayer(this);
        value = mp.getOption(key);
        Log.d(TAG, "testCacheDir getOption key " + key + " value " + value);

        MediaPlayer.setGlobalOption(key, "/sdcard/apolloCache");
    }

    EditText urlEditText;
    TextView tv1,tv2,tv3,tv4,tv5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mOptionCacheInCellular = false;

        // Apollo.setLoadLibraryFromAppLibPath(true);
        // Apollo.updateAppLibPath(this);
        //load lib immediately so we can debug before playing.
        MediaPlayer.globalInitialization(this);
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        if (false) {
            mTestServiceClient = new TestServiceClient(this);
            mTestServiceClient.doBindService();
        }

        // Log.d(TAG, "Apollo.getVersion() return " + Apollo.getVersion());
        Log.d(TAG, "MediaPlayer.getVersionString() return " + MediaPlayer.getVersionString());
        Log.d(TAG, "MediaPlayer.getApiVersion() return " + MediaPlayer.getApiVersion());

        testVersion();
        // testCacheDir();

        //set uri and extra param&value
        Uri uri = getIntent().getData();
        if (uri != null)
            startIntentByParam(uri);

        // Set URL edit
        urlEditText = (EditText) findViewById(R.id.url_field);
        tv1 = findViewById(R.id.url1);
        tv2 = findViewById(R.id.url2);
        tv3 = findViewById(R.id.url3);
        tv4 = findViewById(R.id.url4);
        tv5 = findViewById(R.id.url5);
        tv1.setText("https://s3.ap-south-1.amazonaws.com/img-welike-in/mark_480P_16494645.m3u8.mp4");
        tv2.setText("https://imgaws.welike.in/mark_480P_16494645.m3u8.mp4");
        tv3.setText("https://img.welike.in/video-34f462ae45274b6189278138b35a5ae0.mp4");
        tv4.setText("https://img-01.welike.in/video-34f462ae45274b6189278138b35a5ae0.mp4");
        tv5.setText("https://img-03.welike.in/video-34f462ae45274b6189278138b35a5ae0.mp4");
        tv1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(tv1.getText().toString());
            }
        });
        tv2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(tv2.getText().toString());
            }
        });
        tv3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(tv3.getText().toString());
            }
        });
        tv4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(tv4.getText().toString());
            }
        });
        tv5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                play(tv5.getText().toString());
            }
        });

        urlEditText.setText("");
//        urlEditText.setText("https://r1---sn-5pguxacq-q5je.googlevideo.com/videoplayback?lmt1429513050650448&dur81.571&keyyt6&ip103.25.250.187&signature58DF200DE27006A5341CD0DB53AF1804CBF3973A.C80EC70A2CF14885C6B36E8F3237332FBA2A83E3&itag18&ido-AKls-iuCaoIWAbE0O16KNWxs75C_Y2U5gnDFXIH");
        // urlEditText.setText("http://100.84.35.173:8080/t1Test_500k/super/index.m3u8");
        //urlEditText.setText("http://pl.youku.com/playlist/m3u8?vid=356840185&type=mp4&ts=1451483155&keyframe=0&ep=eiaRG02FVc8F4CTbiT8bYiq2dnFeXP0P8BeBgdNqBdQmQO62&sid=84514831557411256d26a&token=2869&ctype=12&ev=1&oip=244858953");
        //urlEditText.setText("http://pl.youku.com/playlist/m3u8?ts=1451389752&keyframe=0&vid=XMTQyODMwOTg0MA==&type=mp4&r=/3sLngL0Q6CXymAIiF9JUfR5MDecwxp/gSVk/o8apWJ3KUkaGrqktKh7cO9ZZoqYN5iGQUM9dNrj6YzDV+fl4Ojn+ylzMvuNIjNMLNAk7kLfzn4WXk/wjulKJsKt1jTGRWU/E6WoAv1iuQ4racC3f/cRWL+h4c7lh2D5a5TeR6Q4J7r/Rnw6/1o05uZxG8BS&oip=738103351&sid=44513897524752007f23e&token=1907&did=9e07af0e81bab3129afcb614de22673c&ev=1&ctype=20&ep=8KY5bm4jLPbm3IonQ1%2F4xHbYCoXHbXYvz78pHS%2FgyKo6nlH4fcJJTNkwQfBvi40W");
        //urlEditText.setText("http://119.147.178.71/161/17/24/letv-uts/14/ver_00_22-1015165368-avc-420407-aac-32039-102600-5890160-dc5485b0e58783e68e3d8e0d99c153ca-1451357626810.m3u8?crypt=7aa7f2e137&b=458&nlh=3072&nlt=45&bf=24&p2p=1&video_type=mp4&termid=2&tss=ios&geo=CN-19-246-1&platid=3&splatid=304&its=0&qos=4&fcheck=0&mltag=1&proxy=2006169159,244864603,3683272586&uid=244855964.rp&keyitem=GOw_33YJAAbXYE-cnQwpfLlv_b2zAkYctFVqe5bsXQpaGNn3T1-vhw..&ntm=1451583000&nkey=a553e4efb437b227218cdfbed30f5da9&nkey2=9caf9460fbb3de47ae2381eaa605aaad&p1=0&payff=0&ostype=android&cvid=621117487230&g3proxy_tm=1451564400&playid=0&vid=24295058&g3proxy_ercode=0&hwtype=un&uip=14.152.64.76&vtype=13&tm=1451564414&mmsid=40654791&uuid=1564413445397367&p2=06&key=06961cf06047fbe5ff439fa99a41de85&errc=0&gn=831&buss=4701&cips=14.152.64.76");   

        urlEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_NEXT || actionId == EditorInfo.IME_ACTION_DONE) {

                    Log.d(TAG, "onEditorAction mOptionCacheInCellular is " + mOptionCacheInCellular);

                    String url = v.getText().toString();
                    if (url.trim().length() > 0) {
                        CheckBox mediaplayerCheckBox = (CheckBox) findViewById(R.id.mediaplayer_checkbox);
                        CheckBox mediacodecCheckBox = (CheckBox) findViewById(R.id.mediacodec_checkbox);

                        if (mediaplayerCheckBox.isChecked() == false) {
                            CheckBox forceSoftwareCheckBox = (CheckBox) findViewById(R.id.force_software_checkbox);
                            Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                            intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                            intent.putExtra(PlayActivity.EXTRA_KEY_COMMAND, PlayActivity.EXTRA_VALUE_COMMAND_PLAY);
                            intent.putExtra(PlayActivity.EXTRA_KEY_FILE_PATH, url.trim());
                            intent.putExtra(PlayActivity.EXTRA_KEY_FORCE_SOFTWARE, forceSoftwareCheckBox.isChecked());
                            intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                            startActivity(intent);
                        } else {
                            if (shouldUseTextView()) {
                                Intent intent = new Intent(getApplicationContext(), TextureViewPlayActivity.class);
                                intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                                intent.putExtra(TextureViewPlayActivity.EXTRA_KEY_FILE_PATH, url.trim());
                                intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                                startActivity(intent);
                            } else {
                                Intent intent = new Intent(getApplicationContext(), SurfaceViewPlayActivity.class);
                                intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                                intent.putExtra(SurfaceViewPlayActivity.EXTRA_KEY_FILE_PATH, url.trim());
                                intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                                startActivity(intent);
                            }
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        // Set List View
        ListView filesListView = (ListView) findViewById(R.id.files_listview);

        WebConfiguration testUrlsConfig = new WebConfiguration();
        if (testUrlsConfig.isValid()) {
            String[] testUrls = testUrlsConfig.getLines();
            filesListView.setAdapter(new ArrayAdapter<String>(this,
                    android.R.layout.simple_list_item_1, testUrls));

            filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CheckBox mediaplayerCheckBox = (CheckBox) findViewById(R.id.mediaplayer_checkbox);
                    CheckBox mediacodecCheckBox = (CheckBox) findViewById(R.id.mediacodec_checkbox);
                    ListView filesListView = (ListView) findViewById(R.id.files_listview);
                    ListAdapter listAdapter = filesListView.getAdapter();
                    String url = (String) listAdapter.getItem(position);

                    if (mediaplayerCheckBox.isChecked() == false) {
                        CheckBox forceSoftwareCheckBox = (CheckBox) findViewById(R.id.force_software_checkbox);

                        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                        intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                        intent.putExtra(PlayActivity.EXTRA_KEY_COMMAND, PlayActivity.EXTRA_VALUE_COMMAND_PLAY);
                        intent.putExtra(PlayActivity.EXTRA_KEY_FILE_PATH, url);
                        intent.putExtra(PlayActivity.EXTRA_KEY_FORCE_SOFTWARE, forceSoftwareCheckBox.isChecked());
                        intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                        startActivity(intent);
                    } else {
                        if (shouldUseTextView()) {
                            Intent intent = new Intent(getApplicationContext(), TextureViewPlayActivity.class);
                            intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                            intent.putExtra(TextureViewPlayActivity.EXTRA_KEY_FILE_PATH, url);
                            intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), SurfaceViewPlayActivity.class);
                            intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                            intent.putExtra(SurfaceViewPlayActivity.EXTRA_KEY_FILE_PATH, url);
                            intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                            startActivity(intent);
                        }
                    }
                }
            });
        } else {
            FileListAdapter fileListAdapter = new FileListAdapter(this, new File(Environment.getExternalStorageDirectory().toString() /*+ "u3player_test_video"*/));
            filesListView.setAdapter(fileListAdapter);
            fileListAdapter.reload();

            filesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    CheckBox mediaplayerCheckBox = (CheckBox) findViewById(R.id.mediaplayer_checkbox);
                    CheckBox mediacodecCheckBox = (CheckBox) findViewById(R.id.mediacodec_checkbox);
                    ListView filesListView = (ListView) findViewById(R.id.files_listview);
                    ListAdapter listAdapter = filesListView.getAdapter();
                    File file = (File) listAdapter.getItem(position);

                    Log.e("PlayActivity", "mediacodecCheckBox isChecked is " + mediacodecCheckBox.isChecked());
                    if (mediaplayerCheckBox.isChecked() == false) {
                        CheckBox forceSoftwareCheckBox = (CheckBox) findViewById(R.id.force_software_checkbox);

                        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                        intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                        intent.putExtra(PlayActivity.EXTRA_KEY_COMMAND, PlayActivity.EXTRA_VALUE_COMMAND_PLAY);
                        intent.putExtra(PlayActivity.EXTRA_KEY_FILE_PATH, file.getAbsolutePath());
                        intent.putExtra(PlayActivity.EXTRA_KEY_FORCE_SOFTWARE, forceSoftwareCheckBox.isChecked());
                        intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                        startActivity(intent);
                    } else {
                        if (shouldUseTextView()) {
                            Intent intent = new Intent(getApplicationContext(), TextureViewPlayActivity.class);
                            intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                            intent.putExtra(TextureViewPlayActivity.EXTRA_KEY_FILE_PATH, file.getAbsolutePath());
                            intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), SurfaceViewPlayActivity.class);
                            intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                            intent.putExtra(SurfaceViewPlayActivity.EXTRA_KEY_FILE_PATH, file.getAbsolutePath());
                            intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                            startActivity(intent);
                        }
                    }
                }
            });
        }

        Button monkeyButton = (Button) findViewById(R.id.monkey_button);
        monkeyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MediaPlayerMonkeyActivity.class);
                String url = "http://videos-cdn.mozilla.net/serv/webmademovies/Moz_Doc_0329_GetInvolved_ST.webm";
                intent.putExtra(MediaPlayerMonkeyActivity.EXTRA_KEY_FILE_PATH, url);
                startActivity(intent);
            }
        });

        // Set auto play button
        Button autoPlayButton = (Button) findViewById(R.id.autoplay_button);
        autoPlayButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {


//				ListView filesListView = (ListView)findViewById(R.id.files_listview);
//				FileListAdapter fileListAdapter = (FileListAdapter)filesListView.getAdapter();
//				CheckBox forceSoftwareCheckBox = (CheckBox)findViewById(R.id.force_software_checkbox);

//				String [] files = new String[fileListAdapter.getCount()];
//				for ( int i = 0; i < fileListAdapter.getCount(); i++ ) {
//					File file = (File)fileListAdapter.getItem(i);
//					files[i] = file.getAbsolutePath();
//				}
//				
//				String lineFilename = "/sdcard/line.txt";
//				Toast.makeText(MainActivity.this, "read " + lineFilename, Toast.LENGTH_SHORT).show();

                List<String> lineList = new ArrayList<String>();
                lineList.add(urlEditText.getText().toString());


                if (lineList.size() == 0) {
                    Toast.makeText(MainActivity.this, "no file to auto play", Toast.LENGTH_LONG).show();
                } else {
                    String[] files = new String[lineList.size()];
                    lineList.toArray(files);

                    CheckBox mediaplayerCheckBox = (CheckBox) findViewById(R.id.mediaplayer_checkbox);
                    CheckBox mediacodecCheckBox = (CheckBox) findViewById(R.id.mediacodec_checkbox);
                    Log.e("PlayActivity", "mediacodec_enable from mainactivity is " + mediacodecCheckBox.isChecked());

                    if (mediaplayerCheckBox.isChecked() == false) {
                        Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                        intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                        intent.putExtra(PlayActivity.EXTRA_KEY_COMMAND, PlayActivity.EXTRA_VALUE_COMMAND_AUTO_PLAY);
                        intent.putExtra(PlayActivity.EXTRA_KEY_FILE_LIST, files);
//    					intent.putExtra(PlayActivity.EXTRA_KEY_FORCE_SOFTWARE, forceSoftwareCheckBox.isChecked());
                        intent.putExtra(PlayActivity.EXTRA_KEY_FORCE_SOFTWARE, true);
                        intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());

                        startActivity(intent);
                    } else {
                        if (shouldUseTextView()) {
                            Intent intent = new Intent(getApplicationContext(), TextureViewPlayActivity.class);
                            intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                            intent.putExtra(TextureViewPlayActivity.EXTRA_KEY_FILE_PATH, files[0]);
                            intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                            startActivity(intent);
                        } else {
                            Intent intent = new Intent(getApplicationContext(), SurfaceViewPlayActivity.class);
                            intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                            intent.putExtra(SurfaceViewPlayActivity.EXTRA_KEY_FILE_PATH, files[0]);
                            intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                            startActivity(intent);
                        }
                    }
                }
            }
        });


        Button musicButton = (Button) findViewById(R.id.music_btn);
        musicButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MusicActivity.class);
                startActivity(intent);
            }
        });

    }


    private void play(String url){
        List<String> lineList = new ArrayList<String>();
        lineList.add(url);


        if (lineList.size() == 0) {
            Toast.makeText(MainActivity.this, "no file to auto play", Toast.LENGTH_LONG).show();
        } else {
            String[] files = new String[lineList.size()];
            lineList.toArray(files);

            CheckBox mediaplayerCheckBox = (CheckBox) findViewById(R.id.mediaplayer_checkbox);
            CheckBox mediacodecCheckBox = (CheckBox) findViewById(R.id.mediacodec_checkbox);
            Log.e("PlayActivity", "mediacodec_enable from mainactivity is " + mediacodecCheckBox.isChecked());

            if (mediaplayerCheckBox.isChecked() == false) {
                Intent intent = new Intent(getApplicationContext(), PlayActivity.class);
                intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                intent.putExtra(PlayActivity.EXTRA_KEY_COMMAND, PlayActivity.EXTRA_VALUE_COMMAND_AUTO_PLAY);
                intent.putExtra(PlayActivity.EXTRA_KEY_FILE_LIST, files);
//    					intent.putExtra(PlayActivity.EXTRA_KEY_FORCE_SOFTWARE, forceSoftwareCheckBox.isChecked());
                intent.putExtra(PlayActivity.EXTRA_KEY_FORCE_SOFTWARE, true);
                intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());

                startActivity(intent);
            } else {
                if (shouldUseTextView()) {
                    Intent intent = new Intent(getApplicationContext(), TextureViewPlayActivity.class);
                    intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                    intent.putExtra(TextureViewPlayActivity.EXTRA_KEY_FILE_PATH, files[0]);
                    intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), SurfaceViewPlayActivity.class);
                    intent.putExtra(PlayActivity.EXTRA_KEY_CACHE_IN_CELLULAR, mOptionCacheInCellular);
                    intent.putExtra(SurfaceViewPlayActivity.EXTRA_KEY_FILE_PATH, files[0]);
                    intent.putExtra(PlayActivity.EXTRA_KEY_MEDIACODEC, mediacodecCheckBox.isChecked());
                    startActivity(intent);
                }
            }
        }
    }
}


class FileListAdapter extends BaseAdapter {
    private Context _context = null;
    private File _pathFile = null;
    private List<File> _fileList = null;

    public FileListAdapter(Context context, File pathFile) {
        _context = context;
        _pathFile = pathFile;
        _fileList = new ArrayList<File>();

        Log.d("FileListAdatper", "Path is " + pathFile.getAbsolutePath() + ", isDirectory ? " + pathFile.isDirectory() + ", exists = " +
                pathFile.exists() + ", canRead = " + pathFile.canRead() + ", canExecute = " + pathFile.canExecute());
    }

    // Call this to reload file list
    public void reload() {
        _fileList.clear();

        // Walk through files in current path
        File[] files = _pathFile.listFiles();
        Log.d("FIleListAdapter", "path " + _pathFile + ", files = " + files);

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory())
                    continue;

                _fileList.add(file);
            }
        }

        // Notify observers
        notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return _fileList.size();
    }

    @Override
    public Object getItem(int position) {
        return _fileList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView fileNameTextView = null;

        if (convertView != null && (convertView instanceof TextView)) {
            fileNameTextView = (TextView) convertView;
        } else {
            fileNameTextView = new TextView(_context);
        }

        fileNameTextView.setPadding(10, 10, 10, 10);
        fileNameTextView.setTextSize(20);
        fileNameTextView.setText(((File) getItem(position)).getName());

        return fileNameTextView;
    }

} // class FileListAdapter
