package dev.viesoft.paperkit.core.plugin

import dev.viesoft.paperkit.core.coroutines.AsyncPluginDispatcher
import dev.viesoft.paperkit.core.coroutines.PluginCoroutineController
import dev.viesoft.paperkit.core.coroutines.PluginDispatcher
import kotlinx.coroutines.*
import mu.KLogger
import mu.toKLogger
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.java.JavaPlugin
import java.util.concurrent.CancellationException
import kotlin.coroutines.CoroutineContext

@Suppress("unused")
abstract class KotlinPlugin : JavaPlugin(), IKotlinPlugin {

    private val controller by lazy { PluginCoroutineController(this) }
    private val dispatcher by lazy { PluginDispatcher(this, controller) }
    final override val context: CoroutineContext by lazy { newContext("plugin", dispatcher) }
    final override val scope: CoroutineScope by lazy { CoroutineScope(context) }
    private val asyncDispatcher by lazy { AsyncPluginDispatcher(this, controller) }
    final override val asyncContext: CoroutineContext by lazy { newContext("async", asyncDispatcher) }
    final override val asyncScope: CoroutineScope by lazy { CoroutineScope(asyncContext) }
    final override val log: KLogger = slF4JLogger.toKLogger()

    final override fun reloadConfig() {
        controller.runControlled { loadConfig() }
    }

    final override fun onLoad() {
        scope.ensureActive()
        asyncScope.ensureActive()
        runBlocking { onLoaded() }
    }

    final override fun onEnable() {
        controller.runControlled {
            loadConfig()
            onEnabled()
        }
    }

    final override fun onDisable() {
        try {
            runBlocking { onDisabled() }
        } finally {
            val cancellationException = CancellationException("Stopping the plugin")
            dispatcher.cancelChildren(cancellationException)
            asyncDispatcher.cancelChildren(cancellationException)
        }
    }

    final override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<String>
    ): Boolean {
        log.warn {
            """
                The onCommand method has been called on the $name plugin.
                The method is final in KotlinPlugin to avoid using the old fashion CommandExecutor.
                Consider using the KotlinCommand class instead.
            """.trimIndent()
        }
        return false
    }

    final override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<String>
    ): List<String>? {
        log.warn {
            """
                The onTabComplete method has been called on the $name plugin.
                The method is final in KotlinPlugin to avoid using the old fashion TabCompleter.
                Consider using the KotlinCommand class instead.
            """.trimIndent()
        }
        return null
    }

    private fun newContext(name: String, dispatcher: CoroutineDispatcher): CoroutineContext {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            log.warn(throwable) { "An exception has occurred in a coroutine within the $name context." }
        }
        return exceptionHandler + SupervisorJob() + dispatcher
    }
}
