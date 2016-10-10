package net.liutikas.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple widget that will allow users to step up and step down a number and a number will animate
 * as user presses the plus and minus buttons.
 */
public class StepperWidget extends LinearLayout {
    public interface OnUpdateListener {
        void onUpdate(int oldValue, int newValue);
    }

    private final int mLabelHeight;
    private final ImageButton mPlusButton;
    private final ImageButton mMinusButton;
    private final TextView mLabelOne;
    private final TextView mLabelTwo;
    private boolean mLabelOneVisible = false;
    private int mCounter = 0;
    private int mChange = 0;

    private List<OnUpdateListener> mListeners;
    private int mMin = Integer.MIN_VALUE;
    private int mMax = Integer.MAX_VALUE;

    private ValueAnimator mSwitchAnimator;
    private ValueAnimator mBounceAnimator;

    public StepperWidget(Context context) {
        this(context, null);
    }

    public StepperWidget(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StepperWidget(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs);
        setOrientation(VERTICAL);
        inflate(context, R.layout.stepper, this);
        mPlusButton = (ImageButton) findViewById(R.id.plus);
        mMinusButton = (ImageButton) findViewById(R.id.minus);
        mLabelOne = (TextView) findViewById(R.id.label1);
        mLabelTwo = (TextView) findViewById(R.id.label2);
        mLabelHeight =
                context.getResources().getDimensionPixelSize(R.dimen.stepper_widget_label_height);
        mPlusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLabel(1);
            }
        });
        mMinusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLabel(-1);
            }
        });
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            initalizeAnimator();
        }
        mListeners = new ArrayList<>();
        updateLabel(0);
    }

    /**
     * Set the maximum value that the user is allowed to choose when using this widget.
     * @param max The largest integer value that the user is allowed to enter when using this
     *            widget.
     */
    public void setMax(int max) {
        mMax = max;
        updateLabel(0);
    }

    /**
     * Set the minimum value that the user is allowed to choose when using this widget.
     * @param min The smallest integer value that the user is allowed to enter when using this
     *            widget.
     */
    public void setMin(int min) {
        mMin = min;
        updateLabel(0);
    }

    public void addOnUpdateListener(OnUpdateListener listener) {
        mListeners.add(listener);
    }

    public void removeOnUpdateListener(OnUpdateListener listener) {
        mListeners.remove(listener);
    }

    /**
     * @return The value that the user has currently chosen using this widget.
     */
    public int getValue() {
        return mCounter;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initalizeAnimator() {
        mSwitchAnimator = ValueAnimator.ofInt(100);
        mSwitchAnimator.setDuration(300);
        mSwitchAnimator.setInterpolator(new FastOutSlowInInterpolator());
        mSwitchAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Integer value = (Integer) valueAnimator.getAnimatedValue();
                if (mChange == 0) {
                    // Do nothing
                } else if (mChange < 0) {
                    getEnteringLabel().setTranslationY((100 - value) * mLabelHeight / 100f);
                    getLeavingLabel().setTranslationY(-value * mLabelHeight / 100f);
                } else {
                    getEnteringLabel().setTranslationY((-100 + value) * mLabelHeight / 100f);
                    getLeavingLabel().setTranslationY(value * mLabelHeight / 100f);
                }
            }
        });
        mSwitchAnimator.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                getLeavingLabel().setVisibility(INVISIBLE);
                mLabelOneVisible = !mLabelOneVisible;
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        mBounceAnimator = ValueAnimator.ofInt(100);
        mBounceAnimator.setDuration(300);
        mBounceAnimator.setInterpolator(new FastOutSlowInInterpolator());
        mBounceAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Integer value = (Integer) valueAnimator.getAnimatedValue();
                if (value > 50) {
                    value = 100 - value;
                }
                if (mChange == 0) {
                    // Do nothing.
                } else if (mChange < 0) {
                    getLeavingLabel().setTranslationY(-value * mLabelHeight / 100f);
                } else {
                    getLeavingLabel().setTranslationY(value * mLabelHeight / 100f);
                }
            }
        });
    }

    private TextView getEnteringLabel() {
        return mLabelOneVisible ? mLabelTwo : mLabelOne;
    }

    private TextView getLeavingLabel() {
        return mLabelOneVisible ? mLabelOne : mLabelTwo;
    }

    private void updateLabel(int change) {
        mCounter += change;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
            if (mBounceAnimator.isRunning()) {
                mBounceAnimator.end();
            }
            if (mSwitchAnimator.isRunning()) {
                mSwitchAnimator.end();
            }
            if (mCounter < mMin) {
                mCounter = mMin;
                updateLabelVisibility(mLabelOneVisible);
                bounceLabelAnimated(change);
            } else if (mCounter > mMax) {
                mCounter = mMax;
                updateLabelVisibility(mLabelOneVisible);
                bounceLabelAnimated(change);
            } else {
                for (OnUpdateListener listener : mListeners) {
                    listener.onUpdate(mCounter - change, mCounter);
                }
                switchLabelAnimated(change);
            }
        } else {
            updateLabelVisibility(!mLabelOneVisible);
            for (OnUpdateListener listener : mListeners) {
                listener.onUpdate(mCounter - change, mCounter);
            }
        }
    }

    private void updateLabelVisibility(boolean labelOneVisible) {
        if (labelOneVisible) {
            mLabelOne.setText(Integer.toString(mCounter));
            mLabelOne.setVisibility(VISIBLE);
            mLabelTwo.setVisibility(INVISIBLE);
        } else {
            mLabelTwo.setText(Integer.toString(mCounter));
            mLabelOne.setVisibility(INVISIBLE);
            mLabelTwo.setVisibility(VISIBLE);
        }
        mLabelOneVisible = labelOneVisible;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void bounceLabelAnimated(int change) {
        mChange = change;
        mBounceAnimator.start();
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void switchLabelAnimated(final int change) {
        mChange = change;
        final TextView enteringLabel = getEnteringLabel();
        enteringLabel.setText(Integer.toString(mCounter));
        enteringLabel.setVisibility(VISIBLE);

        mSwitchAnimator.start();
    }
}
