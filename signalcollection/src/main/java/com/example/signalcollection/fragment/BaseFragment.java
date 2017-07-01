package com.example.signalcollection.fragment;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.signalcollection.R;
import com.example.signalcollection.activity.BaseActivity;
import com.example.signalcollection.util.UIUtils;
import com.orhanobut.logger.Logger;

import butterknife.ButterKnife;
import butterknife.Unbinder;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * <p/>
 * to handle interaction events.
 * <p/>
 * create an instance of this fragment.
 */
public abstract class BaseFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public String mNetWorkError = "请检查网络";


    private Unbinder unbinder;
    FrameLayout mFlytFragmentContent;

    View mSuccessView;
    FrameLayout.LayoutParams mBaseLayoutParams;
    protected BaseActivity mBaseActivity;
    private Snackbar mSnackbar;

    public BaseFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            getFragmentArguments(getArguments());
        }
    }

    public void getFragmentArguments(Bundle bundle) {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_base, container, false);
        mFlytFragmentContent = (FrameLayout) view.findViewById(R.id.flytFragmentContent);
        mBaseLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        int successLayoutId = createSuccessView();
        mSuccessView = UIUtils.inflate(getActivity(), successLayoutId);
        unbinder = ButterKnife.bind(this, mSuccessView);
        mSuccessView.setLayoutParams(mBaseLayoutParams);
        if (null != mFlytFragmentContent) {
            mFlytFragmentContent.addView(mSuccessView);
        }
        return view;
    }

    public abstract int createSuccessView();

    @Override
    public void onAttach(Activity context) {
        Logger.i("onAttach");
        super.onAttach(context);
        if (context instanceof BaseActivity) {
            mBaseActivity = (BaseActivity) context;
            mBaseActivity.setFunctionsForFragment(getTag());
        }
    }

    @Override
    public void onDetach() {
        Logger.i("onAttach");
        super.onDetach();

    }

    public void showTest(String text, View view) {
        mSnackbar = Snackbar.make(view, text, Snackbar.LENGTH_SHORT);
        setSnackbarMessageTextColor(mSnackbar, Color.WHITE);
        mSnackbar.show();
    }

    public void setSnackbarMessageTextColor(Snackbar snackbar, int color) {
        View view = snackbar.getView();
        ((TextView) view.findViewById(R.id.snackbar_text)).setTextColor(color);
    }


    public void showTest(String text) {
        mSnackbar = Snackbar.make(mFlytFragmentContent, text, Snackbar.LENGTH_SHORT);
        setSnackbarMessageTextColor(mSnackbar, Color.WHITE);
        mSnackbar.show();
    }


    private boolean mIsOpenLog = true;

    public void l(String log) {
        if (mIsOpenLog) {
            Logger.t(this.getClass().getSimpleName() + "1").i(log);
            // Log.i(this.getClass().getSimpleName() + "1", log);
        }
    }


    //ImageView mIvLoading;
    //private Animation mOperatingAnim;


    public void baseStartActivity(Class intentClass) {
        Intent intent = new Intent(getActivity(), intentClass);
        getActivity().startActivity(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unbinder.unbind();
    }

    public Observable wrapObserverWithHttp(Observable observable) {
        return observable.subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
    }
}
