package com.xh.hotme.lay;

import static com.xh.hotme.lay.utils.TextUtil.isNullOrEmpty;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.tsy.sdk.myokhttp.MyOkHttp;
import com.tsy.sdk.myokhttp.response.RawResponseHandler;
import com.xh.hotme.R;
import com.xh.hotme.active.MobileUnbindDialog;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.bluetooth.BluetoothHandle;
import com.xh.hotme.bluetooth.BluetoothManager;
import com.xh.hotme.bluetooth.Device;
import com.xh.hotme.http.OkHttpCallbackDecode;
import com.xh.hotme.http.SdkApi;
import com.xh.hotme.lay.utils.MyToolUtils;
import com.xh.hotme.lay.utils.TextUtil;
import com.xh.hotme.lay.utils.WebModalDialog;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.OkHttpUtil;
import com.xh.hotme.utils.StatusBarUtil;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PushSetAcitivty extends BaseActivity implements View.OnClickListener {
    MyOkHttp mMyOkhttp = new MyOkHttp();

    private TextView ly_push_tips;
    private TextView btn_submit;
    TextView _curr_camera;
    private Context _ctx;

    private WebModalDialog _webDialog;
    private String platformType;
    private String title = "推流设置";

    List<Device> _dataList = new ArrayList<>();
    Handler _handle;
    Device _device;
    private String deviceId;

    private String pushAddress;
    private String pushStream;
    private EditText _push_address;
    private EditText _push_key;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _ctx = getApplication();
        // set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
        }
        // set content view
        setContentView(R.layout.activity_push_set);

        Intent intert=getIntent();
        platformType = intert.getStringExtra("platformType");
        if(isNullOrEmpty(platformType)){ finish();}
        if(!isNullOrEmpty(intert.getStringExtra("title"))){
            title = intert.getStringExtra("title");
        }

        TextView titleView = findViewById(R.id.tv_title);
        titleView.setText(title);
        // back click
        ImageView _backBtn = findViewById(R.id.iv_back);
        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        ly_push_tips = findViewById(R.id.ly_push_tips);
        ly_push_tips.setOnClickListener(this);
        btn_submit = findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(this);
        _curr_camera = findViewById(R.id.photo_name);
        _push_address = findViewById(R.id.push_address);
        _push_key = findViewById(R.id.push_key);

        getCurrentCamera();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.ly_push_tips:
                //如何获取
                showTipsDialog("如何获取推流地址");
                break;

            case R.id.btn_submit:

                pushAddress = _push_address.getText().toString();
                pushStream = _push_key.getText().toString();
                if(isNullOrEmpty(pushAddress)){
                    MyToolUtils.myToast(PushSetAcitivty.this,"请输入推流地址",3000);
                    return;
                }
                setPushForm();
                break;
        }
    }

    private void showTipsDialog(String mac) {
        if (_webDialog != null && _webDialog.isShowing()) {
            _webDialog.dismiss();
        }
        _webDialog = null;

        _webDialog = new WebModalDialog(PushSetAcitivty.this, SdkApi.user_pushUrl);
        _webDialog.setOnClickListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        _webDialog.show();
    }

    private void getCurrentCamera() {
        List<Device> deviceList = BluetoothManager.mInstance.getBleDeviceList();
        if (deviceList != null && deviceList.size() > 0) {
            _dataList.addAll(deviceList);
            _device = _dataList.get(0);
            String deviceName = BluetoothManager.getDisplayDeviceName(_dataList.get(0).getName());
            _curr_camera.setText(deviceName);
        }else{
            _curr_camera.setText("请选择相机");
        }

    }

    public void setPushForm(){
        HashMap<String, String> paramsdata = new HashMap<>();
        paramsdata.put("pushAddress", pushAddress);
        paramsdata.put("pushStream", pushStream);
        paramsdata.put("platformType",platformType);
        paramsdata.put("deviceId","888");
        OkHttpUtil.postData(SdkApi.savePushAddress, paramsdata, null, new OkHttpCallbackDecode<Object>() {
            @Override
            public void onDataSuccess(Object data) {
                System.out.println("9999999999999999999999999");
                System.out.println(data);
            }

            @Override
            public void onFailure(String code, String message) {
                System.out.println("9999999999999999999999999");
                System.out.println(message);
                MyToolUtils.myToast(PushSetAcitivty.this,"保存失败",3000);
            }

            @Override
            public void onFinish() {

            }
        });

    }


}
