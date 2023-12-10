package io.github.sgpublic.gtwb.utils

import nl.adaptivity.xmlutil.ExperimentalXmlUtilApi
import nl.adaptivity.xmlutil.serialization.DefaultXmlSerializationPolicy
import nl.adaptivity.xmlutil.serialization.XML
import nl.adaptivity.xmlutil.serialization.XmlConfig

@Suppress("DEPRECATION")
@OptIn(ExperimentalXmlUtilApi::class)
val XmlGlobal = XML {
    policy = DefaultXmlSerializationPolicy(
        pedantic = false,
        unknownChildHandler = XmlConfig.IGNORING_UNKNOWN_CHILD_HANDLER
    )
}