package com.xiaohongshu.richmodule;

import android.test.AndroidTestCase;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wupengjian on 16/10/27.
 */
public class SimpleTest extends AndroidTestCase {

    @Override
    public void testAndroidTestCaseSetupProperly() {
        super.testAndroidTestCaseSetupProperly();

        String pattern = " #\\S+# ";

        Pattern p = Pattern.compile(pattern);
        String str = "asfdaf #asfdaf#  #sfadjfsagadsgd# ";

        Matcher matcher = p.matcher(str);

        while (matcher.find()) {
            String result = matcher.group();
            System.out.println(result);
        }

        System.out.println();
    }
}
