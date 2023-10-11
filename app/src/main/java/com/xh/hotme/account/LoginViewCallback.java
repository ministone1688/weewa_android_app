package com.xh.hotme.account;


public interface LoginViewCallback {
    void requestCode(String mobile);

    void requestLogin(String mobile, String code);

    void onClose();
}
