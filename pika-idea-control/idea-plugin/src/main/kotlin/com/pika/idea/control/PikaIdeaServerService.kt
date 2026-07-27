package com.pika.idea.control

import com.intellij.openapi.Disposable
import com.intellij.openapi.diagnostic.Logger
import java.nio.charset.StandardCharsets

internal class PikaIdeaServerService : Disposable {
    private val log = Logger.getInstance(PikaIdeaServerService::class.java)
    private val pluginVersion = packagedPluginVersion()

    @Volatile
    private var httpServer: PikaIdeaHttpServer? = null

    val boundPort: Int?
        get() = httpServer?.boundPort

    @Synchronized
    fun ensureStarted() {
        if (httpServer != null) {
            return
        }

        val port = configuredPort()
        val candidate = PikaIdeaHttpServer(
            requestedPort = port,
            api = PikaIdeaApi(),
            pluginVersion = pluginVersion,
        )
        try {
            val actualPort = candidate.start()
            httpServer = candidate
            log.info(
                "Pika Control listening on " +
                    "http://${PikaIdeaHttpServer.LOOPBACK_ADDRESS}:$actualPort${PikaIdeaApi.API_BASE_PATH}",
            )
        } catch (error: Throwable) {
            candidate.stop()
            log.warn("Unable to start Pika Control on port $port", error)
        }
    }

    override fun dispose() {
        httpServer?.stop()
        httpServer = null
    }

    private fun configuredPort(): Int {
        val configured = System.getProperty(PORT_PROPERTY)
            ?: System.getenv(PORT_ENVIRONMENT_VARIABLE)
            ?: return PikaIdeaHttpServer.DEFAULT_PORT
        val port = configured.toIntOrNull()
        if (port == null || port !in 0..65535) {
            log.warn(
                "Invalid Pika Control port '$configured'; using ${PikaIdeaHttpServer.DEFAULT_PORT}",
            )
            return PikaIdeaHttpServer.DEFAULT_PORT
        }
        return port
    }

    companion object {
        // Keep stable so Marketplace upgrades replace existing manual installations in place.
        const val PLUGIN_ID = "com.pika.ideaControlMcp"
        const val PORT_PROPERTY = "pika.idea.port"
        const val PORT_ENVIRONMENT_VARIABLE = "PIKA_IDEA_PORT"

        private val VERSION_TAG = Regex("""<version>\s*([^<]+?)\s*</version>""")

        internal fun packagedPluginVersion(): String =
            PikaIdeaServerService::class.java
                .getResourceAsStream("/META-INF/plugin.xml")
                ?.bufferedReader(StandardCharsets.UTF_8)
                ?.use { reader ->
                    VERSION_TAG.find(reader.readText())?.groupValues?.get(1)
                }
                ?: "dev"
    }
}
