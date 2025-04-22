package com.github.tvbox.osc.ui.adapter;

import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.bean.DoubanSuggestBean;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.picasso.RoundTransformation;
import com.github.tvbox.osc.util.MD5;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import me.jessyan.autosize.utils.AutoSizeUtils;

public class DoubanSuggestAdapter extends BaseQuickAdapter<DoubanSuggestBean, BaseViewHolder> {
    public DoubanSuggestAdapter(List<DoubanSuggestBean> list) {
        super(R.layout.item_douban_suggest, list);
    }

    @Override
    protected void convert(BaseViewHolder helper, DoubanSuggestBean item) {
        helper.setText(R.id.tvName,item.getTitle())
                .setText(R.id.tvRating,"豆瓣: "+item.getDoubanRating()+"\n烂番茄: "+item.getRottenRating()+"\nIMDB: "+item.getImdbRating());

        Picasso.get()
                .load(item.getImg())
                .transform(new RoundTransformation(MD5.string2MD5(item.getImg() + "position=" + helper.getLayoutPosition()))
                        .centerCorp(true)
                        .override(AutoSizeUtils.dp2px(mContext, 110), AutoSizeUtils.dp2px(mContext, 160))
                        .roundRadius(AutoSizeUtils.dp2px(mContext, 6), RoundTransformation.RoundType.ALL))
                .placeholder(R.drawable.img_loading_placeholder)
                .error(R.drawable.img_loading_placeholder)
                .into((ImageView) helper.getView(R.id.ivThumb));
    }
}