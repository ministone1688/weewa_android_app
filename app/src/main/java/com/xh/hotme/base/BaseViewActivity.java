package com.xh.hotme.base;

import android.os.Bundle;
import android.view.LayoutInflater;

import androidx.annotation.Keep;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.viewbinding.ViewBinding;

import com.xh.hotme.R;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Created by liu hong liang on 2017/4/27.
 */
@Keep
public abstract class BaseViewActivity <T extends ViewBinding>extends BaseActivity {

    protected T viewBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        Type superclass = getClass().getGenericSuperclass();
        Class<?> aClass = (Class<?>) ((ParameterizedType) superclass).getActualTypeArguments()[0];
        try {
            Method method = aClass.getDeclaredMethod("inflate", LayoutInflater.class);
            viewBinding = (T) method.invoke(null, getLayoutInflater());
            if (viewBinding == null) {
                onViewInitErr();
            } else {
                setContentView(viewBinding.getRoot());
                initView();
                initData();
            }

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
//            Logger.error(mClassName, " onCreate Error");
            e.printStackTrace();
        }
    }


    public void onViewInitErr() {
        setContentView(R.layout.activity_inititial_err);
        findViewById(R.id.button).setOnClickListener(v -> finish());
    }

    public T getViewBinding() {
        return viewBinding;
    }

    public void setViewBinding(T viewBinding) {
        this.viewBinding = viewBinding;
    }

    /**
     * 初始化布局View
     */
    protected abstract void initView();

    /**
     * 初始化数据
     */
    protected abstract void initData();

}
