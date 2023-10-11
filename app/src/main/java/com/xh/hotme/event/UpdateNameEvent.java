package com.xh.hotme.event;

import androidx.annotation.Keep;

@Keep
public class UpdateNameEvent {
    public String nickName;

    public UpdateNameEvent(String nick) {
        this.nickName = nick;
    }
}
