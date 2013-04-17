package kara.converter

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
    private fun styleClasses(styleAttr : String): String {
        val classes = styleAttr.split(' ')
        val str = StringBuilder()
        for (styleClass in classes) {
            if (str.length() != 0) {
                str.append(" + ")
            }
            str.append(styleClassConvert(styleClass))
        }

        return str.toString()
    }

    public fun attributesConverter(attributes : Attributes): String {
        val str = StringBuilder()
        for (attr in attributes.asList()!!) {
            if (str.length() > 0) {
                str.append(", ")
            }
            if (attr.getKey().equals("class")) {
                str.append("c = ").append(styleClasses(attr.getValue()))
                continue
            }
            str.append(attr.getKey()).append(" = \"").append(attr.getValue()).append("\"")
        }
        return str.toString()
    }
}