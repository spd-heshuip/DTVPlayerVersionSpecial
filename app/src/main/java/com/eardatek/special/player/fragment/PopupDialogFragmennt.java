package com.eardatek.special.player.fragment;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eardatek.special.player.R;


/**
 * Created by Administrator on 16-7-28.
 */
public class PopupDialogFragmennt extends DialogFragment{

    public static final String TOP_STATE = "istop";

    private DialogItemOnClickListener itemOnClickListener;
    @Nullable
    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_popup,container,false);
        TextView onTopTv = (TextView) view.findViewById(R.id.on_top_tv);
        TextView cancelTv = (TextView) view.findViewById(R.id.cancel_top_tv);

        Bundle bundle = getArguments();
        int isTop = bundle.getInt(TOP_STATE);
        if (isTop == 1){
            onTopTv.setVisibility(View.GONE);
            cancelTv.setVisibility(View.VISIBLE);
        }else if (isTop == 0){
            onTopTv.setVisibility(View.VISIBLE);
            cancelTv.setVisibility(View.GONE);
        }

        onTopTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
                if (itemOnClickListener != null){
                    itemOnClickListener.onTop();
                }
            }
        });

        cancelTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
                if (itemOnClickListener != null){
                    itemOnClickListener.onCancel();
                }
            }
        });

        getDialog().getWindow().requestFeature(STYLE_NO_TITLE);
        setStyle(STYLE_NO_FRAME,android.R.style.Theme_Light);
        setCancelable(true);
        getDialog().getWindow().setBackgroundDrawableResource(R.color.white);
        return view;
    }

    public void setItemOnClickListener(DialogItemOnClickListener listener) {
        this.itemOnClickListener = listener;
    }

    public interface DialogItemOnClickListener{

        void onTop();

        void onCancel();
    }
}
