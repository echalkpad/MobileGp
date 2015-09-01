package com.yuwell.mobilegp.ui.widget;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.totoro.commons.adapter.AbstractAdapter;
import com.totoro.commons.adapter.BaseViewHolder;
import com.yuwell.mobilegp.R;

import java.util.Arrays;

import in.srain.cube.util.LocalDisplay;

/**
 * 下拉列表
 * Created by Chen on 15-4-1.
 */
public class PopupListView extends LinearLayout {

    private PopupWindow mPopupWindow;

    private TextView mLevel;

    private ItemAdapter mAdapter;

    private OnItemClickListener onItemClickListener;

    public PopupListView(Context context) {
        this(context, null);
    }

    public PopupListView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PopupListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        LayoutInflater.from(context).inflate(R.layout.popup_level, this);
        mLevel = (TextView) findViewById(R.id.tv_level);
        mLevel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mPopupWindow.isShowing()) {
                    mPopupWindow.showAsDropDown(mLevel, 0, -1);
                } else {
                    mPopupWindow.dismiss();
                }
            }
        });

        ListView mListView = new ListView(context);
        mListView.setBackgroundResource(R.drawable.btn_grey_border);
        mListView.setDivider(new ColorDrawable(getResources().getColor(R.color.grey_border)));
        mListView.setDividerHeight(LocalDisplay.dp2px(1));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mLevel.setText(mAdapter.getItem(position));
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position);
                }
                mPopupWindow.dismiss();
            }
        });
        mAdapter = new ItemAdapter(context, ViewHolder.class, R.layout.item_popup_window);
        mListView.setAdapter(mAdapter);

        mPopupWindow = new PopupWindow(mListView, LocalDisplay.dp2px(70), ViewGroup.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setFocusable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
        mPopupWindow.setOutsideTouchable(true);
    }

    public void setLevels(int arrayRes) {
        String[] array = getResources().getStringArray(arrayRes);
        mAdapter.setData(Arrays.asList(array));
        mLevel.setText(array[0]);
    }

    public void setText(int res) {
        mLevel.setText(res);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    private final class ItemAdapter extends AbstractAdapter<String, ViewHolder> {

        public ItemAdapter(Context mContext, Class<? extends BaseViewHolder> clazz, int itemLayoutId) {
            super(mContext, clazz, itemLayoutId);
        }

        @Override
        public void getItemView(int position, ViewHolder viewHolder) {
            viewHolder.mTextView.setText(getItem(position));
        }
    }

    public static final class ViewHolder extends BaseViewHolder {

        TextView mTextView;

        public ViewHolder(View view) {
            super(view);
            mTextView = (TextView) view.findViewById(R.id.tv_item);
        }
    }

    public interface OnItemClickListener {
        public void onItemClick(int pos);
    }
}
