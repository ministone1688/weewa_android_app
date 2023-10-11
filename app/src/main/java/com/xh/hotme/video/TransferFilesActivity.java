package com.xh.hotme.video;


import static com.xh.hotme.HotmeApplication.getContext;


import android.view.View;


import com.xh.hotme.HotmeApplication;
import com.xh.hotme.R;
import com.xh.hotme.base.AppTitleActivity;
import com.xh.hotme.base.BaseViewActivity;
import com.xh.hotme.base.IEventBus;
import com.xh.hotme.base.TitleLayoutHelper;
import com.xh.hotme.databinding.ActivityTransferfilesBinding;
import com.xh.hotme.databinding.ItemTransferfilesConnectBinding;
import com.xh.hotme.databinding.ItemTransferfilesEmptyBinding;
import com.xh.hotme.databinding.ItemTransferfilesOkBinding;
import com.xh.hotme.event.NetWorkForQEvent;
import com.xh.hotme.http.ConnectLogic;
import com.xh.hotme.utils.AppTrace;
import com.xh.hotme.utils.ClickGuard;
import com.xh.hotme.utils.FileSizeUtil;
import com.xh.hotme.utils.TimeUtil;
import com.xh.hotme.widget.ModalDialog;
import com.xh.hotme.wifi.HotmeWiFiManager;

import org.greenrobot.eventbus.Subscribe;

/**
 * date   : 2023/6/22
 * desc   :传输文件
 */
@IEventBus
public class TransferFilesActivity extends BaseViewActivity<ActivityTransferfilesBinding> {

    int state = 0;//状态 0无内容 1下载ing 2成功
    ItemTransferfilesEmptyBinding emptyView;
    ItemTransferfilesConnectBinding connectBinding;
    ItemTransferfilesOkBinding okBinding;

