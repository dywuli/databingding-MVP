package voice.example.com.myapplication.recordwave;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.os.SystemClock;
import android.support.annotation.StyleableRes;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;

import org.apache.commons.lang3.concurrent.BasicThreadFactory;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import voice.example.com.myapplication.R;

import static android.graphics.Shader.TileMode.CLAMP;

public class MicView extends View implements View.OnClickListener {
    public static final String TAG = "MicView";

    public static final long MIN_CLICK_DURATION = 500;
    public static final long ERROR_DURATION = 600;
    public static final long TIMER = 80;

    private static final long END_DELAY = 260;
    private static final long HIDE_WAVE_DURATION = 400;
    private static final long END_DURATION = 300;
    private static final float MAX_VOLUME = 0.8333f;
    private static final float MIN_VOLUME = 0.4f;
    private static final float WAVE_DIFF = 0.143f;
    private static final float INIT_SCALE = 0.75f;
    private static final float MIN_SCALE = 0.4f;
    private static final float MAX_SCALE = 0.9f;
    private static final int EAR_INIT_ALPHA = 153;
    private static final long START_DURATION = 560;
    private static final long SHOW_WAVE_DURATION = 100;
    private static final long LOADING_DURATION = 500;
    private static final int LEFT = 180;
    private static final int RIGHT = 360;
    private int mCurrentStart = LEFT;
    private int mCurrentEnd = RIGHT;

    private static final int UP = 90;
    private static final int DOWN = 270;

    private static final int WAVE_COUNT = 12;

    /**
     * 话筒当前的状态，不同的状态会执行不同的动画.
     */
    private volatile MicState mMicState;
    private volatile MicState mMicNextState;
    // ((((((（○）))))))
    /**
     * 绘制中间的圆圈.
     */
    private final Paint mMicPaint = new Paint();
    /**
     * 绘制圆圈两边的耳朵.
     */
    private final Paint mEarPaint = new Paint();
    /**
     * 绘制圆圈两边的 10个 波.
     */
    private final Paint mWavePaint = new Paint();

    //For ticmirror begin.
    private final Paint mBgPaint = new Paint();
    //For ticmirror eng
    /**
     * 计算波的颜色，在最大值和最小值之间波动.
     */
    private final ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();

    /**
     * 开始录音的动画；耳朵转圈，圆圈缩小、然后耳朵扩散、圆圈变大.
     */
    private final ValueAnimator mStartAnimator = new ValueAnimator();
    /**
     * LISTENING 到 LOADING 的动画，和 mStartAnimator 的动画相反，两个耳朵出现，波隐藏，圆圈恢复到初始状态.
     */
    private final ValueAnimator mEndAnimator = new ValueAnimator();
    /**
     * 圆圈的缩放系数，默认值为 INIT_SCALE.
     */
    private volatile float mScale = INIT_SCALE;
    /**
     * 在执行下一个动画之前圆圈的缩放系数.
     */
    private float mPreScale = INIT_SCALE;
    /**
     * 在执行下一个动画所需要改变后的缩放系数.
     */
    private float mNewScale = INIT_SCALE;
    /**
     * 中间圆圈两个耳朵转圈的角度.
     */
    private float mEarAngleDiff;
    /**
     * 中间圆圈两个耳朵扩散的位移.
     */
    private float mEarPosDiff;
    private float mLeftEarPosDiff;
    private float mRightEarPosDiff;
    private float mLoadingRadiusDiff;
    /**
     * 两个耳朵的 Alpha.
     */
    private int mEarAlpha;
    /**
     * 中间圆圈两个耳朵扩散位移的最大值.
     */
    private float mEarMaxDiff;
    /**
     * 中间圆圈两个耳朵长度的角度.
     */
    private final float mEarSweep = 36;
    /**
     * 中间圆圈两个耳朵做旋转动画时候的长度的角度最大值.
     */
    private final float mEarSweepMax = 90;
    /**
     * 中间圆圈两个耳朵做旋转动画时候的角度增量.
     */
    private float mEarSweepDiff;
    /**
     * 左耳朵绘制的起始角度.
     */
    private final float mLeftEarStart;
    /**
     * 右耳朵绘制的起始角度.
     */
    private final float mRightEarStart;

    /**
     * Loading 状态的动画， 圆圈放大缩小，耳朵旋转.
     */
    private final ValueAnimator mLoadingAnimator = new ValueAnimator();
    /**
     * 中间圆圈恢复到初始状态的动画.
     */
    private final ValueAnimator mMicToInitAnimator = new ValueAnimator();

    /**
     * 错误状态 RETRY 所做的动画，圆圈左右摇摆.
     */
    private final ValueAnimator mErrorAnimator = new ValueAnimator();

