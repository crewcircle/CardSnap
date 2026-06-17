package com.cardsnap.domain

import com.cardsnap.domain.model.ContactCard

object ContactDeduplicator {

    /**
     * Normalized Levenshtein similarity: 1.0 = identical, 0.0 = completely different.
     */
    fun normalizedSimilarity(a: String, b: String): Double {
        val aStr = a.trim().lowercase()
        val bStr = b.trim().lowercase()
        if (aStr == bStr) return 1.0
        if (aStr.isEmpty() && bStr.isEmpty()) return 1.0
        if (aStr.isEmpty() || bStr.isEmpty()) return 0.0
        val maxLen = maxOf(aStr.length, bStr.length)
        if (maxLen == 0) return 1.0
        return 1.0 - levenshteinDistance(aStr, bStr).toDouble() / maxLen
    }

    /**
     * Find exact duplicate pairs where email (non-blank) matches OR phone (non-blank) matches.
     * Returns only the first matching pair per duplicate group.
     * Only contacts with at least name OR email OR phone non-blank are considered.
     */
    fun findExactDuplicates(contacts: List<ContactCard>): List<Pair<ContactCard, ContactCard>> {
        val valid = contacts.filter { it.name.isNotBlank() || it.email.isNotBlank() || it.phone.isNotBlank() }
        val result = mutableListOf<Pair<ContactCard, ContactCard>>()
        val matchedIds = mutableSetOf<String>()

        valid.groupBy { it.email.trim().lowercase() }
            .filter { it.key.isNotBlank() && it.value.size >= 2 }
            .forEach { (_, group) ->
                val unmatched = group.filter { it.id !in matchedIds }
                if (unmatched.size >= 2) {
                    result.add(Pair(unmatched[0], unmatched[1]))
                    matchedIds.add(unmatched[0].id)
                    matchedIds.add(unmatched[1].id)
                }
            }

        valid.groupBy { it.phone.filter { c -> c.isDigit() } }
            .filter { it.key.isNotBlank() && it.value.size >= 2 }
            .forEach { (_, group) ->
                val unmatched = group.filter { it.id !in matchedIds }
                if (unmatched.size >= 2) {
                    result.add(Pair(unmatched[0], unmatched[1]))
                    matchedIds.add(unmatched[0].id)
                    matchedIds.add(unmatched[1].id)
                }
            }

        return result
    }

    /**
     * Find fuzzy duplicate pairs where names have high similarity (> 0.8)
     * OR same company + similar name (> 0.5).
     * Filters out pairs already found by exact matching.
     * Only contacts with at least name OR email OR phone non-blank are considered.
     */
    fun findFuzzyDuplicates(contacts: List<ContactCard>): List<Pair<ContactCard, ContactCard>> {
        val valid = contacts.filter { it.name.isNotBlank() || it.email.isNotBlank() || it.phone.isNotBlank() }

        val exactMatchedIds = findExactDuplicates(contacts)
            .flatMap { listOf(it.first.id, it.second.id) }
            .toSet()
        val result = mutableListOf<Pair<ContactCard, ContactCard>>()
        val matchedIds = exactMatchedIds.toMutableSet()

        for (i in valid.indices) {
            for (j in i + 1 until valid.size) {
                val a = valid[i]
                val b = valid[j]
                if (a.id in matchedIds || b.id in matchedIds) continue

                val nameSim = normalizedSimilarity(a.name, b.name)
                val sameCompany = a.company.isNotBlank() &&
                    a.company.equals(b.company, ignoreCase = true)
                val sameCompanyAndSimilarName = sameCompany && nameSim > 0.5

                if (nameSim > 0.8 || sameCompanyAndSimilarName) {
                    result.add(Pair(a, b))
                    matchedIds.add(a.id)
                    matchedIds.add(b.id)
                }
            }
        }

        return result
    }

    private fun levenshteinDistance(a: String, b: String): Int {
        val dp = Array(a.length + 1) { IntArray(b.length + 1) }
        for (i in 0..a.length) dp[i][0] = i
        for (j in 0..b.length) dp[0][j] = j
        for (i in 1..a.length) {
            for (j in 1..b.length) {
                val cost = if (a[i - 1] == b[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,
                    dp[i][j - 1] + 1,
                    dp[i - 1][j - 1] + cost
                )
            }
        }
        return dp[a.length][b.length]
    }
}
