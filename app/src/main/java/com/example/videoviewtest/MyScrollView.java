package com.example.videoviewtest;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class MyScrollView extends ScrollView {   
	
	static final private String LOGTAG = "MyScrollView";

    public MyScrollView(Context context) {
        super(context);
    }

    public MyScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
    
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
    	long startTime = System.nanoTime();
        super.onLayout(changed, l, t, r, b);
    	long endTime = System.nanoTime();
    	
    	android.util.Log.i(LOGTAG, String.format("MyScrollView.onLayout()  %d", (endTime - startTime)/1000));
    }
}