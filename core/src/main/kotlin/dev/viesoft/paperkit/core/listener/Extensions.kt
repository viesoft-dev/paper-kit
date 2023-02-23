@file:Suppress("unused")

package dev.viesoft.paperkit.core.listener

import dev.viesoft.paperkit.core.plugin.IKotlinPlugin
import org.bukkit.event.Event

/**
 * Registers the [KotlinListener] to the [IKotlinPlugin].
 */
fun IKotlinPlugin.register(listener: KotlinListener): KotlinListener {
    return listener.register(this)
}

/**
 * Creates a [KotlinListener] from the given [listener] function and registers it to the [IKotlinPlugin].
 */
inline fun <reified T : Event> IKotlinPlugin.registerListener(crossinline listener: suspend (T) -> Unit): KotlinListener {
    return listener(listener).register(this)
}

/**
 * Creates a [KotlinListener] from the given [listener] function.
 * @see dev.viesoft.paperkit.core.listener.registerListener
 */
inline fun <reified T : Event> listener(crossinline listener: suspend (T) -> Unit): KotlinListener {
    return object : KotlinListener {
        suspend fun onEvent(event: T) = listener(event)
    }
}
