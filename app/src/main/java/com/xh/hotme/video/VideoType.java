package com.xh.hotme.video;

import androidx.annotation.Keep;

@Keep
public enum VideoType {
    full(0),
    highlights(1),
    playback(2),
    goal(3);
    VideoType(int ni) {
        nativeInt = ni;
    }

    public int getValue() {
        return nativeInt;
    }

    final int nativeInt;
}
