package com.phoneuse.agent

import com.phoneuse.risk.RiskCategory

/**
 * Every classified high-impact action requires an explicit, per-action confirmation.
 *
 * The legacy "confirm send" preference is retained in the constructor for source compatibility, but it cannot
 * weaken this safety boundary. User intent, model declarations and routine metadata are not substitutes for the
 * user's final confirmation at the moment the action is about to occur.
 */
object ConfirmPolicy {
    /** No risk category is eligible for confirmation bypass. */
    val SWITCHABLE: Set<RiskCategory> = emptySet()

    /** Whether [category] asks the user. The legacy preference is intentionally ignored. */
    @Suppress("UNUSED_PARAMETER")
    fun asks(category: RiskCategory, confirmSend: Boolean): Boolean = true
}

/**
 * A strict confirmation gate. Claims such as user_authorized and the legacy setting cannot bypass a risk card.
 */
class ConfirmPolicyGate(
    private val inner: UserGate,
    @Suppress("UNUSED_PARAMETER") private val claimed: Boolean = false,
    @Suppress("UNUSED_PARAMETER") private val confirmSend: () -> Boolean,
) : UserGate {
    /** Kept for callers that scope a gate to a particular claim; claims no longer bypass confirmation. */
    fun forClaim(userAuthorized: Boolean): ConfirmPolicyGate =
        if (userAuthorized == claimed) this else ConfirmPolicyGate(inner, userAuthorized, confirmSend)

    override suspend fun confirm(category: RiskCategory, summary: String): Boolean =
        inner.confirm(category, summary)

    override suspend fun ask(question: String, options: List<String>): String = inner.ask(question, options)

    override suspend fun handToUser(reason: String) = inner.handToUser(reason)
}