    @Override
    protected void initView() {
        viewBinding.titleBar.tvTitle.setText("传送文件");
        viewBinding.titleBar.ivBack.setOnClickListener(new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                finish();
                return true;
            }
        });

        emptyView = ItemTransferfilesEmptyBinding.bind(viewBinding.getRoot());
        connectBinding = ItemTransferfilesConnectBinding.bind(viewBinding.getRoot());
        okBinding = ItemTransferfilesOkBinding.bind(viewBinding.getRoot());

        connectBinding.tvPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ModalDialog dialog = new ModalDialog(TransferFilesActivity.this);
                dialog.setTitle("取消");
                dialog.setMessage("已传输完成的视频可在本地查看，未完成的视频将停止传输");
                dialog.setLeftButton(getString(R.string.cancel), new ClickGuard.GuardedOnClickListener() {
                    @Override
                    public boolean onClicked() {
                        return true;
                    }
                });
                dialog.setRightButton(getString(R.string.confirm), new ClickGuard.GuardedOnClickListener() {
                    @Override
                    public boolean onClicked() {
                        return true;
                    }
                });
                dialog.show();
            }
        });
    }

    @Override
    protected void initData() {
        viewShow(ConnectLogic.getInstance().getSessionState()
                , ConnectLogic.getInstance().getFileSize()
                , ConnectLogic.getInstance().getSessionProgress(),
                ConnectLogic.getInstance().getRateSize());
        ConnectLogic.getInstance().setCallback(new ConnectLogic.ICallback() {
            @Override
            public void onInfo(ConnectLogic.SessionStatus sessionStatus, double progress, long allSize, long rateSize) {
                viewShow(sessionStatus, allSize, progress, rateSize);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectLogic.getInstance().setCallback(null);
    }

    public void showError() {


        ModalDialog dialog = new ModalDialog(TransferFilesActivity.this);
        dialog.setTitle("设备连接中断");
        dialog.setMessage("请重新连接设备继续传输视频");
        dialog.setLeftButton(getString(R.string.cancel), new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                ConnectLogic.getInstance().unReceive();
                return true;
            }
        });
        dialog.setRightButton("重新连接", new ClickGuard.GuardedOnClickListener() {
            @Override
            public boolean onClicked() {
                HotmeWiFiManager.checkWiFiAndGetVideList(getContext());
                return true;
            }
        });
        dialog.show();
    }


    @Subscribe
    public void onEvent(NetWorkForQEvent event) {
        if (event.type == NetWorkForQEvent.AVAILABLE) {
            if (ConnectLogic.getInstance().getSessionState() == ConnectLogic.SessionStatus.UNCONNECT) {
                ConnectLogic.getInstance().toReceive();
            }

        } else if (event.type == NetWorkForQEvent.UNCONNECT) {
            ConnectLogic.getInstance().showConnectError(getContext());
        }
    }

//    @Subscribe
//    public void onEvent(SessionEvent event) {
//        YLog.d("--->" + event.toString());
//        viewShow(event.sessionStatus, event.allSize, event.progress, event.rateSize);
//    }

    private void viewShow(ConnectLogic.SessionStatus sessionStatus, long allSize, double progress, long rateSize) {
        switch (sessionStatus) {
            case WAITE:
                state = 0;
                break;

            case START:
                state = 1;
                //文件大小：1.23G
                //传送速率：203MB/S
                //预估还需时间：12分钟
                connectBinding.tvFileSize.setText("传送文件大小：" + FileSizeUtil.formatSize(allSize));
                connectBinding.tvRateSize.setText("传送速率：" + FileSizeUtil.formatSize(rateSize) + "/S");
                connectBinding.progressBar.setProgress((int) (progress * 100));
                connectBinding.tvTime.setText("预估还需时间:" + TimeUtil.stringToTime_new((long) ((allSize * (1 - progress)) / rateSize)));
                break;
            case DOWNLOADING:
                if (state >= 0) {
                    //2023-06-29 09:23:11.953 8487-8487/com.bj.rwkc D/YLog: --->SessionEvent{sessionStatus=DOWNLOADING, progress=0.7565619833074394, allSize=30707332, rateSize=23232000}
                    state = 1;
                    //文件大小：1.23G
                    //传送速率：203MB/S
                    //预估还需时间：12分钟
                    connectBinding.tvFileSize.setText("传送文件大小：" + FileSizeUtil.formatSize(allSize));
                    connectBinding.tvRateSize.setText("传送速率：" + FileSizeUtil.formatSize(rateSize) + "/S");
                    connectBinding.progressBar.setProgress((int) (progress * 100));
                    AppTrace.d(TAG, ">>>>" + rateSize + ">>>>>>> " + (allSize * (1 - progress)) + ">>>>" + progress + ">>>" + allSize);
                    String time;
                    if (1 > progress) {//500一次
                        time = TimeUtil.stringToTime_new((long) ((allSize / rateSize/2 * (1 - progress))));
                    } else {
                        time = "0秒";
                    }
                    connectBinding.tvTime.setText("预估还需时间:" + time);
                }
                break;
            case UNCONNECT:
                showError();
                break;
            case Ended:
                state = 2;
//                传送文件大小：1.23G
//                传送速率：203MB/S
                okBinding.tvFileSizeOk.setText("传送文件大小：" + FileSizeUtil.formatSize(allSize));
//                if (rateSize == 0) {
//                    rateSize = allSize;
//                }
                long time = (System.currentTimeMillis() - ConnectLogic.getInstance().getStartDownTime()) / 1000;
                if (time > 0) {
                    rateSize = allSize / time;
                } else {
                    rateSize = allSize;
                }
                okBinding.tvRateSizeOk.setText("传送速率：" + FileSizeUtil.formatSize(rateSize) + "/S");
                okBinding.tvOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        state = 0;
                        showStateView();
                    }
                });
                break;

        }
        showStateView();
    }

    private void showStateView() {
        hideAll();
        if (state <= 0) {
            emptyView.tvNoFile.setVisibility(View.VISIBLE);
        } else if (state == 1) {
            connectBinding.groupConnect.setVisibility(View.VISIBLE);
        }
        if (state == 2) {
            okBinding.groupOK.setVisibility(View.VISIBLE);
        }
    }

    private void hideAll() {
        emptyView.tvNoFile.setVisibility(View.GONE);
        connectBinding.groupConnect.setVisibility(View.GONE);
        okBinding.groupOK.setVisibility(View.GONE);
    }
}
