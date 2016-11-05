package com.xiaohongshu.richmodule.bean;

import android.content.Context;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by wupengjian on 16/10/31.
 */
public class RichParserManager {

    private static RichParserManager mInstance;
    private List<IRichParser> mRichPasers;

    private RichParserManager() {
        mRichPasers = new ArrayList<>();
    }

    public static RichParserManager getManager() {
        if (null == mInstance) {
            synchronized (RichParserManager.class) {
                if (null == mInstance) {
                    mInstance = new RichParserManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * @param richParser
     */
    public void registerRichParser(IRichParser richParser) {

        //是否需要去重处理?
        mRichPasers.add(richParser);
    }

    /**
     * 判断是否包含富文本bean
     *
     * @param str
     * @return
     */
    public boolean containsRichItem(String str) {

        if (TextUtils.isEmpty(str)) {
            return false;
        }
        for (IRichParser richItem : mRichPasers) {
            richItem.resetTargetStr(str);
            if (richItem.containsRichItem()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取字符串中的最后一个富文本串
     *
     * @param targetStr
     * @return 最后一个"话题"或者最后一个"@"或者其他,如果没有富文本串,则返回空字符串("")
     */
    public String getLastRichItem(String targetStr) {

        final String str = targetStr;
        int index = -1;
        IRichParser iRichParser = null;
        for (IRichParser richItem : mRichPasers) {

            //遍历mRichItems进行各种操作时,一定要重置targetStr
            richItem.resetTargetStr(str);

            int temp = richItem.getLastRichIndex();
            if (temp > index) {
                index = temp;
                iRichParser = richItem;
            }
        }
        return iRichParser == null ? "" : iRichParser.getLastRichItem();
    }

    /**
     * 获取字符串中的最后一个富文本串
     *
     * @param targetStr
     * @return 最后一个"话题"或者最后一个"@"或者其他,如果没有富文本串,则返回空字符串("")
     */
    public String getFirstRichItem(String targetStr) {

        final String str = targetStr;
        int index = Integer.MAX_VALUE;
        IRichParser iRichParser = null;
        for (IRichParser richItem : mRichPasers) {

            //遍历mRichItems进行各种操作时,一定要重置targetStr
            richItem.resetTargetStr(str);

            int temp = richItem.getFirstRichIndex();
            if (temp < index && temp != -1) {
                index = temp;
                iRichParser = richItem;
            }
        }
        return iRichParser == null ? "" : iRichParser.getFirstRichItem();
    }

    /**
     * 是否以富文本开头
     *
     * @param targetStr
     * @return
     */
    public boolean isStartWithRichItem(String targetStr) {

        final String str = targetStr;
        if (!RichParserManager.getManager().containsRichItem(str)) {
            return false;
        }
        String firstTopic = RichParserManager.getManager().getFirstRichItem(str);
        return str.startsWith(firstTopic);
    }

    public boolean isEndWithRichItem(String targetStr) {

        final String str = targetStr;
        if (!RichParserManager.getManager().containsRichItem(str)) {
            return false;
        }
        String lastTopic = RichParserManager.getManager().getLastRichItem(str);
        return str.endsWith(lastTopic);
    }

    /**
     * 解析字符串中的富文本并返回一个经过格式化的富文本串
     *
     * @param targetStr
     * @return
     */
    public SpannableStringBuilder parseRichItems(Context context, String targetStr) {
        final String str = targetStr;
        if (!RichParserManager.getManager().containsRichItem(str)) {
            return new SpannableStringBuilder(str);
        }
        String tempStr = str;
        String richStr = getFirstRichItem(tempStr);
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        while (!TextUtils.isEmpty(richStr)) {

            //start string
            int index = tempStr.indexOf(richStr);
            String startStr = tempStr.substring(0, index);
            ssb.append(startStr);
            //rich string
            ssb.append(formateRichStr(context, richStr));
            //循环
            tempStr = tempStr.substring(index + richStr.length(), tempStr.length());
            richStr = getFirstRichItem(tempStr);
        }
        //end String
        ssb.append(tempStr);

        return ssb;
    }

    private SpannableString formateRichStr(Context context, String richStr) {

        final String str = richStr;
        for (IRichParser richItem : mRichPasers) {

            //遍历mRichItems进行各种操作时,一定要重置targetStr
            richItem.resetTargetStr(richStr);

            if (richItem.containsRichItem()) {
                return richItem.getRichSpannable(context, richStr);
            }
        }
        return new SpannableString(str);
    }
}
