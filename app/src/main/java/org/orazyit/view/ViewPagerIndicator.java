package org.orazyit.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.crazyit.customviewpager.R;

import java.util.List;

/**
 * Created by chenti on 2016/4/14.
 */
public class ViewPagerIndicator extends HorizontalScrollView {

    private LinearLayout linearLayout;

    private Paint mPaint;

    private Path mPath;

    private int mTriangleWidth;

    private int mTriangleHeight;

    private static final float RADIO_TRIANGLE_WIDTH = 1/6F;   //三角形宽度比例
    /**
     * 三角形底边的最大宽度
     */
    private final int DIMENSION_TRIANGLE_WIDTH_MAX = (int) (getScreenWidth()/3 * RADIO_TRIANGLE_WIDTH);

    private int mInitTranslationX;

    //画直线的变量
    private int mLineTranslationX;

    private int mTranslationX;

    private int mTabVisibleCount;

    private static final int COUNT_DEFAULT_TAB = 4;
    private static final int COLOR_TEXT_NORMAL = 0x77FFFFFF;
    private static final int COLOR_TEXT_HIGHLIGHT  = 0xFFFFFFFF;

    private List<String> mTitles;

    private ViewPager viewPager;

    public ViewPagerIndicator(Context context) {
        this(context,null);
    }

