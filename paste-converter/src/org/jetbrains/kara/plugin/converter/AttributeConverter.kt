package org.jetbrains.kara.plugin.converter

import org.jsoup.nodes.Attributes

/**
* @author Stanislav Erokhin
*/


private object KaraAttributeConverter {

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

    public fun attributesConverter(attributes : Attributes): String {
        val str = StringBuilder()
        for (attr in attributes.asList()!!) {
            if (str.length() > 0) {
                str.append(", ")
            }
            when (attr.getKey()) {
                "class" -> str.append("c = ").append(styleClasses(attr.getValue()))

                "type" ->  str.append("inputType = ").append(inputType(attr.getValue()))
                else -> str.append(attr.getKey()).append(" = \"").append(attr.getValue()).append("\"")
            }
        }
        return str.toString()
    }
}