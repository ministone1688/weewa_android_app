package com.xh.hotme.listener;


public interface ICommonListener {
    void onSuccess();

    void onFail(String code, String message);

    void onFinish();
}
