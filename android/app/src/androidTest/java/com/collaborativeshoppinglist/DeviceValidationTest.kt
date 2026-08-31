package com.collaborativeshoppinglist

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DeviceValidationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun memberRegister() {
        register("Bruno", MEMBER_EMAIL)
        waitForText("Minhas listas")
        waitForText("Nenhuma lista criada ou compartilhada.")
    }

    @Test
    fun ownerCreatesListItemsAndInvitation() {
        register("Ana", OWNER_EMAIL)
        waitForText("Minhas listas")

        inputField(0).performTextInput("Compra da semana")
        click("Criar")
        waitForText("Compra da semana")

        inputField(0).performTextInput("Leite")
        click("Adicionar")
        waitForText("Leite")
        composeRule.onNodeWithContentDescription("Diminuir quantidade de Leite")
            .assertIsNotEnabled()

        inputField(0).performTextInput(" leite ")
        click("Adicionar")
        composeRule.waitUntil(20_000) {
            runCatching {
                composeRule.onNodeWithContentDescription("Diminuir quantidade de Leite")
                    .assertIsEnabled()
                true
            }.getOrDefault(false)
        }

        click("Convidar")
        waitForText("Compartilhar lista")
        click("Gerar código")
        waitForText("Código do convite")
        click("Voltar")
        waitForText("Participantes (1)")
    }

    @Test
    fun memberAcceptsAndMarksItem() {
        waitForText("Minhas listas")
        click("Entrar com código")
        waitForText("Código do convite", 40_000)
        waitForText("Leite", 40_000)
        waitForText("Participantes (2)")

        composeRule.onNodeWithContentDescription("Marcar Leite como colocado no carrinho")
            .performClick()
        waitForText("Marcado por Bruno")
        composeRule.onNodeWithContentDescription("Aumentar quantidade de Leite")
            .performClick()
    }

    @Test
    fun ownerObservesMemberAndClosesList() {
        waitForText("Compra da semana")
        waitForText("Participantes (2)", 40_000)
        waitForText("Marcado por Bruno", 40_000)
        click("Encerrar lista")
        waitForText("Lista encerrada — somente leitura.", 40_000)
        composeRule.onNodeWithText("Adicionar item").assertDoesNotExist()
    }

    @Test
    fun memberObservesClosedReadOnlyList() {
        waitForText("Compra da semana")
        waitForText("Lista encerrada — somente leitura.", 40_000)
        composeRule.onNodeWithContentDescription("Aumentar quantidade de Leite")
            .assertIsNotEnabled()
        composeRule.onNodeWithText("Adicionar item").assertDoesNotExist()
    }

    private fun register(name: String, email: String) {
        waitForText("Entrar")
        click("Criar uma conta")
        waitForText("Nome")
        inputField(0).performTextInput(name)
        inputField(1).performTextInput(email)
        inputField(2).performTextInput(PASSWORD)
        click("Criar conta")
    }

    private fun inputField(index: Int): SemanticsNodeInteraction =
        composeRule.onAllNodes(hasSetTextAction())[index].also {
            it.performTextClearance()
        }

    private fun click(text: String) {
        composeRule.onNode(hasText(text) and hasClickAction()).performClick()
    }

    private fun waitForText(text: String, timeoutMillis: Long = 30_000) {
        composeRule.waitUntil(timeoutMillis) {
            composeRule.onAllNodesWithText(text).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private companion object {
        const val OWNER_EMAIL = "ana.devices@example.test"
        const val MEMBER_EMAIL = "bruno.devices@example.test"
        const val PASSWORD = "senha123"
    }
}
