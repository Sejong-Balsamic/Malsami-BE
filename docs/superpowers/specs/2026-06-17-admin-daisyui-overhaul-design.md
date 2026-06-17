# 관리자 화면 daisyUI 전면 교체 — 설계 문서

- 작성일: 2026-06-17
- 대상: Malsami-BE 관리자 웹 화면(`/admin/**`)
- 레퍼런스: passQL 프로젝트(`d:\0-suh\project\passQL`)의 admin 화면 스택
- 관련 후속 이슈: 관리자 로그인 방식 개선(URL 토큰 폐기) — 별도 트래킹

## 1. 목표와 원칙

기존 관리자 화면의 **UI를 daisyUI + Tailwind 기반으로 전면 교체**한다. 기능은 모두 유지하고, 군더더기 의존성(Semantic UI / jQuery / Tabulator / 커스텀 CSS)은 전부 제거한다.

### 핵심 원칙: 백엔드는 손대지 않는다 (View-only)

- `AdminPageController`(뷰 라우팅), `AdminApiController`(REST API), 서비스, DTO, 인증 흐름은 **그대로 둔다.**
- 인증은 기존 JWT/accessToken 방식(`?accessToken=` 쿼리파라미터 + localStorage + `auth.js`)을 **그대로 유지**한다. 로그인 방식 자체의 개선은 별도 이슈에서 다룬다.
- 호출하는 API의 URL·파라미터(`multipart/form-data`)·응답 스펙은 **1바이트도 바꾸지 않는다.** 바뀌는 것은 오직 화면 마크업과 그 화면이 렌더링하는 방식(JS)뿐이다.

### 예외적으로 컨트롤러에 허용되는 변경 (View 보조 한정)

각 페이지에서 사이드바 활성 메뉴/페이지 제목을 표시하기 위해 컨트롤러가 `model.addAttribute("currentMenu", ...)`, `model.addAttribute("pageTitle", ...)` 두 줄을 추가하는 것은 허용한다. 비즈니스 로직이 아니라 뷰 렌더링 보조이므로 View-only 범위에 포함한다.

## 2. 기술 스택 / 의존성

| 항목 | 현재 (제거) | 변경 후 |
|---|---|---|
| CSS 프레임워크 | Semantic UI (`semantic.min.css`) | daisyUI 5 (CDN) |
| 유틸 CSS | `sejong-malsami.css`, `common.css`, `semantic-custom-utilities.css` | Tailwind CSS 4 browser (CDN), 커스텀 CSS 0 |
| 테이블 | Tabulator (`tabulator.min.css/js`) | daisyUI `table` + vanilla JS 렌더링 |
| DOM/AJAX | jQuery (`jquery.min.js`) | vanilla JS (`fetch`, DOM API) |
| 아이콘 | Semantic UI 폰트 아이콘 | Lucide (CDN) |
| 차트(대시보드) | Chart.js (CDN) | Chart.js 유지 |
| 레이아웃 | `fragments/header.html` + `footer.html` (`th:replace`) | `admin/layout.html` (`layout:decorate`, Drawer 사이드바) |

### CDN 로딩 (passQL과 동일)

```html
<link href="https://cdn.jsdelivr.net/npm/daisyui@5" rel="stylesheet" type="text/css"/>
<script src="https://cdn.jsdelivr.net/npm/@tailwindcss/browser@4"></script>
<script src="https://unpkg.com/lucide@latest"></script>
```

- 빌드 파이프라인 추가 없음. 관리자 브라우저가 외부 인터넷에 접근 가능한 환경을 전제로 한다.

### build.gradle 변경 (1줄 추가)

```gradle
implementation 'nz.net.ultraq.thymeleaf:thymeleaf-layout-dialect'
```

- Thymeleaf 의존성(`spring-boot-starter-thymeleaf`)이 위치한 `SM-Common/build.gradle`에 추가한다.

## 3. 컴포넌트 구조 (템플릿)

```
SM-Web/src/main/resources/templates/
├── admin/
│   ├── layout.html          신규: Drawer 사이드바 + navbar + 다크모드 토글 (공통 레이아웃)
│   ├── dashboard.html       통계 카드 그리드 + Chart.js + 최근로그 테이블
│   ├── member.html          필터 폼(카드) + table + join 페이지네이션
│   ├── yeopjeon.html        내정보 카드 + 엽전조정 버튼 + 회원검색 + 조정 모달
│   ├── notice.html          작성 폼 + 목록 테이블
│   ├── question.html        필터 + 테이블 + 작성자 상세 모달
│   ├── subject.html         검색(자동완성) + 엑셀 업로드 + 파일관리 모달
│   ├── notification.html    회원검색 + FCM 발송 폼
│   ├── errorCode.html       검색 + 테이블
│   ├── test.html            테스트 계정 생성/목록
│   └── playGround.html      UUID 뽑기
├── login.html               daisyUI 로그인 카드 (동작은 기존 JWT 그대로)
├── error/
│   ├── 403.html / 404.html / 500.html   daisyUI로 정리
└── fragments/               삭제(또는 비움) — layout.html로 대체
```

- 각 페이지는 `layout:decorate="~{admin/layout}"`로 공통 레이아웃을 상속하고, `<div layout:fragment="content">` 안에만 본문을 채운다.
- `login.html`은 레이아웃 상속 없이 독립형(중앙 정렬 카드). 동작은 기존 JWT 로그인 그대로.

### static 정리

