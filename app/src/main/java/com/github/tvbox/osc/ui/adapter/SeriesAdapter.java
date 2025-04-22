package com.github.tvbox.osc.ui.adapter;

import android.graphics.Color;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blankj.utilcode.util.ConvertUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.VodInfo;
import com.lihang.ShadowLayout;

import java.util.ArrayList;

/**
 * @author pj567
 * @date :2020/12/22
 * @description:
 */
public class SeriesAdapter extends BaseQuickAdapter<VodInfo.VodSeries, BaseViewHolder> {
    private boolean isGird;

    public SeriesAdapter(boolean isGird) {
        super(R.layout.item_series, new ArrayList<>());
        this.isGird = isGird;
    }

    @Override
    protected void convert(BaseViewHolder helper, VodInfo.VodSeries item) {
        ShadowLayout sl = helper.getView(R.id.sl);
        TextView tvSeries = helper.getView(R.id.tvSeries);
        sl.setSelected(item.selected);

        // 确保所有集数名称都能正确显示
        String seriesName = item.name;
        // 如果是纯数字，确保显示出来
        tvSeries.setText(seriesName);
        // 设置文本颜色为白色，增强可见度
        tvSeries.setTextColor(helper.itemView.getContext().getResources().getColor(android.R.color.white));

        if (!isGird){// 详情页横向展示时固定宽度
            ViewGroup.LayoutParams layoutParams = sl.getLayoutParams();
            layoutParams.width = ConvertUtils.dp2px(120);
            sl.setLayoutParams(layoutParams);
        } else {
            // 如果是网格布局，检查是否是横屏模式
            if (helper.itemView.getContext().getResources().getConfiguration().orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE) {
                // 横屏模式下调整文本大小
                tvSeries.setTextSize(14);
            }
        }
    }

    public void setGird(boolean gird) {
        isGird = gird;
    }

}