package com.example.newspulse.domain.util

import com.example.newspulse.domain.model.Article

fun scoreRelatedArticles(baseArticle: Article, candidates: List<Article>): List<Article> {
    val baseIds = baseArticle.interests.map { it.name }.toSet()
    return candidates
        .filter { it.id != baseArticle.id }
        .map { article ->
            val sharedInterests = article.interests.count { it.name in baseIds }
            val sameSource = if (article.source == baseArticle.source) 1 else 0
            val score = sharedInterests * 10 + sameSource * 5
            article to score
        }
        .sortedWith(
            compareBy(
                { -it.second },
                { -it.first.publishedAt }
            )
        )
        .map { it.first }
}
