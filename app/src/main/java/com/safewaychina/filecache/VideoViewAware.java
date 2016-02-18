/*******************************************************************************
 * Copyright 2014 Sergey Tarasevich
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.safewaychina.filecache;

import android.view.View;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;


public class VideoViewAware {


    protected Reference<View> viewRef;


    public VideoViewAware(View view) {
        if (view == null) throw new IllegalArgumentException("view must not be null");

        this.viewRef = new WeakReference<View>(view);
    }


    public View getWrappedView() {
        return viewRef.get();
    }

    public boolean isCollected() {
        return viewRef.get() == null;
    }

    public int getId() {
        View view = viewRef.get();
        return view == null ? super.hashCode() : view.hashCode();
    }


}
