package posidon.android.loader.duckduckgo

import android.net.Uri
import org.json.JSONException
import org.json.JSONObject
import posidon.android.loader.text.TextLoader
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

class DuckInstantAnswer private constructor(
    val title: String,
    val sourceName: String,
    val sourceUrl: String,
    val description: String,
    val searchUrl: String,
    val infoTable: Array<InfoboxEntry>?,
    val rawData: String
) {

    companion object {

        /**
         * Asynchronously loads a DuckDuckGo instant answer using [string] as the search query
         * and [t] as an indicator of where the search came from.
         * If the query gives out an instant answer, the [onLoad] function gets
         * run with the result as the parameter
         */
        @OptIn(ExperimentalContracts::class)
        fun load(string: String, t: String, onLoad: (DuckInstantAnswer) -> Unit) {
            contract { callsInPlace(onLoad, InvocationKind.AT_MOST_ONCE) }
            val encoded = Uri.encode(string)
            val url = "https://api.duckduckgo.com/?q=$encoded&format=json&t=$t"
            TextLoader.load(url) textLoader@{
                try {
                    val jObject = JSONObject(it)
                    val title = jObject.getString("Heading")
                    if (title.isBlank()) {
                        return@textLoader
                    }
                    val sourceName = jObject.getString("AbstractSource")
                    if (sourceName.isBlank()) {
                        return@textLoader
                    }
                    val sourceUrl = jObject.getString("AbstractURL")
                    if (sourceUrl.isBlank()) {
                        return@textLoader
                    }
                    val type = jObject.getString("Type")
                    if (type.isBlank()) {
                        return@textLoader
                    }
                    val description = if (type == "D") {
                        jObject.getJSONArray("RelatedTopics").getJSONObject(0).getString("Text")
                    } else {
                        jObject.getString("AbstractText")
                    }
                    val infoArray = jObject.optJSONObject("Infobox")?.optJSONArray("content")
                    val infoTable = if (infoArray == null) null else Array(infoArray.length()) {
                        val e = infoArray.getJSONObject(it)
                        InfoboxEntry(
                            e.getString("data_type"),
                            e.getString("label"),
                            e.getString("value"),
                            e.getInt("wiki_order"),
                        )
                    }
                    onLoad(
                        DuckInstantAnswer(
                            title,
                            sourceName,
                            sourceUrl,
                            description,
                            "https://duckduckgo.com/?q=$encoded&t=$t",
                            infoTable,
                            it
                        )
                    )
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }
        }
    }
}