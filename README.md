# sudokuPGame

오프라인 무료 스도쿠 안드로이드 앱. 개발 계획은 [PLAN.md](PLAN.md) 참고.

## 모듈

| 모듈 | 설명 |
|---|---|
| `app` | 안드로이드 앱 (Jetpack Compose, Hilt) |
| `engine` | 순수 Kotlin 스도쿠 엔진: 솔버, 생성기, 난이도 평가 |
| `puzzle-generator` | 개발용 CLI. 내장 퍼즐 `app/src/main/assets/puzzles.json` 생성 |

## 요구 사항

- JDK 17 이상 (Android Studio 내장 JDK 사용 가능)
- Android SDK Platform 37

## 명령어

```bash
./gradlew assembleDebug          # 디버그 APK 빌드
./gradlew installDebug           # 연결된 기기/에뮬레이터에 설치
./gradlew :engine:test           # 엔진 단위 테스트
./gradlew testDebugUnitTest      # 앱 단위 테스트
./gradlew :puzzle-generator:run  # 퍼즐 생성
./gradlew bundleRelease          # 스토어 업로드용 AAB (서명: keystore.properties)
```

## 문서

- [PLAN.md](PLAN.md) — 개발 플랜
- [docs/RELEASE.md](docs/RELEASE.md) — Play Console 출시 절차
- [docs/store/listing.md](docs/store/listing.md) — 스토어 등록 문구, 앱 콘텐츠 설문 답변
- [docs/privacy-policy.md](docs/privacy-policy.md) — 개인정보처리방침
