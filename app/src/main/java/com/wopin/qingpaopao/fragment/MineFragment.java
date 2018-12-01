package com.wopin.qingpaopao.fragment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.wopin.qingpaopao.R;
import com.wopin.qingpaopao.adapter.MineGridRvAdapter;
import com.wopin.qingpaopao.adapter.MineListRvAdapter;
import com.wopin.qingpaopao.bean.response.CheckNewMessageRsp;
import com.wopin.qingpaopao.bean.response.DrinkListTodayRsp;
import com.wopin.qingpaopao.bean.response.DrinkListTotalRsp;
import com.wopin.qingpaopao.bean.response.LoginRsp;
import com.wopin.qingpaopao.bean.response.NormalRsp;
import com.wopin.qingpaopao.common.Constants;
import com.wopin.qingpaopao.fragment.information_edit.InformationEditFragment;
import com.wopin.qingpaopao.fragment.message.MessageMainFragment;
import com.wopin.qingpaopao.fragment.my.CollectSubtitleFragment;
import com.wopin.qingpaopao.fragment.my.FollowSubtitleFragment;
import com.wopin.qingpaopao.fragment.my.HistorySubtitleFragment;
import com.wopin.qingpaopao.fragment.my.MyDrinkingFragment;
import com.wopin.qingpaopao.fragment.my.MyFansFragment;
import com.wopin.qingpaopao.fragment.my.MyFollowFragment;
import com.wopin.qingpaopao.fragment.my.MyHealthFragment;
import com.wopin.qingpaopao.fragment.system_setting.SystemSettingFragment;
import com.wopin.qingpaopao.fragment.user_guide.UserGuideFragment;
import com.wopin.qingpaopao.http.HttpClient;
import com.wopin.qingpaopao.presenter.BasePresenter;
import com.wopin.qingpaopao.presenter.LoginPresenter;
import com.wopin.qingpaopao.presenter.MinePresenter;
import com.wopin.qingpaopao.presenter.PhotoPresenter;
import com.wopin.qingpaopao.utils.GlideUtils;
import com.wopin.qingpaopao.utils.HttpUtil;
import com.wopin.qingpaopao.utils.SPUtils;
import com.wopin.qingpaopao.utils.ToastUtils;
import com.wopin.qingpaopao.view.MineView;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.sharesdk.onekeyshare.OnekeyShare;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MineFragment extends BaseMainFragment<MinePresenter> implements MineGridRvAdapter.MineGridRvCallback, MineListRvAdapter.MineListRvCallback, View.OnClickListener, MineView {

    private ImageView mMessageIv;
    private TextView mMessageCountTv;
    private ImageView mHeadIconIv;
    private TextView mPhoneNumberTv;
    private TextView mUsernameTv;
    private TextView mScoreTv;
    private TextView mCurrentDrinkTv;
    private TextView mTotalDrinkTv;
    private DrinkListTodayRsp mDrinkListTodayRsp;
    private DrinkListTotalRsp mDrinkListTotalRsp;
    private TextView mSignInTv;

    @Override
    protected int getLayout() {
        return R.layout.fragment_mine;
    }

    @Override
    protected MinePresenter initPresenter() {
        return new MinePresenter(this, this);
    }

    @Override
    protected void initView(View rootView) {
        RecyclerView gridRecyclerView = rootView.findViewById(R.id.grid_recyclerview);
        gridRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 4));
        gridRecyclerView.setAdapter(new MineGridRvAdapter(this));

        RecyclerView listRecyclerView = rootView.findViewById(R.id.list_recyclerview);
        listRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        listRecyclerView.setAdapter(new MineListRvAdapter(
                new int[]{R.string.my_collect, R.string.my_fans, R.string.my_focus},
                new int[]{R.mipmap.pc_source, R.mipmap.h_profile2, R.mipmap.r_head},
                this
        ));

        mMessageIv = rootView.findViewById(R.id.iv_message);
        mMessageCountTv = rootView.findViewById(R.id.count_message);
        mHeadIconIv = rootView.findViewById(R.id.iv_person_head_icon);
        mPhoneNumberTv = rootView.findViewById(R.id.tv_phone_number);
        mUsernameTv = rootView.findViewById(R.id.tv_username);
        mScoreTv = rootView.findViewById(R.id.score);
        mCurrentDrinkTv = rootView.findViewById(R.id.number_current_drink_quantity);
        mTotalDrinkTv = rootView.findViewById(R.id.number_total_drink_quantity);

        mSignInTv = rootView.findViewById(R.id.tv_sign_in);
        String recordTime = (String) SPUtils.get(getContext(), Constants.SIGN_IN_DATA, "");
        String todayTime = getTodayTime();
        if (todayTime.equals(recordTime)) {
            mSignInTv.setText(R.string.had_sign_in);
        } else {
            mSignInTv.setOnClickListener(this);
        }
        mMessageIv.setOnClickListener(this);
        mHeadIconIv.setOnClickListener(this);
    }

    @Override
    protected void initEvent() {

    }

    @Override
    public void refreshData() {
        LoginRsp accountMessage = LoginPresenter.getAccountMessage();
        if (accountMessage != null) {
            LoginRsp.ResultBean result = accountMessage.getResult();
            mPhoneNumberTv.setText(result.getPhone());
            mUsernameTv.setText(result.getUserName());
            mScoreTv.setText(result.getScores() + " " + getString(R.string.score));
            String iconUrl = result.getIcon();
            GlideUtils.loadImage(mHeadIconIv, R.mipmap.i_profileicon, iconUrl, new CircleCrop(), new CenterCrop());
            mPresenter.requestDrinkData();
        }
    }

    @Override
    public void onGridItemClick(int textResource, int position) {
        switch (textResource) {
            case R.string.my_drinking:
                MyDrinkingFragment.build(mDrinkListTodayRsp, mDrinkListTotalRsp).show(getChildFragmentManager(), MyDrinkingFragment.TAG);
                break;
            case R.string.my_health:
                new MyHealthFragment().show(getChildFragmentManager(), MyHealthFragment.TAG);
                break;
            case R.string.information_edit:
                InformationEditFragment informationEditFragment = new InformationEditFragment();
                informationEditFragment.show(getChildFragmentManager(), InformationEditFragment.TAG);
                informationEditFragment.setOnBaseBarDialogFragmentCallback(new BaseBarDialogFragment.OnBaseBarDialogFragmentCallback() {
                    @Override
                    public void onDismiss() {
                        refreshData();
                    }
                });
                break;
            case R.string.invite_friends:
                showShare();
                break;
            case R.string.focus_subtitle:
                new FollowSubtitleFragment().show(getChildFragmentManager(), FollowSubtitleFragment.TAG);
                break;
            case R.string.history_list:
                new HistorySubtitleFragment().show(getChildFragmentManager(), HistorySubtitleFragment.TAG);
                break;
            case R.string.user_guide:
                new UserGuideFragment().show(getChildFragmentManager(), UserGuideFragment.TAG);
                break;
            case R.string.system_setting:
                new SystemSettingFragment().show(getChildFragmentManager(), SystemSettingFragment.TAG);
                break;
        }
    }

    @Override
    public void onListItemClick(int textResource, int position) {
        switch (textResource) {
            case R.string.my_collect:
                new CollectSubtitleFragment().show(getChildFragmentManager(), CollectSubtitleFragment.TAG);
                break;
            case R.string.my_fans:
                new MyFansFragment().show(getChildFragmentManager(), MyFansFragment.TAG);
                break;
            case R.string.my_focus:
                new MyFollowFragment().show(getChildFragmentManager(), MyFollowFragment.TAG);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_sign_in://签到
                HttpUtil.subscribeNetworkTask(
                        HttpClient.getApiInterface().attendance().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()),
                        new BasePresenter.MyObserver<NormalRsp>() {
                            @Override
                            public void onMyNext(NormalRsp normalRsp) {
                                mPresenter.refreshUserData();
                                SPUtils.put(getContext(), Constants.SIGN_IN_DATA, getTodayTime());
                                mSignInTv.setText(R.string.had_sign_in);
                                ToastUtils.showShort(getString(R.string.sign_in_success));
                            }

                            @Override
                            public void onMyError(String errorMessage) {
                                ToastUtils.showShort(errorMessage);
                            }
                        }
                );
                break;
            case R.id.iv_person_head_icon:
                mPresenter.requestPermissionTodo(PhotoPresenter.REQUEST_PERMISSION_ALBUM);
                break;
            case R.id.iv_message:
                MessageMainFragment messageMainFragment = new MessageMainFragment();
                messageMainFragment.show(getChildFragmentManager(), MessageMainFragment.TAG);
                messageMainFragment.setMessageMainFragmentCallback(new MessageMainFragment.MessageMainFragmentCallback() {
                    @Override
                    public void onDismiss() {
                        refreshData();
                    }
                });
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mPresenter.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mPresenter.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onDestroy() {
        mPresenter.deletePhotoFile();
        super.onDestroy();
    }

    private String getTodayTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        return simpleDateFormat.format(new Date());
    }

    private void showShare() {
        OnekeyShare oks = new OnekeyShare();
        //关闭sso授权
        oks.disableSSOWhenAuthorize();

// 分享时Notification的图标和文字  2.5.9以后的版本不调用此方法
        //oks.setNotification(R.drawable.ic_launcher, getString(R.string.app_name));
        // title标题，印象笔记、邮箱、信息、微信、人人网和QQ空间使用
        oks.setTitle("邀请您一起");
        // titleUrl是标题的网络链接，仅在人人网和QQ空间使用
        oks.setTitleUrl("http://wifi.h2popo.com:8081/downloadApp");
        // text是分享文本，所有平台都需要这个字段
        oks.setText("一杯好水改变生活轨迹");
        oks.setImageUrl("https://is3-ssl.mzstatic.com/image/thumb/Purple118/v4/f4/1c/64/f41c64d3-21e4-6e66-26d4-9b48872fd3c9/AppIcon-1x_U007emarketing-85-220-3.png/230x0w.jpg");//确保SDcard下面存在此张图片
        // url仅在微信（包括好友和朋友圈）中使用
        oks.setUrl("http://wifi.h2popo.com:8081/downloadApp");
        // comment是我对这条分享的评论，仅在人人网和QQ空间使用
//        oks.setComment("我是测试评论文本");
        // site是分享此内容的网站名称，仅在QQ空间使用
        oks.setSite(getString(R.string.app_name));
        // siteUrl是分享此内容的网站地址，仅在QQ空间使用
        oks.setSiteUrl("http://wifi.h2popo.com:8081/downloadApp");

// 启动分享GUI
        oks.show(getContext());
    }

    @Override
    public void onTodayDrink(DrinkListTodayRsp drinkListTodayRsp) {
        mDrinkListTodayRsp = drinkListTodayRsp;
        int count = 0;
        if (drinkListTodayRsp != null && drinkListTodayRsp.getResult() != null) {
            count = drinkListTodayRsp.getResult().getDrinks().size();
        }
        mCurrentDrinkTv.setText(String.format(getString(R.string.cup), count));
    }

    @Override
    public void onTotalDrink(DrinkListTotalRsp drinkListTotalRsp) {
        mDrinkListTotalRsp = drinkListTotalRsp;
        int count = 0;
        if (drinkListTotalRsp != null && drinkListTotalRsp.getResult() != null) {
            for (DrinkListTotalRsp.ResultBean resultBean : drinkListTotalRsp.getResult()) {
                count += resultBean.getDrinks().size();
            }
        }
        mTotalDrinkTv.setText(String.format(getString(R.string.cup), count));
    }

    @Override
    public void onRefreshUserData() {
        refreshData();
    }

    @Override
    public void onNewMessage(CheckNewMessageRsp checkNewMessageRsp) {
        int count = checkNewMessageRsp.getResult().getCount();
        if (count > 0) {
            mMessageCountTv.setText(String.valueOf(count));
            mMessageCountTv.setVisibility(View.VISIBLE);
        } else {
            mMessageCountTv.setVisibility(View.GONE);
        }
    }

    @Override
    public void onError(String errorMessage) {
        ToastUtils.showShort(errorMessage);
    }
}