    // 绘制 Error 动画的属性
    /**
     * 圆圈移动到左耳朵的时候，左耳朵缩放系数.
     */
    private float mLeftEarScale = 1f;
    /**
     * 圆圈移动到右耳朵的时候，右耳朵缩放系数.
     */
    private float mRightEarScale = 1f;
    /**
     * 圆圈移动到左耳朵的时候，左耳朵 Alpha 增量.
     */
    private int mLeftEarAlpha;
    /**
     * 圆圈移动到右耳朵的时候，右耳朵 Alpha 增量.
     */
    private int mRightEarAlpha;

    /**
     * 圆圈左右摇摆的位移.
     */
    private float mMicPosDiff;
    /**
     * 显示波的动画.
     */
    private final ValueAnimator mShowWaveAnimator = new ValueAnimator();
    /**
     * 隐藏波的动画.
     */
    private final ValueAnimator mHideWaveAnimator = new ValueAnimator();
    /**
     * 说话过程中波变化的动画.
     */
    private final ValueAnimator mWaveAnimator = new ValueAnimator();

    // 圆圈每边有 10 个波，越靠近圆圈的波的 index 越小.

    /**
     * 每个波长度所对应的角度.
     */
    private final float[] mWaveSweep = new float[WAVE_COUNT];
    /**
     * 波最小长度所对应的角度.
     */
    private final float[] mMinWaveSweep = new float[WAVE_COUNT];
    /**
     * 波最大长度所对应的角度.
     */
    private final float[] mMaxWaveSweep = new float[WAVE_COUNT];
    /**
     * 记录在做动画之前波的长度.
     */
    private final float[] mPreWaveSweep = new float[WAVE_COUNT];
    /**
     * 记录动画做完之后波的长度.
     */
    private final float[] mNewWaveSweep = new float[WAVE_COUNT];
    /**
     * 记录波为最大值的时候每个位置的波的长度，用来计算相邻波的比率，.
     * 然后使用比率来计算波扩散的长度 mWaveLength
     */
    private final float[] mWaveStep = new float[WAVE_COUNT];
    /**
     * 记录靠近圆圈的波在 0.1 秒后往外扩散后的长度，比如 mWaveLength[0] 的长度，.
     * 0.1秒后会扩散到 mWaveLength[1], 而 mWaveLength[1] 的值是根据
     * mWaveStep 的比率和mWaveLength[0]的值计算的
     */
    private final float[] mWaveLength = new float[WAVE_COUNT];
    /**
     * 每个波的 alpha 值，
     * TODO 每个平台不一样，需要分别设置
     */
    private int[] mWaveAlpha = new int[]{255, 255, 255, 255, 255, 255, 255, 204, 128, 51, 51, 40};
    /**
     * 记录 View 的尺寸，方便计算和绘制动画
     */
    private final Rect mRect = new Rect();
    private final RectF mRectF = new RectF();

    /**
     * 倾听中的计时器，每隔 0.1 秒触发一次，更加当前的音量大小来修改波的长度
     * TODO 考虑用其他的实现方式
     */
    private ScheduledExecutorService mExecutor;
    private ThreadFactory mThreadFactory;

    /**
     * 音量大小，
     */
    private volatile float mVolume;

    /**
     * 圆圈的最大半径
     */
    private float mMaxRadius;
    /**
     * 圆圈的最小半径
     * TODO 暂时没用，使用缩放来改变圆圈大小，而不是改变半径
     */
    private float mMinRadius;
    /**
     * 耳朵的宽度
     */
    private float mEarWidth;
    /**
     * 圆圈的宽度，mMicWidth * 0.75 == mEarWidth
     */
    private float mMicWidth;
    private float mInitMicWidth;
    private float mStaticMicWidth;
    /**
     * 波的宽度
     */
    private final float mWaveWidth;
    /**
     * 两个波之间的间隔
     */
    private final float mWaveGap;
    /**
     * 靠近圆圈的第一个波的半径
     */
    private float mFirstWaveSize;

    /**
     * 上面的颜色
     */
    private final int mStartColor = 0xFF1AF28F;
    /**
     * 下面的颜色
     */
    private final int mEndColor = 0xFF009CFF;
    private final int mMiddleColor = 0xFF0DE9C5;
    private final Runnable mCalWaveRunnable;

    public MicView(Context context) {
        this(context, null, 0);
    }

    public MicView(Context context, AttributeSet paramAttributeSet) {
        this(context, paramAttributeSet, 0);
    }

