package com.example.pagination_jc

interface Paginator<Key,Item> {
    suspend fun loadNextItem()
    fun reset()
}