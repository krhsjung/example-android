# ProGuard / R8 가이드

## 개요

ProGuard는 Android 앱의 코드를 **축소(shrink)**, **최적화(optimize)**, **난독화(obfuscate)** 하는 도구입니다.
Android Gradle Plugin 3.4.0부터는 ProGuard 대신 **R8**이 기본 컴파일러로 사용되며, ProGuard 규칙 파일과 호환됩니다.

## 주요 기능

| 기능 | 설명 | 효과 |
|------|------|------|
| **코드 축소 (Shrinking)** | 사용되지 않는 클래스, 메서드, 필드 제거 | APK 크기 감소 |
| **리소스 축소** | 사용되지 않는 리소스 파일 제거 | APK 크기 감소 |
| **최적화 (Optimization)** | 바이트코드 최적화, 불필요한 명령어 제거 | 실행 속도 향상 |
| **난독화 (Obfuscation)** | 클래스, 메서드, 필드명을 짧은 이름으로 변경 | 리버스 엔지니어링 방지 |

## 프로젝트 설정

### build.gradle.kts

```kotlin
android {
    buildTypes {
        release {
            // 코드 축소 및 난독화 활성화
            isMinifyEnabled = true

            // 미사용 리소스 제거 (isMinifyEnabled = true 필수)
            isShrinkResources = true

            // ProGuard 규칙 파일 지정
            proguardFiles(
                // Android SDK 기본 규칙
                getDefaultProguardFile("proguard-android-optimize.txt"),
                // 프로젝트 커스텀 규칙
                "proguard-rules.pro"
            )
        }
    }
}
```

### 기본 ProGuard 파일 종류

| 파일 | 설명 |
|------|------|
| `proguard-android.txt` | 기본 Android 규칙 (최적화 비활성화) |
| `proguard-android-optimize.txt` | 최적화가 포함된 Android 규칙 (권장) |
| `proguard-rules.pro` | 프로젝트별 커스텀 규칙 |

## 주요 규칙 문법

### Keep 규칙

```proguard
# 클래스 전체 유지
-keep class com.example.MyClass { *; }

# 클래스명만 유지 (멤버는 난독화 가능)
-keepnames class com.example.MyClass

# 특정 멤버만 유지
-keepclassmembers class com.example.MyClass {
    public <init>(...);  # 생성자
    public void myMethod(...);  # 특정 메서드
}

# 조건부 유지 (특정 어노테이션이 있는 경우)
-keep @com.example.MyAnnotation class * { *; }
```

### Keep 변형

| 규칙 | 클래스 유지 | 멤버 유지 | 설명 |
|------|:-----------:|:---------:|------|
| `-keep` | ✓ | ✓ | 클래스와 멤버 모두 유지 |
| `-keepnames` | 이름만 | 이름만 | 사용되는 경우에만 이름 유지 |
| `-keepclassmembers` | ✗ | ✓ | 멤버만 유지 |
| `-keepclasseswithmembers` | ✓ | ✓ | 특정 멤버가 있는 클래스 유지 |

### 수정자 (Modifiers)

```proguard
# 축소는 허용, 난독화는 방지
-keep,allowshrinking class com.example.MyClass { *; }

# 난독화는 허용, 축소는 방지
-keep,allowobfuscation class com.example.MyClass { *; }

# 둘 다 허용 (이름만 유지)
-keep,allowshrinking,allowobfuscation class com.example.MyClass { *; }
```

### 경고 무시

```proguard
# 특정 클래스 경고 무시
-dontwarn com.example.SomeClass

# 패키지 전체 경고 무시
-dontwarn com.example.**
```

### 속성 유지

```proguard
# 제네릭 타입 정보 유지 (Gson, Retrofit 필수)
-keepattributes Signature

# 어노테이션 유지
-keepattributes *Annotation*

# 소스 파일명, 라인 번호 유지 (크래시 리포트용)
-keepattributes SourceFile,LineNumberTable

# 내부 클래스 정보 유지
-keepattributes InnerClasses,EnclosingMethod
```

## 라이브러리별 규칙

### Retrofit

Retrofit은 리플렉션을 사용하여 API 인터페이스를 구현합니다.

```proguard
# 제네릭 타입 정보 유지
-keepattributes Signature, InnerClasses, EnclosingMethod

# 어노테이션 유지 (@GET, @POST, @Query 등)
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# API 인터페이스 메서드 유지
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Retrofit 인터페이스 유지 (R8 full mode)
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowobfuscation interface <1>
```

