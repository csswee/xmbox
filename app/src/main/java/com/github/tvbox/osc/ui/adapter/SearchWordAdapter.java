package com.github.tvbox.osc.ui.adapter;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchWordAdapter extends BaseQuickAdapter<String, BaseViewHolder> {
    private int selectedPosition = 0;
    private Map<String, String> wordKeys = new HashMap<>();

    public SearchWordAdapter() {
        super(R.layout.item_search_word, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, String item) {
        TextView tvWord = helper.getView(R.id.tvWord);
        tvWord.setText(item);

        if (helper.getLayoutPosition() == selectedPosition) {
            tvWord.setTextColor(helper.itemView.getContext().getResources().getColor(R.color.md3_on_primary, null)); // 选中状态颜色
            helper.itemView.setBackgroundResource(R.drawable.shape_setting_sort_focus);
        } else {
            tvWord.setTextColor(helper.itemView.getContext().getResources().getColor(R.color.md3_on_surface_variant, null)); // 未选中状态颜色
            helper.itemView.setBackgroundResource(R.drawable.shape_setting_sort_normal);
        }
    }

    public void setSelectedPosition(int position) {
        if (position >= 0 && position < getData().size()) {
            selectedPosition = position;
            notifyDataSetChanged();
        }
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setNewDataWithKeys(List<String> data, Map<String, String> keys) {
        this.wordKeys.clear();
        if (keys != null) {
            this.wordKeys.putAll(keys);
        }
        setNewData(data);
    }

    public String getSelectedKey() {
        if (selectedPosition >= 0 && selectedPosition < getData().size()) {
            return wordKeys.get(getData().get(selectedPosition));
        }
        return "";
    }
}