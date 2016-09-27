package net.liutikas.stepperwidget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class StepperWidget extends LinearLayout {
    private final ImageButton mPlusButton;
    private final ImageButton mMinusButton;
    private final TextView mLabelOne;
    private final TextView mLabelTwo;
    private boolean mLabelOneVisible = false;
    private int mCounter = 0;
    private int mChange = 0;

    private ValueAnimator mAnimator;

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
        updateLabel(0);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initalizeAnimator() {
        mAnimator = ValueAnimator.ofInt(100);
        mAnimator.setDuration(300);
        mAnimator.setInterpolator(new FastOutSlowInInterpolator());
        mAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                Integer value = (Integer) valueAnimator.getAnimatedValue();
                if (value < 100 && mChange == 0) {
                    mAnimator.end();
                } else if (mChange < 0) {
                    getEnteringLabel().setTranslationY(100 - value);
                    getLeavingLabel().setTranslationY(-value);
                } else {
                    getEnteringLabel().setTranslationY(-100 + value);
                    getLeavingLabel().setTranslationY(value);
                }
            }
        });
        mAnimator.addListener(new Animator.AnimatorListener() {
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
            updateLabelAnimated(change);
            return;
        }
        if (mLabelOneVisible) {
            mLabelTwo.setText(Integer.toString(mCounter));
            mLabelOne.setVisibility(INVISIBLE);
            mLabelTwo.setVisibility(VISIBLE);
        } else {
            mLabelOne.setText(Integer.toString(mCounter));
            mLabelOne.setVisibility(VISIBLE);
            mLabelTwo.setVisibility(INVISIBLE);
        }
        mLabelOneVisible = !mLabelOneVisible;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void updateLabelAnimated(final int change) {
        if (mAnimator.isRunning()) {
            mAnimator.end();
        }
        mChange = change;
        final TextView enteringLabel = getEnteringLabel();
        enteringLabel.setText(Integer.toString(mCounter));
        enteringLabel.setVisibility(VISIBLE);

        mAnimator.start();
    }
}
