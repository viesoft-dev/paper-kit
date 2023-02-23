@file:Suppress("unused")

package dev.viesoft.paperkit.core.listener

import dev.viesoft.paperkit.core.plugin.IKotlinPlugin
import org.bukkit.Warning
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.AuthorNagException
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.RegisteredListener
import java.lang.Deprecated
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
import kotlin.reflect.jvm.isAccessible

/**
 * Kotlin implementation of the [Listener] class.
 * @see dev.viesoft.paperkit.core.listener.registerListener
 * @see dev.viesoft.paperkit.core.listener.listener
 */
interface KotlinListener : Listener {

    /**
     * Registers the [KotlinListener] to the [IKotlinPlugin].
     */
    fun register(plugin: IKotlinPlugin) = apply {
        val pluginManager = plugin.server.pluginManager
        val listeners = findEventHandlers(plugin)
        listeners.forEach { (clazz, registeredListeners) ->
            val handlerList = pluginManager.findHandlerListForEvent(clazz)
            handlerList.registerAll(registeredListeners)
        }
    }

    /**
     * Unregisters the [KotlinListener].
     */
    fun unregister() = apply {
        HandlerList.unregisterAll(this)
    }
}

private fun KotlinListener.findEventHandlers(plugin: IKotlinPlugin): Map<KClass<out Event>, MutableSet<RegisteredListener>> {
    val listeners = mutableMapOf<KClass<out Event>, MutableSet<RegisteredListener>>()

    fun processListener(function: KFunction<*>, eventClass: KClass<out Event>, eventHandler: EventHandler) {
        eventClass.logWarnIfDeprecated(function, plugin)
        val executor = KotlinEventExecutor(eventClass, function, plugin)
        val listener = RegisteredListener(this, executor, eventHandler.priority, plugin, eventHandler.ignoreCancelled)
        listeners.computeIfAbsent(eventClass) { mutableSetOf() }.add(listener)
    }

    this::class.memberFunctions.filter { !it.isExternal && !it.isInline && !it.isOperator }.forEach { function ->
        val eventHandlerAnnotation = function.findAnnotation<EventHandler>() ?: return@forEach
        val eventClass = function.singleEventParameterClass()
        if (eventClass == null) {
            logWarnInvalidListenerMethod(function, plugin)
            return@forEach
        }
        processListener(function, eventClass, eventHandlerAnnotation)
    }
    return listeners
}

@Suppress("UNCHECKED_CAST")
private fun KFunction<*>.singleEventParameterClass(): KClass<out Event>? {
    val singleParam = typeParameters.singleOrNull() ?: return null
    val singleParamType = singleParam.starProjectedType
    return if (singleParamType.isSubtypeOf(Event::class.starProjectedType)) {
        return singleParamType.classifier as? KClass<out Event>
    } else null
}

private fun logWarnInvalidListenerMethod(function: KFunction<*>, plugin: IKotlinPlugin) {
    plugin.log.warn {
        """
            Unable to register the listener function $function.
            Expected single parameter of type inherited from ${Event::class.qualifiedName}.
        """
    }
}

private fun KClass<*>.logWarnIfDeprecated(function: KFunction<*>, plugin: IKotlinPlugin) {
    if (!isSubclassOf(Event::class)) return
    if (findAnnotation<Deprecated>() == null) superclasses.forEach { it.logWarnIfDeprecated(function, plugin) }
    val warning = findAnnotation<Warning>() ?: return
    if (!plugin.server.warningState.printFor(warning)) return
    plugin.log.warn(AuthorNagException(null)) {
        """
            Registered listener for Deprecated event $simpleName on function $function.
            ${warning.reason.ifEmpty { "Server performance will be affected." }}
        """
    }
}

private fun PluginManager.findHandlerListForEvent(event: KClass<out Event>): HandlerList {
    val registrationClass = findRegistrationClass(event)
    val function = registrationClass.declaredMemberFunctions.find { it.name == "handlerList" }!!
    function.isAccessible = true
    return function.call(this, event) as HandlerList
}

@Suppress("UNCHECKED_CAST")
private fun findRegistrationClass(kClass: KClass<out Event>): KClass<out Event> {
    val handlerListFunction = kClass.declaredMemberFunctions.find { it.name == "getHandlerList" }
    if (handlerListFunction != null) return kClass
    kClass.superclasses.filter { it.isSubclassOf(Event::class) }.forEach {
        return findRegistrationClass(it as KClass<out Event>)
    }
    error("Unable to find handler list for event ${kClass.qualifiedName}. Static function getHandlerList is required!")
}
