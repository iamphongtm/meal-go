# JWT and Exception Handling Guide

Tai lieu nay mo ta cach cau hinh JWT bang RSA/JWKS va xu ly exception trong he
thong MealGo su dung Spring Boot 4, Spring Security 7 va Spring MVC.

## 1. Kien truc xac thuc

He thong co ba thanh phan lien quan den JWT:

- `identify-service`: xac thuc nguoi dung, ky access token bang RSA private key
  va cong bo RSA public key qua JWKS endpoint.
- `api-gateway`: co the xac minh access token truoc khi chuyen request den service.
- `restaurant-service`: la OAuth2 Resource Server, tu xac minh access token bang
  public key lay tu JWKS endpoint cua `identify-service`.

Luong request truc tiep den `restaurant-service`:

```text
Client
  -> Authorization: Bearer <access-token>
  -> Spring Security filter chain
  -> BearerTokenAuthenticationFilter
  -> JwtDecoder lay public key tu JWKS
  -> kiem tra chu ky, exp, issuer, audience
  -> Controller
```

Neu token sai, request bi chan trong security filter chain va khong di vao
controller. Vi vay `@RestControllerAdvice` va `GlobalExceptionHandler` khong bat
duoc loi JWT.

## 2. Tao RSA key

Access token nen duoc ky bang RSA. Private key chi duoc luu tai
`identify-service`; cac resource server chi can public key.

Tao key bang OpenSSL:

```bash
openssl genpkey -algorithm RSA -pkeyopt rsa_keygen_bits:2048 -out private.pem
openssl rsa -pubout -in private.pem -out public.pem
```

Code hien tai cua `identify-service` doc key Base64 khong co PEM header. Co the
chuyen key thanh chuoi Base64 nhu sau:

```bash
openssl pkcs8 -topk8 -nocrypt -in private.pem -outform DER | base64 -w 0
openssl pkey -pubin -in public.pem -outform DER | base64 -w 0
```

Bien moi truong:

```text
JWT_PRIVATE_KEY=<PKCS8_PRIVATE_KEY_BASE64>
JWT_PUBLIC_KEY=<X509_PUBLIC_KEY_BASE64>
JWT_ISSUER=http://localhost:30171
JWT_AUDIENCE=mealgo-api
JWT_ACCESS_TOKEN_TTL=15m
JWT_REFRESH_TOKEN_TTL=30d
```

Khong commit private key, access token, refresh token hoac secret vao Git.

## 3. Cau hinh token issuer

`identify-service/src/main/resources/application.yaml`:

```yaml
app:
  jwt:
    issuer: ${JWT_ISSUER:http://localhost:${server.port}}
    audience: ${JWT_AUDIENCE:mealgo-api}
    access-token-ttl: ${JWT_ACCESS_TOKEN_TTL:15m}
    refresh-token-ttl: ${JWT_REFRESH_TOKEN_TTL:30d}
    private-key: ${JWT_PRIVATE_KEY:}
    public-key: ${JWT_PUBLIC_KEY:}
```

Properties duoc map vao `JwtProperties`:

```java
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String issuer,
        String audience,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        String privateKey,
        String publicKey
) {
}
```

### JwtEncoder va JwtDecoder

`JwtKeyConfig` tao `JwtEncoder` tu private/public key va tao `JwtDecoder` tu
public key:

```java
@Bean
JwtEncoder jwtEncoder(RSAKey rsaKey) {
    JWKSource<SecurityContext> source =
            new ImmutableJWKSet<>(new JWKSet(rsaKey));
    return new NimbusJwtEncoder(source);
}

@Bean
JwtDecoder jwtDecoder(RSAKey rsaKey, JwtProperties properties)
        throws JOSEException {
    NimbusJwtDecoder decoder = NimbusJwtDecoder
            .withPublicKey(rsaKey.toRSAPublicKey())
            .build();

    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
            JwtValidators.createDefaultWithIssuer(properties.issuer()),
            new JwtClaimValidator<List<String>>(
                    "aud",
                    audience -> audience != null
                            && audience.contains(properties.audience())
            )
    ));

    return decoder;
}
```

