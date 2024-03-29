package com.bernard.beaconportal.activities.settings.colorpicker;

import com.bernard.beaconportal.activities.MainActivity;
import com.bernard.beaconportal.activities.R;
import com.bernard.beaconportal.activities.R.id;
import com.bernard.beaconportal.activities.R.layout;
import com.bernard.beaconportal.activities.R.string;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

public class ColorPickerDialogSecondary extends Dialog implements
		ColorPickerView.OnColorChangedListener, View.OnClickListener {

	Activity mActivity;

	private ColorPickerView mColorPicker;

	private ColorPickerPanelView mOldColor;
	private ColorPickerPanelView mNewColor;

	private ColorPickerPanelView mWhite;
	private ColorPickerPanelView mBlack;
	private ColorPickerPanelView mCyan;
	private ColorPickerPanelView mRed;
	private ColorPickerPanelView mGreen;
	private ColorPickerPanelView mYellow;

	private EditText mHex;
	private ImageButton mSetButton;

	private OnColorChangedListener mListener;

	public interface OnColorChangedListener {
		public void onColorChanged(int color);
	}

	public ColorPickerDialogSecondary(Context context, int initialColor) {
		super(context);

		init(initialColor);
	}

	private void init(int color) {
		// To fight color branding.
		getWindow().setFormat(PixelFormat.RGBA_8888);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setUp(color);

	}

	private void setUp(int color) {

		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View layout = inflater.inflate(R.layout.dialog_color_picker, null);

		setContentView(layout);

		setTitle(R.string.dialog_color_picker);

		mColorPicker = (ColorPickerView) layout
				.findViewById(R.id.color_picker_view);
		mOldColor = (ColorPickerPanelView) layout
				.findViewById(R.id.old_color_panel);
		mNewColor = (ColorPickerPanelView) layout
				.findViewById(R.id.new_color_panel);

		mWhite = (ColorPickerPanelView) layout.findViewById(R.id.white_panel);
		mBlack = (ColorPickerPanelView) layout.findViewById(R.id.black_panel);
		mCyan = (ColorPickerPanelView) layout.findViewById(R.id.cyan_panel);
		mRed = (ColorPickerPanelView) layout.findViewById(R.id.red_panel);
		mGreen = (ColorPickerPanelView) layout.findViewById(R.id.green_panel);
		mYellow = (ColorPickerPanelView) layout.findViewById(R.id.yellow_panel);

		mHex = (EditText) layout.findViewById(R.id.hex);
		mSetButton = (ImageButton) layout.findViewById(R.id.enter);

		((LinearLayout) mOldColor.getParent()).setPadding(
				Math.round(mColorPicker.getDrawingOffset()), 0,
				Math.round(mColorPicker.getDrawingOffset()), 0);

		mOldColor.setOnClickListener(this);
		mNewColor.setOnClickListener(this);
		mColorPicker.setOnColorChangedListener(this);
		mOldColor.setColor(color);
		mColorPicker.setColor(color, true);

		setColorAndClickAction(mWhite, 0xffffffff);
		setColorAndClickAction(mBlack, 0xff4285f4);
		setColorAndClickAction(mCyan, 0xff00BCD4);
		setColorAndClickAction(mRed, 0xffF44336);
		setColorAndClickAction(mGreen, 0xff009688);
		setColorAndClickAction(mYellow, 0xffFFEB3B);

		SharedPreferences sharedpref = getContext().getSharedPreferences(
				"background_color", Context.MODE_PRIVATE);

		int color1 = sharedpref.getInt("Old_Color", 0);

		if (color1 == 0) {

			mOldColor.setColor(-1);
			mColorPicker.setColor(-1);

		} else {

			System.out.println(color1);

			mOldColor.setColor(color1);
			mColorPicker.setColor(color1);

		}

		if (mHex != null) {
			mHex.setText(ColorPickerPreference.convertToARGB(color));
		}
		if (mSetButton != null) {
			mSetButton.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					String text = mHex.getText().toString();
					try {
						int newColor = ColorPickerPreference
								.convertToColorInt(text);
						mColorPicker.setColor(newColor, true);

					} catch (Exception e) {
					}
				}
			});
		}

	}

	@Override
	public void onColorChanged(int color) {

		mNewColor.setColor(color);
		try {
			if (mHex != null) {
				mHex.setText(ColorPickerPreference.convertToARGB(color));

			}
		} catch (Exception e) {

		}

	}

	public void setAlphaSliderVisible(boolean visible) {
		mColorPicker.setAlphaSliderVisible(visible);
	}

	public void setColorAndClickAction(ColorPickerPanelView previewRect,
			final int color) {
		if (previewRect != null) {
			previewRect.setColor(color);
			previewRect.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						mColorPicker.setColor(color, true);

						Log.d("ColorPicker1", "Color Picked");

					} catch (Exception e) {
					}
				}
			});
		}
	}

	/**
	 * Set a OnColorChangedListener to get notified when the color selected by
	 * the user has changed.
	 * 
	 * @param listener
	 */
	public void setOnColorChangedListener(OnColorChangedListener listener) {
		mListener = listener;
	}

	public int getColor() {
		return mColorPicker.getColor();
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.new_color_panel) {
			if (mListener != null) {
				mListener.onColorChanged(mNewColor.getColor());

			}
		}

		int color = mColorPicker.getColor();

		System.out.println(color);

		String mNewColor = ColorPickerPreference.convertToARGB(color)
				.toString();

		SharedPreferences.Editor localEditor = getContext()
				.getSharedPreferences("background_color", Context.MODE_PRIVATE)
				.edit();

		localEditor.putString("background_color", mNewColor);

		localEditor.putInt("Old_Color", color);

		localEditor.commit();

		SharedPreferences.Editor localEditor1 = getContext()
				.getSharedPreferences("return_to_main", Context.MODE_PRIVATE)
				.edit();

		localEditor1.putString("fragment_to_start", "3");

		localEditor1.commit();

		Intent i = new Intent(getContext(), MainActivity.class);

		getContext().startActivity(i);

		dismiss();

		Log.d("put color", "now restarting");

	}

}