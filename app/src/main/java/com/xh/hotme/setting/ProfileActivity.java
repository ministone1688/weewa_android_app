package com.xh.hotme.setting;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.metrics.Event;
import android.os.Build;
import android.os.Bundle;
import android.util.Pair;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.xh.hotme.R;
import com.xh.hotme.account.LoginInteract;
import com.xh.hotme.account.LoginManager;
import com.xh.hotme.account.MobileLoginActivity;
import com.xh.hotme.account.MobileViewActivity;
import com.xh.hotme.account.SetNicknameActivity;
import com.xh.hotme.account.UserAvatarInteract;
import com.xh.hotme.base.BaseActivity;
import com.xh.hotme.bean.UserAvatarBean;
import com.xh.hotme.bean.UserInfoBean;
import com.xh.hotme.event.LoginEvent;
import com.xh.hotme.event.UpdateNameEvent;
import com.xh.hotme.lay.utils.CameraPhotoDialog;
import com.xh.hotme.lay.utils.MyToolUtils;
import com.xh.hotme.listener.ICommonListener;
import com.xh.hotme.me.holder.CommonViewHolder;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.ColorUtil;
import com.xh.hotme.utils.MainHandler;
import com.xh.hotme.utils.StatusBarUtil;
import com.xh.hotme.utils.ToastUtil;
import com.xh.hotme.widget.ActionSheet;
import com.xh.hotme.widget.ModalDialog;
import com.xh.hotme.widget.imagepicker.ImagePickerCallback;
import com.xh.hotme.widget.imagepicker.LetoImagePicker;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProfileActivity extends BaseActivity implements ActionSheet.ActionSheetListener, ImagePickerCallback {
    // view type
    private static final int TYPE_AVATAR = 0;
    private static final int TYPE_INFO = 1;

    // request code
    private static final int REQ_CAMERA_ACCESS = 1001;
    private static final int REQ_ALBUM_ACCESS = 1002;

    // views
    private ImageView _backBtn;
    private TextView _titleLabel;
    private RecyclerView _listView;
    private View _signOutBtn;

    // action sheet
    private ActionSheet _actionSheet;

    // image picker
    LetoImagePicker _imagePicker;

    // model
    private UserInfoBean _loginInfo;
    private List<Pair<String, String>> _infos;
    private final List<String> _genders = Arrays.asList(
            "保密",
            "男",
            "女"
    );
    private UserAvatarBean _setPortraitResult;

    // string
    private String _loading;
    private String _cancel;
    private String _from_camera;
    private String _from_album;
    private String _set_portrait_failed;
    private Context _ctx;

    public static void start(Context context) {
        if (null != context) {
            Intent intent = new Intent(context, ProfileActivity.class);
            context.startActivity(intent);
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set status bar color
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            StatusBarUtil.setStatusBarColor(this, ColorUtil.parseColor("#ffffff"));
        }

        // init
        _loginInfo = LoginManager.getUserLoginInfo(this);
        _infos = new ArrayList<>();
        buildModel();
        _imagePicker = LetoImagePicker.getInstance(this);

        // set content view
        setContentView(R.layout.activity_profile);

        // find views
        _backBtn = findViewById(R.id.iv_back);
        _titleLabel = findViewById(R.id.tv_title);
        _listView = findViewById(R.id.list);
       // _signOutBtn = findViewById(R.id.sign_out);

        // load strings
        _loading = getString(R.string.loading);
        _cancel = getString(R.string.cancel);
        _from_camera = getString(R.string.from_camera);
        _from_album = getString(R.string.from_album);
        _set_portrait_failed = getString(R.string.set_portrait_failed);

        // back click
        _backBtn.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        // title
        _titleLabel.setText("个人资料");

        // setup list
        _listView.setLayoutManager(new LinearLayoutManager(this));
        _listView.setAdapter(new ProfileAdapter());

        // register context menu
        registerForContextMenu(_listView);

        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }

    }

    @Override
    public void onBackPressed() {
        if (_actionSheet != null) {
            _actionSheet.dismiss();
        } else {
            super.onBackPressed();
        }
    }

    private void buildModel() {
        _infos.clear();
        _infos.add(Pair.create("头像", _loginInfo.avatar));
        _infos.add(Pair.create("昵称", _loginInfo.nickName));
        _infos.add(Pair.create("手机号", _loginInfo.phone));
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventNickName(UpdateNameEvent event) {
        MainHandler.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                if (_listView != null) {
                    _loginInfo.nickName = event.nickName;
                    buildModel();

                    _listView.getAdapter().notifyDataSetChanged();
                }

            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();


        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }

        // unregister
        unregisterForContextMenu(_listView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQ_CAMERA_ACCESS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    _imagePicker.pickFromCamera("avatar.jpg", 256, 256, true, false, this);
                }
                break;
            case REQ_ALBUM_ACCESS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    _imagePicker.pickFromAlbum("avatar.jpg", 256, 256, false, this);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        for (String item : _genders) {
            menu.add(item);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        super.onContextItemSelected(item);

        // save change, no matter success or fail
        int gender = _genders.indexOf(item.getTitle());
//		_loginInfo.setGender(gender);
//		LoginControl.saveLoginInfo(ProfileActivity.this, _loginInfo);

        // modify
        doModifyInfo("", gender);

        // return
        return true;
    }

    private void doModifyInfo(String nickname, int gender) {
//		ApiUtil.modifyUserInfo(LeBoxProfileActivity.this, nickname, "", gender, new HttpCallbackDecode<ModifyUserInfoResultBean>(this, null) {
//			@Override
//			public void onDataSuccess(ModifyUserInfoResultBean data) {
//				if(data != null) {
//					buildModel();
//					_listView.getAdapter().notifyDataSetChanged();
//
//					// trigger event to let outer activity updated
//					EventBus.getDefault().post(new DataRefreshEvent());
//				}
//			}
//		});
    }

    private void showMobile() {
        if (_loginInfo != null) {
            MobileViewActivity.start(ProfileActivity.this, _loginInfo.phone);
        }

    }

    private void showNickInput() {
//
        SetNicknameActivity.start(ProfileActivity.this);

    }

    private void showAvatarPicker() {
       /* _actionSheet = ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setCancelButtonTitle(_cancel)
                .setOtherButtonTitles(_from_camera, _from_album)
                .setCancelableOnTouchOutside(true)
                .setListener(this)
                .show(); */
        CameraPhotoDialog dialog = new CameraPhotoDialog(ProfileActivity.this);
        dialog.setCameraBtnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                List<String> perms = new ArrayList<>();
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this,
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        perms.add(Manifest.permission.CAMERA);
                    }
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }
                if (perms.isEmpty()) {
                    _imagePicker.pickFromCamera("avatar.jpg", 256, 256, true, false, ProfileActivity.this);
                    dialog.dismiss();
                } else if (Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(perms.toArray(new String[0]), REQ_CAMERA_ACCESS);
                }


                return true;
            }
        });
        dialog.setAlbumBtnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {

                List<String> perms = new ArrayList<>();
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                    if (ContextCompat.checkSelfPermission(ProfileActivity.this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }
                if (perms.isEmpty()) {
                    _imagePicker.pickFromAlbum("avatar.jpg", 256, 256, false, ProfileActivity.this);
                    dialog.dismiss();
                } else if (Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(perms.toArray(new String[0]), REQ_ALBUM_ACCESS);
                }

                return true;
            }
        });
        dialog.show();
    }


    @Override
    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
        _actionSheet = null;
    }

    @Override
    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
        switch (index) {
            case 0: // from camera
            {
                List<String> perms = new ArrayList<>();
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
                        perms.add(Manifest.permission.CAMERA);
                    }
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }
                if (perms.isEmpty()) {
                   // _imagePicker.pickFromCamera("avatar.jpg", 256, 256, true, false, this);
                    MyToolUtils.myToast(ProfileActivity.this,"点击了上传",2000);
                } else if (Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(perms.toArray(new String[0]), REQ_CAMERA_ACCESS);
                }
                break;
            }
            case 1: // from album
            {
                List<String> perms = new ArrayList<>();
                if (Build.VERSION.SDK_INT >= 23) {
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                    }
                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
                        perms.add(Manifest.permission.READ_EXTERNAL_STORAGE);
                    }
                }
                if (perms.isEmpty()) {
                    _imagePicker.pickFromAlbum("avatar.jpg", 256, 256, false, this);
                } else if (Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(perms.toArray(new String[0]), REQ_ALBUM_ACCESS);
                }
                break;
            }
        }
    }

    @Override
    public void onImagePicked(String path) {
//		try {
        // load avatar jpg file
        File f = new File(path);
//			Uri uri = Uri.fromFile(f);
//			int len = (int) f.length();
//			byte[] buf = new byte[len];
//			ParcelFileDescriptor pfd = getContentResolver().openFileDescriptor(uri, "r");
//			InputStream fileStream = new FileInputStream(pfd.getFileDescriptor());
//			fileStream.read(buf, 0, buf.length);

        // modify portrait
        doUploadPortrait(f);
//		} catch(IOException e) {
//		}
    }

    @Override
    public void onImagePickingCancelled() {

    }

    private void doUploadPortrait(final File file) {
        showLoading(false, _loading);

        UserAvatarInteract.modifyPortrait(ProfileActivity.this, file, new UserAvatarInteract.UserAvatarListener() {
            @Override
            public void onSuccess(UserAvatarBean data) {
                AppTrace.d("modifyPortrait success");
                // save response
                _setPortraitResult = data;

                _loginInfo.avatar = data.imgUrl;
                UserInfoBean userInfoBean = LoginManager.getUserLoginInfo(ProfileActivity.this);
                userInfoBean.avatar = data.imgUrl;

                LoginManager.saveLoginInfo(ProfileActivity.this, userInfoBean);

                // reload ui
                buildModel();
                _listView.getAdapter().notifyDataSetChanged();
//
                // modify user portrait
//					doModifyUserPortrait();
            }

            @Override
            public void onFail(String code, String message) {

            }

            @Override
            public void onFinish() {
                dismissLoading();
            }
        });
    }

    private class ProfileAdapter extends RecyclerView.Adapter<CommonViewHolder<Pair<String, String>>> {
        @NonNull
        @Override
        public CommonViewHolder<Pair<String, String>> onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_AVATAR) {
                return AvatarHolder.create(ProfileActivity.this, parent);
            }
            return SimpleUserInfoHolder.create(ProfileActivity.this, parent);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return TYPE_AVATAR;
            }
            return TYPE_INFO;
        }

        @SuppressLint("RecyclerView")
        @Override
        public void onBindViewHolder(@NonNull CommonViewHolder<Pair<String, String>> holder, final int position) {
            holder.onBind(_infos.get(position), position);
            holder.getItemView().setOnClickListener(new ClickGuard.GuardedOnClickListener() {
                @Override
                public boolean onClicked() {
                    switch (position) {
                        case 0:

                            showAvatarPicker();
                            break;
                        case 1:

                            showNickInput();
                            break;
                        case 2:

                            showMobile();
                            break;
                    }
                    return true;
                }
            });
        }

        @Override
        public int getItemCount() {
            return _infos.size();
        }
    }
}