    // 初始化一些 final 变量
    public MicView(Context context, AttributeSet paramAttributeSet, int defStyle) {
        super(context, paramAttributeSet, defStyle);
        setWillNotDraw(false);
        TypedArray array = context.obtainStyledAttributes(paramAttributeSet, R.styleable.MicView);
        // 下面的参数放到 layout properties 中去， 可配置
        mWaveGap = getDimension(array, R.styleable.MicView_mic_wave_gap, 12);
        mMaxRadius = getDimension(array, R.styleable.MicView_mic_max_radius, 15);
        mMinRadius = getDimension(array, R.styleable.MicView_mic_min_radius, 10);
        mEarWidth = getDimension(array, R.styleable.MicView_mic_circle_width, 6);
        mInitMicWidth = mMicWidth = getDimension(array, R.styleable.MicView_mic_width, 5);
        mStaticMicWidth = getDimension(array, R.styleable.MicView_mic_static_width, 6);
        mWaveWidth = getDimension(array, R.styleable.MicView_mic_wave_width, 3);
        mFirstWaveSize = getDimension(array, R.styleable.MicView_mic_first_wave_radius, 24);
        mCurrentStart = array.getInteger(R.styleable.MicView_mic_left_ear_degree, LEFT);
        mCurrentEnd = array.getInteger(R.styleable.MicView_mic_right_ear_degree, RIGHT);
        mLeftEarStart = mCurrentStart - mEarSweep / 2;
        mRightEarStart = mCurrentEnd - mEarSweep / 2;


        mEarMaxDiff = mWaveGap * 9;
        mEarAlpha = EAR_INIT_ALPHA;
        array.recycle();
        mCalWaveRunnable = new Runnable() {
            @Override
            public void run() {
                calWave();
            }
        };
        init();
    }

    private float getDimension(TypedArray array, @StyleableRes int attr, float defaultValue) {
        return array.getDimension(attr, dip2px(getContext(), defaultValue));
    }

    private void init() {
        final LinearInterpolator linearInterpolator = new LinearInterpolator();
        final AccelerateDecelerateInterpolator accDecInter = new AccelerateDecelerateInterpolator();
        mStartAnimator.setDuration(START_DURATION);
        mStartAnimator.setInterpolator(linearInterpolator);
        mStartAnimator.setFloatValues(0f, 1.65f);
        mStartAnimator.setStartDelay(10);

        mEndAnimator.setDuration(END_DURATION);//START_DURATION
        mEndAnimator.setInterpolator(linearInterpolator);
        mEndAnimator.setFloatValues(2f, 0f);

        // 中间小圆圈恢复初始状态的动画
        mMicToInitAnimator.setDuration(50);
        mMicToInitAnimator.setInterpolator(linearInterpolator);
        mMicToInitAnimator.setFloatValues(1f, 0f);

        mShowWaveAnimator.setDuration(SHOW_WAVE_DURATION);
        mShowWaveAnimator.setInterpolator(linearInterpolator);
        mShowWaveAnimator.setFloatValues(0f, 1f);
        mShowWaveAnimator.setStartDelay(START_DURATION - SHOW_WAVE_DURATION);

        mHideWaveAnimator.setDuration(HIDE_WAVE_DURATION);
        mHideWaveAnimator.setInterpolator(linearInterpolator);
        mHideWaveAnimator.setFloatValues(1f, 0f);

        mErrorAnimator.setDuration(ERROR_DURATION);
        //mErrorAnimator.setInterpolator(new ExpoEaseOut());
        mErrorAnimator.setInterpolator(new LinearInterpolator());
        mErrorAnimator.setFloatValues(0f, -1f, 0f, 1f, 0f, -1f, 0f, 1f, 0f);
        mErrorAnimator.setRepeatCount(0);

        mLoadingAnimator.setDuration(LOADING_DURATION);
        mLoadingAnimator.setInterpolator(accDecInter);
        mLoadingAnimator.setFloatValues(0f, 2f);
        mLoadingAnimator.setRepeatMode(ValueAnimator.RESTART);
        mLoadingAnimator.setRepeatCount(ValueAnimator.INFINITE);

        mWaveAnimator.setDuration(TIMER - 2);
        mWaveAnimator.setFloatValues(0f, 1f);
        mWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //0 到 1
                float value = (float) valueAnimator.getAnimatedValue();
                mScale = mPreScale + (mNewScale - mPreScale) * value;
                if (mScale > MAX_SCALE) {
                    mScale = MAX_SCALE;
                }
                if (mScale < MIN_SCALE) {
                    mScale = MIN_SCALE;
                }
                int length = mWaveLength.length;
                for (int i = 0; i < length; i++) {
                    mWaveSweep[i] = mPreWaveSweep[i] + (mNewWaveSweep[i] - mPreWaveSweep[i]) * value;
                    if (Float.isNaN(mWaveSweep[i])) {
                        mWaveSweep[i] = mMinWaveSweep[i];
                    }
                }
                invalidate();
            }
        });
        mWaveAnimator.setRepeatCount(0);

        mMicPaint.setAntiAlias(true);
        mMicPaint.setDither(true);
        mMicPaint.setStyle(Paint.Style.STROKE);
        mMicPaint.setStrokeWidth(mMicWidth);

