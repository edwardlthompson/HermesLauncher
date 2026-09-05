package org.hermeslauncher.app.feeds

object SubKindFilter {
    fun records(records: List<ArticleRecord>, subs: List<FeedSub>, kind: SubKind): List<ArticleRecord> {
        val urls = subs.filter { it.kind == kind }.map { it.url }.toSet()
        if (urls.isEmpty()) {
            return emptyList()
        }
        return records.filter { rec -> rec.item.sourceUrl in urls }
    }

    fun tags(subs: List<FeedSub>, kind: SubKind): Map<String, String> {
        return subs.filter { it.kind == kind }.associate { it.url to it.tag }.filter { it.value.isNotBlank() }
    }
}
