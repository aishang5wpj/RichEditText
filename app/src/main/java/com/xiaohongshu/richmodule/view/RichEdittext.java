package com.xiaohongshu.richmodule.view;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.xiaohongshu.richmodule.bean.IRichParser;
import com.xiaohongshu.richmodule.bean.RichParserManager;

/**
 * Created by wupengjian on 16/10/27.
 */
public class RichEdittext extends EditText implements View.OnKeyListener {

    /**
     * 光标之前的选中位置
     */
    private int mOldSelStart, mOldSelEnd;
    /**
     * 为了避免死循环触发onSelectionChanged(),设置的两个标志变量
     */
    private int mNewSelStart, mNewSelEnd;
    private OnRichItemClickedListener mOnRichItemClicked;
    /**
     * 原始文本(所有的文本添加、删除操作均在此上面完成)
     */
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

                //先选中,不直接删除
                setSelection(startPos - lenth, startPos);

                //删除该话题
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
     * <p>
     * 注意这种是不合法的: " #asdfads #话题# "
     * <p>
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
     * <p>
     * 注意这种是不合法的: " #话题# asdfads# "
     * <p>
     * 先找出字符串中所有话题,取最后一个话题的index,如果index不等于当前光标的位置
     * ,说明当前光标位置前面的字符串不是一个话题
     *
     * @param str
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

            mNewSelStart = start;
            mNewSelEnd = stop;
            super.setSelection(start, stop);
        }
    }

    /**
     * 需要防止光标进入到话题文字内部,话题应该作为一个整体,不可单独选中话题中的某个文字
     * <p>
     * 每次setText()时会触发onSelectionChanged,而且selStart和selEnd都等于0
     * <p>
     * //疑点1:有的手机选中一段文字时往中间挤压之后可以交差反向选中然后扩大,这时候selStart和selEnd的值是什么?
     * //疑点2:双指触控同时改变光标的start和end时,onSelectionChanged是依次回调还是一次性回调同时告知selStart和endStart的改变?
     * //对于疑点2,下面的逻辑暂时只考虑单指触控时的情形
     *
     * @param selStart
     * @param selEnd
     */
    @Override
    protected void onSelectionChanged(final int selStart, final int selEnd) {
        super.onSelectionChanged(selStart, selEnd);

        //调用setText()会导致先触发onSelectionChanged()并且start和end均为0,然后才是正确的start和end的值
        if (0 == selStart && 0 == selEnd) {
            mOldSelStart = selStart;
            mOldSelEnd = selEnd;
            return;
        }

        //避免下面的setSelection()触发onSelectionChanged()造成死循环
        if (selStart == mNewSelStart && selEnd == mNewSelEnd) {
            mOldSelStart = selStart;
            mOldSelEnd = selEnd;
            return;
        }

        int targetStart = selStart, targetEnd = selEnd;
        String text = toString();
        //如果用户不是通过左移右移来改变位置,而是直接用手指点击文字使光标的位置发生改变
        if (selStart == selEnd && Math.abs(selStart - mOldSelStart) > 1) {

            //如果移到了话题内,则改变移动到其他合理的地方
            int pos = getRecommendSelection(selStart);
            if (-1 != pos) {
                setSelection(pos, pos);
                return;
            }
//            String clickedRichItem = getClickedRichItem(selStart);
//            //话题被点击
//            if (!TextUtils.isEmpty(clickedRichItem)) {
//                if (null != mOnRichItemClicked) {
//                    mOnRichItemClicked.onRichItemClicked(clickedRichItem);
//                }
//                //不改变位置
//                setSelection(mOldSelStart, mOldSelEnd);
//                return;
//            }
        } else {
            //光标左边往右
            if (mOldSelStart < selStart) {
                //事实上,onSelectionChanged()回调时位置已经改变过了
                // ,所以当光标左边往右移动时,如果需要判断光标当前位置pos后是否是一个话题时
                // ,应该判断pos-1时候的位置来判断(或者oldPos,但是oldPos是自己计算出来的,并不一定精准所以)
                int startPos = selStart - 1;
                String endStr = text.substring(startPos, text.length());
                if (RichParserManager.getManager().isStartWithRichItem(endStr)) {

                    String richStr = RichParserManager.getManager().getFirstRichItem(endStr);
                    targetStart = startPos + richStr.length();
                }
            }
            //光标左边往左
            else if (mOldSelStart > selStart) {

                int startPos = selStart + 1;
                //逐个删除文字时,selStart + 1会导致数组越界
                startPos = startPos < text.length() ? startPos : text.length();
                String startStr = text.substring(0, startPos);
                if (RichParserManager.getManager().isEndWithRichItem(startStr)) {

                    String richStr = RichParserManager.getManager().getLastRichItem(startStr);
                    targetStart = startPos - richStr.length();
                }
            }

            //光标右边往右
            if (mOldSelEnd < selEnd) {

                int endPos = selEnd - 1;
                String endStr = text.substring(endPos, text.length());
                if (RichParserManager.getManager().isStartWithRichItem(endStr)) {

                    String richStr = RichParserManager.getManager().getFirstRichItem(endStr);
                    targetEnd = endPos + richStr.length();
                }
            }
            //光标右边往左
            else if (mOldSelEnd > selEnd) {

                int endPos = selEnd + 1;
                String startStr = text.substring(0, endPos);
                if (RichParserManager.getManager().isEndWithRichItem(startStr)) {

                    String richStr = RichParserManager.getManager().getLastRichItem(startStr);
                    targetEnd = endPos - richStr.length();
                }
            }
        }
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
     * 掐头去尾,取中间字符串中的富文本
     *
     * @param pos
     * @return 返回被选中的富文本
     */
    private String getClickedRichItem(int pos) {

        String text = toString();

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
        return RichParserManager.getManager().getFirstRichItem(middleStr);
    }

    /**
     * 插入话题
     *
     * @param topicBean
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

        if (!TextUtils.equals(mContentStr, text)) {
            mContentStr = text;
            SpannableStringBuilder spannableStr = RichParserManager.getManager().parseRichItems(getContext(), mContentStr.toString());
            setText(spannableStr);
        }
    }

    @Override
    public String toString() {
        return mContentStr == null ? "" : mContentStr.toString();
    }

    public void setOnRichItemClickedListener(OnRichItemClickedListener listener) {
        mOnRichItemClicked = listener;
    }

    private void t(String text) {
        final String msg = text;
        if (!TextUtils.isEmpty(msg)) {
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        }
    }

    public interface OnRichItemClickedListener {
        void onRichItemClicked(String richStr);
    }
}
