#前言

没图我说个毛？

<img src='screenshot/screenshot.gif' height='480px'/>

#使用方法

在代码中注册需要解析的类型，比如：

```
RichParserManager.getManager().registerRichParser(new NewTopicRichParser());

RichParserManager.getManager().registerRichParser(new AtRichParser());

RichParserManager.getManager().registerRichParser(new PoiRichParser());      
```

然后`RichEdittext`就会帮你处理好一切了。

如果要插入富文本，可以这样插入：

```
mEditText.insertRichItem("测试", new AtRichParser());
```

第一个参数为要插入的内容，第二个参数为要插入的类型，`RichEdittext`会自动将你传入的内容转换成对应的富文本。

##没解决的bug
上面录制的那张gif看上去是不是很美好，但是事实很残酷，实际操作的时候，还是有两个bug。
- 程序大部分功能基本上没有问题，但是删除话题有的时候删不了，因为有个先选中再删除的过程，不知道为什么选中的时候老是选中不了，所以造成删除话题时一直跳过。

解决办法是：去掉先选中再删除的逻辑，当删除时碰到话题，按删除即直接删除整个话题，而不要选中了。

- 移动选区时，光标没办法跳过整个富文本，光标仍然可以跑到富文本文字中间。

打断点调试的时候逻辑都是对的，打印log的时候貌似是onSelectChanged方法有多次调用导致对正常判断造成了干扰。囧~

如果解决了bug我会及时更新的，欢迎大神前来指点。

<b>（2017.1.19日更新，这两个bug均已解决，非常感谢`Panjianan `同学提供的思路 ）
</b>

##实现过程

详细实现情况博客[仿微博富文本编辑框](http://blog.csdn.net/aishang5wpj/article/details/53065915)


关于
--

博客：[http://blog.csdn.net/aishang5wpj](http://blog.csdn.net/aishang5wpj)

邮箱：337487365@qq.com

License
--
Copyright 2016 aishang5wpj

Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.