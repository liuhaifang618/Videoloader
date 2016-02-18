/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.safewaychina.filecache.interf;

import android.support.v7.widget.RecyclerView;

import com.safewaychina.filecache.VideoLoader;

public class RecyviewPauseOnScrollListener extends RecyclerView.OnScrollListener {


	private VideoLoader videoLoader;
	private final boolean pauseOnScroll;
	private final boolean pauseOnFling;


	public RecyviewPauseOnScrollListener(VideoLoader videoLoader, boolean pauseOnScroll, boolean pauseOnFling) {
		this.videoLoader = videoLoader;
		this.pauseOnScroll = pauseOnScroll;
		this.pauseOnFling = pauseOnFling;
	}

	@Override
	public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
		super.onScrollStateChanged(recyclerView, newState);
		switch (newState) {
			case RecyclerView.SCROLL_STATE_IDLE:
				videoLoader.resume();
				break;
			case RecyclerView.SCROLL_STATE_DRAGGING:
				if (pauseOnScroll) {
					videoLoader.pause();
				}
				break;
			case RecyclerView.SCROLL_STATE_SETTLING:
				if (pauseOnFling) {
					videoLoader.pause();
				}
				break;
		}
	}

}
