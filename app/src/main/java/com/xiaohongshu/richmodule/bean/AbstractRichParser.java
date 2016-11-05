package com.xiaohongshu.richmodule.bean;

import android.text.TextUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wupengjian on 16/10/31.
 */
public abstract class AbstractRichParser implements IRichParser {

    private String mTargetStr;

    @Override
    public void resetTargetStr(String text) {
        mTargetStr = text;
    }

    @Override
    public boolean containsRichItem() {

        if (TextUtils.isEmpty(mTargetStr)) {
            return false;
        }
        Pattern p = Pattern.compile(getRichPattern());
        Matcher matcher = p.matcher(mTargetStr);
        return matcher.find();
    }

    @Override
    public String getFirstRichItem() {

        if (TextUtils.isEmpty(mTargetStr)) {
            return "";
        }
        Pattern p = Pattern.compile(getRichPattern());
        Matcher matcher = p.matcher(mTargetStr);

        String result = "";
        if (matcher.find()) {
            result = matcher.group();
        }
        return result;
    }

    @Override
    public int getFirstRichIndex() {

        if (TextUtils.isEmpty(mTargetStr)) {
            return -1;
        }
        String item = getFirstRichItem();
        if (TextUtils.isEmpty(item)) {
            return -1;
        }
        return mTargetStr.indexOf(item);
    }

    @Override
    public String getLastRichItem() {

        if (TextUtils.isEmpty(mTargetStr)) {
            return "";
        }
        Pattern p = Pattern.compile(getRichPattern());
        Matcher matcher = p.matcher(mTargetStr);

        String result = "";
        while (matcher.find()) {
            result = matcher.group();
        }
        return result;
    }

    @Override
    public int getLastRichIndex() {

        if (TextUtils.isEmpty(mTargetStr)) {
            return -1;
        }
        String item = getLastRichItem();
        if (TextUtils.isEmpty(item)) {
            return -1;
        }
        return mTargetStr.lastIndexOf(item);
    }
}
