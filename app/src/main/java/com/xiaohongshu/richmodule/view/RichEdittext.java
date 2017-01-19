package com.xiaohongshu.richmodule.view;

import android.content.Context;
import android.graphics.Color;
import android.text.Selection;
import android.text.SpanWatcher;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xiaohongshu.richmodule.bean.IRichParser;
import com.xiaohongshu.richmodule.bean.RichParserManager;

/**
 * Created by wupengjian on 16/10/27.
 */
public class RichEdittext extends EditText implements View.OnKeyListener, SpanWatcher {

    private static final int CHANGE_WATCHER_PRIORITY = 100;
    /**
     * 光标之前的选中位置
     */
    private int mOldSelStart, mOldSelEnd;
    /**
     * 为了避免死循环触发onSelectionChanged(),设置的两个标志变量
     */
    private int mNewSelStart, mNewSelEnd;
    private CharSequence mContentStr = "";

    public RichEdittext(Context context) {
        this(context, null);
    }

    public RichEdittext(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RichEdittext(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        setBackgroundColor(Color.WHITE);
        setOnKeyListener(this);
    }

    /**
     * 监听删除按键，执行删除动作
     */
    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        //按下键盘时会出发动作，弹起键盘时同样会触发动作
        if (keyCode == KeyEvent.KEYCODE_DEL && event.getAction() == KeyEvent.ACTION_DOWN) {

            if (startStrEndWithRichItem() && getSelectionStart() == getSelectionEnd()) {

                int startPos = getSelectionStart();
                final String startStr = toString().substring(0, startPos);

                //获取话题,并计算话题长度
                String richItem = RichParserManager.getManager().getLastRichItem(startStr);
                int lenth = richItem.length();

                clearFocus();
                requestFocus();

                //方案1: 先选中,不直接删除
                setSelection(startPos - lenth, startPos);

                v(String.format("del: (%s,%s)", startPos - lenth, startPos));

                //方案2: 直接删除该话题
//                String temp = startStr.substring(0, startStr.length() - lenth);
//                setText(temp + toString().substring(startPos, toString().length()));
//                setSelection(temp.length());

                return true;
            }
        }
        return false;
    }

    /**
     * 判断光标 "后面的" 字符是否是 "话题前缀"
     * 1.字符串以话题前缀开始
     * 2.在该字符串中找得到与之匹配的话题后缀
     * <p/>
     * 注意这种是不合法的: " #asdfads #话题# "
     * <p/>
     * 先找出字符串中所有话题,取第一个话题的index,如果index不等于当前光标的位置
     * ,说明当前光标位置后面的字符串不是一个话题
     *
     * @return
     */
    private boolean endStrStartWithRichItem() {

        int endPos = getSelectionEnd();
        String endStr = toString().substring(endPos, toString().length());
        if (!RichParserManager.getManager().containsRichItem(endStr)) {
            return false;
        }

        String firstTopic = RichParserManager.getManager().getFirstRichItem(endStr);
        return endStr.startsWith(firstTopic);
    }

    /**
     * 判断光标前面是否是一个"话题"
     * 1.字符串结尾是话题后缀
     * 2.在该字符串中找得到与之匹配的话题前缀
     * <p/>
     * 注意这种是不合法的: " #话题# asdfads# "
     * <p/>
     * 先找出字符串中所有话题,取最后一个话题的index,如果index不等于当前光标的位置
     * ,说明当前光标位置前面的字符串不是一个话题
     *
     * @return
     */
    public boolean startStrEndWithRichItem() {

        int startPos = getSelectionStart();
        final String startStr = toString().substring(0, startPos);
        if (!RichParserManager.getManager().containsRichItem(startStr)) {
            return false;
        }

        String lastTopic = RichParserManager.getManager().getLastRichItem(startStr);
        return startStr.endsWith(lastTopic);
    }

