package eu.kanade.tachiyomi.extension.all.mangadex

import android.content.SharedPreferences
import eu.kanade.tachiyomi.source.model.Filter
import eu.kanade.tachiyomi.source.model.FilterList
import okhttp3.HttpUrl

class MangaDexFilters {

    internal fun getMDFilterList(preferences: SharedPreferences, dexLang: String, intl: MangaDexIntl): FilterList {
        return FilterList(
            HasAvailableChaptersFilter(intl),
            OriginalLanguageList(intl, getOriginalLanguage(preferences, dexLang, intl)),
            ContentRatingList(intl, getContentRating(preferences, dexLang, intl)),
            DemographicList(intl, getDemographics(intl)),
            StatusList(intl, getStatus(intl)),
            SortFilter(intl, getSortables(intl)),
            TagsFilter(intl, getTagFilters(intl)),
            TagList(intl.content, getContents(intl)),
            TagList(intl.format, getFormats(intl)),
            TagList(intl.genre, getGenres(intl)),
            TagList(intl.theme, getThemes(intl)),
        )
    }

    private interface UrlQueryFilter {
        fun addQueryParameter(url: HttpUrl.Builder, dexLang: String)
    }

    private class HasAvailableChaptersFilter(intl: MangaDexIntl) :
        Filter.CheckBox(intl.hasAvailableChapters),
        UrlQueryFilter {

        override fun addQueryParameter(url: HttpUrl.Builder, dexLang: String) {
            if (state) {
                url.addQueryParameter("hasAvailableChapters", "true")
                url.addQueryParameter("availableTranslatedLanguage[]", dexLang)
            }
        }
    }

    private class OriginalLanguage(name: String, val isoCode: String) : Filter.CheckBox(name)
    private class OriginalLanguageList(intl: MangaDexIntl, originalLanguage: List<OriginalLanguage>) :
        Filter.Group<OriginalLanguage>(intl.originalLanguage, originalLanguage),
        UrlQueryFilter {

        override fun addQueryParameter(url: HttpUrl.Builder, dexLang: String) {
            state.forEach { lang ->
                if (lang.state) {
                    // dex has zh and zh-hk for chinese manhua
                    if (lang.isoCode == MDConstants.originalLanguagePrefValChinese) {
                        url.addQueryParameter(
                            "originalLanguage[]",
                            MDConstants.originalLanguagePrefValChineseHk
                        )
                    }

                    url.addQueryParameter("originalLanguage[]", lang.isoCode)
                }
            }
        }
    }

    private fun getOriginalLanguage(preferences: SharedPreferences, dexLang: String, intl: MangaDexIntl): List<OriginalLanguage> {
        val originalLanguages = preferences.getStringSet(
            MDConstants.getOriginalLanguagePrefKey(dexLang),
            setOf()
        )!!

        return listOf(
            OriginalLanguage(intl.originalLanguageFilterJapanese, MDConstants.originalLanguagePrefValJapanese)
                .apply { state = MDConstants.originalLanguagePrefValJapanese in originalLanguages },
            OriginalLanguage(intl.originalLanguageFilterChinese, MDConstants.originalLanguagePrefValChinese)
                .apply { state = MDConstants.originalLanguagePrefValChinese in originalLanguages },
            OriginalLanguage(intl.originalLanguageFilterKorean, MDConstants.originalLanguagePrefValKorean)
                .apply { state = MDConstants.originalLanguagePrefValKorean in originalLanguages },
        )
    }

    private class ContentRating(name: String, val value: String) : Filter.CheckBox(name)
    private class ContentRatingList(intl: MangaDexIntl, contentRating: List<ContentRating>) :
        Filter.Group<ContentRating>(intl.contentRating, contentRating),
        UrlQueryFilter {

        override fun addQueryParameter(url: HttpUrl.Builder, dexLang: String) {
            state.forEach { rating ->
                if (rating.state) {
                    url.addQueryParameter("contentRating[]", rating.value)
                }
            }
        }
    }

