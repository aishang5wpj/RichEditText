package com.xiaohongshu.richmodule;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.xiaohongshu.richmodule.bean.AtRichParser;
import com.xiaohongshu.richmodule.bean.PoiRichParser;
import com.xiaohongshu.richmodule.bean.RichParserManager;
import com.xiaohongshu.richmodule.bean.TopicRichParser;
import com.xiaohongshu.richmodule.view.RichEdittext;

/**
 * http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2015/0427/2807.html
 * http://blog.csdn.net/hfut_jf/article/details/49745701
 */
public class MainActivity extends AppCompatActivity implements RichEdittext.OnRichItemClickedListener {

    private RichEdittext mEditText;
    private String[] TOPICS = {"湄公河行动", "血钻", "微微一笑很倾城", "大鱼海棠", "从你的全世界路过", "昨日青空", "最好的我们"};
    private String[] AT = {"暴走萝莉金克丝", "黑暗火女安妮", "琴瑟仙女娑娜", "九尾妖狐阿狸", "暗夜猎手薇恩", "皮城女警凯特琳", "光辉女郎拉克丝"};
    private String[] POI = {"北海", "札幌", "周庄", "婺源", "西双版纳", "兰溪", "西塘"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RichParserManager.getManager().registerRichParser(new TopicRichParser());
        RichParserManager.getManager().registerRichParser(new AtRichParser());
        RichParserManager.getManager().registerRichParser(new PoiRichParser());

        mEditText = (RichEdittext) findViewById(R.id.edittext);
        mEditText.setOnRichItemClickedListener(this);
    }

    public void addTopic(View view) {

        int random = (int) (Math.random() * TOPICS.length);
        mEditText.insertRichItem(TOPICS[random], new TopicRichParser());
    }

    public void addAt(View view) {

        int random = (int) (Math.random() * AT.length);
        mEditText.insertRichItem(AT[random], new AtRichParser());
    }

    public void addPoi(View view) {

        int random = (int) (Math.random() * POI.length);
        mEditText.insertRichItem(POI[random], new PoiRichParser());
    }

    @Override
    public void onRichItemClicked(String richStr) {
        t(richStr);
    }

    private void t(String text) {
        final String msg = text;
        if (!TextUtils.isEmpty(msg)) {
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        }
    }
}
