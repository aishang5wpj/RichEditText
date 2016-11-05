#仿微博富文本编辑框

没图我说个毛？

<img src='screenshot/screenshot.gif' height='480px'/>

首先要说明的是：不管是话题、at还是poi，都应该被看成一个最小子项，即不可在中间插入文字，也不可对话题、at或者poi中的任何文字进行单独的修改或者删除。

结合以上，该富文本编辑框的主要功能如下：

- 1、富文本的高亮处理；
- 2、点击话题、at或者poi时光标会落在旁边合适的位置，光标移动时，如果碰到话题等要直接移动跳过整个话题，而不是仍然逐个文字的移动；
- 3、删除话题、at或者poi时，先整体选中，再点一次删除则执行真正的删除操作；
- 4、对于poi这种特殊类型，需要支持图文混排的展示；
- 5、除了对输入的文字进行展示，同时也要有良好的格式以便将用户输入的内容保存或者传输到服务器。

下面，就开始逐个分析吧。

<img src='biaoqing/06.jpeg' height='150px'/>

##富文本的高亮处理
这个简单，就是富文本的处理。

<img src='biaoqing/02.jpg' height='150px'/>

##边界判断

现在的很多输入法的键盘都有上、下、左、右移动光标的功能，比如搜狗输入法：

<img src='screenshot/keyboard.png' height='180px'/>

所谓边界判断，就是当用户在键盘上移动光标、或者直接用手指点击输入框调整光标的位置时，如果移动时碰到话题等特殊内容时，自动调整光标到旁边合适的位置。

###监听光标位置
要监听光标的位置，TextView中有一个方法叫`onSelectionChanged`，完整声明如下：

```
    protected void onSelectionChanged(final int selStart, final int selEnd) {
	}
```
当光标每次的位置发生改变时都会回调这个方法，两个形参即分别代表了光标的开始位置和结束位置。

而触发`onSelectionChanged`这个方法的原因基本上有两个：

- 1、用户手动输入文字，导致文字改变同时光标也向后移；
- 2、开发者手动调用`setSelection()`方法移动光标从而导致`onSelectionChanged `的回调。

###调整光标位置
上面说到调整光标的位置，`setSelection()`正好符合我们的需要。该方法在EditText中的完整声明如下：

```
    public void setSelection(int index) {
	}
	
    public void setSelection(int start, int stop) {
	}
```
`setSelection()`有两个重名方法，上面那个是纯粹的移动光标，下面那个是选中一段文字或者调整选区。

对于第1种情况处理起来比较简单，我们主要考虑第2种情况。我们来看一个图：

<img src='screenshot/doubleSelection.png' height='480px'/>

用户选中中间一段文字之后，如果此时点击键盘上的 "<-"、"->"，此时应该一次性选中富文本，而不是逐个选中每个文字。

###如何判断光标移动到了话题旁边？
好了，监听以及调整光标位置的事情已经解决了，我怎么知道光标移动到了话题、at或者poi的旁边呢？

这就涉及到了正则表达式了。不管是话题、at还是poi，我们都要分别给每种富文本定义一个格式，这里用话题来举例。

话题的格式是：一个空格+一个#+话题内容+一个空格，即" #话题 "（引号内即为一个完整的话题，至于为什么话题不像大家平常见到的那样是两个话题，我们稍后再讲）。

定下了话题格式之后，就可以写出正则表达式：" #[^#+] "，表示两个空格中间的字符串是#开头的若干个字符。这里推荐一个[在线正则表达式测试](http://tool.oschina.net/regex/)给大家，非常好用。

有了正则表达式，就可以针对话题提供一系列方法：判断字符串中是否包含话题、获取字符串中第一个话题、判断字符串是否以话题开头，等等。

看到这里，怎么判断光标旁边是否是话题就已经非常简单了：

- 1、如果光标是往左移，取光标左边的字符串，并判断这个字符串是否以话题结尾；
- 2、如果光标是往右移，取光标右边的字符串，并判断这个字符串是否以话题开头；

<img src='biaoqing/03.jpeg' height='150px'/>

##删除话题
View类中有个方法是`setOnKeyListener(OnKeyListener l)`,通过它可以设置对设备按键的事件进行监听，我们这里只需要监听`KeyEvent.KEYCODE_DEL`即可，每当用户按下删除键遍判断光标前面的字符串是否是话题，如果是话题则计算该话题的长度并选中之，如果再次点击删除即删除之。

如果前面的字符串是话题，那怎么判断到底是选中还是删除呢？

第一次按删除时，光标start和end的值肯定是相同的，因为只有一个光标，这时按下删除键肯定是执行选中话题的操作；

选中话题之后，光标变成了两个，这时候start和end值变成了两个，这时候按删除键就直接删除话题了。

