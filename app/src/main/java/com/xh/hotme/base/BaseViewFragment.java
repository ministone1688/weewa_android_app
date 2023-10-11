package com.xh.hotme.base;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewbinding.ViewBinding;

import com.xh.hotme.R;
import com.xh.hotme.utils.AppTrace;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;

/**
 * 基类 处理ViewBinding
 *
 * @param <T>
 */
public abstract class BaseViewFragment<T extends ViewBinding> extends Fragment {
    protected T viewBinding;
    private boolean needInit = false;
    private LayoutInflater inflater;
    public View rootView;
    public final String TAG = getClass().getSimpleName();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        AppTrace.d("BaseFragment onCreateView " + TAG + " , viewBinding is null ? " + (viewBinding == null));
        this.inflater = inflater;
        if (viewBinding == null) {
            initViewBinding(inflater, container);
            if (viewBinding != null) {
                needInit = true;
                rootView = viewBinding.getRoot();
                initView();
            } else {
                rootView = getErrLayout(inflater, container);
            }
        }
        if (!dateHasLoad()) {
            needInit = true;
        }
        return rootView;
    }


    /**
     * 数据加载完成
     *
     * @return
     */
    public boolean dateHasLoad() {
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (needInit) {
            initData();
            needInit = false;
        }
    }

    protected abstract void initView();

    protected abstract void initData();

    private View getErrLayout(LayoutInflater inflater, ViewGroup container) {
        View view = inflater.inflate(R.layout.activity_inititial_err, container, false);
        return view;
    }

    public void initViewBinding(LayoutInflater inflater, ViewGroup container) {
        ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
        assert type != null;
        Class cls = (Class) type.getActualTypeArguments()[0];
        try {
            Method inflate = cls.getDeclaredMethod("inflate", LayoutInflater.class, ViewGroup.class, boolean.class);
            viewBinding = (T) inflate.invoke(null, inflater, container, false);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public <K extends View> K findViewById(@IdRes int id) {
        return rootView.findViewById(id);
    }

    public <VM extends ViewModel> VM bindViewModel(Class<VM> clazz) {
        return ViewModelProvider.AndroidViewModelFactory.getInstance(getActivity().getApplication()).create(clazz);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (getClass().isAnnotationPresent(IEventBus.class)) {
            try {
                EventBus.getDefault().register(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (getClass().isAnnotationPresent(IEventBus.class)) {
            try {
                EventBus.getDefault().unregister(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
