package com.example.signalcollection.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.example.signalcollection.activity.MyTaskActivity;
import com.example.signalcollection.R;
import com.example.signalcollection.bean.UserInfoRequest;
import com.example.signalcollection.bean.WorkListResult;
import com.example.signalcollection.network.WorkService;
import com.example.signalcollection.util.TaskStatus;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.signalcollection.util.SPUtils;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.view.LoadingDialog;

import butterknife.BindView;
import rx.Subscriber;
import rx.Subscription;

/**
 * 待提交的任务的fragment
 * Created by Konmin on 2016/7/27.
 */
public class AlreadyAuditFragment extends BaseFragment {


    private LoadingDialog mLoadingDialog;
    private List<WorkListResult.DataBean> mAlreadyAuditTaskList = new ArrayList<>();
    private CommonAdapter<WorkListResult.DataBean> mCommonAdapter;


    @BindView(R.id.rvContent)
    RecyclerView mRecyclerView;


    @BindView(R.id.srf)
    SwipeRefreshLayout refreshLayout;

    private TaskStatus mTaskStatus;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLoadingDialog = new LoadingDialog(getActivity());

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initFreshLayout();
        mTaskStatus = new TaskStatus();
        mCommonAdapter = new CommonAdapter<WorkListResult.DataBean>(getActivity(), R.layout.item_my_task, mAlreadyAuditTaskList) {
            @Override
            public void convert(ViewHolder holder, WorkListResult.DataBean dataBean) {
                holder.setText(R.id.tvAreaName, dataBean.getAreaName());
                holder.setText(R.id.tvAuditStatus, mTaskStatus.getStatusMsg(dataBean.getStatusResult()));
            }
        };
        mRecyclerView.setAdapter(mCommonAdapter);
    }


    private void initFreshLayout() {
        refreshLayout.setColorSchemeColors(R.color.themeBlue);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadAlreadyAuditData2();
            }
        });


    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            loadAlreadyAuditData();
        }
    }


    private void loadAlreadyAuditData2() {


        Logger.i("loadAlreadyAuditData");

        UserInfoRequest infoRequest = new UserInfoRequest();
        infoRequest.setUserName(SPUtils.getUserName());
        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().getChecked(infoRequest)).subscribe(new Subscriber<WorkListResult>() {
            @Override
            public void onCompleted() {
                refreshLayout.setRefreshing(false);
            }

            @Override
            public void onError(Throwable e) {
                refreshLayout.setRefreshing(false);
                mLoadingDialog.dismiss();
                showTest(mNetWorkError);
                e.printStackTrace();
            }

            @Override
            public void onNext(WorkListResult workListResult) {
                if (workListResult.getRetCode() == 0) {


                    mAlreadyAuditTaskList.clear();
                    mAlreadyAuditTaskList.addAll(workListResult.getData());
                    Logger.i("mAlreadyAuditTaskList size" + mAlreadyAuditTaskList.size());
                    mCommonAdapter.notifyDataSetChanged();
                } else {
                    showTest(workListResult.getMsg());
                }
            }
        });

        ((MyTaskActivity) getActivity()).addSubscription(sbMyAccount);
    }


    private void loadAlreadyAuditData() {
        Logger.i("loadAlreadyAuditData");
        mLoadingDialog.show();
        UserInfoRequest infoRequest = new UserInfoRequest();
        infoRequest.setUserName(SPUtils.getUserName());
        Subscription sbMyAccount = wrapObserverWithHttp(WorkService.getWorkService().getChecked(infoRequest)).subscribe(new Subscriber<WorkListResult>() {
            @Override
            public void onCompleted() {
                mLoadingDialog.dismiss();
            }

            @Override
            public void onError(Throwable e) {
                mLoadingDialog.dismiss();
                showTest(mNetWorkError);
                e.printStackTrace();
            }

            @Override
            public void onNext(WorkListResult workListResult) {
                if (workListResult.getRetCode() == 0) {
                    mAlreadyAuditTaskList.clear();
                    mAlreadyAuditTaskList.addAll(workListResult.getData());
                    Logger.i("mWaitAuditTaskList size" + mAlreadyAuditTaskList.size());
                    mCommonAdapter.notifyDataSetChanged();
                } else {
                    showTest(workListResult.getMsg());
                }
            }
        });

        ((MyTaskActivity) getActivity()).addSubscription(sbMyAccount);
    }


    @Override
    public int createSuccessView() {
        return R.layout.fragment_my_task;
    }

    public void setData(List<WorkListResult.DataBean> data) {
        mAlreadyAuditTaskList = data;
        Logger.i("setData");
    }


}