Khi phat access token, nen co toi thieu cac claim:

```java
JwtClaimsSet claims = JwtClaimsSet.builder()
        .issuer(properties.issuer())
        .subject(user.getUserID().toString())
        .audience(List.of(properties.audience()))
        .issuedAt(issuedAt)
        .expiresAt(expiresAt)
        .id(UUID.randomUUID().toString())
        .claim("role", user.getRole().name())
        .build();
```

Header can co `kid` trung voi key duoc cong bo trong JWKS:

```java
JwsHeader.with(SignatureAlgorithm.RS256)
        .keyId(rsaKey.getKeyID())
        .build();
```

## 4. Cong bo JWKS endpoint

`identify-service` cong bo public key, khong cong bo private key:

```java
@RestController
@RequiredArgsConstructor
public class JwkSetController {

    private final RSAKey rsaKey;

    @GetMapping("/.well-known/jwks.json")
    public Map<String, Object> jwkSet() {
        return Map.of(
                "keys",
                List.of(rsaKey.toPublicJWK().toJSONObject())
        );
    }
}
```

Endpoint nay phai duoc permit trong security config:

```java
.authorizeHttpRequests(authorize -> authorize
        .requestMatchers("/.well-known/jwks.json").permitAll()
        .anyRequest().authenticated())
```

Kiem tra endpoint:

```bash
curl http://localhost:30171/.well-known/jwks.json
```

Response phai co `keys`, `kty`, `n`, `e` va `kid`, nhung khong duoc co private
key parameter `d`.

## 5. Cau hinh resource server

`restaurant-service/src/main/resources/application.yaml`:

```yaml
jwt:
  jwts:
    uri: http://localhost:30171/.well-known/jwks.json

spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          jwk-set-uri: ${jwt.jwts.uri}
```

Ten property trong project dang la `jwt.jwts.uri`. Co the don gian hoa trong
tuong lai thanh `app.jwt.jwk-set-uri`, nhung khong can doi neu cau hinh hien tai
da duoc su dung o nhieu moi truong.

Client gui access token:

```http
Authorization: Bearer eyJraWQiOiI...<token>
```

Vi du:

```bash
curl -i http://localhost:30172/restaurants \
  -H "Authorization: Bearer <access-token>"
```

## 6. Cac tang xu ly exception

Khong nen dung mot handler duy nhat cho tat ca loi. Moi loai loi xay ra tai mot
tang khac nhau.

| Truong hop | HTTP status | Noi xu ly |
|---|---:|---|
| Thieu token | 401 | `AuthenticationEntryPoint` |
| Token sai chu ky | 401 | `AuthenticationEntryPoint` |
| Token het han | 401 | `AuthenticationEntryPoint` |
| Sai issuer/audience | 401 | `AuthenticationEntryPoint` |
| Da xac thuc nhung thieu quyen | 403 | `AccessDeniedHandler` |
| Body JSON sai | 400 | `GlobalExceptionHandler` |
| Bean validation sai | 400 | `GlobalExceptionHandler` |
| Business exception | 4xx | `GlobalExceptionHandler` |
| Loi ung dung khong du kien | 500 | `GlobalExceptionHandler` |

### Vi sao GlobalExceptionHandler khong bat loi JWT?

`GlobalExceptionHandler` thuoc Spring MVC va chi xu ly exception sau khi request
da den `DispatcherServlet`. JWT duoc kiem tra trong Spring Security filter chain,
truoc `DispatcherServlet`. Do do loi JWT phai duoc xu ly bang security handler.

## 7. Xu ly 401 bang AuthenticationEntryPoint

