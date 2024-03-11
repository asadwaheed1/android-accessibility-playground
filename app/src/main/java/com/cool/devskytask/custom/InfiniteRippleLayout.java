package com.cool.devskytask.custom;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Property;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.FrameLayout;

import com.cool.devskytask.R;

public class InfiniteRippleLayout extends FrameLayout {

    /**
     * Author：Hardik Talaviya
     * Date：  2020.02.15 1:30 PM
     * Describe:
     */

    private static final int DEFAULT_DURATION = 350;
    private static final int DEFAULT_FADE_DURATION = 75;
    private static final float DEFAULT_ALPHA = 0.2f;
    private static final int DEFAULT_COLOR = Color.BLACK;
    private static final int DEFAULT_BACKGROUND = Color.TRANSPARENT;
    private static final boolean DEFAULT_DELAY_CLICK = true;
    private static final boolean DEFAULT_PERSISTENT = false;
    private static final boolean DEFAULT_SEARCH_ADAPTER = false;
    private static final boolean DEFAULT_RIPPLE_OVERLAY = false;
    private static final int DEFAULT_ROUNDED_CORNERS = 0;

    private static final int FADE_EXTRA_DELAY = 50;

    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect bounds = new Rect();

    private int rippleColor;
    private boolean rippleOverlay;
    private int rippleDuration;
    private int rippleAlpha;
    private boolean rippleDelayClick;
    private int rippleFadeDuration;
    private boolean ripplePersistent;
    private Drawable rippleBackground;
    private boolean rippleInAdapter;
    private float rippleRoundedCorners;

    private float radius;

    private AdapterView parentAdapter;
    private View childView;

    private AnimatorSet rippleAnimator;

    private Point currentCoords = new Point();

    private int layerType;

    private int positionInAdapter;

    /*
     * Animations
     */
    private Property<InfiniteRippleLayout, Float> radiusProperty
            = new Property<InfiniteRippleLayout, Float>(Float.class, "radius") {
        @Override
        public Float get(InfiniteRippleLayout object) {
            return object.getRadius();
        }

        @Override
        public void set(InfiniteRippleLayout object, Float value) {
            object.setRadius(value);
        }
    };
    private Property<InfiniteRippleLayout, Integer> circleAlphaProperty
            = new Property<InfiniteRippleLayout, Integer>(Integer.class, "rippleAlpha") {
        @Override
        public Integer get(InfiniteRippleLayout object) {
            return object.getRippleAlpha();
        }

        @Override
        public void set(InfiniteRippleLayout object, Integer value) {
            object.setRippleAlpha(value);
        }
    };

    public InfiniteRippleLayout(Context context) {
        this(context, null, 0);
    }

