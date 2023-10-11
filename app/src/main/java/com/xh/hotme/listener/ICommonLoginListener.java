package com.xh.hotme.listener;


public interface ICommonLoginListener {
    void onSuccess();

    void onFail(String code, String message);

    void onFinish();
}
