package com.sudokupgame.generator

import com.sudokupgame.engine.Difficulty

/**
 * 개발용 퍼즐 생성 CLI. 생성 결과는 app/src/main/assets/puzzles.json 에 저장한다.
 * 실행: ./gradlew :puzzle-generator:run
 *
 * TODO(1단계): 엔진의 생성기·난이도 평가를 연결해 난이도별 25개씩 생성.
 */
fun main() {
    println("puzzle-generator: 난이도 ${Difficulty.entries.joinToString()} — 생성기는 1단계에서 구현 예정")
}
