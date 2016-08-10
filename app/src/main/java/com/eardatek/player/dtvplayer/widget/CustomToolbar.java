package com.eardatek.player.dtvplayer.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatDrawableManager;
import android.support.v7.widget.TintTypedArray;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.eardatek.player.dtvplayer.R;
import com.eardatek.player.dtvplayer.system.DTVApplication;


/**
 * Created by Administrator on 15-10-12.
 */
public class CustomToolbar extends Toolbar {

    private final static String TAG = "CustomToolbar";
    private LayoutInflater mInflater;

    private TextView mTextTitle;
    private Button mRightButton;

    private Context mContext;

    public Button getRightButton() {
        return mRightButton;
    }

    private Button mLeftButton;
    private View mView;

//    private Tint mTintManager;

    public CustomToolbar(Context context) {
        this(context, null);
    }

    public CustomToolbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomToolbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        initView();
        setContentInsetsRelative(10, 10);
        if (attrs != null) {
            final TintTypedArray a = TintTypedArray.obtainStyledAttributes(getContext(), attrs,
                    R.styleable.CustomToolbar, defStyleAttr, 0);

            final Drawable rightIcon = a.getDrawable(R.styleable.CustomToolbar_rightButtonIcon);
            if (rightIcon != null) {
                setRightButtonIcon(rightIcon);
            }
            a.recycle();
        }
    }

    private void initView() {
        if (mView == null) {
            mInflater = LayoutInflater.from(getContext());
            mView = mInflater.inflate(R.layout.toolbar, null);

            mTextTitle = (TextView) mView.findViewById(R.id.toolbar_title);
            mRightButton = (Button) mView.findViewById(R.id.toolbar_rightButton);
            mLeftButton = (Button) mView.findViewById(R.id.toolbar_leftButton);

            LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL);
            addView(mView, lp);
        }
    }


    public void setRightButtonIcon(Drawable icon) {
        if (mRightButton != null) {
            mRightButton.setBackground(icon);
            mRightButton.setVisibility(VISIBLE);
        }
    }

    public void setRightButtonIcon(int id) {
        if (mRightButton != null) {
            mRightButton.setBackgroundResource(id);
            mRightButton.setVisibility(VISIBLE);
        }
    }

    public void setRightButtonOnClickListener(OnClickListener listener) {
        mRightButton.setOnClickListener(listener);
    }

    @Override
    public void setTitle(int resId) {
        setTitle(getContext().getText(resId));
    }

    @Override
    public void setTitle(CharSequence title) {
        initView();
        if (mTextTitle != null) {
            mTextTitle.setText(title);
            showTitleView();
        }
    }

    @Override
    public void setNavigationIcon(int resId) {
        setNavigationIcon(AppCompatDrawableManager.get().getDrawable(DTVApplication.getAppContext(),resId));
    }

    @Override
    public void setNavigationIcon(Drawable icon) {
        initView();
        if (mLeftButton != null) {
            if (icon != null) {
                mLeftButton.setBackground(icon);
                mLeftButton.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void setNavigationOnClickListener(OnClickListener listener) {
        if (mLeftButton != null) {
            mLeftButton.setOnClickListener(listener);
        }
    }

    public void showNavigationIcon() {
        if (mLeftButton != null) {
            mLeftButton.setVisibility(View.VISIBLE);
        }
    }

    public void showTitleView() {
        if (mTextTitle != null)
            mTextTitle.setVisibility(VISIBLE);
    }

    public void hideTitleView() {
        if (mTextTitle != null)
            mTextTitle.setVisibility(GONE);

    }
}
