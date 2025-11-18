package com.teka.aicalculatorappwithkoog.agent

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.AIAgentEdgeBuilderIntermediate
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.*
import ai.koog.agents.core.environment.ReceivedToolResult
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.features.eventHandler.feature.handleEvents
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.llms.all.simpleGoogleAIExecutor
import com.teka.aicalculatorappwithkoog.BuildConfig
import com.teka.aicalculatorappwithkoog.agent.common.AgentProvider
import com.teka.aicalculatorappwithkoog.agent.common.ExitTool


/**
 * Factory for creating calculator agents
 */
object CalculatorAgentProvider : AgentProvider {
    override val title: String = "Calculator"
    override val description: String = "Hi, I'm a calculator agent, I can do math"

    override suspend fun provideAgent(
        onToolCallEvent: suspend (String) -> Unit,
        onErrorEvent: suspend (String) -> Unit,
        onAssistantMessage: suspend (String) -> String,
    ): AIAgent<String, String> {
        val executor = simpleGoogleAIExecutor(BuildConfig.GEMINI_KEY)


        // Create tool registry with calculator tools
        val toolRegistry = ToolRegistry {
            tool(CalculatorTools.PlusTool)
            tool(CalculatorTools.MinusTool)
            tool(CalculatorTools.DivideTool)
            tool(CalculatorTools.MultiplyTool)

            tool(ExitTool)
        }

        @Suppress("DuplicatedCode")
        val strategy = strategy<String, String>(title) {
            val nodeRequestLLM by nodeLLMRequestMultiple()
            val nodeAssistantMessage by node<String, String> { message -> onAssistantMessage(message) }
            val nodeExecuteToolMultiple by nodeExecuteMultipleTools(parallelTools = true)
            val nodeSendToolResultMultiple by nodeLLMSendMultipleToolResults()
            val nodeCompressHistory by nodeLLMCompressHistory<List<ReceivedToolResult>>()

            edge(nodeStart forwardTo nodeRequestLLM)

            edge(
                nodeRequestLLM forwardTo nodeExecuteToolMultiple
                    onMultipleToolCalls { true }
            )

            edge(
                nodeRequestLLM forwardTo nodeAssistantMessage
                    transformed { it.first() }
                    onAssistantMessage { true }
            )

            edge(nodeAssistantMessage forwardTo nodeRequestLLM)



            val edgeToFinish: AIAgentEdgeBuilderIntermediate<List<ReceivedToolResult>, String, String> =
                (nodeExecuteToolMultiple forwardTo nodeFinish)
                    .onCondition { it.singleOrNull()?.tool == ExitTool.name }
                    .transformed { it.single().result!! as String }

            edge(edgeToFinish)


            edge(
                (nodeExecuteToolMultiple forwardTo nodeCompressHistory)
                    onCondition { _ -> llm.readSession { prompt.messages.size > 100 } }
            )

            edge(nodeCompressHistory forwardTo nodeSendToolResultMultiple)

            edge(
                (nodeExecuteToolMultiple forwardTo nodeSendToolResultMultiple)
                    onCondition { _ -> llm.readSession { prompt.messages.size <= 100 } }
            )

            edge(
                (nodeSendToolResultMultiple forwardTo nodeExecuteToolMultiple)
                    onMultipleToolCalls { true }
            )

            edge(
                nodeSendToolResultMultiple forwardTo nodeAssistantMessage
                    transformed { it.first() }
                    onAssistantMessage { true }
            )

        }

        // Create agent config with proper prompt
        val agentConfig = AIAgentConfig(
            prompt = prompt("test") {
                system(
                    """
                    You are a calculator.
                    You will be provided math problems by the user.
                    Use tools at your disposal to solve them.
                    Provide the answer and ask for the next problem until the user asks to stop.
                    """.trimIndent()
                )
            },
//            model = OpenAIModels.Chat.GPT4o,
            model = GoogleModels.Gemini2_0Flash,
            maxAgentIterations = 50
        )

        // Return the agent
        return AIAgent(
            promptExecutor = executor,
            strategy = strategy,
            agentConfig = agentConfig,
            toolRegistry = toolRegistry,
        ) {
            handleEvents {
                onToolCall { ctx ->
                    onToolCallEvent("Tool ${ctx.tool.name}, args ${ctx.toolArgs}")
                }

                onAgentRunError { ctx ->
                    onErrorEvent("${ctx.throwable.message}")
                }

                onAgentFinished { ctx ->
                    // Skip finish event handling
                }
            }
        }
    }
}
