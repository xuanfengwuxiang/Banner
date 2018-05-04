

How To use STEP1:

allprojects { repositories { ... maven { url 'https://www.jitpack.io' } } }

STEP2:

dependencies { compile 'com.github.xuanfengwuxiang:Banner:1.2' }

STEP3:

mBannerView.setImages(BannerList) .setImageLoader(new GlideImageLoader()) .setOnBannerListener(new OnBannerListener() { @Override public void OnBannerClick(int position) { Toast.makeText(MainActivity.this, "" + position, Toast.LENGTH_SHORT).show(); } }) .setBannerTitles(tittle) .setBannerStyle(BannerConfig.CIRCLE_INDICATOR_TITLE) .start();

Check demo for more details
