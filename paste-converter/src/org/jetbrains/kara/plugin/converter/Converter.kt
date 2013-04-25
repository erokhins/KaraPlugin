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

import java.util.HashMap
import org.jsoup.select.NodeVisitor
import org.jsoup.nodes.Node
import org.jsoup.Jsoup
import org.jsoup.select.NodeTraversor
import org.jetbrains.kara.plugin.converter.NodeType.*
import org.jsoup.nodes.Attributes
import java.util.Collections
import java.util.ArrayList
import java.util.regex.Pattern
import org.jetbrains.kara.plugin.KaraPluginOptions
import org.apache.commons.lang.StringUtils

enum class NodeType {
    document
    doctype
    text
    comment
    data
    element
}

fun Node.getType(): NodeType {
    return when (nodeName()) {
        "#document" -> document
        "#doctype" -> doctype
        "#text" -> text
        "#comment" -> comment
        "#data" -> data
        else -> element
    }
}

public object HtmlUtils {
    private val HTML_BODY_PATTERN = Pattern.compile("(<body[\\s>])", Pattern.CASE_INSENSITIVE)

    public fun hasBodyTag(htmlText : String): Boolean {
        return HTML_BODY_PATTERN.matcher(htmlText).find()
    }

    public fun containsHtml(text: String): Boolean {
        if (hasBodyTag(text)) {
            return true
        }
        val doc = Jsoup.parseBodyFragment(text)
        for (element in doc.body()!!.childNodes()!!) {
            if (element.getType() !== NodeType.text) {
                return true
            }
        }
        return false
    }
}


public class Formatter(val indent : String = "\t", val lineBreak : String = "\n", val startIndentCount : Int = 0) {
    public fun generateIndent(indentCount: Int) : String {
        return indent.repeat(indentCount)
    }

    public fun saveString(str : String): String {
        return StringUtils.replaceEach(str, array("$", "\\", "\""), array("\\\$", "\\\\", "\\\""))!!
    }
}

public class HtmlToKaraConverter(val pluginOptions : KaraPluginOptions, val formatter : Formatter = Formatter()) {

    fun StringBuilder.indent(depth : Int) : StringBuilder {
        return this.append(formatter.generateIndent(depth))
    }
    fun StringBuilder.threeQuo() : StringBuilder {
        return this.append("\"\"\"")
    }
    fun StringBuilder.lineBreak() : StringBuilder {
        return this.append(formatter.lineBreak)
    }
    
    public fun convert(htmlText : String): String {
        val str = StringBuilder()
        val nodeTraversor = NodeTraversor(KaraConvertNodeVisitor(str, formatter.startIndentCount,
                HtmlAttributeConverter(pluginOptions)))
        if (HtmlUtils.hasBodyTag(htmlText)) {
            val doc = Jsoup.parse(htmlText)
            nodeTraversor.traverse(doc!!.head())
            nodeTraversor.traverse(doc.body())
        } else {
            val doc = Jsoup.parseBodyFragment(htmlText)
            for (element in doc.body()!!.childNodes()!!) {
                nodeTraversor.traverse(element)
            }
        }
        return str.toString()
    }


    private fun getTrimSaveLines(text : String): List<String> {
        val trimText = text.trim()
        if (trimText.isEmpty()) return Collections.emptyList()

        return trimText.split("\n").map { s -> formatter.saveString(s.trim()) }
    }

    private fun dataConverter(text : String, depth :  Int): String {
        val str = StringBuilder()
        for (line in getTrimSaveLines(text)) {
            str.indent(depth).append(line).lineBreak()
        }
        return str.toString()
    }

    private fun textConverter(text : String, depth :  Int): String {
        val str = StringBuilder()
        val trimLines = getTrimSaveLines(text)
        if (trimLines.size() > 1) {
            str.indent(depth).threeQuo().lineBreak()
            for (line in trimLines) {
                str.indent(depth + 1).append("$line").lineBreak()
            }
            str.indent(depth).threeQuo().lineBreak()
        } else {
            if (!trimLines.isEmpty()) {
                str.indent(depth).append("+\"${trimLines[0]}\"").lineBreak()
            }
        }
        return str.toString()
    }

    private inner class KaraConvertNodeVisitor(val stringBuilder : StringBuilder, val startDepth : Int,
                                         val attributeConverter : HtmlAttributeConverter) : NodeVisitor {
        override fun head(node: Node, depth: Int) {
            val realDepth = depth + startDepth
            when (node.getType()) {
                document, doctype -> {}
                text -> {
                    val convertedText = textConverter(node.attr("text")!!, realDepth)
                    if (!convertedText.isEmpty()) stringBuilder.append(convertedText)
                }

                comment -> stringBuilder.indent(realDepth).append("/*").lineBreak()
                        .append(dataConverter(node.attr("comment")!!, realDepth + 1))

                data -> stringBuilder.indent(realDepth).threeQuo().lineBreak()
                        .append(dataConverter(node.attr("data")!!, realDepth + 1))

                element -> {
                    stringBuilder.indent(realDepth).append(node.nodeName())
                    val attrStr = attributeConverter.convert(node.attributes()!!)
                    if (!attrStr.isEmpty()) stringBuilder.append('(').append(attrStr).append(')')

                    if (node.childNodeSize() != 0) {
                        stringBuilder.append(" {").lineBreak()
                    } else {
                        stringBuilder.lineBreak()
                    }
                }
            }
        }

        override fun tail(node: Node, depth: Int) {
            val realDepth = depth + startDepth
            when (node.getType()) {
                document, doctype -> {}
                text -> {}
                comment -> stringBuilder.indent(realDepth).append("*/").lineBreak()
                data -> stringBuilder.indent(realDepth).threeQuo().lineBreak()
                element -> {
                    if (node.childNodeSize() != 0) stringBuilder.indent(realDepth).append("}").lineBreak()
                }
            }
        }
    }
}





