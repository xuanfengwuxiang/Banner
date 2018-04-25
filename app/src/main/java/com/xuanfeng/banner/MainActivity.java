package com.xuanfeng.banner;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xuanfeng.bannerview.BannerConfig;
import com.xuanfeng.bannerview.BannerView;
import com.xuanfeng.bannerview.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    public static final int SHOW_NEXT_PAGE = 1;

    TextView tvDesc;
    LinearLayout llDots;
    private int[] imageResIds = {
            R.drawable.a,
            R.drawable.b,
            R.drawable.c,
            R.drawable.d,
            R.drawable.e,
    };

    private String[] descs = {
            "巩俐不低俗，我就不能低俗",
            "扑树又回来啦！再唱经典老歌引万人大合唱",
            "揭秘北京电影如何升级",
            "乐视网TV版大派送",
            "热血屌丝的反杀",
    };

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SHOW_NEXT_PAGE:
                    showNextPage();
                    break;
            }
        }
    };
    private ViewPager mView_pager;
    private BannerView mBannerView;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBannerView = findViewById(R.id.bannerView);


        mView_pager = (ViewPager) findViewById(R.id.view_pager);
        tvDesc = findViewById(R.id.tv_desc);
        llDots = findViewById(R.id.ll_dots);
        mView_pager.setAdapter(new BannerAdapter(imageResIds));
        initeDots();
        changeDescAndDot(0);

        mView_pager.setOnPageChangeListener(mOnPageChangeListener);
        mView_pager.setCurrentItem(mView_pager.getAdapter().getCount() / 2);
        handler.sendEmptyMessageDelayed(SHOW_NEXT_PAGE, 3000);
        mView_pager.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        handler.removeCallbacksAndMessages(null);

                        break;
                    case MotionEvent.ACTION_UP:
                        handler.sendEmptyMessageDelayed(SHOW_NEXT_PAGE, 3000);
                        break;
                }
                return false;
            }


        });
        testBanner();
        Log.i("最大值",""+Integer.MAX_VALUE);

    }


    private class BannerAdapter extends PagerAdapter {
        private int[] imageResIds;

        public BannerAdapter(int[] imageResIds) {
            this.imageResIds = imageResIds;

        }

        @Override
        public int getCount() {
            return imageResIds.length * 10000 * 100;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            ImageView imageView = new ImageView(container.getContext());
            imageView.setBackgroundResource(imageResIds[position % imageResIds.length]);
            container.addView(imageView);
            return imageView;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }//适配器

    private void initeDots() {
        for (int i = 0; i < imageResIds.length; i++) {
            View dot = new View(this);
            dot.setBackgroundResource(R.drawable.selector_dot);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dp2px(5), dp2px(5));
            params.leftMargin = i == 0 ? 0 : 5;
            dot.setLayoutParams(params);
            llDots.addView(dot);
        }
    }

    private void changeDescAndDot(int position) {
        position = position % imageResIds.length;
        tvDesc.setText(descs[position]);
        for (int i = 0; i < llDots.getChildCount(); i++) {
            llDots.getChildAt(i).setSelected(i == position);
        }
    }


    public int dp2px(int dp) {
        float density = getResources().getDisplayMetrics().density;    // 获取手机屏幕的密度
        return (int) (dp * density + 0.5f);    // 加0.5是为了四舍五入
    }

    ViewPager.OnPageChangeListener mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            changeDescAndDot(position);
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }
    };

    private void showNextPage() {
        mView_pager.setCurrentItem(mView_pager.getCurrentItem() + 1);
        handler.sendEmptyMessageDelayed(SHOW_NEXT_PAGE, 2000);
    }


    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

    private void testBanner() {
        List<Integer> BannerList = new ArrayList<>();
        for (int i = 0; i < imageResIds.length; i++) {
            BannerList.add(imageResIds[i]);
        }
        List<String> tittle = new ArrayList<>();
        for (int i = 0; i < descs.length; i++) {
            tittle.add(descs[i]);
        }
        mBannerView.setImages(BannerList)
                .setImageLoader(new GlideImageLoader())
                .setOnBannerListener(new OnBannerListener() {
                    @Override
                    public void OnBannerClick(int position) {
                        Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show();
                    }
                })
                .setBannerTitles(tittle)
                .setBannerStyle(BannerConfig.NO_INDICATOR)
                .start();
    }
}
