package com.test4x.penknife

import io.netty.handler.codec.http.HttpMethod
import java.util.*

class Route(val method: HttpMethod, rawPath: String, val action: Action) {

    val path: String

    init {
        this.path = rawPath.fixPath()
    }


    fun match(method: HttpMethod, url: String): MatchResult {
        if (method != this.method) {
            return MatchResult()
        }
        val paths = path.split("/")
        val urls = url.split("/")
        if (urls.size != paths.size) {
            return MatchResult()
        } else {
            val pathArgMap = HashMap<String, String>()
            var index = 0
            do {
                val p = paths[index]
                val u = urls[index]
                if (p.startsWith(":")) {
                    pathArgMap[p] = u
                } else {
                    if (p != u) {
                        return MatchResult()
                    }
                }
                index++
            } while (index < paths.size)
            return MatchResult(pathArgMap, action)
        }
    }

    companion object {
        val discard = Action { req, res -> }
    }


    data class MatchResult(val result: Boolean = false, val pathArgMap: Map<String, String> = emptyMap(), val action: Action = discard) {
        constructor(pathArgMap: Map<String, String>, action: Action) : this(true, pathArgMap, action)
    }

    private fun String.fixPath(): String {
        return if (this.startsWith("/")) {
            this
        } else {
            "/$this"
        }
    }
}