Ten class phu hop voi contract dang dung la
`ProblemAuthenticationEntryPoint` hoac
`ProblemDetailAuthenticationEntryPoint`. Nen dung cung mot ten o tat ca service.

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ProblemAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        log.warn(
                "Authentication failed: method={}, path={}, reason={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getClass().getSimpleName()
        );

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                "Authentication is required or the access token is invalid."
        );
        problem.setTitle("Unauthorized");
        problem.setType(URI.create(
                "urn:mealgo:" + serviceName + ":invalid-token"
        ));
        problem.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Bearer");
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
```

Khong log header `Authorization` hoac token. Token la credential va co the bi lo
qua log aggregation.

Response mau:

```json
{
  "type": "urn:mealgo:restaurant-service:invalid-token",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Authentication is required or the access token is invalid.",
  "instance": "/restaurants"
}
```

## 8. Xu ly 403 bang AccessDeniedHandler

401 va 403 khac nhau:

- 401: chua xac thuc hoac credential khong hop le.
- 403: da xac thuc thanh cong nhung khong du quyen.

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class ProblemAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Value("${spring.application.name}")
    private String serviceName;

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception) throws IOException {

        log.warn(
                "Access denied: method={}, path={}, reason={}",
                request.getMethod(),
                request.getRequestURI(),
                exception.getClass().getSimpleName()
        );

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.FORBIDDEN,
                "You do not have permission to access this resource."
        );
        problem.setTitle("Access Denied");
        problem.setType(URI.create(
                "urn:mealgo:" + serviceName + ":access-denied"
        ));
        problem.setInstance(URI.create(request.getRequestURI()));

        response.setStatus(HttpStatus.FORBIDDEN.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
```

## 9. Dang ky security handlers

Handler phai duoc dang ky vao `SecurityFilterChain`. Rieng entry point cho JWT
phai gan truc tiep vao `oauth2ResourceServer`.

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ProblemAuthenticationEntryPoint authenticationEntryPoint,
            ProblemAccessDeniedHandler accessDeniedHandler) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/actuator/health",
                                "/v3/api-docs/**",
                                "/swagger-ui/**"
                        ).permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .build();
    }
}
```

Hai vi tri dang ky co y nghia:

- `.oauth2ResourceServer(...).authenticationEntryPoint(...)`: xu ly JWT Bearer
  token khong hop le trong resource server filter.
- `.exceptionHandling(...).authenticationEntryPoint(...)`: fallback cho cac loi
  authentication khac trong security filter chain.

Neu chi dang ky trong `.exceptionHandling(...)`, loi JWT khong hop le co the van
duoc entry point mac dinh cua OAuth2 Resource Server xu ly.

## 10. Xu ly loi MVC bang ProblemDetail

Loi sau khi request da vao controller duoc xu ly boi
`GlobalExceptionHandler`.

Voi Spring MVC 7, nen ke thua `ResponseEntityExceptionHandler` va override cac
extension point chinh thuc:

```java
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler
        extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        // Build validation ProblemDetail here.
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(
            HttpMessageNotReadableException exception,
            HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {
        // Build malformed JSON ProblemDetail here.
    }

    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ProblemDetail> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request) {
        // Resolve title/detail from MessageSource.
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpectedException(
            Exception exception,
            HttpServletRequest request) {
        // Log stack trace and return a safe 500 detail.
    }
}
```

Khong tra stack trace, SQL query, class name hoac raw database error cho client.
Chi ghi thong tin chi tiet vao server log.

## 11. I18n cho security error

FE gui ngon ngu qua header:

```http
Accept-Language: vi
```

Them message key vao bundle:

```properties
# messages.properties
security.invalid-token.title=Unauthorized
security.invalid-token.detail=Authentication is required or the access token is invalid.
security.access-denied.title=Access Denied
security.access-denied.detail=You do not have permission to access this resource.
```

Trong handler, resolve message bang `MessageSource`:

```java
Locale locale = request.getLocale();

