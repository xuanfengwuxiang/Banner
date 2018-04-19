package com.xuanfeng.bannerview;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xuanfeng.bannerview.listener.OnBannerListener;
import com.xuanfeng.bannerview.loader.ImageLoaderInterface;
import com.xuanfeng.bannerview.widget.BannerViewPager;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xuanfengwuxiang on 2018/4/18.
 * 轮播图Banner
 */

public class BannerView extends FrameLayout {

    //常量
    public String TAG = this.getClass().getSimpleName();

    //全局变量
    private Context mContext;
    private ImageLoaderInterface mImageLoader;//图片加载器
    private Handler handler = new Handler();
    private OnBannerListener listener;
    private ViewPager.OnPageChangeListener mUserDefineOnPageChangeListener;


    //Banner的变量
    private List mUrlList;//图片的地址(不指定固定格式);
    private List<View> mImageViewList;//展示的ImageView集合
    private int mDelayTime = BannerConfig.DELAY_TIME;//进入下一页的时间间隔
    private int mScrollingTime = BannerConfig.SCROLL_DURATION;//item滚动的动画时间
    private int mBannerStyle = BannerConfig.CIRCLE_INDICATOR;//Banner类型
    private boolean mIsAutoPlay = BannerConfig.IS_AUTO_PLAY;//是否自动播放
    private BannerPagerAdapter mAdapter;
    private int scaleType = 1;//默认center_crop
    private boolean mIsScroll = BannerConfig.IS_SCROLL;


    //指示器的变量
    private List<ImageView> mIndicatorList;//指示器的集合
    private int indicatorSize;//指示器大小
    private int mIndicatorSelectedResId = R.drawable.gray_radius;//指示器选中
    private int mIndicatorUnselectedResId = R.drawable.white_radius;//指示器未选中
    private int mIndicatorWidth;//指示器宽
    private int mIndicatorHeight;//指示器高
    private int mIndicatorMargin = BannerConfig.INDICATOR_MARGIN;//指示器之间的间隔
    private int mLastPosition = 0;//上一个选中位置，用于把上次选中的位置置为正常颜色
    private int gravity = -1;

    //标题的变量
    private int titleBackground;//标题背景色
    private int titleHeight;
    private int titleTextColor;//标题文字色
    private int titleTextSize;//标题文字大小
    private List<String> mTittleList;//标题集合


    //控件
    private BannerViewPager mViewPager;
    private BannerScroller mScroller;
    //样式1
    private LinearLayout mLlOnlyIndicator;//单独的指示器
    //样式2
    private LinearLayout mLlTitleAndIndicator;//标题和指示器父布局
    private TextView mTvTittle;//标题
    private LinearLayout mLlInnerIndicator;//指示器
    //样式3
    private TextView mTvNumIndicator;//数字指示器


    public BannerView(@NonNull Context context) {
        this(context, null);
    }