    @Override
    public void setSelection(int start, int stop) {
        if (0 <= start && stop <= getText().toString().length()) {

//            mNewSelStart = start;
//            mNewSelEnd = stop;
            super.setSelection(start, stop);
        }
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        try {

            selectChanged(selStart, selEnd);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void selectChanged(int selStart, int selEnd) {
        //调用setText()会导致先触发onSelectionChanged()并且start和end均为0,然后才是正确的start和end的值
        if (0 == selStart && 0 == selEnd
                //避免下面的setSelection()触发onSelectionChanged()造成死循环
                || selStart == mNewSelStart && selEnd == mNewSelEnd) {
            mOldSelStart = selStart;
            mOldSelEnd = selEnd;
            return;
        }
        //校准左边光标
        int targetStart = getRecommendSelection(selStart);
        targetStart = targetStart == -1 ? selStart : targetStart;
        //校准右边光标
        int targetEnd = getRecommendSelection(selEnd);
        targetEnd = targetEnd == -1 ? selEnd : targetEnd;
        //保存旧值
        mOldSelStart = selStart;
        mOldSelEnd = selEnd;
        //保存新值
        mNewSelStart = targetStart;
        mNewSelEnd = targetEnd;
        //更新选中区域
        setSelection(targetStart, targetEnd);
    }

    /**
     * 掐头去尾,取中间字符串中的富文本
     *
     * @param pos
     * @return 由于富文本无法选中, 所以返回一个合适的位置(返回-1表示不做特殊处理)
     */
    private int getRecommendSelection(int pos) {

        String text = toString();
        if (TextUtils.isEmpty(text)) {
            return -1;
        }

        //取前面字符串中最后一个富文本
        String startStr = text.substring(0, pos);
        String richStr = RichParserManager.getManager().getLastRichItem(startStr);
        //start默认指向最前
        int start = 0;
        //如果点击的是最前面的话题,则richStr可能为空
        if (!TextUtils.isEmpty(richStr)) {

            start = startStr.lastIndexOf(richStr) + richStr.length();
        }

        //取后面字符串中第一个富文本
        String endStr = text.substring(pos, text.length());
        richStr = RichParserManager.getManager().getFirstRichItem(endStr);
        //end默认指向最后
        int end = text.length();
        //如果点击的是最后面的话题,则richStr可能为空
        if (!TextUtils.isEmpty(richStr)) {

            end = startStr.length() + endStr.indexOf(richStr);
        }
        String middleStr = text.substring(start, end);
        richStr = RichParserManager.getManager().getFirstRichItem(middleStr);
        if (TextUtils.isEmpty(richStr)) {
            return -1;
        }
        //"01 #456# 9",这种话题的start并不是从0,而是2
        start = start + middleStr.indexOf(richStr);
        end = start + richStr.length();
        //将光标移动离当前位置较近的地方
        return (pos - start < end - pos) ? start : end;
    }

    /**
     * 插入话题
     *
     * @param richKeyword
     * @param richItem
     */
    public void insertRichItem(String richKeyword, IRichParser richItem) {

        final IRichParser item = richItem;
        if (null == item) {
            return;
        }
        //setText()的操作会导致selection的改变,所以要先记录selection的位置
        int currentPos = getSelectionStart();

        String text = toString();
        //截取光标前的字符串
        String startStr;
        if (0 == currentPos) {

            startStr = "";
        } else {

            startStr = text.substring(0, currentPos);
        }
        //截图光标后的字符串
        String endStr;
        if (currentPos == text.length()) {

            endStr = "";
        } else {

            endStr = text.substring(currentPos, text.length());
        }
        //插入话题
        String richText = item.getRichText(richKeyword);
        setText(startStr + richText + endStr);
        setSelection(currentPos + richText.length());
    }

    @Override
    protected void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        super.onTextChanged(text, start, lengthBefore, lengthAfter);

        //不管是用户删除还是输入文字,都会触发这个方法,每次触发时判断界面上的文字跟记录的文字是否相同,如果不同就
        //转换成富文本,避免死循环
        if (!TextUtils.equals(mContentStr, text)) {
            mContentStr = text;
            SpannableStringBuilder spannableStr = RichParserManager.getManager().parseRichItems(getContext(), mContentStr.toString());
            setText(spannableStr);

            Spannable sp = getText();
            sp.setSpan(this, 0, getText().length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE |
                    (CHANGE_WATCHER_PRIORITY << Spanned.SPAN_PRIORITY_SHIFT));
        }
    }

    @Override
    public String toString() {
        return mContentStr == null ? "" : mContentStr.toString();
    }

    private void v(String msg) {
        final String text = msg;
        if (TextUtils.isEmpty(text)) {
            return;
        }
        Log.v("RichEdittext", text);
    }

    @Override
    public void onSpanAdded(Spannable text, Object what, int start, int end) {

    }

    @Override
    public void onSpanRemoved(Spannable text, Object what, int start, int end) {

    }

    @Override
    public void onSpanChanged(Spannable text, Object what, int ostart, int oend, int nstart, int nend) {

        if (Selection.SELECTION_START.equals(what)) {

            onSpanStartChanged(nstart);

        } else if (Selection.SELECTION_END.equals(what)) {

            onSpanEndChanged(nend);
        }
    }

    private void onSpanStartChanged(int start) {

        v(String.format("start: %s", start));
    }

    private void onSpanEndChanged(int end) {

        v(String.format("end: %s", end));
    }
}
