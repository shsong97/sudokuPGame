package com.sudokupgame.app.data

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

/**
 * 1~9를 무엇으로 보여줄지. 퍼즐·규칙·기록은 모드와 관계없이 같다.
 * 내비게이션 인자로 쓰이므로 릴리스 빌드에서 직렬화기가 지워지지 않게 @Keep.
 */
@Keep
@Serializable
enum class GameMode { NUMBER, ANIMAL }