    public BannerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BannerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        handleTypedArray(context, attrs);
        initViews(context);
        initData();
    }

    //获取属性值
    private void handleTypedArray(Context context, AttributeSet attrs) {
        if (attrs == null) {
            return;
        }
        indicatorSize = context.getResources().getDisplayMetrics().widthPixels / 80;
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BannerView);
        mIndicatorSelectedResId = typedArray.getResourceId(R.styleable.BannerView_indicator_drawable_selected, R.drawable.gray_radius);
        mIndicatorUnselectedResId = typedArray.getResourceId(R.styleable.BannerView_indicator_drawable_unselected, R.drawable.white_radius);
        mDelayTime = typedArray.getInt(R.styleable.BannerView_delay_time, BannerConfig.DELAY_TIME);
        mScrollingTime = typedArray.getInt(R.styleable.BannerView_scroll_time, BannerConfig.SCROLL_DURATION);
        scaleType = typedArray.getInt(R.styleable.BannerView_image_scale_type, scaleType);
        titleBackground = typedArray.getColor(R.styleable.BannerView_title_background, BannerConfig.TITLE_BACKGROUND);
        titleTextColor = typedArray.getColor(R.styleable.BannerView_title_textcolor, BannerConfig.TITLE_TEXT_COLOR);
        titleTextSize = typedArray.getDimensionPixelSize(R.styleable.BannerView_title_textsize, BannerConfig.TITLE_TEXT_SIZE);
        titleHeight = typedArray.getDimensionPixelSize(R.styleable.BannerView_title_height, BannerConfig.TITLE_HEIGHT);
        mIndicatorWidth = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicator_width, indicatorSize);
        mIndicatorHeight = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicator_height, indicatorSize);
        mIndicatorMargin = typedArray.getDimensionPixelSize(R.styleable.BannerView_indicator_margin, BannerConfig.INDICATOR_MARGIN);
        mIsAutoPlay = typedArray.getBoolean(R.styleable.BannerView_is_auto_play, BannerConfig.IS_AUTO_PLAY);
        typedArray.recycle();
    }

    //初始化视图
    private void initViews(@NonNull Context context) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_banner, this, true);
        mViewPager = (BannerViewPager) view.findViewById(R.id.bannerViewPager);
        //单独的指示器
        mLlOnlyIndicator = (LinearLayout) view.findViewById(R.id.ll_only_indicator);
        //数字指示器
        mTvNumIndicator = (TextView) view.findViewById(R.id.tv_num_indicator);
        //标题和指示器
        mLlTitleAndIndicator = (LinearLayout) view.findViewById(R.id.ll_title_and_indicator);
        mTvTittle = (TextView) view.findViewById(R.id.tv_tittle);
        mLlInnerIndicator = (LinearLayout) view.findViewById(R.id.ll_inner_indicator);
        initViewPagerScroll();
    }

    //设置进入下一页，动画速度
    private void initViewPagerScroll() {
        try {
            Field mField = ViewPager.class.getDeclaredField("mScroller");
            mField.setAccessible(true);
            mScroller = new BannerScroller(mViewPager.getContext());
            mScroller.setDuration(mScrollingTime);
            mField.set(mViewPager, mScroller);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    //初始化
    private void initData() {
        mUrlList = new ArrayList();
        mImageViewList = new ArrayList<>();
        mTittleList = new ArrayList<>();
        mIndicatorList = new ArrayList<>();
    }

    //暴露的方法区=================

    public BannerView setIndicatorSelectedResId(int selectedResId) {
        mIndicatorSelectedResId = selectedResId;
        return this;
    }

    public BannerView setIndicatorUnselectedResId(int unSelectedResId) {
        mIndicatorUnselectedResId = unSelectedResId;
        return this;
    }

    public BannerView setDelayTime(int delayTime) {
        this.mDelayTime = delayTime;
        return this;
    }

    public BannerView setScrollingTime(int millisecond) {
        mDelayTime = millisecond;
        return this;
    }

    public BannerView setBannerTitles(List<String> titles) {
        this.mTittleList = titles;
        return this;
    }

    public BannerView setBannerStyle(int bannerStyle) {
        mBannerStyle = bannerStyle;
        return this;
    }

    public BannerView setImages(List<?> imageUrls) {
        mUrlList = imageUrls;
        return this;
    }

    public BannerView isAutoPlay(boolean isAutoPlay) {
        mIsAutoPlay = isAutoPlay;
        return this;
    }

    public BannerView setImageLoader(ImageLoaderInterface imageLoader) {
        mImageLoader = imageLoader;
        return this;
    }

    public BannerView setIndicatorGravity(int type) {
        switch (type) {
            case BannerConfig.LEFT:
                this.gravity = Gravity.LEFT | Gravity.CENTER_VERTICAL;
                break;
            case BannerConfig.CENTER:
                this.gravity = Gravity.CENTER;
                break;
            case BannerConfig.RIGHT:
                this.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
                break;
        }
        return this;
    }

    public BannerView setOffscreenPageLimit(int limit) {
        if (mViewPager != null) {
            mViewPager.setOffscreenPageLimit(limit);
        }
        return this;
    }

    public BannerView setViewPagerIsScroll(boolean isScroll) {
        this.mIsScroll = isScroll;
        return this;
    }

    public BannerView setUserDefineOnPageChangeListener(ViewPager.OnPageChangeListener userDefineOnPageChangeListener) {
        mUserDefineOnPageChangeListener = userDefineOnPageChangeListener;
        return this;
    }

    public void update(List<?> imageUrls, List<String> titles) {
        this.mUrlList.clear();
        this.mTittleList.clear();
        this.mUrlList.addAll(imageUrls);
        this.mTittleList.addAll(titles);
        start();
    }

    public BannerView setOnBannerListener(OnBannerListener listener) {
        this.listener = listener;
        return this;
    }

    public void releaseBanner() {
        handler.removeCallbacksAndMessages(null);
    }

    public BannerView start() {
        setBannerStyleUI();
        setImageList(mUrlList);
        setData();
        return this;
    }

    //设置指示器的风格
    private void setBannerStyleUI() {
        int visibility = View.GONE;
        if (mUrlList.size() > 1) {
            visibility = View.VISIBLE;
        }
        switch (mBannerStyle) {
            case BannerConfig.CIRCLE_INDICATOR:
                mLlOnlyIndicator.setVisibility(visibility);
                break;
            case BannerConfig.NUM_INDICATOR:
                mTvNumIndicator.setVisibility(visibility);
                break;
            case BannerConfig.CIRCLE_INDICATOR_TITLE:
                mLlTitleAndIndicator.setVisibility(visibility);
                setTitleStyleUI();
                break;

        }
    }

    //设置标题的颜色、大小、背景
    private void setTitleStyleUI() {
        if (mTittleList.size() != mUrlList.size()) {
            throw new RuntimeException("[Banner] --> The number of titles and images is different");
        }
        if (titleBackground != -1) {
            mLlTitleAndIndicator.setBackgroundColor(titleBackground);
        }
        if (titleHeight != -1) {
            mLlTitleAndIndicator.setLayoutParams(new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, titleHeight));
        }
        if (titleTextColor != -1) {
            mTvTittle.setTextColor(titleTextColor);
        }
        if (titleTextSize != -1) {
            mTvTittle.setTextSize(TypedValue.COMPLEX_UNIT_SP, titleTextSize);
        }
        if (mTittleList != null && mTittleList.size() > 0) {
            mTvTittle.setText(mTittleList.get(0));
            mTvTittle.setVisibility(View.VISIBLE);
            mLlTitleAndIndicator.setVisibility(View.VISIBLE);
        }
    }

    //设置ImageView集合
    private void setImageList(List<?> imagesUrl) {
        if (imagesUrl == null || imagesUrl.size() <= 0) {
            return;
        }
        initIndicators();
        mImageViewList.clear();
        for (int i = 0; i < imagesUrl.size(); i++) {
            View imageView = null;
            if (mImageLoader != null) {
                imageView = mImageLoader.createImageView(mContext);
            }
            if (imageView == null) {
                imageView = new ImageView(mContext);
            }
            setScaleType(imageView);
            mImageViewList.add(imageView);
            if (mImageLoader != null) {
                mImageLoader.displayImage(mContext, imagesUrl.get(i), imageView);
            }

        }
    }

    //分类初始化指示器
    private void initIndicators() {
        if (mBannerStyle == BannerConfig.CIRCLE_INDICATOR || mBannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE) {
            createIndicator();
        } else if (mBannerStyle == BannerConfig.NUM_INDICATOR) {
            mTvNumIndicator.setText("1/" + mTittleList.size());
        }
    }

    //新建单独指示器
    private void createIndicator() {
        mIndicatorList.clear();
        mLlOnlyIndicator.removeAllViews();
        mLlInnerIndicator.removeAllViews();
        for (int i = 0; i < mUrlList.size(); i++) {
            ImageView imageView = new ImageView(mContext);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(mIndicatorWidth, mIndicatorHeight);
            params.leftMargin = mIndicatorMargin;
            params.rightMargin = mIndicatorMargin;
            if (i == 0) {
                imageView.setImageResource(mIndicatorSelectedResId);
            } else {
                imageView.setImageResource(mIndicatorUnselectedResId);
            }
            mIndicatorList.add(imageView);
            if (mBannerStyle == BannerConfig.CIRCLE_INDICATOR) {
                mLlOnlyIndicator.addView(imageView, params);
            } else if (mBannerStyle == BannerConfig.CIRCLE_INDICATOR_TITLE) {
                mLlInnerIndicator.addView(imageView, params);
            }


        }
    }

    //设置图片的模式
    private void setScaleType(View imageView) {
        if (imageView instanceof ImageView) {
            ImageView view = ((ImageView) imageView);
            switch (scaleType) {
                case 0:
                    view.setScaleType(ImageView.ScaleType.CENTER);
                    break;
                case 1:
                    view.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    break;
                case 2:
                    view.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                    break;
                case 3:
                    view.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    break;
                case 4:
                    view.setScaleType(ImageView.ScaleType.FIT_END);
                    break;
                case 5:
                    view.setScaleType(ImageView.ScaleType.FIT_START);
                    break;
                case 6:
                    view.setScaleType(ImageView.ScaleType.FIT_XY);
                    break;
                case 7:
                    view.setScaleType(ImageView.ScaleType.MATRIX);
                    break;
            }

        }
    }

    //给ViewPager设置数据
    private void setData() {
        if (mAdapter == null) {
            mAdapter = new BannerPagerAdapter(mUrlList);
            mViewPager.addOnPageChangeListener(mOnPageChangeListener);
        }
        mViewPager.setAdapter(mAdapter);
        mViewPager.setFocusable(true);
        mViewPager.setCurrentItem(mViewPager.getAdapter().getCount() / 2);//从中间开始循环
        if (gravity != -1) mLlOnlyIndicator.setGravity(gravity);
        if (mIsScroll && mUrlList.size() > 1) {
            mViewPager.setScrollable(true);
        } else {
            mViewPager.setScrollable(false);
        }
        if (mIsAutoPlay) startAutoPlay();
    }

    public void startAutoPlay() {
        handler.removeCallbacks(mRunnable);
        handler.postDelayed(mRunnable, mDelayTime);
    }

    public void stopAutoPlay() {
        handler.removeCallbacks(mRunnable);
    }

    Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            if (mUrlList.size() > 1 && mIsAutoPlay) {
                mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
                handler.postDelayed(mRunnable, mDelayTime);
            }
        }
    };

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mIsAutoPlay) {
            int action = ev.getAction();
            if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL
                    || action == MotionEvent.ACTION_OUTSIDE) {
                startAutoPlay();
            } else if (action == MotionEvent.ACTION_DOWN) {
                stopAutoPlay();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private class BannerPagerAdapter extends PagerAdapter {
        private List<Integer> imageResIds;

        public BannerPagerAdapter(List imageResIds) {
            this.imageResIds = imageResIds;
        }

        @Override
        public int getCount() {
            return imageResIds == null ? 0 : imageResIds.size() * 10000 * 100;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            final int realPostion = position % imageResIds.size();
            View view = mImageViewList.get(realPostion);
            if (listener != null) {
                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        listener.OnBannerClick(realPostion);
                    }
                });
            }
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }

    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            int realPosition = position % mUrlList.size();
            if (mUserDefineOnPageChangeListener != null) {
                mUserDefineOnPageChangeListener.onPageScrolled(realPosition, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageSelected(int position) {
            changeTittleAndIndicator(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            if (mUserDefineOnPageChangeListener != null) {
                mUserDefineOnPageChangeListener.onPageScrollStateChanged(state);
            }
        }
    };

    //改变指示器、标题
    private void changeTittleAndIndicator(int position) {
        int realPosition = position % mUrlList.size();
        if (mUserDefineOnPageChangeListener != null) {
            mUserDefineOnPageChangeListener.onPageSelected(realPosition);
        }
        switch (mBannerStyle) {
            case BannerConfig.CIRCLE_INDICATOR_TITLE:
                mTvTittle.setText(mTittleList.get(realPosition));
            case BannerConfig.CIRCLE_INDICATOR:
                mIndicatorList.get(mLastPosition).setImageResource(mIndicatorUnselectedResId);
                mIndicatorList.get(realPosition).setImageResource(mIndicatorSelectedResId);
                mLastPosition = realPosition;
                break;
            case BannerConfig.NUM_INDICATOR:
                mTvNumIndicator.setText(realPosition + "/" + mUrlList.size());
                break;
        }
    }
}