    private fun getContentRating(preferences: SharedPreferences, dexLang: String, intl: MangaDexIntl): List<ContentRating> {
        val contentRatings = preferences.getStringSet(
            MDConstants.getContentRatingPrefKey(dexLang),
            MDConstants.contentRatingPrefDefaults
        )
        return listOf(
            ContentRating(intl.contentRatingSafe, MDConstants.contentRatingPrefValSafe).apply {
                state = contentRatings
                    ?.contains(MDConstants.contentRatingPrefValSafe) ?: true
            },
            ContentRating(intl.contentRatingSuggestive, MDConstants.contentRatingPrefValSuggestive).apply {
                state = contentRatings
                    ?.contains(MDConstants.contentRatingPrefValSuggestive) ?: true
            },
            ContentRating(intl.contentRatingErotica, MDConstants.contentRatingPrefValErotica).apply {
                state = contentRatings
                    ?.contains(MDConstants.contentRatingPrefValErotica) ?: false
            },
            ContentRating(intl.contentRatingPornographic, MDConstants.contentRatingPrefValPornographic).apply {
                state = contentRatings
                    ?.contains(MDConstants.contentRatingPrefValPornographic) ?: false
            },
        )
    }

    private class Demographic(name: String, val value: String) : Filter.CheckBox(name)
    private class DemographicList(intl: MangaDexIntl, demographics: List<Demographic>) :
        Filter.Group<Demographic>(intl.publicationDemographic, demographics),
        UrlQueryFilter {

        override fun addQueryParameter(url: HttpUrl.Builder, dexLang: String) {
            state.forEach { demographic ->
                if (demographic.state) {
                    url.addQueryParameter("publicationDemographic[]", demographic.value)
                }
            }
        }
    }

    private fun getDemographics(intl: MangaDexIntl) = listOf(
        Demographic(intl.publicationDemographicNone, "none"),
        Demographic(intl.publicationDemographicShounen, "shounen"),
        Demographic(intl.publicationDemographicShoujo, "shoujo"),
        Demographic(intl.publicationDemographicSeinen, "seinen"),
        Demographic(intl.publicationDemographicJosei, "josei")
    )

    private class Status(name: String, val value: String) : Filter.CheckBox(name)
    private class StatusList(intl: MangaDexIntl, status: List<Status>) :
        Filter.Group<Status>(intl.status, status),
        UrlQueryFilter {

        override fun addQueryParameter(url: HttpUrl.Builder, dexLang: String) {
            state.forEach { status ->
                if (status.state) {
                    url.addQueryParameter("status[]", status.value)
                }
            }
        }
    }

    private fun getStatus(intl: MangaDexIntl) = listOf(
        Status(intl.statusOngoing, "ongoing"),
        Status(intl.statusCompleted, "completed"),
        Status(intl.statusHiatus, "hiatus"),
        Status(intl.statusCancelled, "cancelled"),
    )

    data class Sortable(val title: String, val value: String) {
        override fun toString(): String = title
    }

    private fun getSortables(intl: MangaDexIntl) = arrayOf(
        Sortable(intl.sortAlphabetic, "title"),
        Sortable(intl.sortChapterUploadedAt, "latestUploadedChapter"),
        Sortable(intl.sortNumberOfFollows, "followedCount"),
        Sortable(intl.sortContentCreatedAt, "createdAt"),
        Sortable(intl.sortContentInfoUpdatedAt, "updatedAt"),
        Sortable(intl.sortRelevance, "relevance"),
        Sortable(intl.sortYear, "year"),
        Sortable(intl.sortRating, "rating")
    )

    class SortFilter(intl: MangaDexIntl, private val sortables: Array<Sortable>) :
        Filter.Sort(
            intl.sort,
            sortables.map(Sortable::title).toTypedArray(),
            Selection(5, false)
        ),
        UrlQueryFilter {

        override fun addQueryParameter(url: HttpUrl.Builder, dexLang: String) {
            if (state != null) {
                val query = sortables[state!!.index].value
                val value = when (state!!.ascending) {
                    true -> "asc"
                    false -> "desc"
                }

                url.addQueryParameter("order[$query]", value)
            }
        }
    }

