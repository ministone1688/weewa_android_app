/******************************************************************************
 *
 *  Copyright (C) 2013-2014 Cypress Semiconductor
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 ******************************************************************************/
package com.xh.hotme.bluetooth;

import java.util.UUID;

/**
 * Contains the UUID of services, characteristics, and descriptors
 */
public class BleConstants {
    public static final String TAG_PREFIX = "Home."; //used for debugging

    /**
     * Transmit packet size
     */
    public static final int SIZE_TRANSMIT_PACKET = 134;



    public static final UUID BLE_WRITE_SERVICE_UUID = UUID
            .fromString("0000ffe5-0000-1000-8000-00805f9b34fb");

    /**
     * UUID of Wifi Characteristic
     */
//    public static final UUID WIFI_CHARACTERISTIC_UUID = UUID
//            .fromString("00009999-0000-1000-8000-00805F9B34FB");
    public static final UUID BLE_WRITE_CHARACTERISTIC_UUID = UUID
            .fromString("0000ffe9-0000-1000-8000-00805f9b34fb");

    /**
     * UUID of the client configuration descriptor
     */
    public static final UUID CLIENT_CONFIG_DESCRIPTOR_UUID = UUID
            .fromString("00002902-0000-1000-8000-00805f9b34fb");

    public static final UUID BLE_READ_SERVICE_UUID = UUID
            .fromString("0000ffe0-0000-1000-8000-00805f9b34fb");

    public static final UUID BLE_READ_CHARACTERISTIC_UUID = UUID
            .fromString("0000ffe4-0000-1000-8000-00805f9b34fb");

    public static final String SOFT_AP_IP = "10.201.126.1";
    public static final byte BLE_CMD_CHAR_START = 0x01;
    public static final byte BLE_CMD_CHAR_END = 0x04;

    public static final String BLE_CMD = "c";
    public static final String BLE_KEY_SSID = "s";
    public static final String BLE_KEY_PASSWORD = "p";
    public static final String BLE_KEY_RESULT = "r";

    public static final String BLE_KEY_FREE = "free";

    public static final String BLE_KEY_TOTAL = "total";
    public static final String BLE_KEY_USED = "used";

    public static final String BLE_KEY_WIFI = "wifi";
    public static final String BLE_KEY_ETH = "eth";
    public static final String BLE_KEY_SIM = "sim";
    public static final String BLE_KEY_HTTP = "http";

    public static final String BLE_KEY_ST = "st";
    public static final String BLE_KEY_IP = "ip";

    public static final String BLE_KEY_SOFTAP = "ap";
    public static final String BLE_KEY_SOFTAP_SSID = "s";
    public static final String BLE_KEY_SOFTAP_PASSWORD = "p";

    public static final String BLE_RESULT_SUCCESS = "ok";
    public static final String BLE_KEY_WIFILIST = "wl";

    public static final String BLE_KEY_DEVICE_NAME = "dn";
    public static final String BLE_KEY_DEVICE_ID = "did";
    public static final String BLE_KEY_DEVICE_MAC = "mac";
    public static final String BLE_KEY_DEVICE_ACTIVE_STATUS = "act";
    public static final String BLE_KEY_DEVICE_ACCOUNT = "acc";
    public static final String BLE_KEY_DEVICE_VERTION = "v";

    public static final String BLE_KEY_UPDATE_NAME = "n";

    public static final String BLE_CMD_POWER_ON  = "poweron";
    public static final String BLE_CMD_POWER_OFF  = "poweroff";
    public static final String BLE_CMD_RESTART  = "restart";
    public static final String BLE_CMD_WIFILIST  ="wifilist";
    public static final String BLE_CMD_WIFI_SETUP  ="wifisetup";
    public static final String BLE_CMD_SOFTAP_OPEN  ="softap_open";
    public static final String BLE_CMD_SOFTAP_CLOSE  ="softap_close";

    public static final String BLE_CMD_SOFTAP_STATUS  ="softap_status";

    public static final String BLE_CMD_NETWORK_STATUS  ="net_status";
    public static final String BLE_CMD_SIM_OPEN  ="sim_open";
    public static final String BLE_CMD_SIM_CLOSE  ="sim_close";

