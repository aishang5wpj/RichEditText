package com.xiaohongshu.richmodule.bean;

import android.content.Context;
import android.text.SpannableString;

/**
 * Created by wupengjian on 16/10/31.
 */
public interface IRichParser {

    /**
     * 设置待判断的字符串
     *
     * @param text
     */
    void resetTargetStr(String text);

    /**
     * 判断是否是话题的正则表达式
     *
     * @return
     */
    String getRichPattern();

    /**
     * 判断字符串中是否包含话题
     *
     * @param str
     * @return
     */
    boolean containsRichItem();

    /**
     * 获取字符串中的第一个话题,匹配到的结果已经是格式化之后的了,不需要对匹配结果再次格式化
     *
     * @return
     */
    String getFirstRichItem();

    /**
     * 获取字符串中的第一个话题在字符串中的索引
     *
     * @return
     */
    int getFirstRichIndex();

    /**
     * 获取字符串中的最后一个话题,匹配到的结果已经是格式化之后的了,不需要对匹配结果再次格式化
     *
     * @return
     */
    String getLastRichItem();

    /**
     * 获取字符串中的最后一个话题在字符串中的索引
     *
     * @return
     */
    int getLastRichIndex();

    /**
     * 格式化输出
     *
     * @return
     */
    String getRichText(String richStr);

    /**
     * 输出富文本的形式
     *
     * @param richStr
     * @return
     */
    SpannableString getRichSpannable(Context context, String richStr);
}
