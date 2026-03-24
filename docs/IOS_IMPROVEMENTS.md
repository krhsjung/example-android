# iOS 개선사항 Android 적용 가이드

iOS(example-ios)에서 더 잘 구현된 부분 중 Android에 적용할 수 있는 개선사항을 정리합니다.

---

## 적용 완료

### 1. RetryInterceptor Jitter 추가

**문제**: 기존 RetryInterceptor는 고정 지수 백오프(1초, 2초, 4초)만 사용합니다.
여러 클라이언트가 동시에 재시도하면 **Thundering Herd** 문제가 발생할 수 있습니다.

**iOS 구현**: 지수 백오프 + Random Jitter를 적용하여 재시도 타이밍을 분산합니다.

**Android 적용**:
- `RetryInterceptor.calculateDelay()`에 `kotlin.random.Random`을 사용한 jitter 추가
- Jitter 범위: 기본 딜레이의 0~50%
- 예시: 기존 2초 → 2.0초 ~ 3.0초 사이 랜덤 딜레이
- `NetworkConstants`에 `JITTER_FACTOR` 상수 추가

**변경 파일**:
- `data/remote/RetryInterceptor.kt`
- `data/remote/NetworkConstants.kt`

---

### 2. 네트워크 로그 민감 정보 마스킹

**문제**: Debug 빌드에서 `HttpLoggingInterceptor`가 BODY 레벨로 동작하여 password, token 등 민감 정보가 로그에 노출됩니다.

**iOS 구현**: 네트워크 로그에서 민감한 필드(password, token, cookie)를 마스킹 처리합니다.

**Android 적용**:
- `SanitizingLoggingInterceptor` 생성: 민감 필드를 `***` 로 치환
- 마스킹 대상 헤더: `Authorization`, `Cookie`, `Set-Cookie`
- 마스킹 대상 Body 필드: `password`, `token`, `refreshToken`, `accessToken`, `secret`
- JSON Body에서 정규식으로 값을 마스킹
- Release 빌드에서는 로깅 자체가 비활성화 (기존과 동일)

**변경 파일**:
- `data/remote/SanitizingLoggingInterceptor.kt` (신규)
- `di/NetworkModule.kt`

---

### 3. EncryptedSharedPreferences 쿠키 보안 강화

**문제**: `PersistentCookieJar`가 Proto DataStore에 쿠키를 평문으로 저장합니다.

**iOS 구현**: Keychain + SecureCookieStorage로 암호화 저장합니다.

**Android 적용**:
- `EncryptedCookieStorage` 생성: `EncryptedSharedPreferences` 기반 암호화 쿠키 저장소
- AES-256-GCM 키/값 암호화 (MasterKey 자동 관리)
- `PersistentCookieJar`의 디스크 저장 계층을 DataStore → EncryptedCookieStorage로 교체
- `initialize()`/`clear()`가 `suspend` → 일반 함수로 변경 (SharedPreferences는 동기식)
- 기존 `CookieStoreSerializer`, `cookieDataStore` 확장 프로퍼티 제거
- `CookieProto`, `CookieStore` 데이터 모델은 유지

**변경 파일**:
- `data/local/datastore/EncryptedCookieStorage.kt` (신규)
- `data/local/datastore/CookieDataStore.kt` (Serializer/확장 프로퍼티 제거)
- `data/remote/PersistentCookieJar.kt` (EncryptedCookieStorage 주입)
- `gradle/libs.versions.toml` (security-crypto 추가)
- `app/build.gradle.kts` (의존성 추가)

---

### 4. SSL Pinning

**문제**: 기본 OkHttp SSL 처리만 사용하여 MITM 공격에 취약합니다.

**iOS 구현**: `SSLPinningDelegate`에서 공개 키 해시를 검증합니다. 핀이 비어있으면 기본 SSL 처리로 동작합니다.

**Android 적용**:
- `SSLPinningConfig` 생성: OkHttp `CertificatePinner` 설정 관리
- iOS와 동일하게 핀이 비어있으면 `CertificatePinner.DEFAULT` 반환 (기본 SSL 검증)
- 핀이 설정되면 공개 키 SHA-256 해시 검증 활성화
- `NetworkModule`의 OkHttpClient에 `certificatePinner` 추가
- 실제 해시는 TODO로 남겨둠 (서버 배포 환경 확정 후 추가)

**변경 파일**:
- `data/remote/SSLPinningConfig.kt` (신규)
- `di/NetworkModule.kt`

**해시 생성 방법**:
```bash
openssl s_client -connect HOST:443 2>/dev/null | \
  openssl x509 -pubkey -noout | \
  openssl pkey -pubin -outform DER | \
  openssl dgst -sha256 -binary | base64
```
