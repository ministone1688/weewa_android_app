//
// Copyright (c) 2017, ledong.com
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// * Redistributions of source code must retain the above copyright notice, this
// list of conditions and the following disclaimer.
//
// * Redistributions in binary form must reproduce the above copyright notice,
// this list of conditions and the following disclaimer in the documentation
// and/or other materials provided with the distribution.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//


package com.xh.hotme.utils;


import androidx.annotation.Keep;

@Keep
public class Constants {

    private Constants() {
    }

    public static String userToken = "";
    public static String HEADER_TOKEN = "";
    public static String HEADER_TOKEN_NAME = "Authorization";

    public static String TEST_TOKEN = "00000000";

    public static boolean TEST_MODE = false;
    public static final int REQUEST_CODE_SCAN = 0x1010;


    public static final String REQUEST_CODE = "request_code";
    public static final String REQUEST_DEVICE = "request_device";
    public static final String REQUEST_VIDEO_BEAN = "request_video_bean";

    public static final String REQUEST_DEVICE_INFO = "request_device_INFO";
    public static final String REQUEST_ACTIVE_STEP = "request_active_step";
    public static final String REQUEST_MOBILE = "request_mobile";
    public static final String REQUEST_NETWORK_INFO = "request_network_info";


    public static final String INTENT_SSID = "ssid";
    public static final String INTENT_PASSWORD = "password";
    public static final String INTENT_BLE = "ble_address";

    public static final int REQUEST_CODE_LOGIN_TAB_ME = 0x1020;
    public static final int REQUEST_CODE_LOGIN_TAB_LIVE = 0x1022;
    public static final int REQUEST_CODE_LOGIN_HOME_DEVICE = 0x1021;
    public static final int REQUEST_CODE_WIFI_SETUP = 0x1101;


    public static final int REQUEST_CODE_PREVIEW_WIFI = 0x2101;

    // other flag
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000;

    public static final String PREF_EXTRA = "PREF_EXTRA";

    public static final String UMENG_APP_ID = "649abce2a1a164591b3c5ef7";

    public static final String WEIXIN_APP_ID = "";
    public static final String WEIXIN_SECRECT_KEY = "";

    public final static String WINDOW_TYPE = "window_type";
    public final static String TITLE_NAME = "titleName";
    public final static String REQUEST_TYPE = "REQUEST_TYPE";
    public final static String ORIENTATION_TYPE = "ORIENTATION_TYPE";
    public final static String APP_TYPE = "APP_TYPE";
    public static final String SHOW_LOADING = "show_loading";
    public final static String URL = "url";
    public final static String MODIFY_TITLE = "modify_title";
    public final static String VIDEO_URL = "video_url";


    public final static int ME_MODULE_PROFILE = 1;
    public final static int ME_MODULE_SETTING = 2;
    public final static int ME_MODULE_OUT = 3;


    public final static int DEVICE_STATUS_OFFLINE = 0;
    public final static int DEVICE_STATUS_ONLINE = 1;
    public final static int DEVICE_STATUS_CONNECT = 2;
    public final static int DEVICE_STATUS_CONNECT_FAIL = 3;

    public final static int LIVE_TYPE_FOOTBALL = 0;
    public final static int LIVE_TYPE_BASKETBALL = 1;
    public final static int LIVE_TYPE_TRAINING = 2;

    public final static int PLACE_TYPE_FIVE = 0;
    public final static int PLACE_TYPE_EIGHT = 1;
    public final static int PLACE_TYPE_ELEVEN = 2;

    public final static int AGE_TYPE_FORTY_LOW = 0;
    public final static int AGE_TYPE_FORTY_OVER = 1;

    public final static String VIDEO_CATEGORY = "type";
    public final static int VIDEO_CATEGORY_TOP = 1;
    public final static int VIDEO_CATEGORY_GALLERY = 2;

    public final static String VIDEO_PLAY_MODE = "play_mode";


    public final static String ACTIVE_LOGIN = "active";
    public final static int ACTIVE_TYPE_LOGIN = 1;
    public final static int ACTIVE_TYPE_ACTIVE = 2;
    public final static int ACTIVE_TYPE_BIND = 3;

    public final static String SSID = "ssid";

    public final static String TYPE_BIND = "BIND";

    public final static String TYPE_UNBIND = "UN_BIND";
    public final static String TYPE_LOGIN = "LOGIN";
    public final static String TYPE_UPDATE_PHONE = "UPDATE_PHONE";
    public final static String REQUEST_INTENT_USAGE_INFO = "USAGE_INFO";
    public final static String REQUEST_INTENT_DEVICE_INFO = "DEVICE_INFO";

    public final static int ACTIVE_STEP_SETUP_WIFI = 0;
    public final static int ACTIVE_STEP_ACTIVE = 1;

    public final static int ACTIVE_STEP_BIND_FAIL = 2;
    public final static int ACTIVE_STEP_BIND_SUCCESS = 3;

    public final static int ACTIVE_STEP_BOUND = 4;

    public final static int ACTIVE_STEP_ACTIVE_SUCCESS = 5;

    public final static int ACTIVE_STEP_ACTIVE_FAIL = 6;

    public final static int BIND_DEVICE_PAGE_NUMBER = 10;

    public final static String LOGIN_TYPE = "login_type";
    public final static int LOGIN_TYPE_LOGIN = 0;
    public final static int LOGIN_TYPE_CHANGE_MOBILE = 1;

    public final static int sms_count_down = 90;

}