//        float[] pos = new float[]{0, 0.5f, 1f};
        //圆圈 Paint 的 Shader，为两个颜色的渐变色

        //For ticmirror beginD
        mWavePaint.setAntiAlias(true);
        mWavePaint.setDither(true);
        mWavePaint.setStrokeCap(Paint.Cap.ROUND);
        mWavePaint.setStyle(Paint.Style.STROKE);
        mWavePaint.setColor(mEndColor);
        mWavePaint.setStrokeWidth(mWaveWidth);

        mEarPaint.setAntiAlias(true);
        mEarPaint.setDither(true);
        mEarPaint.setStrokeCap(Paint.Cap.ROUND);
        mEarPaint.setStyle(Paint.Style.STROKE);
        mEarPaint.setAlpha(EAR_INIT_ALPHA);// 60%
        mEarPaint.setStrokeWidth(mEarWidth);


        //For Ticmirror
        mBgPaint.setColor(Color.BLACK);

        //TODO 下面是计算每个波的相关参数，
        //mWaveStep 是在音量最大的时候，每个波的三角形短边长度，
        // mMinWaveSweep 每个波的最小角度， 这个角度可以考虑根据不同设备（每个波的间隔距离, 每个
        // 波的半径不一样，最大角度和最小角度也不一样，不同平台的波间距应该是有差别的）
        // 让设计师标注出来，而不是计算出来。下面计算的是一个大概的值，
        // mMaxWaveSweep 每个波的最大角度，同样可以标注
        //0.309  sin(18)
        // |\   角度是 18 度
        // | \  mFirstWaveSize 是斜边的长
        // |  \
        // | __\  计算出短边的长度
        float size = (float) (Math.sin(Math.PI * 18f / 180f) * mFirstWaveSize);

        for (int i = 0; i < mWaveStep.length; i++) {
            mWaveStep[i] = mFirstWaveSize * (mWaveStep.length - i) / mWaveStep.length / 2;
        }

        for (int i = 0; i < mMinWaveSweep.length; i++) {
            float height = mEarWidth / 6f * ((10f - i) / 10f); // 加上 Paint.Cap.ROUND 所以 1/6 高度即可
            float radian = (float) Math.asin(height / (mFirstWaveSize + mWaveGap * i));
            mMinWaveSweep[i] = (float) (radian * 180f / Math.PI);
        }
        for (int i = 0; i < mMaxWaveSweep.length; i++) {
            float height = mWaveStep[i];
            float radian = (float) Math.asin(height / (mFirstWaveSize + mWaveGap * i));
            mMaxWaveSweep[i] = (float) (radian * 180f / Math.PI) * 2;
        }

        setupAnimatorListener();

        mThreadFactory = new BasicThreadFactory.Builder().namingPattern(TAG).daemon(true).build();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final float y1 = (mMaxRadius + mMicWidth) * 2;
        int[] colors = new int[]{mStartColor, mMiddleColor, mEndColor};
        int centerY = getHeight() / 2;

        Shader shader = new LinearGradient(0, centerY - y1 / 2, 0, centerY + y1 / 2, colors, null, CLAMP);
        mMicPaint.setShader(shader);
        shader = new LinearGradient(0, centerY - mFirstWaveSize, 0, centerY + mFirstWaveSize, colors, null, CLAMP);
        mEarPaint.setShader(shader);
    }

    private void setupAnimatorListener() {
        mStartAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // 从 0 到 2f, 0到1 为耳朵转圈，圆圈缩小； 1到2位 耳朵扩散、圆圈变大
                // mScale 初始值为 0.75f
                float value = (float) valueAnimator.getAnimatedValue();
                if (value <= 1f) {
                    mEarAlpha = EAR_INIT_ALPHA;
                    mEarPosDiff = 0;
                    mEarAngleDiff = 180 * value;
                    mScale = MIN_SCALE + (INIT_SCALE - MIN_SCALE) * (1 - value);
                } else {
                    mEarAngleDiff = 0;
                    mEarPosDiff = mEarMaxDiff * (value - 1);
                    mEarAlpha = (int) (EAR_INIT_ALPHA * (1.65f - value));
                    mScale = MIN_SCALE + (MAX_SCALE - MIN_SCALE) * (value - 1);
                    if (value >= 1.4f) {
                        mWavePaint.setColor(mEndColor);
//                        mWaveAlpha = (int) (255 * (value - 1.4f)/0.6f);
                    }
                }
                invalidate();
            }
        });
        // 和 mStartAnimator 执行相反的操作
        mEndAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                // 从 2f 到 0f, 1到10 为耳朵转圈，圆圈缩小； 2到1位 耳朵扩散、圆圈变大
                // mScale 初始值为 0.75f
                float value = (float) valueAnimator.getAnimatedValue();
                if (value <= 1f) {
                    mEarAlpha = EAR_INIT_ALPHA;
                    mEarPosDiff = 0;
                    mEarAngleDiff = 180 * value;
                    mScale = MIN_SCALE + (INIT_SCALE - MIN_SCALE) * (1 - value);
                } else {
                    mEarAngleDiff = 0;
                    mEarPosDiff = mEarMaxDiff * (value - 1);
                    mEarAlpha = (int) (EAR_INIT_ALPHA * (2 - value));
                    // 从当前值到最小值
                    mScale = mPreScale - (mPreScale - MIN_SCALE) * (2 - value);
                    if (value >= 1.4f) {
                        mWavePaint.setColor(mEndColor);
                    }
                }
                invalidate();
            }
        });

        mEndAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // 在 mEndAnimator 执行完，再开始执行 mLoadingAnimator
                if (mMicState == MicState.LOADING) {
                    mLoadingAnimator.start();
                }
            }
        });

        mHideWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                //1 到 0
                float value = (float) valueAnimator.getAnimatedValue();
                for (int i = 0; i < mWaveSweep.length; i++) {
                    float exponent = 1f + i / 10f;
                    // 最外面的波先隐藏
                    mWaveSweep[i] = (float) (mPreWaveSweep[i] * Math.pow(value, exponent));
                }
                invalidate();

            }
        });

        mMicToInitAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                // 圆圈从当前的缩放值变化到初始值 并且重置两边耳朵的参数
                mScale = INIT_SCALE + (mPreScale - INIT_SCALE) * value;
                mEarAlpha = EAR_INIT_ALPHA;
                mEarAngleDiff = 0;
                mEarPosDiff = 0;
                mEarSweepDiff = 0;
                invalidate();
            }
        });

        mErrorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mScale = INIT_SCALE;
                // 0f, -1f, 0f, 1f, 0f， 中间到左边、左边到中间、中间到右边、右边到中间
                float value = (float) valueAnimator.getAnimatedValue();
