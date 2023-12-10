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
    @XmlElement(false)
    @SerialName("count")
    val count: Int,
    @XmlElement(true)
    val content: List<VcsRootInstance>,
) {
    @Serializable
    @SerialName("vcs-root-instance")
    data class VcsRootInstance(
        @XmlElement(false)
        @SerialName("id")
        val id: Int,
        @XmlElement(false)
        @SerialName("name")
        val name: String,
    )
}

@Serializable
data class GitLabWebhookContent(
    @SerialName("ref")
    val ref: String,
    @SerialName("project")
    val project: Project,
) {
    @Serializable
    data class Project(
        @SerialName("path_with_namespace")
        val pathWithNameSpace: String,
    )
}
