package com.xiaohongshu.richmodule.bean;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;

import com.xiaohongshu.richmodule.R;
import com.xiaohongshu.richmodule.view.CenteredImageSpan;

/**
 * Created by wupengjian on 16/10/31.
 */
public class TopicRichParser extends AbstractRichParser {

    /**
     * http://blog.csdn.net/hfut_jf/article/details/49745701
     *
     * @return
     */
    @Override
    public String getRichPattern() {
//        #\\S+# 对 ##我的房间搜案发## 这种话题中的话题不能很好的识别
//        return getStartSuffix() + "\\S+" + getEndSuffix();
//        return getStartSuffix() + "[^#]+" + getEndSuffix();
        //添加 oo 当imagespan 的占位符
//        return "#o[^#]+#"; //" #话题# "
        return " #[^#]\\S+ ";//" #话题 "
    }

    @Override
    public String getRichText(String richStr) {
        if (null == richStr) {
            return "";
        }
        return String.format(" #%s ", richStr);
    }

    @Override
    public SpannableString getRichSpannable(Context context, String richStr) {

        if (TextUtils.isEmpty(richStr)) {
            return new SpannableString("");
        }
        String str = richStr;
        SpannableString spannableString = new SpannableString(str);
        //color spannable
//        int color = Color.rgb((int) (Math.random() * 256), (int) (Math.random() * 256), (int) (Math.random() * 256));
        ForegroundColorSpan highLightSpan = new ForegroundColorSpan(Color.parseColor("#0099FF"));
        spannableString.setSpan(highLightSpan, 0, str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ImageSpan imageSpan = new CenteredImageSpan(context, R.mipmap.transparent);
        spannableString.setSpan(imageSpan, 1, 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
}
