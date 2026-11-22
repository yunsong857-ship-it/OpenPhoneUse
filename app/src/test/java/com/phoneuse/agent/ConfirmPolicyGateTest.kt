package com.phoneuse.agent

import com.phoneuse.risk.RiskCategory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class ConfirmPolicyGateTest {
    @Test
    fun everyRiskCategoryRequiresConfirmationEvenWhenLegacySwitchIsOff() = runTest {
        val categories = listOf(
            RiskCategory.MONEY,
            RiskCategory.SECURITY,
            RiskCategory.DELETE,
            RiskCategory.SEND,
            RiskCategory.CHANGE,
        )
        val inner = FakeUserGate().apply { confirmResult = false }
        val gate = ConfirmPolicyGate(inner, claimed = true, confirmSend = { false })

        for (category in categories) {
            assertFalse(gate.confirm(category, "review this action"))
        }

        assertEquals(categories, inner.confirms.map { it.first })
        assertEquals(0, inner.asks.size)
    }

    @Test
    fun userAuthorizedClaimCannotBypassConfirmation() = runTest {
        val inner = FakeUserGate().apply { confirmResult = false }
        val gate = ConfirmPolicyGate(inner, confirmSend = { false }).forClaim(userAuthorized = true)

        assertFalse(gate.confirm(RiskCategory.SEND, "send this message"))
        assertEquals(RiskCategory.SEND, inner.confirms.single().first)
    }
}
