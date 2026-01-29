# ============================================================================
# ProGuard / R8 규칙
# ============================================================================
# 이 파일은 Release 빌드 시 코드 난독화 및 축소를 위한 규칙을 정의합니다.
# build.gradle.kts의 proguardFiles 설정에서 참조됩니다.
#
# 참고: https://developer.android.com/guide/developing/tools/proguard.html

# ============================================================================
# 디버깅 지원
# ============================================================================
# 크래시 리포트에서 스택 트레이스의 라인 번호 정보를 유지합니다.
# 이를 통해 난독화된 앱에서도 정확한 오류 위치를 파악할 수 있습니다.
-keepattributes SourceFile,LineNumberTable

# 소스 파일명을 'SourceFile'로 대체하여 실제 파일명을 숨깁니다.
-renamesourcefileattribute SourceFile

# ============================================================================
# Kotlin
# ============================================================================
# Kotlin 리플렉션에 필요한 메타데이터를 유지합니다.
# 런타임에 클래스 정보를 조회하는 기능에 필수적입니다.
-keep class kotlin.Metadata { *; }
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# ============================================================================
# Retrofit
# ============================================================================
# Retrofit은 제네릭 파라미터에 리플렉션을 사용합니다.
# Signature: 제네릭 타입 정보 유지 (API 응답 파싱에 필수)
# InnerClasses, EnclosingMethod: 내부 클래스 구조 정보 유지
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit은 메서드와 파라미터 어노테이션에 리플렉션을 사용합니다.
# @GET, @POST, @Query 등의 어노테이션 정보를 유지합니다.
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# 어노테이션 기본값을 유지합니다. (예: @Field.encoded의 기본값)
-keepattributes AnnotationDefault

# API 인터페이스의 메서드 파라미터를 최적화 시에도 유지합니다.
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# 빌드 도구용 어노테이션 경고를 무시합니다.
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

# JSR 305 어노테이션(@Nullable, @NonNull 등) 경고를 무시합니다.
-dontwarn javax.annotation.**

# Kotlin Unit 타입 경고를 무시합니다.
-dontwarn kotlin.Unit

# Kotlin 전용 확장 함수 경고를 무시합니다.
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*

# R8 full mode에서 Retrofit 인터페이스를 유지합니다.
# Proxy로 생성되는 인터페이스의 타입 정보가 필요합니다.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>

# 상속된 API 인터페이스도 유지합니다.
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface * extends <1>

# Retrofit Response 클래스의 제네릭 시그니처를 유지합니다.
# Gson 컨버터가 응답을 역직렬화할 때 필요합니다.
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# ============================================================================
# OkHttp
# ============================================================================
# PublicSuffixDatabase 리소스 파일 경로를 유지합니다.
# 쿠키 도메인 검증에 사용됩니다.
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz

# Java 버전 호환성 검사 도구 경고를 무시합니다.
-dontwarn org.codehaus.mojo.animal_sniffer.*

# OkHttp 플랫폼별 보안 제공자 경고를 무시합니다.
# JVM에서만 사용되거나 선택적으로 사용되는 클래스들입니다.
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# ============================================================================
# Gson
# ============================================================================
# Gson은 필드 작업 시 제네릭 타입 정보를 사용합니다.
# ProGuard는 기본적으로 이 정보를 제거하므로 유지하도록 설정합니다.
-keepattributes Signature

# @Expose 등 Gson 어노테이션을 유지합니다.
-keepattributes *Annotation*

# sun.misc 패키지 경고를 무시합니다.
-dontwarn sun.misc.**

# TypeAdapter, JsonSerializer, JsonDeserializer 구현체를 유지합니다.
# @JsonAdapter에서 참조될 수 있으므로 인터페이스 정보가 필요합니다.
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# @SerializedName이 붙은 필드를 유지합니다.
# R8이 이 필드들을 null로 두는 것을 방지합니다.
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# TypeToken과 그 서브클래스의 제네릭 시그니처를 유지합니다.
# List<User>, Map<String, Object> 등의 타입 정보를 런타임에 사용합니다.
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# ============================================================================
# 애플리케이션 DTO 클래스
# ============================================================================
# API 요청/응답에 사용되는 DTO 클래스를 유지합니다.
# Gson 직렬화/역직렬화 시 필드명과 타입 정보가 필요합니다.
-keep class kr.hs.jung.example.data.remote.dto.** { *; }

# 도메인 모델 클래스를 유지합니다.
# DTO에서 변환되거나 직렬화될 수 있습니다.
-keep class kr.hs.jung.example.domain.model.** { *; }

# ============================================================================
# Hilt (의존성 주입)
# ============================================================================
# Hilt가 생성하는 컴포넌트 클래스들을 유지합니다.
# 런타임에 리플렉션으로 접근합니다.
-keep class **_HiltModules* { *; }
-keep class **_HiltComponents* { *; }
-keep class **_GeneratedInjector { *; }

# @Inject 어노테이션이 붙은 생성자를 유지합니다.
# Hilt가 의존성 주입 시 이 생성자를 호출합니다.
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

# @HiltViewModel 어노테이션이 붙은 ViewModel 클래스를 유지합니다.
# Hilt가 ViewModel 생성 시 이 클래스를 찾아 인스턴스화합니다.
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# ============================================================================
# Coroutines (비동기 처리)
# ============================================================================
# ServiceLoader를 통해 로드되는 Dispatcher 클래스명을 유지합니다.
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# volatile 필드를 유지합니다.
# AtomicFieldUpdater(AFU)가 이 필드들을 업데이트하므로 이름이 변경되면 안 됩니다.
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}