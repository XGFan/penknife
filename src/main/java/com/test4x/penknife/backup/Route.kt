//package com.test4x.penknife
//
//import java.util.*
//
//class Route(val path: String) {
//
//    init {
//        val split = path.split("/")//分隔开
//
//    }
//
//
//    companion object {
//        @JvmStatic
//        fun main(args: Array<String>) {
//            val x = "/aaa/bbb/{d}/{d}"
//            val y = "/aaa/bbb/{c}/{d}"
//            val z = "/bbb/ccc/{d}"
//
//            val tree = PlainRouteNode("")
//            tree.add(x.split("/"), "x")
//            tree.add(y.split("/"), "y")
//            tree.add(z.split("/"), "z")
//
//            val get = tree.get(x.split("/"))
//            println(get?.invoke())
//        }
//    }
//
//}
//
//enum class NodeType {
//    REGEX, PLAIN
//}
//
//
///**
// * 路由的树
// */
//interface RouteTree {
//
//    fun type(): NodeType
//
//    fun path(): String
//
//    fun result(path: String): Boolean
//
//    fun invoke(): String? //假装这就是执行部分
//
//    /**
//     * 添加
//     */
//    fun add(ps: List<String>, invoke: String)
//
//    /**
//     * 查找
//     */
//    fun get(ps: List<String>): RouteTree?
//}
//
//abstract class AbstractRouteNode(val path: String,
//                                 var invoke: String? = null) : RouteTree {
//
//
//    val plainChild = HashMap<String, RouteTree>()
//    val regexChild = LinkedList<RouteTree>()
//
//    abstract val type: NodeType
//
//    override fun add(ps: List<String>, invoke: String) {
//        if (ps.isNotEmpty()) {
//            val cur = ps[0]
//            val routeTree = if (cur.isRegexPath()) {
//                for (routeTree in this.regexChild) {
//
//
//                }
//                this.regexChild.firstOrNull {
//                    it.result(cur)
//                } ?: let {
//                    val child = PlainRouteNode(cur)
//                    this.regexChild[cur] = child
//                    child
//                }
//            } else {
//                this.plainChild[cur] ?: let {
//                    val child = PlainRouteNode(cur)
//                    this.plainChild[cur] = child
//                    child
//                }
//            }
//            routeTree.add(ps.drop(1), invoke)
//        } else {
//            this.invoke = invoke
//        }
//    }
//
//    override fun get(ps: List<String>): RouteTree? {
//        if (ps.isEmpty()) {
//            return this
//        }
//        return (plainChild[ps[0]] ?: let {
//            regexChild.firstOrNull { it.result(ps[0]) }
//        })?.get(ps.drop(1))
//    }
//
//
//    override fun type(): NodeType {
//        return type
//    }
//
//    override fun path(): String {
//        return path
//    }
//
//    override fun invoke(): String? {
//        return invoke
//    }
//
//}
//
//
//class RegexRouteNode(path: String,
//                     invoke: String? = null) : AbstractRouteNode(path, invoke) {
//    override val type: NodeType = NodeType.REGEX
//
//    val name: String
//    val regex: Regex
//
//
//    init {
//        val toPathArg = path.toPathArg()
//        name = toPathArg.first
//        regex = toPathArg.second
//    }
//
//    override fun result(path: String): Boolean {
//        return regex.matches(path)
//    }
//
//}
//
//class PlainRouteNode(path: String,
//                     invoke: String? = null) : AbstractRouteNode(path, invoke) {
//    override val type: NodeType = NodeType.PLAIN
//
//    override fun result(path: String): Boolean {
//        return this.path == path
//    }
//
//}
//
//fun String.toPathArg(): Pair<String, Regex> {
//    val inner = this.drop(1).dropLast(1)
//    val splitIndex = inner.indexOf(":")
//    return if (splitIndex != -1) {
//        Pair(inner.substring(0, splitIndex), inner.substring(splitIndex).toRegex())
//    } else {
//        Pair(inner, "\\w+".toRegex())
//    }
//}
//
//fun String.isRegexPath(): Boolean {
//    return this.startsWith("{") && this.endsWith("}")
//}
