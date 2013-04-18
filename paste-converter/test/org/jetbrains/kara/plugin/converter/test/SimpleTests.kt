/**
* @author Stanislav Erokhin
*/
package org.jetbrains.kara.plugin.converter.test

import org.junit.Test as test
import org.junit.Assert.*
import org.jetbrains.kara.plugin.converter.KaraHTMLConverter

public class Simple {
    val t = "___"

    fun outPrepare(out : String) : String {
        val str = StringBuilder()
        for (line in out.trim().split('\n')) {
            str.append(line.trim()).append('\n')
        }
        return str.toString().replace(t, "\t")
    }

    fun runTest(inp: String, out: String) {
        assertEquals(outPrepare(out), KaraHTMLConverter.converter(inp))

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

}