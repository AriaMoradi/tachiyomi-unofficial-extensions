package eu.kanade.tachiyomi.extension.en.madaradex

import eu.kanade.tachiyomi.multisrc.madara.Madara
import okhttp3.Headers
import java.text.SimpleDateFormat
import java.util.Locale

class MadaraDex : Madara(
    "MadaraDex",
    "https://madaradex.org",
    "en",
    dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
) {
    override fun headersBuilder() = Headers.Builder()
        .add("User-Agent", "cumlung")
}