所以只需要判断光标start和end的值是否一样即可。

##POI图文混排
平时做图文混排的方式有很多种，但是这里用的是ImageSpan，就不讲了。

<img src='biaoqing/07.jpg' height='150px'/>

##格式化保存
这是最主要的问题，也是之前困扰我很长一段时间的问题，其实回过头来看看也并没有我想的那么复杂，因为我们在前面定义话题格式、写话题正则表达式的时候，一个这样的字符串`这里有一个话题 #话题 不信你看`其实已经包含了很多信息了，所以我们并不需要额外的数据来存储任何其他的信息。

当时想的东西比较多，这么简单一说可能你们不能体会到上面这段话的意思，也可能我表达的不够好。

下面开始分析代码吧。

<img src='biaoqing/08.jpg' height='150px'/>

##源码分析

###架构设计
下面是程序uml类图。

<img src='uml.png' height='537px' width='1483px'/>

分别说一下图中接口和各个类的作用：

####`IRichParser`
解析器必须实现的接口，规范了一系列方法，分别如下：

```
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
```
注释说的很清楚了，说几个难懂一点的。

- `void resetTargetStr(String text)`
要明白这个方法，先来看看它的使用场景。比如如果我要判断`"这个字符串中有一个 #话题 "`这样一个字符串中有没有话题，代码是这样写的：

```
boolean isContains = new TopicRichParser().resetTargetStr("这个字符串中有一个 #话题 "). containsRichItem();
```
可以看到，`resetTargetStr()`的作用即每次要对字符串进行判断时，则调用这个方法对要判断的mTargetStr进行设置，然后再调用其他的方法进行判断。

那为什么不在`TopicRichParser`类中直接写一个静态方法呢，比如这样：

```
boolean isContains = TopicRichParser.containsRichItem("这个字符串中有一个 #话题 ");
```
看，多么省事。

乍一看好像挺对，因为我一开始也是这么写的，这么写也没错，目前有话题、at和poi这3种，你可能只需要写3个if else就可以判断完：

```
String targetStr = "这个字符串中有一个 #话题 ";
boolean isContains = false;
if(TopicRichParser.containsRichItem(targetStr)){

	isContains = true;
}else ir( AtRichParser.containsRichItem(targetStr)){

	isContains = true;
}else if(PoiRichParser.containsRichItem(targetStr))

	isContains = true;
}
```
甚至可以写成这样：

```
String targetStr = "这个字符串中有一个 #话题 ";
boolean isContains = TopicRichParser.containsRichItem(targetStr)
							&& AtRichParser.containsRichItem(targetStr)
							&& PoiRichParser.containsRichItem(targetStr) ;
```
对于目前的需求，这种写法是没有问题的，因为现在的富文本样式只有3中，如果以后变成5种、8种甚至更多呢？你需要在每个方法里面重复写这样相似的代码，但是如果有一个接口统一定义了这些方法，然后让子类分别实现各自不同的部分，就可以不用写这么多重复的代码了。

具体细节讲到`RichParserManager`时即可明白了。

<img src='biaoqing/09.jpg' height='150px'/>

####`AbstractRichParser`
实现了`IRichParser`接口，并重写了所有子类共同的方法，但是对于需要不同实现的方法仍然保持没有实现的状态。具体情况如下：

- 已实现部分：`resetTargetStr`、`containsRichItem`、`getFirstRichItem`、`getFirstRichIndex`、`getLastRichItem`、`getLastRichIndex`

- 未实现部分：`getRichPattern`、`getRichText`、`getRichSpannable`

####`TopicRichParser`、`AtRichParser`、`PoiRichParser`：
均继承自`AbstractRichParser`，并实现了父类所有虚方法（`getRichPattern`、`getRichText`、`getRichSpannable`），负责提供判断富文本的正则表达式、输出富文本字符串、输出富文本。
####`RichParserManager`:
上面介绍`IRichParser`说到：如果富文本样式有很多种时，如果仍然用`静态方法+if else`这种陈旧落后的方式将导致重复代码太多，最重要的是容易出错，如果某个方法中漏写了某种富文本的判断，则可能导致最终的结果并不准确。
在`RichParserManager`中是怎么处理的呢？

