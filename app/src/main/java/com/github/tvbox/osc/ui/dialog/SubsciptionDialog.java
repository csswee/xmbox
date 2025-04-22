package com.github.tvbox.osc.ui.dialog;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.ToastUtils;
import com.github.tvbox.osc.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.radiobutton.MaterialRadioButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.lxj.xpopup.core.CenterPopupView;

/**
 * @Author : Liu XiaoRan
 * @Email : 592923276@qq.com
 * @Date : on 2023/8/17 09:28.
 * @Description :
 */
public class SubsciptionDialog extends CenterPopupView {

    public interface OnSubsciptionListener {
        void onConfirm(String name,String url,boolean check);
    }

    private final String mDefaultName;
    private OnSubsciptionListener listener;

    public SubsciptionDialog(@NonNull Context context, String defaultName, OnSubsciptionListener listener) {
        super(context);
        mDefaultName = defaultName;
        this.listener = listener;
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_input_subscription_m3;
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        View rootView = getPopupImplView();

        TextInputEditText etName = rootView.findViewById(R.id.et_name);
        TextInputEditText etUrl = rootView.findViewById(R.id.et_url);
        MaterialCheckBox cbCheck = rootView.findViewById(R.id.cb_check);
        MaterialButton btnCancel = rootView.findViewById(R.id.btn_cancel);
        MaterialButton btnConfirm = rootView.findViewById(R.id.btn_confirm);

        etName.setText(mDefaultName);
        etName.setSelection(mDefaultName.length());

        // MaterialCheckBox 自带切换功能，无需额外代码

        btnCancel.setOnClickListener(v -> dismiss());

        btnConfirm.setOnClickListener(view -> {
            String name = etName.getText().toString().trim();
            if (name.isEmpty()) {
                ToastUtils.showShort("请输入名称");
                return;
            }
            String url = etUrl.getText().toString().trim();
            if (url.isEmpty()) {
                ToastUtils.showShort("请输入订阅地址");
                return;
            }
            if (listener != null) {
                listener.onConfirm(name, url, cbCheck.isChecked());
            }
            dismiss();
        });


    }
}