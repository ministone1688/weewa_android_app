package com.xh.hotme.event;

import androidx.annotation.Keep;

@Keep
public class HighTempEvent {
    public float temp;

    public HighTempEvent(float t) {
        this.temp = t;
    }
}
