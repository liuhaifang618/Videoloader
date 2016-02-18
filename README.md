# Videoloader

#### 采用imageloader设计思路，实现video的缓冲加载播放。


<img src="/snapshot/shapshot.gif" alt="alt text" style="width:200;height:200">

#Usage

1. 添加xml布局.

```xml
      <com.safewaychina.filecache.demo.TextureVideoView
        android:id="@+id/video"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1"
        android:text="@string/hello_world" />                
```

2.  代码初始化

```java
        final TextureVideoView view = recylerViewHelper.getView(R.id.video);
        VideoLoader instance = VideoLoader.getInstance(context);
        int position = recylerViewHelper.getPosition();

        recylerViewHelper.setText(R.id.title,item.getTitle());
        instance.displayVideoView(item.getUrl(),new VideoViewAware(view), new FileResponseListener() {
            @Override
            public void onSuccess(File file) {
                view.setVideoPath(file.getAbsolutePath());
                view.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer mp) {
                        view.start();
                    }
                });
            }

            @Override
            public void onFailure(Throwable throwable) {
                System.out.print("aaaa");
            }
        });
```

 



#Compatibility
  
  * Android GINGERBREAD 2.3+
  
# 历史记录


### Version: 1.0

  * 初始化编译


## License

    Copyright 2015, liuhaifang

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

