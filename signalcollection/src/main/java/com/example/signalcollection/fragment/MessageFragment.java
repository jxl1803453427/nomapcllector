package com.example.signalcollection.fragment;


import android.app.MediaRouteButton;
import android.content.ContentValues;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.activity.MainActivity;
import com.example.signalcollection.bean.MessageBean;
import com.example.signalcollection.recyclerview.CommonAdapter;
import com.example.signalcollection.recyclerview.OnItemClickListener;
import com.example.signalcollection.recyclerview.ViewHolder;
import com.example.signalcollection.util.UIUtils;
import com.example.signalcollection.view.MessageDetailDialog;
import com.orhanobut.logger.Logger;

import org.litepal.crud.DataSupport;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Observable;
import rx.Subscriber;

/**
 * 消息的列表，
 * Created by Konmin on 2016/11/1.
 */

public class MessageFragment extends BaseFragment {


    @BindView(R.id.rv_content)
    RecyclerView mRecyclerView;

    private CommonAdapter<MessageBean> mAdapter;

    private List<MessageBean> mMessageBeanList;

    private int mCurrentOffset;

    private MessageDetailDialog mMessageDetailDialog;

    @BindView(R.id.tv_load_more)
    TextView tvMoreTask;


    private boolean canScorll = true;
    public static final int PAGE_SIZE = 10;

    @Override

    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mMessageDetailDialog = new MessageDetailDialog(getActivity());
        mMessageDetailDialog.setCancelable(false);
        mMessageBeanList = new ArrayList<>();
        mAdapter = new CommonAdapter<MessageBean>(getActivity(), R.layout.item_message, mMessageBeanList) {
            @Override
            public void convert(ViewHolder holder, MessageBean messageBean) {

                if (messageBean.getStatus() == 0) {
                    holder.setTextColor(R.id.tv_title, getResources().getColor(R.color.red));
                } else {
                    holder.setTextColor(R.id.tv_title, getResources().getColor(R.color.grayText));
                }
                holder.setText(R.id.tv_title, "[" + messageBean.getAreaName() + "]" + messageBean.getTitle());
                if (TextUtils.isEmpty(messageBean.getContent())) {
                    if (messageBean.getRetCode() == 0) {
                        messageBean.setContent("你在" + UIUtils.convertDateTime(messageBean.getPushTime()) + "对商圈【" + messageBean.getAreaName() + "】提交的数据，后台已经成功处理你的数据");
                    } else {
                        messageBean.setContent("你在" + UIUtils.convertDateTime(messageBean.getPushTime()) + "对商圈【" + messageBean.getAreaName() + "】提交的数据，后台处理失败，请重新提交该商圈的数据");
                    }

                }
                holder.setText(R.id.tv_content, messageBean.getContent());
                holder.setText(R.id.tv_time, UIUtils.convertDateTime(messageBean.getRececiveTime()));
            }
        };

        mAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(ViewGroup parent, View view, Object o, int position, ViewHolder viewHolder) {

                final MessageBean bean = mMessageBeanList.get(position);
                mMessageDetailDialog.show(bean, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bean.getStatus() != 1) {
                            bean.setStatus(1);
                            ContentValues values = new ContentValues();
                            values.put("status", 1);
                            int i = DataSupport.updateAll(MessageBean.class, values, "pushtime = ?", bean.getPushTime() + "");
                            Logger.i("更新了" + i + "条数据");
                            ((MainActivity) getActivity()).setMsgMinusOne();
                            mAdapter.notifyDataSetChanged();
                        }
                        mMessageDetailDialog.dismiss();
                    }
                });
            }

            @Override
            public boolean onItemLongClick(ViewGroup parent, View view, Object o, int position, ViewHolder viewHolder) {
                return false;
            }
        });


        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (isSlideToBottom(recyclerView) && canScorll) {
                    canScorll = false;
                    //没有数据的时候这里会被执行
                    Logger.i("is slideTo Bottom:");
                    mCurrentOffset = mMessageBeanList.size();
                    tvMoreTask.setVisibility(View.VISIBLE);
                    loadData(mCurrentOffset);

                }
            }
        });
        mCurrentOffset = 0;
        loadData(0);
    }


    /**
     * 从数据库拉取数据
     * 应该是倒叙的按时间获取
     */
    private void loadData(final int offset) {

        Observable<List<MessageBean>> observable = Observable.create(new Observable.OnSubscribe<List<MessageBean>>() {
            @Override
            public void call(Subscriber<? super List<MessageBean>> subscriber) {
                List<MessageBean> messages = DataSupport.order("rececivetime desc").limit(PAGE_SIZE).offset(offset).find(MessageBean.class);
                subscriber.onNext(messages);
                subscriber.onCompleted();
            }
        });

        wrapObserverWithHttp(observable);
        observable.subscribe(new Subscriber<List<MessageBean>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(List<MessageBean> messageBeen) {
                canScorll = true;
                tvMoreTask.setVisibility(View.GONE);
                if (messageBeen != null && !messageBeen.isEmpty()) {
                    mMessageBeanList.addAll(messageBeen);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }


    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
       /* if (getUserVisibleHint()) {
            //有时候可能dialog还没初始化，这里延迟一点点启动

        }*/

    }

    public void addMessage(MessageBean bean) {
        mMessageBeanList.add(0, bean);
        mAdapter.notifyDataSetChanged();
    }


    protected boolean isSlideToBottom(RecyclerView recyclerView) {
        if (recyclerView == null || recyclerView.getChildCount() == 0) return false;
        if (recyclerView.computeVerticalScrollExtent() + recyclerView.computeVerticalScrollOffset() >= recyclerView.computeVerticalScrollRange())
            return true;
        return false;
    }


    @Override
    public int createSuccessView() {
        return R.layout.fragment_message;
    }
}
