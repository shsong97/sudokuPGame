# 출시 가이드 (Play Console 내부 테스트)

## 1. 업로드 키 만들기 (최초 1회)

```bash
keytool -genkeypair -v -keystore release.jks -keyalg RSA -keysize 4096 -validity 10000 -alias sudokupgame
cp keystore.properties.example keystore.properties   # 비밀번호 등 입력
```

- `release.jks`와 `keystore.properties`는 git에 올라가지 않습니다 (`.gitignore`).
- **키 파일과 비밀번호는 안전한 곳에 따로 백업하세요.** Play 앱 서명을 쓰면 업로드 키를 잃어버려도 재설정할 수 있지만 절차가 번거롭습니다.

## 2. 번들 빌드

```bash
./gradlew bundleRelease
# 결과: app/build/outputs/bundle/release/app-release.aab
```

`keystore.properties`가 없으면 디버그 키로 서명됩니다. 이 파일은 Play Console이 거부하므로, 업로드 전에 1단계를 꼭 마치세요.
새 버전을 올릴 때마다 `app/build.gradle.kts`의 `versionCode`를 1씩 올립니다.

## 3. Play Console

1. [Play Console](https://play.google.com/console)에서 개발자 계정 등록 (최초 1회, 등록비 있음)
2. **앱 만들기**: 이름 `sudokuPGame`, 기본 언어 한국어, 게임, 무료
3. **테스트 → 내부 테스트 → 새 버전 만들기**
   - Play 앱 서명 사용 (권장, 기본값)
   - `app-release.aab` 업로드
4. **앱 콘텐츠** 설문 작성: `docs/store/listing.md`의 답변 가이드 참고
5. **스토어 등록정보** 작성: `docs/store/listing.md`와 `docs/store/` 이미지 사용
6. **개인정보처리방침 URL**: `docs/privacy-policy.md`를 공개 웹 주소로 올려 입력
   - 저장소가 공개라면 GitHub 파일 주소를 그대로 쓸 수 있습니다:
     `https://github.com/shsong97/sudokuPGame/blob/main/docs/privacy-policy.md`
7. **테스터** 목록에 이메일 추가 → 테스트 링크 공유

## 4. 출시 전 확인 목록

- [ ] `./gradlew assembleDebug :engine:test :puzzle-generator:test testDebugUnitTest lintDebug` 통과
- [ ] 릴리스 APK를 실제 기기에 설치해 새 게임·이어하기·힌트·통계·설정 확인
- [ ] `versionCode` / `versionName` 확인
- [ ] 개인정보처리방침·스토어 등록정보의 연락처 이메일 입력
