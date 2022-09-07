package com.github.thamid_gamer.locatereborn.frontend.util

public inline fun <T, R : Comparable<R>> Iterable<T>.sortedBy(
    descending: Boolean,
    crossinline selector: (T) -> R?
): List<T> {
    return if (descending) {
        sortedByDescending(selector)
    }
    else {
        sortedBy(selector)
    }
}