### Gson

Gson은 리플렉션으로 JSON을 객체로 변환합니다.

```proguard
# 제네릭 타입 정보 유지
-keepattributes Signature
-keepattributes *Annotation*

# @SerializedName 필드 유지
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# TypeToken 제네릭 시그니처 유지
-keep,allowobfuscation,allowshrinking class com.google.gson.reflect.TypeToken
-keep,allowobfuscation,allowshrinking class * extends com.google.gson.reflect.TypeToken

# TypeAdapter 구현체 유지
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
```

### OkHttp

```proguard
# 플랫폼별 클래스 경고 무시
-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# PublicSuffixDatabase 리소스 경로 유지
-adaptresourcefilenames okhttp3/internal/publicsuffix/PublicSuffixDatabase.gz
```

### Hilt

Hilt는 컴파일 타임에 코드를 생성하고 런타임에 리플렉션으로 접근합니다.

```proguard
# 생성된 컴포넌트 유지
-keep class **_HiltModules* { *; }
-keep class **_HiltComponents* { *; }
-keep class **_GeneratedInjector { *; }

# @Inject 생성자 유지
-keepclasseswithmembers class * {
    @javax.inject.Inject <init>(...);
}

# @HiltViewModel 클래스 유지
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
```

### Coroutines

```proguard
# Dispatcher 클래스명 유지
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# volatile 필드 유지 (AtomicFieldUpdater 사용)
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
```

### DTO / 데이터 클래스

API 요청/응답에 사용되는 클래스는 필드명이 JSON 키와 매핑되므로 유지해야 합니다.

```proguard
# DTO 클래스 유지
-keep class com.example.data.remote.dto.** { *; }

# 도메인 모델 유지
-keep class com.example.domain.model.** { *; }
```

## 디버깅

### 스택 트레이스 라인 번호 유지

```proguard
# 라인 번호 정보 유지 (크래시 리포트에서 위치 파악 가능)
-keepattributes SourceFile,LineNumberTable

# 소스 파일명 숨기기 (선택사항)
-renamesourcefileattribute SourceFile
```

### 매핑 파일

난독화된 앱의 크래시 리포트를 해석하려면 매핑 파일이 필요합니다.

**위치**: `app/build/outputs/mapping/release/mapping.txt`

```
# 매핑 파일 예시
com.example.MyClass -> a.a.a:
    void myMethod() -> a
    java.lang.String myField -> b
```

### 난독화된 스택 트레이스 복원

```bash
# Android SDK의 retrace 도구 사용
$ANDROID_HOME/tools/proguard/bin/retrace.sh \
    mapping.txt \
    stacktrace.txt
```

## 문제 해결

### 일반적인 오류

| 오류 | 원인 | 해결 |
|------|------|------|
| `ClassNotFoundException` | 필요한 클래스가 제거됨 | `-keep` 규칙 추가 |
| `NoSuchMethodException` | 필요한 메서드가 제거됨 | `-keepclassmembers` 규칙 추가 |
| `NoSuchFieldException` | 필요한 필드가 제거됨 | `-keepclassmembers` 규칙 추가 |
| JSON 파싱 실패 | 필드명이 난독화됨 | DTO 클래스에 `-keep` 추가 |
| 리플렉션 실패 | 타입 정보 손실 | `-keepattributes Signature` 추가 |

### 어떤 코드가 제거되었는지 확인

```bash
# usage.txt: 사용되지 않아 제거된 코드 목록
app/build/outputs/mapping/release/usage.txt

# seeds.txt: keep 규칙으로 유지된 코드 목록
app/build/outputs/mapping/release/seeds.txt
```

### R8 Full Mode

AGP 8.0+에서는 R8 Full Mode가 기본입니다. 더 공격적인 최적화를 수행합니다.

```properties
# gradle.properties에서 비활성화 (호환성 문제 발생 시)
android.enableR8.fullMode=false
```

## 참고 자료

- [Android 공식 문서 - 앱 축소, 난독화, 최적화](https://developer.android.com/build/shrink-code)
- [R8 FAQ](https://r8.googlesource.com/r8/+/refs/heads/main/compatibility-faq.md)
- [ProGuard 매뉴얼](https://www.guardsquare.com/manual/home)