    public ViewPagerIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);

        setHorizontalScrollBarEnabled(false);

        linearLayout = new LinearLayout(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
        , ViewGroup.LayoutParams.MATCH_PARENT);
        linearLayout.setLayoutParams(lp);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        addView(linearLayout);

        //获取可见Tab的数量
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.ViewPagerIndicator);
        mTabVisibleCount = a.getInt(R.styleable.ViewPagerIndicator_visible_tab_count,COUNT_DEFAULT_TAB);
        if(mTabVisibleCount < 0){
            mTabVisibleCount = COUNT_DEFAULT_TAB;
        }
        a.recycle();

        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStrokeWidth(15);
        mPaint.setColor(Color.parseColor("#ffffffff"));
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setPathEffect(new CornerPathEffect(3)); //让三角形不要太尖锐

    }

    @Override
    protected void dispatchDraw(Canvas canvas) {


        canvas.save();
        /**
         *画布平移，画三角形
         */

//        canvas.translate(mInitTranslationX + mTranslationX,getHeight() + 2);
//        canvas.drawPath(mPath,mPaint);

        /**
         * 画直线
         */
        canvas.drawLine(mLineTranslationX,getHeight() + 2,
                mLineTranslationX+getScreenWidth()/mTabVisibleCount,
                getHeight() + 2,mPaint);

        canvas.restore();

        super.dispatchDraw(canvas);
    }


    //设置三角形大小，只要控件宽高发生变化时回调此方法
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mTriangleWidth = (int) (w / mTabVisibleCount * RADIO_TRIANGLE_WIDTH);  //一个Tab的宽度的1/6
        mTriangleWidth = Math.min(mTriangleWidth,DIMENSION_TRIANGLE_WIDTH_MAX);
        mInitTranslationX = (w / mTabVisibleCount / 2) - (mTriangleWidth / 2);  //(w / 3 / 2)半个Tab


        initTriangle();
    }

    //当XML加载完成后回调这个方法
    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        int cCount = linearLayout.getChildCount();
        if(cCount == 0) return;
        for(int i=0; i<cCount;i++){
            View view = linearLayout.getChildAt(i);
            LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) view.getLayoutParams();

            lp.weight = 0;
            lp.width = getScreenWidth()/mTabVisibleCount;
            view.setLayoutParams(lp);
        }

        setItemClickEvent();
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);
        Log.d("Tag","scroll");
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        Log.d("Tag","scrolling");
    }

    /**
     * 初始化三角形
     */
    private void initTriangle() {

        mTriangleHeight = mTriangleWidth / 2;

        mPath = new Path();
        //画布平移之后
        mPath.moveTo(0,0);
        mPath.lineTo(mTriangleWidth,0);
        mPath.lineTo(mTriangleWidth / 2, -mTriangleHeight);
        mPath.close();  //完成闭合
    }

    /**
     * 指示器跟随手指进行滚动
     * @param position
     * @param offset
     */
    public void scroll(int position, float offset) {
        int tabWidth = getWidth() / mTabVisibleCount;
        mTranslationX = (int) (tabWidth * (offset + position));

        mLineTranslationX = (int) (tabWidth * (offset + position));

        //容器移动，当Tab处于移动至当前界面最后一个时，但不是总的Tab最后一个
        if(position >= mTabVisibleCount - 2 && offset > 0 && linearLayout.getChildCount() > mTabVisibleCount
                && position != linearLayout.getChildCount()-2){
            if(mTabVisibleCount != 1){
                this.scrollTo((position - (mTabVisibleCount - 2)) * tabWidth + (int)(tabWidth * offset),0);
            }
            else {
                this.scrollTo(position * tabWidth + (int)(tabWidth * offset),0);
            }
        }

        invalidate();
    }

    //获取屏幕宽度
    public int getScreenWidth() {
        WindowManager wm = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    public void setTabItemTitles(List<String> titles){
        if(titles != null && titles.size() > 0){
            this.linearLayout.removeAllViews();
            mTitles = titles;
            for(String title:mTitles){
                linearLayout.addView(generateTextView(title));
            }

            setItemClickEvent();
        }
    }

    /**
     *根据title创建Tab
     * @param title
     * @return
     */
    private View generateTextView(String title) {
        TextView tv = new TextView(getContext());
//        LinearLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
//                ViewGroup.LayoutParams.MATCH_PARENT);
        FrameLayout.LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);

        lp.width = getScreenWidth()/mTabVisibleCount;
        tv.setText(title);
        tv.setGravity(Gravity.CENTER);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP,16);
        tv.setTextColor(COLOR_TEXT_NORMAL);
        tv.setLayoutParams(lp);
        return tv;
    }

    /**
     * 设置可见的Tab数量
     * @param count
     */
    public void setVisibleTabCount(int count){
        mTabVisibleCount = count;
    }

    /**
     * 设置关联的ViewPager
     * @param viewPager
     * @param pos
     */
    public void setViewPager(ViewPager viewPager,int pos){
        this.viewPager = viewPager;
        viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                //tabWidth * positionOffset + position * tabWidth
                scroll(position,positionOffset);

                if(mListener != null){
                    mListener.onPageScrolled(position,positionOffset,positionOffsetPixels);
                }
            }

            @Override
            public void onPageSelected(int position) {

                if(mListener != null){
                    mListener.onPageSelected(position);
                }
                highLightTextView(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                if(mListener != null){
                    mListener.onPageScrollStateChanged(state);
                }
            }
        });

        viewPager.setCurrentItem(pos);

        highLightTextView(pos);
    }

    /**
     * 重置Tab的Title颜色
     */
    private void resetTextViewColor(){
        for(int i = 0;i<linearLayout.getChildCount();i++){
            View view = linearLayout.getChildAt(i);
            if(view instanceof TextView){
                ((TextView) view).setTextColor(COLOR_TEXT_NORMAL);
            }
        }
    }

    /**
     * 高亮某个Tab的Title
     * @param pos
     */
    private void highLightTextView(int pos){
        resetTextViewColor();
        View view = linearLayout.getChildAt(pos);
        if(view instanceof TextView){
            ((TextView) view).setTextColor(COLOR_TEXT_HIGHLIGHT);
        }
    }

    /**
     * 设置Tab的点击事件
     */
    private void setItemClickEvent(){
        int cCount = linearLayout.getChildCount();

        for(int i = 0;i<cCount;i++){
            final int j = i;
            View view = linearLayout.getChildAt(i);
            view.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    resetTextViewColor();
                    viewPager.setCurrentItem(j);
                }
            });
        }
    }

    /**
     *因为ViewPager的接口在这个类中被用了，所以自定义接口提供给用户
     */
    public interface PageOnChangeListener{
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels);

        public void onPageSelected(int position);

        public void onPageScrollStateChanged(int state);
    }

    public PageOnChangeListener mListener;

    public void setOnPageChangeListener(PageOnChangeListener listener){
        this.mListener = listener;
    }
}