String title = messageSource.getMessage(
        "security.invalid-token.title",
        null,
        locale
);
String detail = messageSource.getMessage(
        "security.invalid-token.detail",
        null,
        locale
);
```

Khong dung `exception.getMessage()` lam message cho client. Message cua Spring,
Nimbus, Hibernate hoac PostgreSQL co the thay doi theo version va co the chua
thong tin noi bo.

## 12. Logging trong production

Muc log de xuat:

- JWT sai, het han hoac thieu: `WARN`.
- Khong du quyen: `WARN`.
- Validation/business error du kien: thuong khong can stack trace; dung `INFO`
  hoac `DEBUG` tuy nhu cau audit.
- Loi 500 khong du kien: `ERROR` kem stack trace.

Vi du an toan:

```java
log.warn(
        "Authentication failed: method={}, path={}, reason={}",
        request.getMethod(),
        request.getRequestURI(),
        exception.getClass().getSimpleName()
);
```

Khong log:

- Authorization header.
- Access token va refresh token.
- Password.
- Private key.
- Toan bo request body neu co du lieu nhay cam.

## 13. Test va debug

### Khong gui token

```bash
curl -i http://localhost:30172/restaurants
```

Ket qua mong doi: HTTP 401 va `application/problem+json`.

### Gui token sai

```bash
curl -i http://localhost:30172/restaurants \
  -H "Authorization: Bearer invalid-token"
```

Ket qua mong doi:

- HTTP 401.
- Response do `ProblemAuthenticationEntryPoint` tao.
- Console cua `restaurant-service` co log `Authentication failed`.

### Goi qua gateway

```bash
curl -i http://localhost:30170/api/v1/restaurants \
  -H "Authorization: Bearer invalid-token"
```

Neu gateway tu xac minh JWT, token sai se bi chan tai gateway. Khi do:

- Request khong den `restaurant-service`.
- `restaurant-service` khong co authentication log.
- Can cau hinh reactive authentication entry point tai `api-gateway`.

### Bat Spring Security debug tam thoi

Chi dung trong local/development:

```yaml
logging:
  level:
    org.springframework.security: DEBUG
```

Khong nen de `TRACE` trong production vi log co the rat lon va de lo metadata
nhay cam.

## 14. Checklist loi thuong gap

### AuthenticationEntryPoint khong chay

Kiem tra:

1. Class co `@Component` hoac duoc khai bao bang `@Bean`.
2. Bean duoc inject vao dung `SecurityFilterChain`.
3. Da gan handler vao `.oauth2ResourceServer(...).authenticationEntryPoint(...)`.
4. Da restart service sau khi doi config.
5. Request co thuc su den service hay bi gateway chan truoc.
6. Dang xem dung console/process cua service.
7. Logging level co cho phep `WARN` hay khong.

### Token hop le o identify-service nhung sai o resource server

Kiem tra:

1. `kid` trong JWT header ton tai trong JWKS.
2. Resource server truy cap duoc JWKS URL.
3. Token va JWKS dung cung mot RSA key pair.
4. Token chua het han.
5. `issuer` giong nhau chinh xac, ke ca protocol va port.
6. `audience` cua token chua audience ma service yeu cau.
7. Dong ho he thong giua cac service khong lech qua nhieu.

### Response 401 khong dung ProblemDetail tuy bien

Thuong do resource server dang dung entry point mac dinh. Gan handler truc tiep:

```java
.oauth2ResourceServer(oauth -> oauth
        .jwt(Customizer.withDefaults())
        .authenticationEntryPoint(authenticationEntryPoint))
```

## 15. Khuyen nghi cho MealGo

1. Dung JWKS cho ca gateway va cac resource service de tranh phai copy
   `public.pem` vao tung project.
2. Validate `issuer` va `audience` tai moi noi xac minh token, khong chi tai
   `identify-service`.
3. Dung `ProblemDetail` cho moi response loi; khong boc loi trong mot
   `ApiResponse` rieng lam mat contract RFC 9457.
4. FE dua vao HTTP status va `type` de xu ly; `detail` dung de hien thi khi phu
   hop.
5. Tach ro 401 va 403.
6. Them integration test cho token thieu, token sai, token het han va role
   khong hop le.
7. Khong tra raw exception message cua database/framework cho client.