    internal class Tag(val id: String, name: String) : Filter.TriState(name)

    private class TagList(collection: String, tags: List<Tag>) :
        Filter.Group<Tag>(collection, tags),
        UrlQueryFilter {

        override fun addQueryParameter(url: HttpUrl.Builder, dexLang: String) {
            state.forEach { tag ->
                if (tag.isIncluded()) {
                    url.addQueryParameter("includedTags[]", tag.id)
                } else if (tag.isExcluded()) {
                    url.addQueryParameter("excludedTags[]", tag.id)
                }
            }
        }
    }

    internal fun getContents(intl: MangaDexIntl): List<Tag> {
        val tags = listOf(
            Tag("b29d6a3d-1569-4e7a-8caf-7557bc92cd5d", intl.contentGore),
            Tag("97893a4c-12af-4dac-b6be-0dffb353568e", intl.contentSexualViolence),
        )

        return tags.sortIfTranslated(intl)
    }

    internal fun getFormats(intl: MangaDexIntl): List<Tag> {
        val tags = listOf(
            Tag("b11fda93-8f1d-4bef-b2ed-8803d3733170", intl.formatFourKoma),
            Tag("f4122d1c-3b44-44d0-9936-ff7502c39ad3", intl.formatAdaptation),
            Tag("51d83883-4103-437c-b4b1-731cb73d786c", intl.formatAnthology),
            Tag("0a39b5a1-b235-4886-a747-1d05d216532d", intl.formatAwardWinning),
            Tag("b13b2a48-c720-44a9-9c77-39c9979373fb", intl.formatDoujinshi),
            Tag("7b2ce280-79ef-4c09-9b58-12b7c23a9b78", intl.formatFanColored),
            Tag("f5ba408b-0e7a-484d-8d49-4e9125ac96de", intl.formatFullColor),
            Tag("3e2b8dae-350e-4ab8-a8ce-016e844b9f0d", intl.formatLongStrip),
            Tag("320831a8-4026-470b-94f6-8353740e6f04", intl.formatOfficialColored),
            Tag("0234a31e-a729-4e28-9d6a-3f87c4966b9e", intl.formatOneshot),
            Tag("891cf039-b895-47f0-9229-bef4c96eccd4", intl.formatUserCreated),
            Tag("e197df38-d0e7-43b5-9b09-2842d0c326dd", intl.formatWebComic),
        )

        return tags.sortIfTranslated(intl)
    }

    internal fun getGenres(intl: MangaDexIntl): List<Tag> {
        val tags = listOf(
            Tag("391b0423-d847-456f-aff0-8b0cfc03066b", intl.genreAction),
            Tag("87cc87cd-a395-47af-b27a-93258283bbc6", intl.genreAdventure),
            Tag("5920b825-4181-4a17-beeb-9918b0ff7a30", intl.genreBoysLove),
            Tag("4d32cc48-9f00-4cca-9b5a-a839f0764984", intl.genreComedy),
            Tag("5ca48985-9a9d-4bd8-be29-80dc0303db72", intl.genreCrime),
            Tag("b9af3a63-f058-46de-a9a0-e0c13906197a", intl.genreDrama),
            Tag("cdc58593-87dd-415e-bbc0-2ec27bf404cc", intl.genreFantasy),
            Tag("a3c67850-4684-404e-9b7f-c69850ee5da6", intl.genreGirlsLove),
            Tag("33771934-028e-4cb3-8744-691e866a923e", intl.genreHistorical),
            Tag("cdad7e68-1419-41dd-bdce-27753074a640", intl.genreHorror),
            Tag("ace04997-f6bd-436e-b261-779182193d3d", intl.genreIsekai),
            Tag("81c836c9-914a-4eca-981a-560dad663e73", intl.genreMagicalGirls),
            Tag("50880a9d-5440-4732-9afb-8f457127e836", intl.genreMecha),
            Tag("c8cbe35b-1b2b-4a3f-9c37-db84c4514856", intl.genreMedical),
            Tag("ee968100-4191-4968-93d3-f82d72be7e46", intl.genreMystery),
            Tag("b1e97889-25b4-4258-b28b-cd7f4d28ea9b", intl.genrePhilosophical),
            Tag("423e2eae-a7a2-4a8b-ac03-a8351462d71d", intl.genreRomance),
            Tag("256c8bd9-4904-4360-bf4f-508a76d67183", intl.genreSciFi),
            Tag("e5301a23-ebd9-49dd-a0cb-2add944c7fe9", intl.genreSliceOfLife),
            Tag("69964a64-2f90-4d33-beeb-f3ed2875eb4c", intl.genreSports),
            Tag("7064a261-a137-4d3a-8848-2d385de3a99c", intl.genreSuperhero),
            Tag("07251805-a27e-4d59-b488-f0bfbec15168", intl.genreThriller),
            Tag("f8f62932-27da-4fe4-8ee1-6779a8c5edba", intl.genreTragedy),
            Tag("acc803a4-c95a-4c22-86fc-eb6b582d82a2", intl.genreWuxia),
        )

        return tags.sortIfTranslated(intl)
    }

