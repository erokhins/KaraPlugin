/*
 * Copyright 2010-2013 JetBrains s.r.o.
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
 */

package org.jetbrains.kara.plugin;

import com.intellij.openapi.components.*;
import com.intellij.util.xmlb.XmlSerializerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(
        name = "KaraPluginOptions",
        storages = {
                @Storage(id = "default", file = "$APP_CONFIG$/kara.plugin.xml")
        }
)
public class KaraPluginOptions implements PersistentStateComponent<KaraPluginOptions> {

    private boolean donTShowConversionDialog = false;
    private boolean enableHtmlToKaraConversion = true;
    private boolean enableHrefToDirectLinkConversion = false;

    public boolean isEnableHrefToDirectLinkConversion() {
        return enableHrefToDirectLinkConversion;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setEnableHrefToDirectLinkConversion(boolean enableHrefToDirectLinkConversion) {
        this.enableHrefToDirectLinkConversion = enableHrefToDirectLinkConversion;
    }

    public boolean isDonTShowConversionDialog() {
        return donTShowConversionDialog;
    }

    public void setDonTShowConversionDialog(boolean donTShowConversionDialog) {
        this.donTShowConversionDialog = donTShowConversionDialog;
    }

    public boolean isEnableHtmlToKaraConversion() {
        return enableHtmlToKaraConversion;
    }

    @SuppressWarnings("UnusedDeclaration")
    public void setEnableHtmlToKaraConversion(boolean enableHtmlToKaraConversion) {
        this.enableHtmlToKaraConversion = enableHtmlToKaraConversion;
    }



    @Nullable
    @Override
    public KaraPluginOptions getState() {
        return this;
    }

    @Override
    public void loadState(KaraPluginOptions state) {
        XmlSerializerUtil.copyBean(state, this);
    }

    @NotNull
    public static KaraPluginOptions getInstance() {
        return ServiceManager.getService(KaraPluginOptions.class);
    }
}
