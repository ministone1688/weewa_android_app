package com.xh.hotme.wxapi;//package com.xh.hotme.widget.wxapi;
//
//import android.app.Activity;
//import android.content.Intent;
//import android.os.Bundle;
//import android.widget.TextView;
//
//import com.leto.sandbox.engine.LSBEngine;
//import com.mgc.leto.game.base.utils.BaseAppUtil;
//import com.mgc.letobox.happy.R;
//import com.mgc.letobox.happy.utils.AppUtils;
//import com.tencent.mm.opensdk.constants.ConstantsAPI;
//import com.tencent.mm.opensdk.modelbase.BaseReq;
//import com.tencent.mm.opensdk.modelbase.BaseResp;
//import com.tencent.mm.opensdk.modelpay.PayResp;
//import com.tencent.mm.opensdk.openapi.IWXAPI;
//import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
//import com.tencent.mm.opensdk.openapi.WXAPIFactory;
//
//public class WXPayEntryActivity extends Activity implements IWXAPIEventHandler {
//	private IWXAPI api;
//	private TextView _statusLabel;
//
//	@Override
//	public void onCreate(Bundle savedInstanceState) {
//		super.onCreate(savedInstanceState);
//		setContentView(R.layout.pay_result);
//		_statusLabel = findViewById(R.id.status);
//
//		String appId = BaseAppUtil.getMetaStringValue(this, "MGC_WECHAT_APPID");
//		api = WXAPIFactory.createWXAPI(this, appId);
//		api.handleIntent(getIntent(), this);
//	}
//
//	@Override
//	protected void onNewIntent(Intent intent) {
//		super.onNewIntent(intent);
//		setIntent(intent);
//		api.handleIntent(intent, this);
//	}
//
//	@Override
//	public void onReq(BaseReq baseReq) {
//
//	}
//
//	@Override
//	public void onResp(BaseResp resp) {
//		if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
//			switch(resp.errCode) {
//				case BaseResp.ErrCode.ERR_OK:
//					_statusLabel.setText("支付成功");
//					PayResp payResp = (PayResp)resp;
//					runOnUiThread(() -> LSBEngine.get().notifyWxPaySuccess(payResp.prepayId, payResp.extData));
//					break;
//				case BaseResp.ErrCode.ERR_USER_CANCEL:
//					_statusLabel.setText("用户取消支付");
//					runOnUiThread(() -> LSBEngine.get().notifyWxPayCancelled());
//					break;
//				default:
//					_statusLabel.setText(resp.errStr);
//					runOnUiThread(() -> LSBEngine.get().notifyWxPayFailed(resp.errCode, resp.errStr));
//					break;
//			}
//		}
//	}
//}
