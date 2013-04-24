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
import org.jetbrains.kara.plugin.converter.KaraHTMLConverter
import org.jetbrains.kara.plugin.KaraPluginOptions

public class Simple {
    val t = "___"

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
        return str.toString().replace(t, "\t")
    }

    fun runTest(inp: String, out: String, options : KaraPluginOptions = getPluginOptions()) {
        assertEquals(outPrepare(out), KaraHTMLConverter.converter(inp, options))

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

    test fun buttonTest() {
        runTest(
            """
                <button type="submit" class="btn">Submit</button>
            """,
            """
                button(inputType = InputType.submit, c = btn) {
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

    test fun forAttributeDisable() {
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