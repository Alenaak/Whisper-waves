package com.example.whisper_waves.data

import androidx.lifecycle.Observer


open class Event<out T>(private val content: T) {
    private var isHandled = false

    fun getContentIfNotHandled(): T? {
        return if (isHandled) {
            null
        } else {
            isHandled = true
            content
        }
    }
}

 class EventObserver<T>(private val onEventUnhandledContent: (T) -> Unit) : Observer<Event<T>> {
    override fun onChanged(value: Event<T>) {
        value?.getContentIfNotHandled()?.let { onEventUnhandledContent(it) }
    }
}
