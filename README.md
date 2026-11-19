# NewSBB-4

[NewSBB-3](https://github.com/bin0421/NewSBB-3) 의 글·댓글 REST API 에 회원(회원가입, 내 정보)과 `actorId` 기반 작성자·권한 검사를 TDD 로 아주 작은 단계마다 커밋하며 더하는 저장소.

- 참고 : [강의 설명 1~68강](https://www.slog.gg/p/14172), 강사 저장소 [jhs512/p-14172-2](https://github.com/jhs512/p-14172-2) (068 커밋까지)
- 시작점 : NewSBB-3 마지막 커밋([3e39d9e](https://github.com/bin0421/NewSBB-3/commit/3e39d9ea9a66e2bd50e32c31ba0172493af36acc))의 코드. 단계 번호는 001 부터 다시 센다.
- 진행 방식 : 기능마다 테스트를 먼저 커밋(실패)하고 구현을 커밋(통과)한다.
- 단계 번호 : 세 자리 번호 001, 002 ... / 한 단계 안의 커밋은 하위 번호 -1, -2 ... (`-1` 테스트 추가, `-2` 구현)
  - 제목 끝의 `(실패)` 는 테스트가 실패하는 상태로 커밋했다는 뜻이다.
- 진행 기록·결정 사항 : [NewSBB-4.txt](NewSBB-4.txt)

## 개발 환경

| 항목 | 버전 |
|---|---|
| Java | 25 (Temurin) |
| Spring Boot | 4.1.2 |
| Gradle | 9.8.2 (Kotlin DSL) |
| DB | H2 (개발 : 파일, 테스트 : 메모리) |
| 테스트 | JUnit 5, MockMvc |
| API 문서 | springdoc-openapi 3.2.0 (`/swagger-ui/index.html`) |

## actorId 규칙

로그인 기능은 없다. 회원으로서 요청하려면 주소에 `?actorId=회원번호` 를 붙인다.

| 상황 | 상태 | 응답 본문 |
|---|---|---|
| `actorId` 가 필요한 액션(내 정보, 글·댓글 작성·수정·삭제)에 `actorId` 가 없거나 없는 회원 | 401 | `{"resultCode": "401-1", "msg": "로그인 후 이용해주세요.", "data": null}` |
| 글·댓글 수정·삭제를 작성자가 아닌 회원이 요청 | 403 | `{"resultCode": "403-1", "msg": "권한이 없습니다.", "data": null}` |

샘플 회원(비밀번호 모두 `12345678`) : 1 `system`(시스템), 2 `admin`(관리자), 3 `user1`(유저1), 4 `user2`(유저2), 5 `user3`(유저3), 6 `user4`(유저4)

## 개발 일지

각 단계는 다음 형식으로 적는다.

- **링크** : 단어 "링크"를 누르면 해당 커밋 페이지로 이동해 직전 단계와의 코드 변경(diff)을 볼 수 있다
- **원인** : 이 단계가 필요한 이유 (발견한 문제나 개선점)
- **작업** : 추가·수정·삭제한 기능, 객체, 클래스, 메서드
- **설명** : 작업의 구현 방식과 동작 원리 (작업에 적은 내용은 반복하지 않는다)

### 001 NewSBB-3 코드로 시작

- **링크**
- **원인** : 회원 기능을 NewSBB-3 에서 완성한 글·댓글 API 위에 쌓는다.
- **작업**
  - 추가 : NewSBB-3 마지막 커밋의 코드 트리 전체 (글·댓글 CRUD, 샘플 데이터, 테스트 15개)
  - 추가 : `README.md`, `NewSBB-4.txt` (NewSBB-3 의 README·진행 기록은 가져오지 않는다)
- **설명**
  - `git archive` 로 커밋 시점의 파일만 가져왔다. 이전 히스토리는 [NewSBB-3](https://github.com/bin0421/NewSBB-3) 에서 본다.
  - 결과 : 테스트 15개(샘플 데이터 1, 글 9, 댓글 5)가 모두 통과한다.

### 002 SpringDoc 설정

- **링크**
- **원인** : 57~63강. API 가 늘어나므로 주소·요청·응답을 브라우저에서 보고 바로 호출해 볼 수 있는 문서가 필요하다.
- **작업**
  - 추가 : 의존성 `org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0`
  - 추가 : `SpringDocConfig` — `@OpenAPIDefinition`(제목·버전·설명), 그룹 `apiV1`(`/api/v1/**`), 그룹 `home`(`/api/**` 제외)
  - 추가 : 글·댓글 컨트롤러에 `@Tag`, 액션마다 `@Operation(summary)`
  - 추가 : `application.yaml` 의 `springdoc.default-produces-media-type: application/json;charset=UTF-8`
- **설명**
  - `/swagger-ui/index.html` 에서 문서를 본다. 그룹을 나눠 API 와 그 밖의 주소(홈 등)를 따로 볼 수 있다.
  - 응답 타입을 지정하지 않으면 문서의 응답 예시가 `*/*` 로 나온다. 액션마다 `produces` 를 붙이는 대신(62-1) 설정 한 줄로 기본 타입을 JSON 으로 바꾼다(62-2).
  - 결과 : 기존 테스트 15개 통과.

### 003 홈 컨트롤러

- **링크**
- **원인** : 60·63·65강. 서버 주소(`/`)로 들어왔을 때 서버 정보와 API 문서로 가는 길을 보여 줄 페이지, 브라우저에서 API 를 `fetch` 로 불러 보는 테스트 페이지가 필요하다.
- **작업**
  - 추가 : `HomeController.main()` — `GET /`, 호스트 이름·주소와 API 문서 링크를 HTML 로 응답
  - 추가 : `HomeController.testFetchPosts()` — `GET /test/fetchPosts`, 글 다건·단건을 `fetch` 해 콘솔에 찍는 스크립트
- **설명**
  - 002 에서 기본 응답 타입을 JSON 으로 바꿨으므로, HTML 을 돌려주는 액션은 `produces = TEXT_HTML_VALUE` 로 직접 지정한다(63강).
  - `fetch` 주소는 상대 경로(`/api/v1/posts`)라 같은 서버에서 열면 CORS 문제가 없다(65-3).
  - 결과 : 기존 테스트 15개 통과.

### 004 CORS 설정

- **링크**
- **원인** : 67·68강. 다른 출처(codepen `https://cdpn.io`, 프론트 개발 서버 `http://localhost:3000`)의 페이지가 API 를 `fetch` 하면 브라우저가 CORS 정책으로 응답을 막는다.
- **작업**
  - 추가 : `WebMvcConfig.addCorsMappings()` — `/api/**` 에 허용 출처 2곳, 메서드 GET·POST·PUT·DELETE·PATCH, 모든 헤더, 쿠키 포함 허용
- **설명**
  - 서버가 응답에 `Access-Control-Allow-Origin` 등을 붙여 주면 브라우저가 다른 출처의 요청 결과를 스크립트에 넘겨 준다.
  - `allowCredentials(true)` 와 함께 쓸 때는 출처를 `*` 로 둘 수 없어 정확한 출처를 적는다(68강).
  - 결과 : 기존 테스트 15개 통과.

### 005 회원 엔티티

- **링크**
- **원인** : 회원가입·내 정보·작성자 기능을 만들려면 회원을 저장할 엔티티가 있어야 한다.
- **작업**
  - 추가 : 엔티티 `Member` — `username`(유니크), `password`, `name`, 생성자 `Member(username, password, name)`
  - 추가 : `MemberRepository` — `findByUsername(username)`
- **설명**
  - `BaseEntity` 를 상속해 `id`, `createDate`, `modifyDate` 를 글·댓글과 같은 방식으로 갖는다.
  - `@Column(unique = true)` 로 DB 에도 username 유니크 제약을 건다. 중복 가입은 009 에서 서비스가 먼저 검사해 409 로 응답하고, DB 제약은 마지막 안전장치다.
  - 비밀번호 암호화는 이번 범위(68강까지)에 없으므로 평문으로 저장한다.
  - 엔티티 뼈대만 만드는 단계라 테스트 없이 커밋한다. (NewSBB-3 의 003·004 엔티티 단계와 같음)
  - 결과 : 기존 테스트 15개 통과.

### 006-1 샘플 회원 테스트 추가 (실패)

- **링크**
- **원인** : 회원가입·내 정보·작성자 테스트에 쓸 회원이 처음부터 있어야 한다. 먼저 샘플 회원을 테스트로 정한다.
- **작업**
  - 추가 : 테스트 `BaseInitDataTest.t2` — 회원이 생성 순서대로 `system`(시스템), `admin`(관리자), `user1`(유저1), `user2`(유저2), `user3`(유저3), `user4`(유저4) 이고 비밀번호가 모두 `12345678` 인지 확인
- **설명**
  - 요구사항의 마지막 회원 `user3(유저4)` 는 username 이 유니크여야 하므로 오타로 보고 `user4`(유저4) 로 정했다.
  - `extracting(...)` + `containsExactly(tuple(...))` 로 회원 수, 순서(= id 순서), 세 필드를 한 번에 비교한다.
  - 실패 확인 : 샘플 회원을 만드는 코드가 없어 `Expecting actual: [] to contain exactly ...` 로 실패한다.

### 006-2 샘플 회원 6명

- **링크**
- **원인** : 006-1 테스트가 실패한다. 샘플 회원을 만드는 코드와 회원 서비스가 없다.
- **작업**
  - 추가 : `MemberService` — `count()`, `join(username, password, name)`
  - 수정 : `BaseInitData`
    - 이전 : `work1()` 이 글·댓글 생성
    - 이후 : `work1()` 이 회원 6명 생성, 글·댓글 생성은 `work2()` 로 옮김. 실행 순서 `work1()` → `work2()`
- **설명**
  - 회원을 글보다 먼저 만들어 회원 id 가 1(system)~6(user4) 으로 고정되고, 이후 글·댓글 작성자로 바로 쓸 수 있다.
  - 각 작업은 자기 데이터가 이미 있으면 건너뛴다. 개발 DB(파일)에 이미 글이 있어도 회원만 따로 채울 수 있다.
  - 결과 : `BaseInitDataTest.t2` 통과, 테스트 16개 모두 통과.

### 007-1 회원가입 테스트 추가 (실패)

- **링크**
- **원인** : `POST /api/v1/members/join` 회원가입이 필요하다. 먼저 응답과 결과를 테스트로 정한다.
- **작업**
  - 추가 : 테스트 `ApiV1MemberControllerTest.t1` — `{"username": "usernew", "password": "12345678", "name": "무명"}` 으로 가입 요청 후 처리한 메서드(`join`), 201, `resultCode` `"201-1"`, `msg` `"무명님 환영합니다. 회원가입이 완료되었습니다."`, `data` `null` 확인, username 으로 다시 찾은 회원의 비밀번호·이름 확인
- **설명**
  - 환영 메시지의 `%s` 에는 이름(`name`)을 넣는다.
  - 실패 확인 : `ApiV1MemberController` 와 `MemberService.findByUsername()` 이 없어 컴파일에 실패한다.

### 007-2 회원가입

- **링크**
- **원인** : 007-1 테스트가 실패한다. 회원 컨트롤러와 username 으로 회원을 찾는 메서드가 없다.
- **작업**
  - 추가 : `ApiV1MemberController.join(reqBody)` — `POST /api/v1/members/join`, 요청 본문 `MemberJoinReqBody(username, password, name)`, 응답 `RsData<Void>`
  - 추가 : `MemberService.findByUsername(username)`
- **설명**
  - 응답 본문의 `data` 가 없으므로 `RsData<Void>` 를 쓴다. `RsData(resultCode, msg)` 생성자가 `data` 를 `null` 로 채우고, Jackson 은 `null` 필드도 `"data": null` 로 내보낸다.
  - 상태 코드 201 은 `ResponseAspect` 가 `resultCode` 앞부분으로 정한다.
  - 요청 검증과 중복 검사는 다음 단계에서 테스트를 먼저 넣고 더한다.
  - 결과 : `ApiV1MemberControllerTest.t1` 통과, 테스트 17개 모두 통과.

### 008-1 회원가입 요청 검증 테스트 추가 (실패)

- **링크**
- **원인** : 요구사항의 길이 제약 username(4~30), password(8~30), name(2~30) 을 어기면 가입되면 안 된다. 먼저 테스트로 정한다.
- **작업**
  - 추가 : 테스트 `ApiV1MemberControllerTest.t2` — 세 필드 모두 최소 길이보다 1 짧게(`abc`, `1234567`, `무`) 요청 후 처리한 메서드(`join`), 400, `resultCode` `"400-1"`, 필드별 `Size` 위반 메시지 3줄 확인, `abc` 회원이 생기지 않았는지 확인
- **설명**
  - 메시지 형식은 글 작성 검증(NewSBB-3 010)과 같은 `필드-제약-기본메시지` 를 정렬해 줄바꿈으로 이은 것이다.
  - 빈 문자열이 아니라 "짧은 값"으로 보내 `@Size` 경계만 확인한다.
  - 실패 확인 : 검증이 없어 그대로 가입되므로 `Status expected:<400> but was:<201>` 로 실패한다.

### 008-2 회원가입 요청 검증

- **링크**
- **원인** : 008-1 테스트가 실패한다. 회원가입 요청 본문에 검증이 없다.
- **작업**
  - 수정 : `MemberJoinReqBody` 필드에 `@NotBlank` + `@Size(username 4~30, password 8~30, name 2~30)`
  - 수정 : `join()` 의 `@RequestBody` 에 `@Valid`
- **설명**
  - 검증 실패는 이미 있는 `GlobalExceptionHandler` 의 `MethodArgumentNotValidException` 처리가 400 으로 바꾼다.
  - 요구사항에는 `@Size` 만 있지만, 글·댓글처럼 `null`·공백만 있는 값도 막기 위해 `@NotBlank` 를 함께 붙였다. 008-1 테스트는 짧은 값이라 `Size` 위반만 나온다.
  - 결과 : `ApiV1MemberControllerTest.t2` 통과, 테스트 18개 모두 통과.

### 009-1 username 중복 테스트 추가 (실패)

- **링크**
- **원인** : 이미 있는 username 으로 가입하면 409 와 안내 메시지로 응답해야 한다. 먼저 테스트로 정한다.
- **작업**
  - 추가 : 테스트 `ApiV1MemberControllerTest.t3` — 샘플 회원 `user1` 과 같은 username 으로 가입 요청 후 처리한 메서드(`join`), 409, `resultCode` `"409-1"`, `msg` `"user1(은)는 이미 사용중인 username 입니다."`, `data` `null` 확인
- **설명**
  - 중복 메시지의 `%s` 에는 username 을 넣는다.
  - 실패 확인 : 서비스가 중복을 검사하지 않아 INSERT 까지 가고, 005 에서 건 DB 유니크 제약에 걸려 `DataIntegrityViolationException (Unique index or primary key violation)` 이 처리되지 않은 채 던져져 실패한다.

### 009-2 username 중복 409

- **링크**
- **원인** : 009-1 테스트가 실패한다. 중복을 미리 검사하지 않아 DB 제약 위반 예외가 그대로 터진다.
- **작업**
  - 추가 : `GlobalExceptionHandler.MemberDuplicateUsernameException` — 중첩 static 예외 클래스, 중복된 `username` 을 담는다
  - 추가 : `GlobalExceptionHandler.handle(MemberDuplicateUsernameException)` — 409, `"409-1"`, `"%s(은)는 이미 사용중인 username 입니다."`
  - 수정 : `MemberService.join()` — 저장 전에 `findByUsername()` 으로 검사해 있으면 `MemberDuplicateUsernameException` 을 던짐
- **설명**
  - 요구사항의 클래스 이름 `com.back.global.GlobalExceptionHandler.MemberDuplicateUsernameException` 을 "GlobalExceptionHandler 안의 중첩 클래스"로 해석했다. 패키지는 NewSBB-3 의 `com.back.global.globalExceptionHandler` 그대로 둔다.
  - 예외는 username 만 들고, 사용자에게 보일 문구는 핸들러가 만든다. 응답 형식을 한 곳에서 관리하기 위해서다.
  - 결과 : `ApiV1MemberControllerTest.t3` 통과, 테스트 19개 모두 통과.
