package org.jetbrains.kara.plugin.converter
/**
* @author Stanislav Erokhin
*/

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

public object KaraHTMLConverter {
    val HTML_BODY_PATTERN = Pattern.compile("(<body[\\s>])", Pattern.CASE_INSENSITIVE)

    public fun itMayContentHTML(text: String): Boolean {
        if (hasBodyTag(text)) {
            return true
        }
        val doc = Jsoup.parseBodyFragment(text)
        for (element in doc.body()!!.childNodes()!!) {
            if (element.nodeName() !== "#text") {
                return true
            }
        }
        return false
    }

    public fun converter(htmlText : String, startDepth : Int = 0): String {
        val str = StringBuilder()
        if (hasBodyTag(htmlText)) {
            val doc = Jsoup.parse(htmlText)
            NodeTraversor(KaraConvertNodeVisitor(str, startDepth)).traverse(doc!!.head())
            NodeTraversor(KaraConvertNodeVisitor(str, startDepth)).traverse(doc.body())
        } else {
            val doc = Jsoup.parseBodyFragment(htmlText)
            for (element in doc!!.body()!!.childNodes()!!) {
                NodeTraversor(KaraConvertNodeVisitor(str, startDepth)).traverse(element)
            }
        }
        return str.toString()
    }

    private fun hasBodyTag(htmlText : String): Boolean {
        return HTML_BODY_PATTERN.matcher(htmlText).find()
    }

    private fun spaces(depth : Int) : String {
        return "\t".repeat(depth)
    }

    private fun getTrimLines(text : String): List<String> {
        val trimText = text.trim()
        if (trimText.isEmpty()) return Collections.emptyList()

        return trimText.split('\n').map { s -> s.trim() }
    }

    private fun dataConverter(text : String, depth :  Int): String {
        val str = StringBuilder()
        for (line in getTrimLines(text)) {
            str.append(spaces(depth)).append(line).append('\n')
        }
        return str.toString()
    }

    private fun textConverter(text : String, depth :  Int): String {
        val str = StringBuilder()
        for (line in getTrimLines(text)) {
            str.append(spaces(depth)).append("+\"$line\"\n")
        }
        return str.toString()
    }

    private class KaraConvertNodeVisitor(val stringBuilder : StringBuilder, val startDepth : Int) : NodeVisitor {
        override fun head(node: Node?, depth: Int) {
            val realDepth = depth + startDepth
            when (node!!.getType()) {
                document, doctype -> {}
                text -> {
                    val convertedText = textConverter(node.attr("text")!!, realDepth)
                    if (!convertedText.isEmpty()) stringBuilder.append(convertedText)
                }

                comment -> stringBuilder.append(spaces(realDepth)).append("/*\n")
                        .append(dataConverter(node.attr("comment")!!, realDepth + 1))

                data -> stringBuilder.append(spaces(realDepth)).append("\"\"\"\n")
                        .append(dataConverter(node.attr("data")!!, realDepth + 1))

                element -> {
                    stringBuilder.append(spaces(realDepth)).append(node.nodeName())
                    val attrStr = KaraAttributeConverter.attributesConverter(node.attributes()!!)
                    if (!attrStr.isEmpty()) stringBuilder.append('(').append(attrStr).append(')')

                    if (node.childNodeSize() != 0) {
                        stringBuilder.append(" {\n")
                    } else {
                        stringBuilder.append("\n")
                    }
                }
            }
        }

        override fun tail(node: Node?, depth: Int) {
            val realDepth = depth + startDepth
            when (node!!.getType()) {
                document, doctype -> {}
                text -> {}
                comment -> stringBuilder.append(spaces(realDepth)).append("*/\n")
                data -> stringBuilder.append(spaces(realDepth)).append("\"\"\"\n")
                element -> {
                    if (node.childNodeSize() != 0) stringBuilder.append(spaces(realDepth)).append("}\n")
                }
            }
        }
    }

}





