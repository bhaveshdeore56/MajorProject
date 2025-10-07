package com.example.edai.data.model

import com.google.gson.annotations.SerializedName

// Wikipedia API models
data class WikipediaResponse(
    @SerializedName("type") val type: String? = null,
    @SerializedName("title") val title: String,
    @SerializedName("displaytitle") val displayTitle: String? = null,
    @SerializedName("namespace") val namespace: Namespace? = null,
    @SerializedName("wikibase_item") val wikibaseItem: String? = null,
    @SerializedName("titles") val titles: Titles? = null,
    @SerializedName("pageid") val pageId: Int? = null,
    @SerializedName("thumbnail") val thumbnail: Thumbnail? = null,
    @SerializedName("originalimage") val originalImage: OriginalImage? = null,
    @SerializedName("lang") val lang: String? = null,
    @SerializedName("dir") val dir: String? = null,
    @SerializedName("revision") val revision: String? = null,
    @SerializedName("tid") val tid: String? = null,
    @SerializedName("timestamp") val timestamp: String? = null,
    @SerializedName("description") val description: String? = null,
    @SerializedName("description_source") val descriptionSource: String? = null,
    @SerializedName("content_urls") val contentUrls: ContentUrls? = null,
    @SerializedName("extract") val extract: String? = null,
    @SerializedName("extract_html") val extractHtml: String? = null
)

data class Namespace(
    @SerializedName("id") val id: Int,
    @SerializedName("text") val text: String
)

data class Titles(
    @SerializedName("canonical") val canonical: String,
    @SerializedName("normalized") val normalized: String,
    @SerializedName("display") val display: String
)

data class Thumbnail(
    @SerializedName("source") val source: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int
)

data class OriginalImage(
    @SerializedName("source") val source: String,
    @SerializedName("width") val width: Int,
    @SerializedName("height") val height: Int
)

data class ContentUrls(
    @SerializedName("desktop") val desktop: UrlInfo,
    @SerializedName("mobile") val mobile: UrlInfo
)

data class UrlInfo(
    @SerializedName("page") val page: String,
    @SerializedName("revisions") val revisions: String,
    @SerializedName("edit") val edit: String,
    @SerializedName("talk") val talk: String
)

// Simplified model for UI
data class PlaceInfo(
    val name: String,
    val description: String,
    val imageUrl: String? = null,
    val wikiUrl: String? = null,
    val location: LocationInfo? = null
)