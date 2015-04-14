package com.raidzero.lolstats.data;

import android.app.AlertDialog;
import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

import com.raidzero.lolstats.R;

/**
 * Created by posborn on 4/13/15.
 */
public class NumberPickerPreference extends DialogPreference {
    private static final String tag = "NumberPickerPreference";

    private NumberPicker mNumPicker;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setPersistent(true);
        setDialogLayoutResource(R.layout.number_picker);
    }

    @Override
    public void onBindDialogView(View view) {
        mNumPicker = (NumberPicker) view.findViewById(R.id.numPicker);

        // disable direct text input
        mNumPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        // min 1, max 60
        mNumPicker.setMinValue(1);
        mNumPicker.setMaxValue(60);

        mNumPicker.setValue(getPersistedInt(4));

        // set up listener
        mNumPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                persistInt(newVal);
            }
        });

        super.onBindDialogView(view);
    }
}