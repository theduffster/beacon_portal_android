package com.bernard.beaconportal.activities.activity;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MotionEvent;

import com.bernard.beaconportal.activities.activity.K9ActivityCommon.K9ActivityMagic;
import com.bernard.beaconportal.activities.activity.misc.SwipeGestureDetector.OnSwipeGestureListener;

public class K9ActivityMaterial extends ActionBarActivity implements
		K9ActivityMagic {

	private K9ActivityCommon mBase;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		mBase = K9ActivityCommon.newInstance(this);
		super.onCreate(savedInstanceState);

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		mBase.preDispatchTouchEvent(event);
		return super.dispatchTouchEvent(event);
	}

	@Override
	public void setupGestureDetector(OnSwipeGestureListener listener) {
		mBase.setupGestureDetector(listener);
	}
}