    public InfiniteRippleLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public InfiniteRippleLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        setWillNotDraw(false);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.InfiniteRippleLayout);
        rippleColor = a.getColor(R.styleable.InfiniteRippleLayout_mrl_rippleColor, DEFAULT_COLOR);
        rippleOverlay = a.getBoolean(R.styleable.InfiniteRippleLayout_mrl_rippleOverlay, DEFAULT_RIPPLE_OVERLAY);
        rippleDuration = a.getInt(R.styleable.InfiniteRippleLayout_mrl_rippleDuration, DEFAULT_DURATION);
        rippleAlpha = (int) (255 * a.getFloat(R.styleable.InfiniteRippleLayout_mrl_rippleAlpha, DEFAULT_ALPHA));
        rippleDelayClick = a.getBoolean(R.styleable.InfiniteRippleLayout_mrl_rippleDelayClick, DEFAULT_DELAY_CLICK);
        rippleFadeDuration = a.getInteger(R.styleable.InfiniteRippleLayout_mrl_rippleFadeDuration, DEFAULT_FADE_DURATION);
        rippleBackground = new ColorDrawable(a.getColor(R.styleable.InfiniteRippleLayout_mrl_rippleBackground, DEFAULT_BACKGROUND));
        ripplePersistent = a.getBoolean(R.styleable.InfiniteRippleLayout_mrl_ripplePersistent, DEFAULT_PERSISTENT);
        rippleInAdapter = a.getBoolean(R.styleable.InfiniteRippleLayout_mrl_rippleInAdapter, DEFAULT_SEARCH_ADAPTER);
        rippleRoundedCorners = a.getDimensionPixelSize(R.styleable.InfiniteRippleLayout_mrl_rippleRoundedCorners, DEFAULT_ROUNDED_CORNERS);

        a.recycle();

        paint.setColor(rippleColor);
        paint.setAlpha(rippleAlpha);

        enableClipPathSupportIfNecessary();

        startRipple();
    }

    @Override
    public final void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("MaterialRippleLayout can host only one child");
        }
        //noinspection unchecked
        childView = child;
        super.addView(child, index, params);
    }

    @Override
    public void setOnClickListener(OnClickListener onClickListener) {
        if (childView == null) {
            throw new IllegalStateException("MaterialRippleLayout must have a child view to handle clicks");
        }
        childView.setOnClickListener(onClickListener);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener onClickListener) {
        if (childView == null) {
            throw new IllegalStateException("MaterialRippleLayout must have a child view to handle clicks");
        }
        childView.setOnLongClickListener(onClickListener);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return !findClickableViewInChild(childView, (int) event.getX(), (int) event.getY());
    }

    private void startRipple() {
        float endRadius = getEndRadius();

        cancelAnimations();

        rippleAnimator = new AnimatorSet();
        rippleAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                try {
                    if (!ripplePersistent) {
                        setRadius(0);
                        setRippleAlpha(rippleAlpha);
                    }
                    if (rippleDelayClick) {
                        startRipple();
                    }
                    childView.setPressed(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        ObjectAnimator ripple = ObjectAnimator.ofFloat(this, radiusProperty, radius, endRadius);
        ripple.setDuration(rippleDuration);
        ripple.setInterpolator(new DecelerateInterpolator());
        ObjectAnimator fade = ObjectAnimator.ofInt(this, circleAlphaProperty, rippleAlpha, 0);
        fade.setDuration(rippleFadeDuration);
        fade.setInterpolator(new AccelerateInterpolator());
        fade.setStartDelay(rippleDuration - rippleFadeDuration - FADE_EXTRA_DELAY);

        if (ripplePersistent) {
            rippleAnimator.play(ripple);
        } else if (getRadius() > endRadius) {
            fade.setStartDelay(0);
            rippleAnimator.play(fade);
        } else {
            rippleAnimator.playTogether(ripple, fade);
        }
        rippleAnimator.start();
    }

    private void cancelAnimations() {
        if (rippleAnimator != null) {
            rippleAnimator.cancel();
            rippleAnimator.removeAllListeners();
        }
    }

    private float getEndRadius() {
        final int width = getWidth();
        final int height = getHeight();

        final int halfWidth = width / 2;
        final int halfHeight = height / 2;

        final float radiusX = halfWidth > currentCoords.x ? width - currentCoords.x : currentCoords.x;
        final float radiusY = halfHeight > currentCoords.y ? height - currentCoords.y : currentCoords.y;

        return (float) Math.sqrt(Math.pow(radiusX, 2) + Math.pow(radiusY, 2)) * 1.2f;
    }

    private AdapterView findParentAdapterView() {
        if (parentAdapter != null) {
            return parentAdapter;
        }
        ViewParent current = getParent();
        while (true) {
            if (current instanceof AdapterView) {
                parentAdapter = (AdapterView) current;
                return parentAdapter;
            } else {
                try {
                    current = current.getParent();
                } catch (NullPointerException npe) {
                    throw new RuntimeException("Could not find a parent AdapterView");
                }
            }
        }
    }

    private boolean adapterPositionChanged() {
        if (rippleInAdapter) {
            int newPosition = findParentAdapterView().getPositionForView(InfiniteRippleLayout.this);
            final boolean changed = newPosition != positionInAdapter;
            positionInAdapter = newPosition;
            if (changed) {
                cancelAnimations();
                childView.setPressed(false);
                setRadius(0);
            }
            return changed;
        }
        return false;
    }

    private boolean findClickableViewInChild(View view, int x, int y) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                final Rect rect = new Rect();

                child.getHitRect(rect);

                final boolean contains = rect.contains(x, y);
                if (contains) {
                    return findClickableViewInChild(child, x - rect.left, y - rect.top);
                }
            }
        } else if (view != childView) {
            return (view.isEnabled() && (view.isClickable() || view.isLongClickable() || view.isFocusableInTouchMode()));
        }

        return view.isFocusableInTouchMode();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bounds.set(0, 0, w, h);
        rippleBackground.setBounds(bounds);
    }

    @Override
    public boolean isInEditMode() {
        return true;
    }

    /*
     * Drawing
     */
    @Override
    public void draw(Canvas canvas) {
        final boolean positionChanged = adapterPositionChanged();
        currentCoords = new Point(getWidth() / 2, getHeight() / 2);
        if (rippleOverlay) {
            if (!positionChanged) {
                rippleBackground.draw(canvas);
            }
            super.draw(canvas);
            if (!positionChanged) {
                if (rippleRoundedCorners != 0) {
                    Path clipPath = new Path();
                    RectF rect = new RectF(0, 0, canvas.getWidth(), canvas.getHeight());
                    clipPath.addRoundRect(rect, rippleRoundedCorners, rippleRoundedCorners, Path.Direction.CW);
                    canvas.clipPath(clipPath);
                }
                canvas.drawCircle(currentCoords.x, currentCoords.y, radius, paint);
            }
        } else {
            if (!positionChanged) {
                rippleBackground.draw(canvas);
                canvas.drawCircle(currentCoords.x, currentCoords.y, radius, paint);
            }
            super.draw(canvas);
        }
    }

    private float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        invalidate();
    }

    public int getRippleAlpha() {
        return paint.getAlpha();
    }

    public void setRippleAlpha(Integer rippleAlpha) {
        paint.setAlpha(rippleAlpha);
        invalidate();
    }

    /**
     * {@link Canvas#clipPath(Path)} is not supported in hardware accelerated layers
     * before API 18. Use software layer instead
     * <p/>
     * https://developer.android.com/guide/topics/graphics/hardware-accel.html#unsupported
     */
    private void enableClipPathSupportIfNecessary() {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (rippleRoundedCorners != 0) {
                layerType = getLayerType();
                setLayerType(LAYER_TYPE_SOFTWARE, null);
            } else {
                setLayerType(layerType, null);
            }
        }
    }
}