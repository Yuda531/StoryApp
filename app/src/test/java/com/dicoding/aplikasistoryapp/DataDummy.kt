package com.dicoding.aplikasistoryapp

import com.dicoding.aplikasistoryapp.data.remote.response.ListStoryItem

object DataDummy {
    fun generateDummyStoryResponse(): List<ListStoryItem> {
        val items: MutableList<ListStoryItem> = arrayListOf()
        for (i in 0..100) {
            val story = ListStoryItem(
                i.toString(),
                name = "name + $i",
                description = "description + $i",
                photoUrl = "photoUrl + $i",
                createdAt = "createdAt + $i",
                lat = null,
                lon = null
            )
            items.add(story)
        }
        return items
    }
}