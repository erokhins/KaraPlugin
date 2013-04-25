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


public class Formatter(val indent : String = "\t", val lineBreak : String = "\n", val startIndentCount : Int = 0)

public class HtmlToKaraConverter(val pluginOptions : KaraPluginOptions, val formatter : Formatter = Formatter()) {
    private val n = formatter.lineBreak
    private val k = "\"\"\""

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

    private fun generateIndent(depth : Int) : String {
        return formatter.indent.repeat(depth)
    }

    private fun getTrimLines(text : String): List<String> {
        val trimText = text.trim()
        if (trimText.isEmpty()) return Collections.emptyList()

        return trimText.split("$n").map { s -> s.trim() }
    }

    private fun dataConverter(text : String, depth :  Int): String {
        val str = StringBuilder()
        for (line in getTrimLines(text)) {
            str.append(generateIndent(depth)).append(line).append("$n")
        }
        return str.toString()
    }

    private fun textConverter(text : String, depth :  Int): String {
        val str = StringBuilder()
        val trimLines = getTrimLines(text)
        if (trimLines.size() > 1) {
            str.append(generateIndent(depth)).append("$k$n")
            for (line in trimLines) {
                str.append(generateIndent(depth+1)).append("$line$n")
            }
            str.append(generateIndent(depth)).append("$k$n")
        } else {
            if (!trimLines.isEmpty()) {
                str.append(generateIndent(depth)).append("+\"${trimLines[0]}\"$n")
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

                comment -> stringBuilder.append(generateIndent(realDepth)).append("/*$n")
                        .append(dataConverter(node.attr("comment")!!, realDepth + 1))

                data -> stringBuilder.append(generateIndent(realDepth)).append("$k$n")
                        .append(dataConverter(node.attr("data")!!, realDepth + 1))

                element -> {
                    stringBuilder.append(generateIndent(realDepth)).append(node.nodeName())
                    val attrStr = attributeConverter.convert(node.attributes()!!)
                    if (!attrStr.isEmpty()) stringBuilder.append('(').append(attrStr).append(')')

                    if (node.childNodeSize() != 0) {
                        stringBuilder.append(" {$n")
                    } else {
                        stringBuilder.append("$n")
                    }
                }
            }
        }

        override fun tail(node: Node, depth: Int) {
            val realDepth = depth + startDepth
            when (node.getType()) {
                document, doctype -> {}
                text -> {}
                comment -> stringBuilder.append(generateIndent(realDepth)).append("*/$n")
                data -> stringBuilder.append(generateIndent(realDepth)).append("$k$n")
                element -> {
                    if (node.childNodeSize() != 0) stringBuilder.append(generateIndent(realDepth)).append("}$n")
                }
            }
        }
    }
}