```
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
		//细节省略
    }

    /**
     * 获取字符串中的最后一个富文本串
     *
     * @param targetStr
     * @return 最后一个"话题"或者最后一个"@"或者其他,如果没有富文本串,则返回空字符串("")
     */
    public String getFirstRichItem(String targetStr) {
		//细节省略
    }

    /**
     * 是否以富文本开头
     *
     * @param targetStr
     * @return
     */
    public boolean isStartWithRichItem(String targetStr) {
		//细节省略
    }

    public boolean isEndWithRichItem(String targetStr) {
		//细节省略
    }

    /**
     * 解析字符串中的富文本并返回一个经过格式化的富文本串
     *
     * @param targetStr
     * @return
     */
    public SpannableStringBuilder parseRichItems(Context context, String targetStr) {
		//细节省略
    }

    private SpannableString formateRichStr(Context context, String richStr) {
		//细节省略
    }
}
```
`RichParserManager`采用单例模式对所有`IRichParser`进行管理，上面的需求中只有3中富文本，假设现在添加一种“音乐”类型的富文本，则只需要定义一个`MusicRichParser`继承自`AbstractRichParser`，并实现所有方法，然后调用`RichParserManager `的`registerRichParser()`即可使编辑器支持对`音乐`类型的富文本解析。

是不是很简单？说出来你可能不信，实际操作起来简单到我自己都怕。

<img src='biaoqing/01.png' height='150px'/>

举个栗子，如果要判断字符串是否包含富文本，外部只需要用下面的代码即可：

```
boolean isContains = RichParserManager.getManager().containsRichItem("这个字符串中有一个 #话题 "). containsRichItem();
```
是不是非常简单？再来看`containsRichItem()`的实现:

```
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
```
如你所见，正是遍历所有的`IRichParser`，依次检查是否含有富文本。其他方法类似，不多提了。

###话题、AT、POI，以及以后任何可能的富文本样式
假设现在添加一种“音乐”类型的富文本，则只需要定义一个`MusicRichParser`继承自`AbstractRichParser`，并实现下列3个方法即可：

- `getRichPattern` 提供正则表达

- `getRichText` 格式化输出富文本字符串

- `getRichSpannable` 将字符串富文本化(图文混排)等

`getRichText`和`getRichSpannable`有什么区别呢？

<img src='biaoqing/10.jpg' height='150px'/>

恩，这是个好问题。我们用POI富文本来举例，先来看一个图：

<img src='screenshot/poi.png' height='150px'/>

这是一个poi富文本图文混排之后的结果，poi的正则表达式是` &[^&]+ `，即两个空格中间是一个`&`开头的字符串。所有符合这种格式的字符串都将被解析成poi。

所以对于上图中的poi，它的真实字符串其实是` &兰溪 `，而且这也是调用`getRichText()`得到的结果。`getRichText()`代码如下：

```
    @Override
    public String getRichText(String richStr) {
        return String.format(" &%s ", richStr);
    }
```

而为什么会输出成上图中图文混排的结果呢？这就要看`getRichSpannable()`中的实现了。

```    @Override
    public SpannableString getRichSpannable(Context context, String richStr) {

        if (TextUtils.isEmpty(richStr)) {
            return new SpannableString("");
        }
        String str = richStr;
        SpannableString spannableString = new SpannableString(str);
        //color spannable
        ForegroundColorSpan highLightSpan = new ForegroundColorSpan(Color.parseColor("#FF6699"));
        spannableString.setSpan(highLightSpan, 0, str.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        ImageSpan imageSpan = new CenteredImageSpan(context, R.mipmap.poi);
        spannableString.setSpan(imageSpan, 1, 2, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannableString;
    }
```
在`getRichSpannable()`方法中，形参的值一般是`getRichText()`返回的结果，对于上图，这里的形参即为：` &兰溪 `。

在`getRichSpannable()`的第9~10行，先将富文本字符串颜色高亮。

第11~12，将`&`替换成POI富文本的icon。

看到这里是不是一切都了然了，所以这里的`&`其实是充当了一个占位符的作用。事实上如果你回过头去看，不管是话题、at还是poi，都有一个占位符。他们分别是`#`、`@`、`&`，只不过at的保留了占位符，而话题的占位符被我替换成了一个透明图片。

为什么要这么做呢？

对于在EditText中图文混排这件事情本身是不困难的，但是现在要支持对一个特定格式的富文本进行正则表达式的判断以及各种处理，特别是光标的控制，假设没有站位符，直接将图片加在文字后面变成富文本，光标移动的时候计算的文字长度仍然是原来的长度，而显示在屏幕上的富文本长度其实是超过了文字的实际长度，所以就有可能出现光标移动时被遮挡等等各种奇怪的bug。

不知道你们能不能明白，这个需要自己去体会了。

###RichEditText

##没解决的bug
程序大部分功能基本上没有问题，但是删除话题有的时候删不了，因为有个先选中再删除的过程，不知道为什么选中的时候老是选中不了，所以造成删除话题时一直跳过。

解决办法是：去掉先选中再删除的逻辑，当删除时碰到话题，按删除即直接删除整个话题，而不要选中了。

如果解决了bug我会及时更新的。