package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.github.tvbox.osc.R;
import com.lxj.xpopup.core.BottomPopupView;

import org.jetbrains.annotations.NotNull;

public class AboutDialog extends BottomPopupView {

    public AboutDialog(@NonNull @NotNull Context context) {
        super(context);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_about_m3;
    }

    @Override
    protected void onCreate() {
        super.onCreate();

    }
}