```
SM-Web/src/main/resources/static/
├── css/        semantic*, sejong-malsami.css, common.css, tabulator.min.css 전부 삭제
├── js/
│   ├── jquery.min.js / semantic*.js / tabulator.min.js   삭제
│   ├── auth.js                          토큰 검증·네비게이션 로직만 유지(필요 시 jQuery 의존 제거)
│   ├── common-sejong-malsami.js         공통 로직 유지·정리(jQuery 의존 제거)
│   └── admin-table.js                   신규: 공통 테이블 fetch+렌더+페이지네이션 헬퍼
├── fonts/      유지(브랜드 폰트 필요 시)
└── images/     유지(로고 등)
```

## 4. 사이드바 메뉴 배치 (UI 재배치)

기존에 평면 나열된 9~10개 메뉴를 daisyUI `menu` + `menu-title`로 기능 그룹화한다.

```
[로고 + Admin 배지]
─ 대시보드
─ 사용자 관리
   · 회원 관리        (/admin/member)
   · 엽전 관리        (/admin/yeopjeon)
   · 알림 발송        (/admin/notification)
─ 콘텐츠 관리
   · 질문 게시글      (/admin/question)
   · 공지사항        (/admin/notice)
   · 교과목/파일      (/admin/subject)
─ 시스템
   · 에러코드        (/admin/error-code)
   · 테스트 계정      (/admin/test)
   · 개발자 놀이터    (/admin/play-ground)
```

- 현재 메뉴 강조: `th:classappend="${currentMenu == 'member'} ? 'active'"` 패턴.
- navbar에는 페이지 제목(`pageTitle`), 다크모드 토글, 로그아웃 버튼을 둔다(다크모드는 `localStorage` + `data-theme`).

## 5. 데이터 흐름 / 테이블 렌더링

기존 API는 전부 `multipart/form-data` POST이며 응답은 페이징된 JSON(`Page<T>` 형태)이다. 이 스펙을 유지하고 Tabulator를 공통 vanilla JS 헬퍼(`admin-table.js`)로 대체한다.

1. **필터 폼 submit** → `FormData` 구성 후 기존 엔드포인트로 `fetch` POST. 토큰은 기존 방식(헤더/파라미터)대로 첨부.
2. **응답 JSON** → daisyUI `<table>`의 `<tbody>`에 행을 그린다.
3. **페이지네이션** → daisyUI `join` 버튼. 페이지 클릭 시 `page` 파라미터만 바꿔 동일 fetch 재호출.
4. **정렬** → `<th>` 클릭 시 정렬 파라미터를 붙여 재호출(서버 정렬 지원 범위 내). 미지원 컬럼은 정렬 비활성.
5. **페이지 크기** → 10/30/50/100 셀렉트(기존 동작 유지).

핵심: 렌더링 코드만 JS로 교체되고, 호출 API·파라미터·응답 계약은 그대로다.

## 6. 에러 처리 / 엣지케이스

- fetch 실패(401 등): daisyUI `alert alert-error` 토스트 표시 + 기존 `auth.js` 토큰 만료 처리 흐름 유지.
- 빈 결과: 테이블에 Lucide 아이콘 + "데이터가 없습니다" empty state(passQL 패턴).
- 모달: daisyUI 네이티브 `<dialog class="modal">` 사용(jQuery modal 제거).
- 에러 페이지(403/404/500): daisyUI 카드 스타일로 통일.

## 7. UI 패턴 가이드 (passQL에서 차용)

- **카드**: `card bg-base-100 shadow` + `card-body`
- **통계 카드**: 숫자 강조(`text-3xl font-bold text-primary`) + 우측 원형 아이콘 배경(`p-3 bg-primary/10 rounded-full`)
- **배지(상태)**: `badge` + 상태별 색(`badge-success` / `badge-error` / `badge-warning` / `badge-ghost`)
- **필터 폼**: `card` 안에 `flex flex-wrap gap-2 items-end` + `input input-sm input-bordered` / `select select-sm`
- **테이블**: `table table-sm` + `overflow-x-auto`
- **페이지네이션**: `join` + `join-item btn btn-sm`
- **버튼**: `btn btn-sm` + 용도별 색(`btn-primary` / `btn-success` / `btn-error` / `btn-ghost` / `btn-outline`)
- **반응형**: `lg:` / `xl:` 브레이크포인트, 사이드바는 `drawer lg:drawer-open`

## 8. 검증 방법

서버사이드 뷰라 수동 검증 중심으로 한다.

- 앱 기동 후 각 `/admin/*` 페이지가 정상 렌더되는지 확인.
- 페이지별 핵심 기능 1개 이상 동작 확인:
  - 회원 관리: 필터 검색 → 테이블 렌더 → 페이지네이션
  - 엽전 관리: 내 엽전 조정, 회원 검색 후 조정 모달
  - 공지: 작성/고정, 목록 필터
  - 교과목: 자동완성, 엑셀 업로드, 파일 다운로드
  - 알림: FCM 발송 폼
  - 질문/에러코드/테스트/놀이터: 각 조회·액션
- daisyUI 다크모드 토글, 모바일(드로어 접힘) 반응형 확인.
- gradle 빌드는 사용자가 별도 환경에서 수행(내부망 gradle 제약).

## 9. 범위에서 제외 (Non-goals)

- 백엔드 API/서비스/DTO 변경.
- 인증/로그인 방식 변경(별도 이슈에서 진행).
- multipart POST → RESTful 전환.
- 신규 기능 추가(기존 미구현 버튼은 미구현 상태 유지).
- 빌드 기반 Tailwind 파이프라인 도입(현재는 CDN).
