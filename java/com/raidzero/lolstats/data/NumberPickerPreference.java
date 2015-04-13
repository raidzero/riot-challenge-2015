package com.raidzero.lolstats.data;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.raidzero.lolstats.R;

/**
 * Created by posborn on 4/13/15.
 */
public class NumberPickerPreference extends Preference {
    private static final String tag = "NumberPickerPreference";

    private int currentValue;
    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.number_picker);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        final NumberPicker numPicker = (NumberPicker) view.findViewById(R.id.numPicker);

        // disable direct text editing
        numPicker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

        numPicker.setMinValue(1);
        numPicker.setMaxValue(60);

        numPicker.setValue(numPicker.getValue());

        numPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker numberPicker, int i, int i1) {
                persistInt(numberPicker.getValue());
            }
        });
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, 4);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            currentValue = getPersistedInt(4);
        } else {
            currentValue = (int) defaultValue;
            persistInt(currentValue);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();

        if (isPersistent()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.currentValue = currentValue;

        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (!state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());

        currentValue = myState.currentValue;
        notifyChanged();
    }

    private static class SavedState extends BaseSavedState {

        int currentValue;

        private SavedState(Parcel source) {
            super(source);

            currentValue = source.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);

            dest.writeInt(currentValue);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Creator<SavedState> CREATOR =
                new Creator<SavedState>() {
                    @Override
                    public SavedState createFromParcel(Parcel source) {
                        return new SavedState(source);
                    }

                    @Override
                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }

}