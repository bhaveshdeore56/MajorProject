package com.example.edai.data.model

import com.google.gson.annotations.SerializedName

data class LocationInfo(
    val latitude: Double,
    val longitude: Double,
    val placeName: String,
    val displayName: String? = null,
    val country: String? = null,
    val city: String? = null
)

// Nominatim API models
data class NominatimResult(
    @SerializedName("place_id") val placeId: Long? = null,
    @SerializedName("licence") val licence: String? = null,
    @SerializedName("osm_type") val osmType: String? = null,
    @SerializedName("osm_id") val osmId: Long? = null,
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String,
    @SerializedName("class") val classType: String? = null,
    @SerializedName("type") val type: String? = null,
    @SerializedName("place_rank") val placeRank: Int? = null,
    @SerializedName("importance") val importance: Double? = null,
    @SerializedName("addresstype") val addressType: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("address") val address: NominatimAddress? = null,
    @SerializedName("boundingbox") val boundingBox: List<String>? = null
)

data class NominatimAddress(
    @SerializedName("house_number") val houseNumber: String? = null,
    @SerializedName("road") val road: String? = null,
    @SerializedName("neighbourhood") val neighbourhood: String? = null,
    @SerializedName("suburb") val suburb: String? = null,
    @SerializedName("city") val city: String? = null,
    @SerializedName("municipality") val municipality: String? = null,
    @SerializedName("county") val county: String? = null,
    @SerializedName("state") val state: String? = null,
    @SerializedName("ISO3166-2-lvl4") val iso31662Lvl4: String? = null,
    @SerializedName("postcode") val postcode: String? = null,
    @SerializedName("country") val country: String? = null,
    @SerializedName("country_code") val countryCode: String? = null
)