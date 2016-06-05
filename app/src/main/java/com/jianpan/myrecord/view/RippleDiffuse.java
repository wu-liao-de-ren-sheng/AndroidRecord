package com.jianpan.myrecord.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

import com.jianpan.myrecord.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by 键盘 on 2016/5/22.
 */
public class RippleDiffuse extends ViewGroup implements View.OnTouchListener{

    private static final int ANIMATION_EACH_OFFSET = 800; // 每个动画的播放时间间隔
    private static final int RIPPLE_VIEW_COUNT     = 3;//波纹view的个数
    private static final float DEFAULT_SCALE       = 1.6f;//波纹放大后的大小

    //点击有扩散效果的view
    private CircleImageView mBtnImg;
    private int mBtnViewHeight;
    private int mBtnViewWidth;
    private float mScale = DEFAULT_SCALE;
    //图片资源
    private int mBtnImgRes;
    private int mRippleRes;
    //是否初始化完成
    private boolean mInitDataSucceed = false;

    private OnTouchListener mBtnOnTouchListener;

    private List<CircleImageView> mWaves = new ArrayList<>();
    private List<AnimationSet> mAnimas = new ArrayList<>();

    public RippleDiffuse(Context context) {
        this(context, null);
    }

    public RippleDiffuse(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RippleDiffuse(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initAttrs(context, attrs);
    }

    private void initAttrs(Context context, AttributeSet attributeSet){
        if (attributeSet != null){
            TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.RippleDiffuse);
            mScale = typedArray.getFloat(R.styleable.RippleDiffuse_rd_scale, DEFAULT_SCALE);
            mBtnImgRes = typedArray.getResourceId(R.styleable.RippleDiffuse_btn_img_res, 0);
            mRippleRes = typedArray.getResourceId(R.styleable.RippleDiffuse_ripple_img_res, 0);

            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
        if (!mInitDataSucceed){
            initData();
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);

    }

    private void initData(){
        if (getMeasuredHeight() > 0 && getMeasuredWidth() > 0){
            mInitDataSucceed = true;

            int height = getMeasuredHeight() - getPaddingTop() - getPaddingBottom();
            int width  = getMeasuredWidth() - getPaddingLeft() - getPaddingRight();
            mBtnViewHeight = (int) (height / mScale);
            mBtnViewWidth  = (int) (width / mScale);

            mBtnImg = new CircleImageView(getContext());
            mBtnImg.setImageResource(mBtnImgRes);
            mBtnImg.setOnTouchListener(this);

            addView(mBtnImg, getWaveLayoutParams());

            for (int i = 0; i < RIPPLE_VIEW_COUNT; i++){
                //创建view
                CircleImageView wave = createWave();
                mWaves.add(wave);
                //创建动画
                mAnimas.add(getNewAnimationSet());

                addView(wave, 0, getWaveLayoutParams());
            }
        }
    }
    private CircleImageView createWave(){
        CircleImageView CircleImageView = new CircleImageView(getContext());
        CircleImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        CircleImageView.setImageResource(mRippleRes);
        return CircleImageView;
    }

    private LayoutParams getWaveLayoutParams(){
        LayoutParams lp = new LayoutParams(mBtnViewWidth, mBtnViewHeight);
        return  lp;
    }

    private AnimationSet getNewAnimationSet() {
        AnimationSet as = new AnimationSet(true);
        ScaleAnimation sa = new ScaleAnimation(1f, mScale, 1f, mScale,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        sa.setDuration(ANIMATION_EACH_OFFSET * 3);
        sa.setRepeatCount(-1);// 设置循环
        AlphaAnimation aniAlp = new AlphaAnimation(1, 0.1f);
        aniAlp.setRepeatCount(-1);// 设置循环
        as.setDuration(ANIMATION_EACH_OFFSET * 3);
        as.addAnimation(sa);
        as.addAnimation(aniAlp);
        return as;
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (mInitDataSucceed) {
            int childLeft = (getMeasuredWidth() - mBtnViewWidth) / 2;
            int childTop = (getMeasuredHeight() - mBtnViewHeight) / 2;
            for (int i = 0; i < RIPPLE_VIEW_COUNT; i++) {
                CircleImageView wave = mWaves.get(i);
                wave.layout(childLeft, childTop, mBtnViewWidth + childLeft, mBtnViewHeight + childTop);
            }
            mBtnImg.layout(childLeft, childTop, mBtnViewWidth + childLeft, mBtnViewHeight + childTop);
        }else {
            initData();
        }
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                showWaveAnimation();
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                cancelWaveAnimation();
                break;
        }
        if (mBtnOnTouchListener != null){
            boolean flag = mBtnOnTouchListener.onTouch(v, event);
            if (!flag){
                cancelWaveAnimation();
                return false;
            }
        }
        return true;
    }

    private void showWaveAnimation() {
        for (int i = 0; i < RIPPLE_VIEW_COUNT; i++){
            Message message = new Message();
            message.obj = i;
            handler.sendMessageDelayed(message, ANIMATION_EACH_OFFSET * i);
        }
    }

    private void cancelWaveAnimation() {
        for (int i = 0; i < RIPPLE_VIEW_COUNT; i++){
            handler.removeMessages(i);
            CircleImageView wave = mWaves.get(i);
            wave.clearAnimation();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int position = (int) msg.obj;
            CircleImageView wave = mWaves.get(position);
            wave.startAnimation(mAnimas.get(position));
            super.handleMessage(msg);
        }
    };

    public void setScale(float scale){
        this.mScale = scale;
    }

    public void setBtnImgRes(int res){
        mBtnImgRes = res;
    }

    public void setRippleRes(int rippleRes){
        mRippleRes = rippleRes;
    }

    public void setBtnOnTouchListener(OnTouchListener onTouchListener){
        mBtnOnTouchListener = onTouchListener;
    }
}
