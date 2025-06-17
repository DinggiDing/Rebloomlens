package com.hdil.voice_input.llm

import android.util.Log
import com.aallam.openai.api.chat.*
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.hdil.voice_input.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.add
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import org.json.JSONObject
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class OpenAIParser {
    private var apiKey: String = ""
    private var client: OpenAI? = null

    fun initialize() {
        apiKey = BuildConfig.OPEN_API_KEY
        Log.d("OpenAIParser", "Initializing OpenAI client with key: $apiKey")
        client = OpenAI(apiKey)
    }

    /**
     * rawText 에서 날짜, 식사 종류, 음식 리스트(음식명, 수량, 단위)를 추출하여
     * 다음 JSON 스키마로 반환합니다:
     * {
     *   "date": "YYYY-MM-DD",
     *   "meals": [
     *     {
     *       "mealType": "breakfast|lunch|dinner|snack",
     *       "items": [
     *         { "food": "사과", "quantity": 1, "unit": "개" },
     *         ...
     *       ]
     *     },
     *     ...
     *   ]
     * }
     */
    suspend fun parseFoodIntake(rawText: String): JSONObject? = withContext(Dispatchers.IO) {
        try {
            if (client == null) {
                Log.e("OpenAIParser", "OpenAI client not initialized.")
                return@withContext null
            }

// 시스템 런타임 기준 오늘 날짜 계산 (예: "2025-06-17")
            val today: String = LocalDate.now().format(DateTimeFormatter.ISO_DATE)

            val systemPrompt = """
                당신은 음식 섭취 기록을 파싱하여 **오직** JSON 객체만 출력하는 생성기입니다.  
                사용자 입력으로부터 다음 스키마에 엄격히 맞는 JSON을 반환해야 하며, 다른 설명이나 텍스트는 일절 포함하지 마세요.
            
                스키마:
                {
                  "dateTime": "YYYY-MM-DDTHH:mm:ss",      // 날짜와 시간(문장 내 시간 없으면 ${today}T00:00:00)
                  "meals": [
                    {
                      "mealType": "breakfast|lunch|dinner|snack",
                      "items": [
                        {
                          "food": "string",              
                          "quantity": number,            
                          "unit": "string"               
                        }
                      ]
                    }
                    // 여러 식사가 입력되면 배열에 추가
                  ]
                }
            
                지침:
                1. **dateTime**  
                   - ISO 8601 형식(`YYYY-MM-DDTHH:mm:ss`)으로.  
                   - “2025년 6월 16일 오후 1시 30분” → `2025-06-16T13:30:00`  
                   - “6/16 8시” → `${today}T08:00:00`  
                   - 시간 언급이 전혀 없으면 `${today}T00:00:00`
                2. **mealType** 매핑  
                   - “아침”→`breakfast`, “점심”→`lunch`, “저녁”→`dinner`, “간식”→`snack`  
                3. **items**  
                   - `quantity`: 한글 숫자, 소수, 분수(“반”→0.5) 인식 후 숫자형  
                   - `unit`: 표준 한국어 단위(“개”, “그릇”, “컵” 등)  
                   - 수량 언급 없으면 `quantity: 1`, 단위 없으면 `unit: "개"`  
                4. 문장에 여러 식사가 나오면 순서대로 `meals` 배열에 추가  
                5. 출력 예시 (오직 JSON):
                ```json
                {
                  "dateTime": "${today}T19:45:00",
                  "meals": [
                    {
                      "mealType": "dinner",
                      "items": [
                        { "food": "사과", "quantity": 2, "unit": "개" },
                        { "food": "치킨", "quantity": 1, "unit": "그릇" }
                      ]
                    }
                  ]
                }
                ```
            """.trimIndent()

            val chatMessages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = systemPrompt
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = rawText
                )
            )

            // Define the parsing function schema
            val request = chatCompletionRequest {
                model = ModelId("gpt-4.1-nano")
                messages = chatMessages
                tools {
                    function(
                        name = "parse_food_intake",
                        description = "날짜와 식사 정보를 추출합니다."
                    ) {
                        put("type", "object")
                        putJsonObject("properties") {
                            putJsonObject("date") {
                                put("type", "string")
                                put("description", "YYYY-MM-DD 형식의 날짜")
                            }
                            putJsonObject("meals") {
                                put("type", "array")
                                putJsonObject("items") {
                                    put("type", "object")
                                    putJsonObject("properties") {
                                        putJsonObject("mealType") {
                                            put("type", "string")
                                            put(
                                                "description",
                                                "식사 종류 (breakfast, lunch, dinner, snack)"
                                            )
                                        }
                                        putJsonObject("items") {
                                            put("type", "array")
                                            putJsonObject("items") {
                                                put("type", "object")
                                                putJsonObject("properties") {
                                                    putJsonObject("food") {
                                                        put("type", "string")
                                                        put("description", "음식 이름")
                                                    }
                                                    putJsonObject("quantity") {
                                                        put("type", "number")
                                                        put("description", "음식 개수(소수 가능)")
                                                    }
                                                    putJsonObject("unit") {
                                                        put("type", "string")
                                                        put("description", "수량 단위")
                                                    }
                                                }
                                                putJsonArray("required") {
                                                    add("food")
                                                    add("quantity")
                                                }
                                            }
                                        }
                                    }
                                    putJsonArray("required") {
                                        add("mealType")
                                        add("items")
                                    }
                                }
                            }
                        }
                        putJsonArray("required") {
                            add("date")
                            add("meals")
                        }
                    }
                }
                toolChoice = ToolChoice.Auto
            }

            val response = client!!.chatCompletion(request)
            val message = response.choices.firstOrNull()?.message
            val toolCalls = message?.toolCalls.orEmpty()

            // 결과 처리를 위한 변수
            var resultObj: JSONObject? = null

            // 여러 Function 호출 결과 처리
            for (call in toolCalls) {
                if (call is ToolCall.Function) {  // FunctionCall 대신 ToolCall.Function 사용
                    val jsonArgs = call.function.argumentsAsJson()
                    val callResult = JSONObject(jsonArgs.toString())

                    if (resultObj == null) {
                        resultObj = callResult
                    } else {
                        // 두 번째 이상 함수 호출 결과를 첫 번째와 병합
                        val mealsList = resultObj.getJSONArray("meals")
                        val newMealsList = callResult.getJSONArray("meals")

                        // 새 식사 항목을 기존 meals 배열에 추가
                        for (i in 0 until newMealsList.length()) {
                            mealsList.put(newMealsList.get(i))
                        }
                    }
                }
            }

            if (resultObj == null) {
                Log.e("OpenAIParser", "도구 호출 결과를 찾을 수 없습니다")
            }

            return@withContext resultObj
        } catch (e: Exception) {
            Log.e("OpenAIParser", "Parsing error: ${e.message}")
            e.printStackTrace()
            return@withContext null
        }
    }
}