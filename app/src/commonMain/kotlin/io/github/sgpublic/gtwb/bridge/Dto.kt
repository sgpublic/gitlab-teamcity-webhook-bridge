package io.github.sgpublic.gtwb.bridge

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement

/*
<vcs-root-instances count="1" href="/app/rest/vcs-root-instances?locator=buildType:xxx">
    <vcs-root-instance id="1" vcs-root-id="xxx" name="https://xxx.git#refs/heads/master" href="/app/rest/vcs-root-instances/id:1"/>
</vcs-root-instances>
 */
@Serializable
@SerialName("vcs-root-instances")
data class VcsRootInstances(
    @SerialName("count")
    val count: Int,
    @XmlElement
    val content: List<VcsRootInstance>,
) {
    @Serializable
    @SerialName("vcs-root-instance")
    data class VcsRootInstance(
        @SerialName("id")
        val id: Int,
        @SerialName("name")
        val name: String,
    )
}