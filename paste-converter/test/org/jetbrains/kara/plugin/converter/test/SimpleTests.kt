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

package org.jetbrains.kara.plugin.converter.test

import org.junit.Test as test
import org.junit.Assert.*
import org.jetbrains.kara.plugin.converter.HtmlToKaraConverter
import org.jetbrains.kara.plugin.KaraPluginOptions
import org.jetbrains.kara.plugin.converter.Formatter

public class HtmlToKaraConverterTest {
    val t = "___"
    val k = "\"\"\""

    fun getPluginOptions(hrefConvert : Boolean = false): KaraPluginOptions {
        val options = KaraPluginOptions()
        options.setEnableHrefToDirectLinkConversion(hrefConvert)
        return options
    }

    fun outPrepare(out : String) : String {
        val str = StringBuilder()
        for (line in out.trim().split('\n')) {
            str.append(line.trim()).append('\n')
        }
        return str.toString()
    }

    fun runTest(inp: String, out: String, options : KaraPluginOptions = getPluginOptions()) {
        val converter = HtmlToKaraConverter(options, Formatter(indent = t))
        assertEquals(outPrepare(out), converter.convert(inp))

    }




    test fun simple() {
        runTest(
            """
                <div> text </div>
            """,

            """
                div {
                ${t}+"text"
                }
            """
        );
    }

    test fun severalLinesText() {
        runTest(
                """
                <div>
                    line1
                    line2
                </div>
            """,

                """
                div {
                ${t}${k}
                ${t}${t}line1
                ${t}${t}line2
                ${t}${k}
                }
            """
        );
    }

    test fun classStyle1() {
        runTest(
            """
                <div class = "class-1"> text </div>
            """,

            """
                div(c = class_1) {
                ${t}+"text"
                }
            """
        )
    }

    test fun classStyleMore() {
        runTest(
            """
                <div class = "class-1 btn-info"> text </div>
            """,

            """
                div(c = class_1 + btn_info) {
                ${t}+"text"
                }
            """
        );
    }

    test fun comment() {
        runTest(
            """
                <!-- Comment -->
            """,
            """
                /*
                ${t}Comment
                */
            """
        );
    }

    test fun inputAttribute() {
        runTest(
            """
                <button type="submit">Submit</button>
            """,
            """
                button(inputType = InputType.submit) {
                ${t}+"Submit"
                }
            """
        )
    }

    test fun severalAttributes() {
        runTest(
            """
                <button type="submit" class = "class-1 btn-info">Submit</button>
            """,
                """
                button(inputType = InputType.submit, c = class_1 + btn_info) {
                ${t}+"Submit"
                }
            """
        )
    }

    test fun hrefAttributeDisable() {
        runTest(
            """
                <a href="#">Link</a>
            """,
                """
                a(href = "#") {
                ${t}+"Link"
                }
            """
        )
    }

    test fun hrefAttributeEnable() {
        runTest(
            """
                <a href="#">Link</a>
            """,
                """
                a(href = DirectLink("#")) {
                ${t}+"Link"
                }
            """,
                getPluginOptions(hrefConvert = true)
        )
    }

    test fun forAttribute() {
        runTest(
            """
                <label for="inputId">Label</label>
            """,
            """
                label(forId = "inputId") {
                ${t}+"Label"
                }
            """
        )
    }
}