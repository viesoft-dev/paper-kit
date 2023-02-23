package dev.viesoft.paperkit.core.listener

import dev.viesoft.paperkit.core.plugin.IKotlinPlugin
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import org.bukkit.event.Event
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.callSuspend
import kotlin.reflect.full.isSuperclassOf

internal class KotlinEventExecutor(
    private val eventClass: KClass<out Event>,
    private val function: KFunction<*>,
    private val plugin: IKotlinPlugin,
) : EventExecutor {

    override fun execute(listener: Listener, event: Event) {
        if (!event::class.isSuperclassOf(eventClass)) return
        plugin.scope.launch(start = CoroutineStart.UNDISPATCHED) {
            runCatching {
                if (function.isSuspend) {
                    function.callSuspend(listener, event)
                } else {
                    function.call(listener, event)
                }
            }.onFailure {
                plugin.log.warn(it) { "$function has thrown an exception while processing $event." }
            }
        }
    }
}
