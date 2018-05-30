package ru.kazantsev.template.util;

import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.TextPaint;
import android.text.style.MetricAffectingSpan;

public class VerticalAlignmentSpan extends MetricAffectingSpan  implements ParcelableSpan {
    private double ratio = 0.5;

    public VerticalAlignmentSpan() {
    }

    public VerticalAlignmentSpan(double ratio) {
        this.ratio = ratio;
    }

    public VerticalAlignmentSpan(Parcel src) {
        this.ratio = src.readDouble();
    }

    @Override
    public void updateDrawState(TextPaint paint) {
        paint.baselineShift += (int) (paint.ascent() * ratio);
    }

    @Override
    public void updateMeasureState(TextPaint paint) {
        paint.baselineShift += (int) (paint.ascent() * ratio);
    }

    @Override
    public int getSpanTypeId() {
        return 0x2A710;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(ratio);
    }
}