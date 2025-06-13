package com.hdil.voice_input.llm

import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.hdil.rebloomlens.common.utils.Logger
import com.hdil.voice_input.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.add
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.json.JSONObject

class OpenAIParser {
    private var apiKey: String = ""
    private var client: OpenAI? = null

    fun initialize() {
        apiKey = BuildConfig.OPEN_API_KEY
        Logger.e("[$apiKey] OpenAI 클라이언트 초기화")
        client = OpenAI(apiKey)
    }



    suspend fun parseFoodIntake(rawText: String): JSONObject? = withContext(Dispatchers.IO) {
        try {
            if (client == null) {
                Logger.e("[OpenAIParser] OpenAI 클라이언트가 초기화되지 않았습니다.")
                return@withContext null
            }

            val chatMessages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = "음식명(food)과 개수(quantity)를 추출해 JSON으로 반환해 주세요."
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = rawText
                )
            )

            // 요청 생성
            val request = chatCompletionRequest {
                model = ModelId("gpt-4.1-nano")
                messages = chatMessages
                tools {
                    function(
                        name = "parse_food_intake",
                        description = "먹은 음식과 개수를 반환합니다."
                    ) {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("food") {
                                put("type", "string")
                                put("description", "음식 이름")
                            }
                            putJsonObject("quantity") {
                                put("type", "integer")
                                put("description", "음식 개수")
                            }
                        }
                        putJsonArray("required") {
                            add("food")
                            add("quantity")
                        }
                    }
                }
                toolChoice = ToolChoice.Auto
            }

            // 응답 처리
            val response = client!!.chatCompletion(request)
            val message = response.choices.firstOrNull()?.message

            // 툴 호출 결과 추출
            val toolCalls = message?.toolCalls.orEmpty()
            for (toolCall in toolCalls) {
                if (toolCall is ToolCall.Function) {
                    val jsonArgs = toolCall.function.argumentsAsJson()
                    val foodStr = jsonArgs["food"]?.jsonPrimitive?.content ?: "알 수 없음"
                    val quantity = jsonArgs["quantity"]?.jsonPrimitive?.intOrNull ?: 0

                    val result = JSONObject()
                    result.put("food", foodStr)
                    result.put("quantity", quantity)
                    return@withContext result
                }
            }

            return@withContext null
        } catch (e: Exception) {
            Logger.e("[OpenAIParser] 파싱 오류: ${e.message}")
            e.printStackTrace()
            return@withContext null
        }
    }
}