# kotlinx.serialization: 네비게이션 라우트와 퍼즐 JSON 모델 보존
-keepattributes *Annotation*, InnerClasses
-keepclassmembers @kotlinx.serialization.Serializable class com.sudokupgame.app.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
