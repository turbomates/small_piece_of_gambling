package io.betforge.player.model.verification

import com.google.inject.Inject
import io.betforge.player.infrasturcture.exceptions.NodeException
import io.betforge.player.model.Status

class RulesEngine @Inject constructor(private val loader: RulesLoader, private val factory: Factory) {
    suspend fun init(info: VerificationInfo) {
        types().forEach {
            factory.getVerifier(it).init(info)
        }
    }

    fun types(): Set<String> {
        return loader.rules().flatMap {
            RuleTree.parse(it.conditions).types()
        }.toSet()
    }

    fun statusFor(checks: Map<String, Boolean>): Status? {
        var status: Status? = null

        loader.rules().map {
            val tree = RuleTree.parse(it.conditions)
            if (tree.check(checks)) {
                status = it.status
            }
        }
        return status
    }
}

internal class RuleTree(private var root: Node? = null) {
    fun types(): List<String> {
        return root?.let { nodeRules(it) }.orEmpty()
    }

    fun check(checks: Map<String, Boolean>): Boolean {
        return root != null && root!!.check(checks)
    }

    private fun nodeRules(node: Node): List<String> {
        val list = mutableListOf<String>()
        if (node.kind == Kind.VALIDATION) {
            list.add(node.data)
        } else {
            node.right?.let { list.addAll(nodeRules(it)) }
            node.left?.let { list.addAll(nodeRules(it)) }
        }
        return list.distinct()
    }

    companion object {
        internal fun parse(conditions: String): RuleTree {
            val matches = Regex("(?<validation>[^&|\\|]+)|(?<operation>[.&|\\|]+)").findAll(conditions)
            var root: Node? = null
            matches.forEachIndexed { _, matchResult ->
                val data = matchResult.value.trim()
                data.run {
                    val node = Node(this)
                    if (root == null) {
                        root = node
                    } else {
                        when (node.kind) {
                            Kind.OPERATOR -> {
                                node.right = root
                                root = node
                            }
                            Kind.VALIDATION -> root!!.left = node
                        }
                    }
                }
            }
            return RuleTree(root)
        }
    }

    class Node(val data: String) {
        var left: Node? = null
        var right: Node? = null
        var kind: Kind

        init {
            kind = Kind.VALIDATION
            if (data == "&" || data == "|") {
                kind = Kind.OPERATOR
            }
        }

        fun check(checks: Map<String, Boolean>): Boolean {
            if (kind == Kind.VALIDATION) {
                return checks.containsKey(data) && checks[data] == true
            }
            if (left == null || right == null) {
                throw NodeException("Left and right nodes are requred for OPERATOR kind")
            }
            if (data == "&") {
                return left!!.check(checks) && right!!.check(checks)
            }
            if (data == "|") {
                return left!!.check(checks) || right!!.check(checks)
            }
            throw NodeException("Should not rich here")
        }
    }

    enum class Kind {
        OPERATOR, VALIDATION
    }
}
