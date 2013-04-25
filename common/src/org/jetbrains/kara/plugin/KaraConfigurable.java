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

import com.intellij.openapi.options.BeanConfigurable;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class KaraConfigurable extends BeanConfigurable<KaraPluginOptions> implements Configurable {

    protected KaraConfigurable(KaraPluginOptions beanInstance) {
        super(beanInstance);
        checkBox("dontShowConversionDialog", "Don't show HTML to Kara conversion dialog");
        checkBox("enableHtmlToKaraConversion", "Enable Html To Kara conversion");
        checkBox("enableHrefToDirectLinkConversion", "Enable href to DirectLink conversion");
    }

    @Nls
    @Override
    public String getDisplayName() {
        return "Kara plugin";
    }

    @Nullable
    @Override
    public String getHelpTopic() {
        return "Settings for Kara plugin";
    }

    // need for appearance
    @Override
    public JComponent createComponent() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));

        JComponent jComponent = super.createComponent();
        assert jComponent != null;
        panel.add(jComponent);
        return panel;
    }
}