    internal fun getThemes(intl: MangaDexIntl): List<Tag> {
        val tags = listOf(
            Tag("e64f6742-c834-471d-8d72-dd51fc02b835", intl.themeAliens),
            Tag("3de8c75d-8ee3-48ff-98ee-e20a65c86451", intl.themeAnimals),
            Tag("ea2bc92d-1c26-4930-9b7c-d5c0dc1b6869", intl.themeCooking),
            Tag("9ab53f92-3eed-4e9b-903a-917c86035ee3", intl.themeCrossdressing),
            Tag("da2d50ca-3018-4cc0-ac7a-6b7d472a29ea", intl.themeDelinquents),
            Tag("39730448-9a5f-48a2-85b0-a70db87b1233", intl.themeDemons),
            Tag("2bd2e8d0-f146-434a-9b51-fc9ff2c5fe6a", intl.themeGenderSwap),
            Tag("3bb26d85-09d5-4d2e-880c-c34b974339e9", intl.themeGhosts),
            Tag("fad12b5e-68ba-460e-b933-9ae8318f5b65", intl.themeGyaru),
            Tag("aafb99c1-7f60-43fa-b75f-fc9502ce29c7", intl.themeHarem),
            Tag("5bd0e105-4481-44ca-b6e7-7544da56b1a3", intl.themeIncest),
            Tag("2d1f5d56-a1e5-4d0d-a961-2193588b08ec", intl.themeLoli),
            Tag("85daba54-a71c-4554-8a28-9901a8b0afad", intl.themeMafia),
            Tag("a1f53773-c69a-4ce5-8cab-fffcd90b1565", intl.themeMagic),
            Tag("799c202e-7daa-44eb-9cf7-8a3c0441531e", intl.themeMartialArts),
            Tag("ac72833b-c4e9-4878-b9db-6c8a4a99444a", intl.themeMilitary),
            Tag("dd1f77c5-dea9-4e2b-97ae-224af09caf99", intl.themeMonsterGirls),
            Tag("36fd93ea-e8b8-445e-b836-358f02b3d33d", intl.themeMonsters),
            Tag("f42fbf9e-188a-447b-9fdc-f19dc1e4d685", intl.themeMusic),
            Tag("489dd859-9b61-4c37-af75-5b18e88daafc", intl.themeNinja),
            Tag("92d6d951-ca5e-429c-ac78-451071cbf064", intl.themeOfficeWorkers),
            Tag("df33b754-73a3-4c54-80e6-1a74a8058539", intl.themePolice),
            Tag("9467335a-1b83-4497-9231-765337a00b96", intl.themePostApocalyptic),
            Tag("3b60b75c-a2d7-4860-ab56-05f391bb889c", intl.themePsychological),
            Tag("0bc90acb-ccc1-44ca-a34a-b9f3a73259d0", intl.themeReincarnation),
            Tag("65761a2a-415e-47f3-bef2-a9dababba7a6", intl.themeReverseHarem),
            Tag("81183756-1453-4c81-aa9e-f6e1b63be016", intl.themeSamurai),
            Tag("caaa44eb-cd40-4177-b930-79d3ef2afe87", intl.themeSchoolLife),
            Tag("ddefd648-5140-4e5f-ba18-4eca4071d19b", intl.themeShota),
            Tag("eabc5b4c-6aff-42f3-b657-3e90cbd00b75", intl.themeSupernatural),
            Tag("5fff9cde-849c-4d78-aab0-0d52b2ee1d25", intl.themeSurvival),
            Tag("292e862b-2d17-4062-90a2-0356caa4ae27", intl.themeTimeTravel),
            Tag("31932a7e-5b8e-49a6-9f12-2afa39dc544c", intl.themeTraditionalGames),
            Tag("d7d1730f-6eb0-4ba6-9437-602cac38664c", intl.themeVampires),
            Tag("9438db5a-7e2a-4ac0-b39e-e0d95a34b8a8", intl.themeVideoGames),
            Tag("d14322ac-4d6f-4e9b-afd9-629d5f4d8a41", intl.themeVillainess),
            Tag("8c86611e-fab7-4986-9dec-d1a2f44acdd5", intl.themeVirtualReality),
            Tag("631ef465-9aba-4afb-b0fc-ea10efe274a8", intl.themeZombies)
        )

        return tags.sortIfTranslated(intl)
    }

