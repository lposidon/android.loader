package posidon.android.loader.rss

import java.util.*

class RssItem(
    val title: String,
    val link: String,
    val img: String?,
    val time: Date,
    val source: RssSource
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as RssItem
        if (title != other.title) return false
        if (link != other.link) return false
        return true
    }

    override fun hashCode() = 31 * title.hashCode() + link.hashCode()

    inline fun isBefore(other: RssItem) = time.before(other.time)
}