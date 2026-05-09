/*
 * Copyright (C) 2024 Tapan Kashyap <tapank415@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package ch.blinkenlights.android.vanilla;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import androidx.appcompat.view.ContextThemeWrapper;

import com.skydoves.colorpickerview.ColorEnvelope;
import com.skydoves.colorpickerview.ColorPickerDialog;
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener;

public class ColorPreference extends DialogPreference {
	private int mColor;

	public ColorPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onClick() {
		Context themedContext = new ContextThemeWrapper(getContext(), androidx.appcompat.R.style.Theme_AppCompat_DayNight_Dialog);
		ColorPickerDialog.Builder builder = new ColorPickerDialog.Builder(themedContext)
			.setTitle(getTitle().toString())
			.setPositiveButton(android.R.string.ok, new ColorEnvelopeListener() {
				@Override
				public void onColorSelected(ColorEnvelope envelope, boolean fromUser) {
					mColor = envelope.getColor();
					persistInt(mColor);
					notifyChanged();
				}
			})
			.setNegativeButton(android.R.string.cancel, null)
			.attachAlphaSlideBar(true)
			.attachBrightnessSlideBar(true);
		
		builder.show();
	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		return a.getInt(index, Color.WHITE);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		mColor = restoreValue ? getPersistedInt(mColor) : (Integer) defaultValue;
	}
}
