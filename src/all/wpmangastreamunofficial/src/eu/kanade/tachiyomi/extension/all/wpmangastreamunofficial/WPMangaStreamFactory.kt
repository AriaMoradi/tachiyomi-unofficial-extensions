package eu.kanade.tachiyomi.extension.all.wpmangastreamunofficial

import eu.kanade.tachiyomi.source.Source
import eu.kanade.tachiyomi.source.SourceFactory

class WPMangaStreamFactory : SourceFactory {
    override fun createSources(): List<Source> = listOf(
        AsuraScans(),
    )
}

class AsuraScans : WPMangaStream("AsuraScans", "https://asurascans.com", "en") {
    override val pageSelector = "div#readerarea img[lazy]"
}
