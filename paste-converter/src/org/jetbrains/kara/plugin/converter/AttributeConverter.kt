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

package org.jetbrains.kara.plugin.converter

import org.jsoup.nodes.Attributes
import org.jetbrains.kara.plugin.KaraPluginOptions

private class HtmlAttributeConverter(val pluginOptions : KaraPluginOptions) {

    private fun styleClassConvert(styleClass : String): String {
        return styleClass.replace('-', '_')
    }

    /**
        styleAttr = "main1 btn-info"
        @return  main1 + btn_info
    */
    private fun styleClasses(value: String): String {
        val classes = value.split(' ')
        val str = StringBuilder()
        for (styleClass in classes) {
            if (str.length() != 0) {
                str.append(" + ")
            }
            str.append(styleClassConvert(styleClass))
        }

        return str.toString()
    }

    private fun inputType(value: String): String {
        return "InputType." + value
    }

    private fun href(value : String): String {
        if (pluginOptions.isEnableHrefToDirectLinkConversion()) {
            return "DirectLink(\"$value\")"
        } else {
            return "\"$value\""
        }
    }

    public fun convert(attributes : Attributes): String {
        val str = StringBuilder()
        for (attr in attributes.asList()!!) {
            if (str.length() > 0) {
                str.append(", ")
            }
            when (attr.getKey()) {
                "class" -> str.append("c = ").append(styleClasses(attr.getValue()))

                "type" ->  str.append("inputType = ").append(inputType(attr.getValue()))
                "href" -> str.append("href = ").append(href(attr.getValue()))
                "for" -> str.append("forId = \"${attr.getValue()}\"")
                else -> str.append(attr.getKey()).append(" = \"${attr.getValue()}\"")
            }
        }
        return str.toString()
    }
}