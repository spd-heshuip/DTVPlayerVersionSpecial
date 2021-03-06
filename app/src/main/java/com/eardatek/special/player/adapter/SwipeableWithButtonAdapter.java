package com.eardatek.special.player.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eardatek.special.player.R;
import com.eardatek.special.player.data.AbstractDataProvider;
import com.eardatek.special.player.data.TvDataProvider;
import com.eardatek.special.player.system.DTVApplication;
import com.eardatek.special.player.util.DrawableUtils;
import com.eardatek.special.player.util.LogUtil;
import com.eardatek.special.player.util.PreferencesUtils;
import com.eardatek.special.player.util.ViewUtils;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.SwipeableItemConstants;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultAction;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionDefault;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.action.SwipeResultActionMoveToSwipedDirection;
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableSwipeableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.utils.RecyclerViewAdapterUtils;

import java.util.List;
import java.util.Locale;

/**
 * Created by Administrator on 16-7-25.
 */
public class SwipeableWithButtonAdapter
        extends RecyclerView.Adapter<SwipeableWithButtonAdapter.MyViewHolder>
            implements SwipeableItemAdapter<SwipeableWithButtonAdapter.MyViewHolder>,
            DraggableItemAdapter<SwipeableWithButtonAdapter.MyViewHolder>{

    private static final String TAG = SwipeableWithButtonAdapter.class.getSimpleName();

    private int swipedPosition = -1;

    private int mTopCount = 0;

    private volatile int mSelectItem = -1;

    private boolean isCanMoveByTouchButton = false;

    private StringBuffer mSelectProgram = new StringBuffer("");

    private OnItemMoveListenner onItemMoveListenner;

    // NOTE: Make accessible with short name
    private interface Swipeable extends SwipeableItemConstants {
    }

    private interface Draggable extends DraggableItemConstants {
    }

    private AbstractDataProvider mProvider;
    private EventListener mEventListener;
    private View.OnClickListener mSwipeableViewContainerOnClickListener;
    private View.OnClickListener mUnderSwipeableViewButtonOnClickListener;
    private View.OnClickListener mOnTopButtonOnClickListener;
    private View.OnLongClickListener mContainerOnLongClickListener;

    public interface EventListener {
        void onItemPinned(int position);

        void onItemViewClicked(View v);

        void onUnderSwipeableViewButtonClicked(View v);

        void onContainerLongClicked(View v);

        void onTopButtonClicked(View v);
    }

    public SwipeableWithButtonAdapter(AbstractDataProvider dataProvider) {
        this.mProvider = dataProvider;
        mSwipeableViewContainerOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                LogUtil.i(TAG,"item click");
                onSwipeableViewContainerClick(v);
            }
        };
        mUnderSwipeableViewButtonOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onUnderSwipeableViewButtonClick(v);
            }
        };

        mContainerOnLongClickListener = new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                onContainerLongClick(view);
                return true;
            }
        };

        mOnTopButtonOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onTopButtonClick(view);
            }
        };
        // SwipeableItemAdapter requires stable ID, and also
        // have to implement the getItemId() method appropriately.
        setHasStableIds(true);
    }


    private void onSwipeableViewContainerClick(View v) {
        if (mEventListener != null) {
            mEventListener.onItemViewClicked(
                    RecyclerViewAdapterUtils.getParentViewHolderItemView(v));
        }
    }

    private void onUnderSwipeableViewButtonClick(View v) {
        if (mEventListener != null) {
            mEventListener.onUnderSwipeableViewButtonClicked(
                    RecyclerViewAdapterUtils.getParentViewHolderItemView(v));
        }
    }

    private void onContainerLongClick(View v){
        if (mEventListener != null){
            mEventListener.onContainerLongClicked(RecyclerViewAdapterUtils.getParentViewHolderItemView(v));
        }
    }

    private void onTopButtonClick(View v){
        if (mEventListener != null){
            mEventListener.onTopButtonClicked(RecyclerViewAdapterUtils.getParentViewHolderItemView(v));
        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View v = inflater.inflate(R.layout.video_list_item_grid,parent,false);
        return new MyViewHolder(v);
    }

    @Override
    public long getItemId(int position) {
        return mProvider.getItem(position).getId();
    }

    @Override
    public int getItemViewType(int position) {
        return mProvider.getItem(position).getViewType();
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final AbstractDataProvider.Data item = mProvider.getItem(position);

        LogUtil.i(TAG,item.getText());
        String params[] = item.getText().split("-");
        int freq = Integer.parseInt(params[0].substring(4));
        String name = item.getTitle();
        boolean isEncrypt = item.isEncrypt();

        // set listeners
        // (if the item is *pinned*, click event comes to the mContainer)
        holder.mProgramName.setText(name);
        holder.mFreq.setText(String.format(Locale.ENGLISH,"%dMHz", freq / 1000));
        if (isEncrypt)
            holder.mLock.setVisibility(View.VISIBLE);
        else
            holder.mLock.setVisibility(View.GONE);

        holder.mDeleteButton.setOnClickListener(mUnderSwipeableViewButtonOnClickListener);
        holder.mContainer.setOnClickListener(mSwipeableViewContainerOnClickListener);
        holder.mContainer.setOnLongClickListener(mContainerOnLongClickListener);
        holder.mTopButton.setOnClickListener(mOnTopButtonOnClickListener);

        final int width = holder.mContainer.getWidth();
        holder.mDeleteButton.setWidth(width/5);
        holder.mTopButton.setWidth(width/5);
        holder.mMoveButton.setWidth(width/5);
        holder.mDragHandle.setText(String.format(Locale.ENGLISH,"%d", position + 1));

        if (item.isTop() == 1 && !mSelectProgram.toString().equals(item.getText())){
            holder.mProgramLayout.setBackgroundResource(R.drawable.photo_camera_normal);
            holder.mDragHandle.setBackgroundResource(R.drawable.photo_camera_normal);
            holder.mDragHandle.setTextColor(DTVApplication.getAppContext().getResources().getColor(R.color.white));
            holder.mTopButton.setText(R.string.cancel_top);
            holder.mDivideLine.setBackgroundResource(R.drawable.photo_program_card_normal);
        }else if (item.isTop() == 1 && mSelectProgram.toString().equals(item.getText())){
            holder.mProgramLayout.setBackgroundResource(R.drawable.top_and_selected_background);
            holder.mDragHandle.setBackgroundResource(R.drawable.top_and_selected_background);
            holder.mDragHandle.setTextColor(DTVApplication.getAppContext().getResources().getColor(R.color.white));
            mSelectItem = position;
            holder.mTopButton.setText(R.string.cancel_top);
            holder.mDivideLine.setBackgroundResource(R.drawable.photo_program_card_normal);
        } else if(item.isTop() == 0 && !mSelectProgram.toString().equals(item.getText())){
            holder.mProgramLayout.setBackgroundResource(R.drawable.photo_program_card_normal);
            holder.mDragHandle.setBackgroundResource(R.drawable.photo_program_card_normal);
            holder.mDragHandle.setTextColor(DTVApplication.getAppContext().getResources().getColor(R.color.grey400));
            holder.mTopButton.setText(R.string.top);
            holder.mDivideLine.setBackgroundResource(R.drawable.photo_camera_normal);
        }else if (item.isTop() == 0 && mSelectProgram.toString().equals(item.getText())){
            holder.mProgramLayout.setBackgroundResource(R.drawable.photo_program_card_press);
            holder.mDragHandle.setBackgroundResource(R.drawable.photo_program_card_press);
            holder.mDragHandle.setTextColor(DTVApplication.getAppContext().getResources().getColor(R.color.white));
            mSelectItem = position;
            holder.mTopButton.setText(R.string.top);
            holder.mDivideLine.setBackgroundResource(R.drawable.photo_program_card_normal);
        }

        // set background resource (target view ID: container)
        final int dragState = holder.getDragStateFlags();
        final int swipeState = holder.getSwipeStateFlags();

        if (((dragState & Draggable.STATE_FLAG_IS_UPDATED) != 0) ||
                ((swipeState & Swipeable.STATE_FLAG_IS_UPDATED) != 0)) {
            int bgResId;

            if ((dragState & Draggable.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = R.drawable.bg_item_dragging_active_state;

                // need to clear drawable state here to get correct appearance of the dragging item.
                DrawableUtils.clearState(holder.mContainer.getForeground());
            } else if ((dragState & Draggable.STATE_FLAG_DRAGGING) != 0) {
                bgResId = R.drawable.bg_item_dragging_state;
            } else if ((swipeState & Swipeable.STATE_FLAG_IS_ACTIVE) != 0) {
                bgResId = R.drawable.bg_item_swiping_active_state;
            } else if ((swipeState & Swipeable.STATE_FLAG_SWIPING) != 0) {
                bgResId = R.drawable.bg_item_swiping_state;
            } else {
                bgResId = R.drawable.bg_item_normal_state;
            }

            holder.mContainer.setBackgroundResource(bgResId);
        }
        // set swiping properties
        holder.setMaxLeftSwipeAmount(-0.6f);
        holder.setMaxRightSwipeAmount(0);
        holder.setSwipeItemHorizontalSlideAmount(
                item.isPinned() ? -0.6f : 0);
    }

    @Override
    public int getItemCount() {
        return mProvider.getCount();
    }

    @Override
    public SwipeResultAction onSwipeItem(MyViewHolder holder, int position, int result) {
        LogUtil.i(TAG, "onSwipeItem(position = " + position + ", result = " + result + ")");
        switch (result) {
            // swipe left --- pin
            case Swipeable.RESULT_SWIPED_LEFT:
                swipedPosition = position;
                isCanMoveByTouchButton = true;
                return new SwipeLeftResultAction(this, position);
            // other --- do nothing
            case Swipeable.RESULT_SWIPED_RIGHT:
            case Swipeable.RESULT_CANCELED:
            default:
                if (position != RecyclerView.NO_POSITION) {
                    swipedPosition = -1;
                    isCanMoveByTouchButton = false;
                    return new UnpinResultAction(this, position);
                } else {
                    return null;
                }
        }
    }

    @Override
    public int onGetSwipeReactionType(MyViewHolder holder, int position, int x, int y) {
        if (onCheckCanStartDrag(holder, position, x, y)) {
            return Swipeable.REACTION_CAN_SWIPE_BOTH_H;
        } else {
            return Swipeable.REACTION_CAN_SWIPE_LEFT;
        }
    }

    @Override
    public void onSetSwipeBackground(MyViewHolder holder, int position, int type) {

        int bgRes;
        switch (type) {
            case Swipeable.DRAWABLE_SWIPE_NEUTRAL_BACKGROUND:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
            default:
                bgRes = R.drawable.bg_swipe_item_neutral;
                break;
        }

        holder.itemView.setBackgroundResource(bgRes);
    }


    @Override
    public boolean onCheckCanStartDrag(MyViewHolder holder, int position, int x, int y) {
        // x, y --- relative from the itemView's top-left
        final View containerView = holder.mContainer;
        final View dragHandleView = holder.mMoveButton;

//        final int offsetX = containerView.getLeft() + (int) (ViewCompat.getTranslationX(containerView) + 0.5f);
//        final int offsetY = containerView.getTop() + (int) (ViewCompat.getTranslationY(containerView) + 0.5f);


        //判断是否可移动
        return ViewUtils.hitTest(dragHandleView, x, y) && isCanMoveByTouchButton && swipedPosition == position;
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(MyViewHolder holder, int position) {
        return null;
    }

    private void doMoveAction(int fromPosition,int toPosition){

        mProvider.moveItem(fromPosition, toPosition);

        notifyItemMoved(fromPosition, toPosition);

        if (onItemMoveListenner != null){
            onItemMoveListenner.onItemMove();
        }
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        LogUtil.i(TAG, "onMoveItem(fromPosition = " + fromPosition + ", toPosition = " + toPosition + ")");
        isCanMoveByTouchButton = false;

        if (fromPosition == toPosition) {
            return;
        }
        if (mProvider.getItem(fromPosition).isTop() == 1){
            return;
        }

        if (toPosition < mTopCount && mTopCount > 0){
            toPosition = mTopCount;
            if (getItem(fromPosition).getText().equals(mSelectProgram.toString()))
                mSelectItem = toPosition;
            doMoveAction(fromPosition, toPosition);
            return;
        }
        if (getItem(fromPosition).getText().equals(mSelectProgram.toString()))
            mSelectItem = toPosition;
        doMoveAction(fromPosition,toPosition);

    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return true;
    }

    public EventListener getEventListener() {
        return mEventListener;
    }

    public void setEventListener(EventListener eventListener) {
        mEventListener = eventListener;
    }

    static class MyViewHolder extends AbstractDraggableSwipeableItemViewHolder {
        FrameLayout mContainer;
        RelativeLayout mProgramLayout;
        Button mDeleteButton;
        private Button mTopButton;
        TextView mFreq;
        TextView mProgramName;
        private TextView mDragHandle;
        private View mDivideLine;
        private Button mMoveButton;
        ImageView mLock;


        MyViewHolder(View itemView) {
            super(itemView);
            mContainer = (FrameLayout) itemView.findViewById(R.id.container);
            mDeleteButton = (Button) itemView.findViewById(R.id.delete);
            mTopButton = (Button) itemView.findViewById(R.id.top);
            mFreq = (TextView) itemView.findViewById(R.id.channel_freq);
            mProgramName = (TextView) itemView.findViewById(R.id.chanel_name);
            mDragHandle = (TextView) itemView.findViewById(R.id.drag_handle);
            mProgramLayout = (RelativeLayout) itemView.findViewById(R.id.program_layout);
            mDivideLine = itemView.findViewById(R.id.line_divide);
            mMoveButton = (Button) itemView.findViewById(R.id.move);
            mLock = (ImageView) itemView.findViewById(R.id.lock_encrypt);

        }

        @Override
        public View getSwipeableContainerView() {
            return mContainer;
        }
    }

    private static class SwipeLeftResultAction extends SwipeResultActionMoveToSwipedDirection {
        private SwipeableWithButtonAdapter mAdapter;
        private final int mPosition;
        private boolean mSetPinned;

        SwipeLeftResultAction(SwipeableWithButtonAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            AbstractDataProvider.Data item = mAdapter.mProvider.getItem(mPosition);

            if (!item.isPinned()) {
                item.setPinned(true);
                mAdapter.notifyItemChanged(mPosition);
                mSetPinned = true;
            }
        }

        @Override
        protected void onSlideAnimationEnd() {
            super.onSlideAnimationEnd();

            if (mSetPinned && mAdapter.mEventListener != null) {
                mAdapter.mEventListener.onItemPinned(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

    private static class UnpinResultAction extends SwipeResultActionDefault {
        private SwipeableWithButtonAdapter mAdapter;
        private final int mPosition;

        UnpinResultAction(SwipeableWithButtonAdapter adapter, int position) {
            mAdapter = adapter;
            mPosition = position;
        }

        @Override
        protected void onPerformAction() {
            super.onPerformAction();

            AbstractDataProvider.Data item = mAdapter.mProvider.getItem(mPosition);
            if (item.isPinned()) {
                item.setPinned(false);
                mAdapter.notifyItemChanged(mPosition);
            }
        }

        @Override
        protected void onCleanUp() {
            super.onCleanUp();
            // clear the references
            mAdapter = null;
        }
    }

    public void setTopCountValue(int value){
        mTopCount = value;
    }

    public int getTopCountValue(){
        return mTopCount;
    }

    public void decreaseTopCount() {
        if (mTopCount > 0){
            this.mTopCount--;
            PreferencesUtils.putInt(DTVApplication.getAppContext(),"topCount",mTopCount);
        }
    }

    public void increaseTopCount(){
        mTopCount = mTopCount + 1;
        PreferencesUtils.putInt(DTVApplication.getAppContext(),"topCount",mTopCount);
    }

    public void setOnItemMoveListenner(OnItemMoveListenner listenner) {
        this.onItemMoveListenner = listenner;
    }

    public int getSwipedPosition() {
        return swipedPosition;
    }

    public void setSwipedPosition(int swipedPosition) {
        this.swipedPosition = swipedPosition;
    }

    public List<TvDataProvider.ConcreteData> getList(){
        return ((TvDataProvider)mProvider).getList();
    }

    public TvDataProvider.ConcreteData getItem(int position){
        return (TvDataProvider.ConcreteData) mProvider.getItem(position);
    }

    public String getItemProgramName(int position){
        return getItem(position).getText();
    }


    public void removeItemFromDadaBase(int position){
        ((TvDataProvider) mProvider).removeItemFromDataBase(position);
    }

    public void updateItemName(String location,String newName,int postion){
        mProvider.updateItemName(location,newName,postion);
    }

    public void moveItem(int fromPosition, int toPosition){
        mProvider.moveItem(fromPosition, toPosition);
    }

    public void swapItem(int fromPosition,int toPositon){
        mProvider.swapItem(fromPosition,toPositon);
    }

    public void clearItem(){
        ((TvDataProvider)mProvider).clearItem();
        notifyDataSetChanged();
    }

    public void addItem(List<TvDataProvider.ConcreteData> lists){
        ((TvDataProvider)mProvider).addItem(lists);
        notifyDataSetChanged();
    }

    public void setSelectProgram(String program,int position){
        mSelectProgram = new StringBuffer(program);
        notifyDataSetChanged();
    }

    public String getSelectProgram(){
        return mSelectProgram.toString();
    }

    public int getSelectItem() {
        return mSelectItem;
    }

    public int isTop(int position){
        return mProvider.getItem(position).isTop();
    }

    public void setTop(int position){
        mProvider.getItem(position).setTop(1);
        mProvider.getItem(position).setTime(System.currentTimeMillis());
    }

    public void cancelTop(int position){
        mProvider.getItem(position).setTop(0);
        mProvider.getItem(position).setTime(System.currentTimeMillis());
    }

    public interface OnItemMoveListenner{
        void onItemMove();
    }
}
