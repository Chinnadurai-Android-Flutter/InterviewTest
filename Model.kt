package com.interviewtest.techTask

import com.google.gson.annotations.SerializedName
import java.io.Serializable

object Model {
    interface Result : Serializable {
    }

    data class ModelValues(
            @SerializedName("page") var page: Int? = null,
            @SerializedName("per_page") var per_page: Int? = null,
            @SerializedName("total") var total: Int? = null,
            @SerializedName("total_pages") var total_pages: Int? = null,
            @SerializedName("data") var data: ArrayList<Data>? = ArrayList(),
            @SerializedName("support") var support: Support? = null
    ) : Result

    data class Data(
            @SerializedName("id") var id: Int? = null,
            @SerializedName("email") var email: String? = null,
            @SerializedName("first_name") var first_name: String? = null,
            @SerializedName("last_name") var last_name: String? = null,
            @SerializedName("avatar") var avatar: String? = null
    ) : Result

    data class Support(
            @SerializedName("url") var url: String? = null,
            @SerializedName("text") var text: String? = null
    ) : Result
}