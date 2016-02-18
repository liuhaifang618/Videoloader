package com.safewaychina.filecache.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.safewaychina.filecache.VideoLoader;
import com.safewaychina.filecache.VideoLoaderConfiguration;

import java.util.LinkedList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private android.support.v7.widget.RecyclerView view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.view = (RecyclerView) findViewById(R.id.view);
        view.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        VideoAdapter adapter = new VideoAdapter(getApplicationContext(),initData());
        view.setAdapter(adapter);

        VideoLoader videoLoader = VideoLoader.getInstance(getApplicationContext());
        VideoLoaderConfiguration videoLoaderConfiguration = VideoLoaderConfiguration.createDefault(getApplicationContext());
        videoLoader.init(videoLoaderConfiguration);
    }

    private List<Viewitem> initData(){
        Viewitem viewitem;
        List<Viewitem> data = new LinkedList<>();
        for (int i = 0; i < 100; i++) {
            viewitem = new Viewitem("http://192.168.0.220/os/2015831171039.mp4","title"+i);
            data.add(viewitem);
        }
        return data;
    }

}
