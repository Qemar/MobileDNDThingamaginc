package com.example.myapplication;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.NumberPicker;
import android.widget.ScrollView;

public class SmartScrollView extends ScrollView {
    public SmartScrollView(Context c) { super(c); }
    public SmartScrollView(Context c, AttributeSet a) { super(c, a); }
    public SmartScrollView(Context c, AttributeSet a, int s) { super(c, a, s); }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
            // найти view под координатами — если это NumberPicker, не перехватывать
            View touched = findViewAt((int)ev.getX(), (int)ev.getY());
            if (touched instanceof NumberPicker || hasNumberPickerInHierarchy(touched)) {
                return false;
            }
        }
        return super.onInterceptTouchEvent(ev);
    }

    private View findViewAt(int x, int y) {
        // ищем рекурсивно — простая реализация: пробегаем детей и проверяем hitRect
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            Rect rect = new Rect();
            child.getHitRect(rect);
            if (rect.contains(x + getScrollX(), y + getScrollY())) return child;
        }
        return this;
    }

    private boolean hasNumberPickerInHierarchy(View v) {
        if (v instanceof NumberPicker) return true;
        if (!(v instanceof ViewGroup)) return false;
        ViewGroup vg = (ViewGroup) v;
        for (int i = 0; i < vg.getChildCount(); i++) {
            if (hasNumberPickerInHierarchy(vg.getChildAt(i))) return true;
        }
        return false;
    }
}
