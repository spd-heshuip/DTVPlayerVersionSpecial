package com.eardatek.player.dtvplayer.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

/**
 * Created by Administrator on 16-4-5.
 */
public  class StatedFragment extends Fragment{

    protected Bundle saveBundle;

    public StatedFragment() {
        if (getArguments() == null){
            setArguments(new Bundle());
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        restoreState();
        if (!restoresStateFromArguments())
            onFirstTimeLaunch();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveStateToArguments();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        saveStateToArguments();
    }

    protected void onFirstTimeLaunch(){

    }

    private void saveStateToArguments(){
        if (getView() != null)
            saveBundle = saveState();
        if (saveBundle != null){
            Bundle b = getArguments();
            b.putBundle("internalSavedViewState8954201239547", saveBundle);
        }
    }

    private boolean restoresStateFromArguments(){
        Bundle b = getArguments();
        saveBundle = b.getBundle("internalSavedViewState8954201239547");
        if (saveBundle != null){
            restoreState();
            return true;
        }
        return false;
    }

    private void restoreState(){
        if (saveBundle != null){
            onRestore(saveBundle);
        }
    }

    private Bundle saveState(){
        Bundle state = new Bundle();
        onSaveState(state);
        return state;
    }

    protected void onSaveState(Bundle bundle){

    }

    protected void onRestore(Bundle restoreBundle){

    }

}