    // to get all tags from dex https://api.mangadex.org/manga/tag
    internal fun getTags(intl: MangaDexIntl): List<Tag> {
        return getContents(intl) + getFormats(intl) + getGenres(intl) + getThemes(intl)
    }

    private data class TagMode(val title: String, val value: String) {
        override fun toString(): String = title
    }

    private fun getTagModes(intl: MangaDexIntl) = arrayOf(
        TagMode(intl.modeAnd, "AND"),
        TagMode(intl.modeOr, "OR")
    )

    private class TagInclusionMode(intl: MangaDexIntl, modes: Array<TagMode>) :
        Filter.Select<TagMode>(intl.includedTagsMode, modes, 0),
        UrlQueryFilter {

        override fun addQueryParameter(url: HttpUrl.Builder, dexLang: String) {
            url.addQueryParameter("includedTagsMode", values[state].value)
        }
    }

    private class TagExclusionMode(intl: MangaDexIntl, modes: Array<TagMode>) :
        Filter.Select<TagMode>(intl.excludedTagsMode, modes, 1),
        UrlQueryFilter {

        override fun addQueryParameter(url: HttpUrl.Builder, dexLang: String) {
            url.addQueryParameter("excludedTagsMode", values[state].value)
        }
    }

    private class TagsFilter(intl: MangaDexIntl, innerFilters: FilterList) :
        Filter.Group<Filter<*>>(intl.tags, innerFilters),
        UrlQueryFilter {

        override fun addQueryParameter(url: HttpUrl.Builder, dexLang: String) {
            state.filterIsInstance<UrlQueryFilter>()
                .forEach { filter -> filter.addQueryParameter(url, dexLang) }
        }
    }

    private fun getTagFilters(intl: MangaDexIntl): FilterList = FilterList(
        TagInclusionMode(intl, getTagModes(intl)),
        TagExclusionMode(intl, getTagModes(intl)),
    )

    internal fun addFiltersToUrl(url: HttpUrl.Builder, filters: FilterList, dexLang: String): String {
        filters.filterIsInstance<UrlQueryFilter>()
            .forEach { filter -> filter.addQueryParameter(url, dexLang) }

        return url.toString()
    }

    private fun List<Tag>.sortIfTranslated(intl: MangaDexIntl): List<Tag> = apply {
        if (intl.availableLang == MangaDexIntl.ENGLISH) {
            return this
        }

        return sortedWith(compareBy(intl.collator, Tag::name))
    }
}
