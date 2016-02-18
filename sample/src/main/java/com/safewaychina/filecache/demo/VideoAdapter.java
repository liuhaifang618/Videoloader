package com.safewaychina.filecache.demo;

import android.content.Context;
import android.media.MediaPlayer;

import com.safewaychina.basecommon.base.adapter.recycleradaper.RecyclerQuickAdapter;
import com.safewaychina.basecommon.base.adapter.recycleradaper.RecylerViewHelper;
import com.safewaychina.filecache.VideoLoader;
import com.safewaychina.filecache.VideoViewAware;
import com.safewaychina.filecache.interf.FileResponseListener;

import java.io.File;
import java.util.List;

/**
 * @author liu_haifang
 * @version 1.0
 * @Title：SAFEYE@
 * @Description：
 * @date 2015-09-23
 */
public class VideoAdapter extends RecyclerQuickAdapter<Viewitem> {


    public VideoAdapter(Context context, List datas) {
        super(context, R.layout.view_item_video, datas);
    }

    @Override
    protected void convert(int type, RecylerViewHelper recylerViewHelper, Viewitem item) {

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
    }

}