    public static final String BLE_CMD_CAMERA_SETUP  ="camera_setup";
    public static final String BLE_CMD_CAMERA_START  ="camera_start";
    public static final String BLE_CMD_CAMERA_STOP  ="camera_stop";
    public static final String BLE_CMD_CAMERA_STATUS  ="camera_status";


    public static final String BLE_CMD_RKIPC_START  ="rkipc_start";
    public static final String BLE_CMD_RECORDER_START  ="record_start";
    public static final String BLE_CMD_RECORDER_STOP  ="record_stop";
    public static final String BLE_CMD_RECORDER_STATUS  ="record_status";


    public static final String BLE_CMD_LIVE_START  ="live_start";
    public static final String BLE_CMD_LIVE_END  ="live_stop";
    public static final String BLE_CMD_LIVE_STATUS  ="live_status";

    public static final String BLE_CMD_USAGE_INFO  ="usage_info";
    public static final String BLE_CMD_DEVICE_INFO  ="deviceinfo";
    public static final String BLE_CMD_ENERGY  ="energy_info";
    public static final String BLE_CMD_STORAGE  ="storage_info";

    public static final String BLE_CMD_UPDATE_NAME  ="update_dev_name";

    public static final String BLE_CMD_DEVICE_ACTIVE_SMS  ="active_sms";

    public static final String BLE_CMD_DEVICE_ACTIVE_BIND  ="active_bind";
    public static final String BLE_CMD_DEVICE_UNBIND_SMS  ="unbind_sms";
    public static final String BLE_CMD_DEVICE_UNBIND  ="device_unbind";
    public static final String BLE_CMD_SET_TOKEN  ="set_token";

    public static final String BLE_CMD_VIDEO_DIR_UPLOAD  ="video_dir_upload";
    public static final String BLE_CMD_VIDEO_LIST  ="videolist";
    public static final String BLE_CMD_VIDEO_LIST_URL  ="video_list";

    public static final String BLE_CMD_VIDEO_DETAIL_LIST  ="video_detail_list";
    public static final String BLE_CMD_VIDEO_FILTER  ="video_filter";

    public static final String BLE_CMD_HIGH_TEMP ="high_temp";

    public static final String BLE_CMD_WEEWA_START  ="weewa_start";

    public static final String BLE_CMD_LOWER_POWEROFF  ="lower_poweroff";


    public static final String BLE_KEY_AUTHOR = "au";
    public static final String BLE_KEY_MAX_RATE = "mbr";
    public static final String BLE_KEY_VIDEO_NAME = "vn";
    public static final String BLE_KEY_VIDEO_WIDTH = "vw";
    public static final String BLE_KEY_VIDEO_HEIGHT = "vh";
    public static final String BLE_KEY_VIDEO_TIME = "vt";
    public static final String BLE_KEY_VIDEO_ADDRESS = "vs";
    public static final String BLE_KEY_VIDEO_CITY = "city";
    public static final String BLE_KEY_VIDEO_PLACE_TYPE = "place_type";
    public static final String BLE_KEY_HOST_TEAM = "host_team";
    public static final String BLE_KEY_GUSET_TEAM = "guest_team";
    public static final String BLE_KEY_MOTION_TYPE = "motion_type";

    public static final String BLE_KEY_MOBILE = "mb";

    public static final String BLE_KEY_BLE_MAC = "ble";

    public static final String BLE_KEY_MAC = "mac";
    public static final String BLE_KEY_TOKEN = "t";
    public static final String BLE_KEY_CODE = "code";

    public static final String BLE_SMS_SUCCESS_BIND = "0";

    public static final String BLE_SMS_SUCCESS_LOGIN = "1";

    public static final String BLE_SMS_SUCCESS_DEVICE_BOUND = "2";

    public static final String BLE_KEY_PATH = "p";
    public static final String BLE_KEY_VIDEOS = "videos";
    public static final String BLE_KEY_VIDEO = "video";
    public static final String BLE_KEY_GROUP = "groups";

    public static final String BLE_KEY_FILTER  ="filter";



    public static final int BLE_CONNECTED_DELAY  = 2000;
}
