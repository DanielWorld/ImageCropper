/*
 * Copyright (c) 2017 DanielWorld.
 * @Author Namgyu Park
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.danielpark.imageeditor;

/**
 * Created by Namgyu Park on 2017-07-26.
 */

public interface OnEditorModeStateChangeListener {

    /**
     * Deliver current EditorMode state
     * @param currentEditorMode
     */
    void onEditorModeState(EditorMode currentEditorMode);
}