//                mMicPosDiff = (mFirstWaveSize - mMaxRadius * INIT_SCALE) * value;
                // 22 17
                mMicPosDiff = (mFirstWaveSize + mEarWidth - mMaxRadius * INIT_SCALE) * value;
//
                if (value < 0) {
                    resetRightEarProperties();
                    if (-mMicPosDiff + mMaxRadius * INIT_SCALE >= mFirstWaveSize - mEarWidth / 2) {
                        // 靠近 耳朵了
                        mLeftEarScale = 1 + (0.3f * -value);
                        mLeftEarAlpha = (int) ((255 - EAR_INIT_ALPHA) * (-value));
                        mLeftEarPosDiff = -mMicPosDiff + mMaxRadius * INIT_SCALE - mFirstWaveSize + mEarWidth / 2;
                    } else {
                        resetLeftEarProperties();
                    }
                } else {
                    resetLeftEarProperties();
                    if (mMicPosDiff + mMaxRadius * INIT_SCALE >= mFirstWaveSize - mEarWidth / 2) {
                        // 靠近 耳朵了
                        mRightEarScale = 1 + (0.3f * value);
                        mRightEarAlpha = (int) ((255 - EAR_INIT_ALPHA) * value);
                        mRightEarPosDiff = mMicPosDiff + mMaxRadius * INIT_SCALE - mFirstWaveSize + mEarWidth / 2;
                    } else {
                        resetRightEarProperties();
                    }
                }
                invalidate();
            }
        });

        mErrorAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                mMicPosDiff = 0;
                mEarPosDiff = 0;
                resetRightEarProperties();
                resetLeftEarProperties();
                if (mMicState == MicState.RETRY && mClickListener != null) {
                    mClickListener.onUserStart();
                }
            }
        });

        mShowWaveAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                if (mMicState != MicState.START) {
                    return;
                }
                //0 到 1
                float value = (float) valueAnimator.getAnimatedValue();
                for (int i = 0; i < mWaveSweep.length; i++) {
                    if (value > WAVE_DIFF * i) {
                        // 利用波长除以半价来近似计算波的角度，不是很准确...
                        float height = mEarWidth / 2f * ((10f - i) / 10f) * (value - WAVE_DIFF * i);
                        float radian = (float) Math.asin(height / (mFirstWaveSize + mWaveGap * i));
                        mWaveSweep[i] = (float) (radian * 180f / Math.PI);
                    }
                }
                invalidate();

            }
        });
        mShowWaveAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (mMicState != MicState.START) {
                    return;
                }
                setMicState(MicState.LISTENING);
                System.arraycopy(mMinWaveSweep, 0, mWaveSweep, 0, mWaveSweep.length);
                startTimer();
            }
        });

        mLoadingAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                final float loadingMaxScale = INIT_SCALE - 0.15f;
                // 0 到 2
                float value = (float) valueAnimator.getAnimatedValue();
                if (value <= 1f) {
                    mEarSweepDiff = (mEarSweepMax - mEarSweep) * value;
                    // 旋转 180 度
                    mEarAngleDiff = -180 * value;
//                    mLoadingRadiusDiff = (mFirstWaveSize - mMaxRadius) * value;
                    // 从初始值到最小值， mEndAnimator 执行完后，为 INIT_SCALE

                    mScale = loadingMaxScale - (loadingMaxScale - MIN_SCALE) * value;
                } else {
                    mEarSweepDiff = (mEarSweepMax - mEarSweep) * (2 - value);
                    // 旋转 180 度
                    mEarAngleDiff = -180 * value;
//                    mLoadingRadiusDiff = (mFirstWaveSize - mMaxRadius) *  (2 - value);
                    // 最小值到初始值
                    mScale = MIN_SCALE + (loadingMaxScale - MIN_SCALE) * (value - 1);
                }
                invalidate();
            }
        });

        mLoadingAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                mLoadingRadiusDiff = (mFirstWaveSize - mMaxRadius) * 0.7f;
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                mPreScale = mScale;
                if (mMicNextState != null && mMicNextState != MicState.LOADING) {
                    mLoadingAnimator.cancel();
                    mEndAnimator.cancel();
                    mLoadingRadiusDiff = 0;
                    setMicState(mMicNextState);
                    mMicNextState = null;
                }
            }
        });
    }

    private void resetLeftEarProperties() {
        mLeftEarScale = 1f;
        mLeftEarPosDiff = 0;
        mLeftEarAlpha = 0;
    }

    private void resetRightEarProperties() {
        mRightEarScale = 1f;
        mRightEarPosDiff = 0;
        mRightEarAlpha = 0;
    }

    /**
     * 设置当前的状态
     */
    private void setMicState(MicState state) {
        Log.d(TAG, " setMicState  state " + state + " before " + mMicState);
        if (mMicState == state) {
            return;
        }

        MicState pre = mMicState;
        mMicState = state;

        if (mMicState != MicState.LISTENING) {
            cancelTimer();
        }

        if (mMicState == MicState.LOADING) {
            enterLoadingAnimator();
        } else {
            if (mLoadingAnimator.isRunning()) {
                mLoadingAnimator.cancel();
            }
            if (mEndAnimator.isRunning()) {
                mEndAnimator.cancel();
            }
            mLoadingRadiusDiff = 0;
        }

        //For ticmirror
        if (mMicState == MicState.PAUSE) {
            if (mLoadingAnimator.isRunning()) {
                mLoadingAnimator.cancel();
            }
            if (mEndAnimator.isRunning()) {
                mEndAnimator.cancel();
            }
            if (mHideWaveAnimator.isRunning()) {
                mHideWaveAnimator.cancel();
            }
            for (int i = 0; i < mWaveSweep.length; i++) {
                mWaveSweep[i] = 0;
            }
            mEarAlpha = EAR_INIT_ALPHA;
            mEarAngleDiff = 0;
            mEarPosDiff = 0;
            mPreScale = INIT_SCALE;
            mEarSweepDiff = 0;
            mScale = INIT_SCALE;

            if (pre == MicState.LISTENING) {
                enterLoadingAnimator();
            } else {
                mMicToInitAnimator.start();
            }
        }
    }

    /**
     * 进入 LOADING 状态
     */
    private void enterLoadingAnimator() {

        // 取消所有在做的动画
        cancelAllAnimator();

        // 保存波的角度
        System.arraycopy(mWaveSweep, 0, mPreWaveSweep, 0, mWaveSweep.length);

//        mEarPosDiff = 0;
        mPreScale = mScale;
        mEarPosDiff = mEarMaxDiff;
        mEarAngleDiff = 0;
        mEarAlpha = 0;
        mLoadingRadiusDiff = 0;
        // 先隐藏 波
        mHideWaveAnimator.start();
        // 波快隐藏完的时候，开始显示左右耳朵
        mEndAnimator.setStartDelay(END_DELAY);
        mEndAnimator.start();
    }

    private void reset() {
        cancelAllAnimator();
        for (int i = 0; i < mWaveSweep.length; i++) {
            mWaveSweep[i] = 0;
        }
        mEarAlpha = EAR_INIT_ALPHA;
        mEarAngleDiff = 0;
        mEarPosDiff = 0;
        mPreScale = mScale = INIT_SCALE;
        mEarSweepDiff = 0;
        mMicPosDiff = 0;
        mLoadingRadiusDiff = 0;
        resetRightEarProperties();
        resetLeftEarProperties();
    }

    private void cancelAllAnimator() {
        if (mLoadingAnimator.isRunning()) {
            mLoadingAnimator.cancel();
        }
        if (mStartAnimator.isRunning()) {
            mStartAnimator.cancel();
        }
        if (mEndAnimator.isRunning()) {
            mEndAnimator.cancel();
        }
        if (mShowWaveAnimator.isRunning()) {
            mShowWaveAnimator.cancel();
        }
        if (mHideWaveAnimator.isRunning()) {
            mHideWaveAnimator.cancel();
        }
        if (mMicToInitAnimator.isRunning()) {
            mMicToInitAnimator.cancel();
        }
        if (mErrorAnimator.isRunning()) {
            mErrorAnimator.cancel();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mMicState == MicState.LISTENING) {
            mMicWidth = mInitMicWidth * (1f / mScale);
        } else {
            mMicWidth = mStaticMicWidth * (1f / mScale);
        }
        canvas.save();
        canvas.scale(mScale, mScale, mRect.centerX(), mRect.centerY());
        mMicPaint.setStrokeWidth(mMicWidth);
        canvas.translate(mMicPosDiff, 0);
        canvas.drawCircle(mRect.centerX(), mRect.centerY(), mMaxRadius, mMicPaint);
        canvas.restore();

        float left = mRect.centerX() - mFirstWaveSize + mLoadingRadiusDiff;
        float top = mRect.centerY() - mFirstWaveSize + mLoadingRadiusDiff;
        float right = mRect.centerX() + mFirstWaveSize - mLoadingRadiusDiff;
        float bottom = mRect.centerY() + mFirstWaveSize - mLoadingRadiusDiff;
        float startAngle = mLeftEarStart - mEarAngleDiff;
        float sweepAngle = mEarSweep + mEarSweepDiff;
        canvas.save();
        canvas.translate(-mEarPosDiff - mLeftEarPosDiff, 0);
        canvas.scale(1, mLeftEarScale, mRect.centerX(), mRect.centerY());
        mEarPaint.setAlpha(mEarAlpha + mLeftEarAlpha);
        // 左边耳朵
        canvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, false, mEarPaint);
        canvas.restore();

        canvas.save();
        canvas.translate(mEarPosDiff + mRightEarPosDiff, 0);
        canvas.scale(1, mRightEarScale, mRect.centerX(), mRect.centerY());
        startAngle = mRightEarStart - mEarAngleDiff;
        mEarPaint.setAlpha(mEarAlpha + mRightEarAlpha);
        // 右边耳朵
        canvas.drawArc(left, top, right, bottom, startAngle, sweepAngle, false, mEarPaint);
        canvas.restore();

        if (mMicState == MicState.LISTENING || mMicState == MicState.START || mMicState == MicState.LOADING) {
            for (int i = 0; i < mWaveSweep.length; i++) {
                float diff = mWaveGap * i;
                startAngle = mCurrentStart - mWaveSweep[i] / 2;
                int color = getWaveColor(mWaveSweep[i], i);
                mWavePaint.setColor(color);
                mWavePaint.setAlpha(mWaveAlpha[i]);
                canvas.drawArc(left - diff, top - diff, right + diff, bottom + diff, startAngle, mWaveSweep[i], false, mWavePaint);
                startAngle = mCurrentEnd - mWaveSweep[i] / 2;
                canvas.drawArc(left - diff, top - diff, right + diff, bottom + diff, startAngle, mWaveSweep[i], false, mWavePaint);

            }
        }
    }

    /**
     * 每隔 TIMER 毫秒根据当前的 mVolume 计算一次波长和圆圈缩放系数
     */
    private void calWave() {
        // 取消上一次没有执行完的动画
        mWaveAnimator.cancel();
        final float volume = mVolume;
        mPreScale = mScale;
        mNewScale = MIN_SCALE + (MAX_SCALE - MIN_SCALE) * (volume / MAX_VOLUME);
        int length = mWaveLength.length;
        System.arraycopy(mWaveSweep, 0, mPreWaveSweep, 0, length);
        // 根据边长计算角度
        for (int i = 0; i < length; i++) {
            if (i < length - 1) {
                mWaveLength[length - i - 1] = mWaveLength[length - i - 2] * mWaveStep[length - i - 1] / mWaveStep[length - i - 2];
                float height = mWaveLength[length - i - 1];
                float radian = (float) Math.asin(height / (mFirstWaveSize + mWaveGap * (length - i - 1)));
                float sweep = (float) (radian * 180f / Math.PI);
                mNewWaveSweep[length - i - 1] = Math.max(Math.min(mMaxWaveSweep[length - i - 1], sweep), /*mMinWaveSweep[length - i - 1]*/0);
            }
        }
        mWaveLength[0] = (float) (Math.pow((volume / (MAX_VOLUME - 0.35f)), 3) * (mFirstWaveSize)) * 1.3f;
        float height = mWaveLength[0];
        float radian = (float) Math.asin(height / (mFirstWaveSize));
        float sweep = (float) (radian * 180f / Math.PI);
        mNewWaveSweep[0] = Math.max(Math.min(mMaxWaveSweep[0], sweep), /*mMinWaveSweep[0]*/0);

        // 从新执行波长变化动画
        mWaveAnimator.start();

    }

    /***
     * 根据波的 角度 来计算波的颜色
     *
     * @param sweep 波的角度
     * @param i     波的序号，靠近中间圆环的为 0， 最外面的为 10
     * @return 波的颜色值
     */
    private int getWaveColor(float sweep, int i) {
        float diff = (mMaxWaveSweep[i] - mMinWaveSweep[i]);
        float fraction;
        if (diff == 0) {
            fraction = 0.1f;
        } else {
            fraction = (sweep - mMinWaveSweep[i]) / diff;
        }
        if (fraction < 0) {
            fraction = 0;
        }
        if (fraction > 1) {
            fraction = 1;
        }
        return (Integer) mArgbEvaluator.evaluate(fraction, mEndColor, mStartColor);
    }

    public void start() {
        reset();
        for (int i = 0; i < mWaveSweep.length; i++) {
            mWaveSweep[i] = 0;
        }
        mEarSweepDiff = 0f;
        setMicState(MicState.START);
        setVisibility(VISIBLE);
        // 转圈、耳朵扩散隐藏
        mStartAnimator.start();
        // 波 延时出现
        mShowWaveAnimator.start();

    }

    /**
     * 录音中的定时动画任务
     */
    private void startTimer() {
        cancelTimer();
        mExecutor = new ScheduledThreadPoolExecutor(1, mThreadFactory);
        mExecutor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                post(mCalWaveRunnable);
            }
        }, 1000, TIMER, TimeUnit.MILLISECONDS);
    }

    private void cancelTimer() {
        mWaveAnimator.cancel();
        if (mExecutor != null) {
            mExecutor.shutdown();
            mExecutor = null;
        }
    }

    private static int dip2px(Context context, float dp) {
        //For Ticmirror
        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources()
                .getDisplayMetrics()) * 1.428f);
    }

    /**
     * 更新音量大小
     *
     * @param volume 音量大小
     */
    public void updateVolume(float volume) {
        // 过滤掉最小的音量（杂音）
        volume -= MIN_VOLUME;
        if (volume < 0) {
            volume = 0;
        }
        if (mMicState == MicState.LISTENING) {
            mVolume = Math.min(volume, MAX_VOLUME);
//            invalidate();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelTimer();
        reset();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        setMicState(MicState.END);
        //For Ticmirror
        setOnClickListener(this);
    }

    public void pause() {
        if (mMicState == MicState.LOADING) {
            mMicNextState = MicState.PAUSE;
            return;
        }
        setMicState(MicState.PAUSE);
    }

    public void end() {
        cancelTimer();
        enterLoadingAnimator();
        setMicState(MicState.END);
    }

    public void loading() {
        setMicState(MicState.LOADING);
    }

    public void retry() {
        reset();
        setMicState(MicState.RETRY);
        // 执行完 Error 动画后，重新开始语音
        mErrorAnimator.start();
    }

    public void error() {
        setMicState(MicState.ERROR);
        reset();
        // 执行Error 动画后
        mErrorAnimator.start();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mRect.set(0, 0, w, h);
    }

    private long mPreClickTime;
    private UserOnClickListener mClickListener;

    public void setMicOnClickListener(UserOnClickListener listener) {
        mClickListener = listener;
    }

    @Override
    public void onClick(View view) {
        //处理 TICTIC-4194 ,录音按钮不停的被点击，导致crash
        long now = SystemClock.elapsedRealtime();
        if (now - mPreClickTime < MIN_CLICK_DURATION) {
            return;
        }
        mPreClickTime = now;

        switch (mMicState) {
            case PAUSE:
                setMicState(MicState.START);
                //mClickListener.onUserStartRecord(TriggerType.SINGLE_CLICK);
                mClickListener.onUserStart();
                break;
            case LISTENING:
                //TICTIC-3934 点击的时候只有 开始和取消，没有 loading 状态了
                setMicState(MicState.PAUSE);
                mClickListener.onUserCancel();
                break;
            case LOADING:
                setMicState(MicState.PAUSE);
                mClickListener.onUserCancel();
            default:
                break;
        }
    }

    private enum MicState {
        START, // 开始录音状态，执行一个开始动画，然后自动进入 LISTENING 状态
        LISTENING, // 录音的状态，录音结束进入 LOADING 状态，如果没有检测到语音输入，
        // 则进入 RETRY 重试一次，如果重试后还没有，进入 PAUSE 状态
        LOADING, // 等待服务器结果返回
        RETRY, // 出错了 重试一次，执行一个重试动画后进入 LISTENING
        PAUSE, // 服务器结果返回的状态
        END, // View 初始化后或者销毁后，进入这个状态，但是这个看起来好像是没用的 ... ...
        ERROR // 错误状态
    }

    public interface UserOnClickListener {
        void onUserCancel();

        void onUserStart();
    }
}
