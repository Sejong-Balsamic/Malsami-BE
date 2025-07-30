package com.balsamic.sejongmalsami.application.test;

import static com.balsamic.sejongmalsami.constants.DocumentType.DOCUMENT;
import static com.balsamic.sejongmalsami.constants.DocumentType.PAST_EXAM;
import static com.balsamic.sejongmalsami.constants.DocumentType.SOLUTION;
import static com.balsamic.sejongmalsami.constants.MimeType.AAC;
import static com.balsamic.sejongmalsami.constants.MimeType.DOCX;
import static com.balsamic.sejongmalsami.constants.MimeType.JPG;
import static com.balsamic.sejongmalsami.constants.MimeType.MP3;
import static com.balsamic.sejongmalsami.constants.MimeType.PDF;
import static com.balsamic.sejongmalsami.constants.MimeType.XLSX;
import static com.balsamic.sejongmalsami.constants.PostTier.CHEONMIN;
import static com.balsamic.sejongmalsami.constants.PostTier.JUNGIN;
import static com.balsamic.sejongmalsami.constants.PostTier.KING;
import static com.balsamic.sejongmalsami.constants.PostTier.YANGBAN;
import static com.balsamic.sejongmalsami.constants.QuestionPresetTag.BETTER_SOLUTION;
import static com.balsamic.sejongmalsami.constants.QuestionPresetTag.EXAM_PREPARATION;
import static com.balsamic.sejongmalsami.constants.QuestionPresetTag.OUT_OF_CLASS;
import static com.balsamic.sejongmalsami.constants.QuestionPresetTag.STUDY_TIPS;
import static com.balsamic.sejongmalsami.constants.QuestionPresetTag.UNKNOWN_CONCEPT;

import com.balsamic.sejongmalsami.config.PostTierConfig;
import com.balsamic.sejongmalsami.constants.AccountStatus;
import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.constants.Role;
import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.postgres.Exp;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.post.object.postgres.AnswerPost;
import com.balsamic.sejongmalsami.post.object.postgres.Comment;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.post.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.post.repository.postgres.AnswerPostRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.CommentRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.DocumentFileRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.DocumentRequestPostRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.post.service.QuestionPostCustomTagService;
import com.balsamic.sejongmalsami.repository.postgres.CourseRepository;
import com.balsamic.sejongmalsami.repository.postgres.ExpRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.YeopjeonRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// TODO: ENUM 타입 랜덤으로 입력받기
@Component
@RequiredArgsConstructor
@Slf4j
public class TestDataGenerator {

  private final MemberRepository memberRepository;
  private final DocumentPostRepository documentPostRepository;
  private final DocumentFileRepository documentFileRepository;
  private final DocumentRequestPostRepository documentRequestPostRepository;
  private final QuestionPostRepository questionPostRepository;
  private final QuestionPostCustomTagService questionPostCustomTagService;
  private final AnswerPostRepository answerPostRepository;
  private final CommentRepository commentRepository;
  private final CourseRepository courseRepository;
  private final YeopjeonRepository yeopjeonRepository;
  private final ExpRepository expRepository;
  private final PostTierConfig postTierConfig;

  private final Faker faker = new Faker(new Locale("ko"));
  private final Random random = new Random();
  private final ObjectMapper objectMapper = new ObjectMapper();

  // 연도 범위 계산 저장
  private final int startYear = calculateStartYear();
  private final int endYear = calculateEndYear();

  // 전공 정의
  private final List<String> majors = Arrays.asList(
      "건축공학부",
      "건설환경공학과",
      "환경에너지공간융합학과",
      "지구자원시스템공학과",
      "기계항공우주공학부",
      "나노신소재공학과",
      "양자원자력공학과",
      "국방시스템공학과",
      "항공시스템공학과",
      "컴퓨터공학과",
      "정보보호학과",
      "소프트웨어학과",
      "데이터사이언스학과",
      "지능기전공학부",
      "창의소프트학부 (디자인이노베이션전공)",
      "창의소프트학부 (만화애니메이션텍전공)",
      "회화과",
      "패션디자인학과",
      "음악과",
      "체육학과",
      "무용과",
      "영화예술학과",
      "법학부",
      "자연과학대학",
      "수학통계학부",
      "물리천문학과",
      "화학과",
      "생명시스템학부",
      "전자정보통신공학과",
      "국어국문학과",
      "국제학부",
      "역사학과",
      "교육학과",
      "행정학과",
      "미디어커뮤니케이션학과",
      "경영학부",
      "경제학과",
      "호텔관광외식경영학부",
      "호텔외식관광프랜차이즈경영학과",
      "글로벌조리학과"
  );

  // 교과목명 정의
  private final List<String> subjects = loadSubjectsFromJson();

  // 교과목명 정의 (외부 JSON 파일에서 읽어오기)
  private List<String> loadSubjectsFromJson() {
    try (InputStream inputStream = getClass().getResourceAsStream("/courses/subject.json")) {
      return objectMapper.readValue(inputStream, new TypeReference<List<String>>() {});
    } catch (IOException e) {
      throw new RuntimeException("교과목명 로딩 실패", e);
    }
  }

  // 시작 연도 계산 (현재 년도 기준 10년 전)
  private int calculateStartYear() {
    int currentYear = LocalDate.now().getYear() % 100; // 현재 년도의 뒤 두 자리
    int start = currentYear - 10;
    return Math.max(start, 0); // 음수 방지
  }

  // 종료 연도 계산 (현재 년도)
  private int calculateEndYear() {
    return LocalDate.now().getYear() % 100;
  }

  /**
   * 학생 ID 생성 메서드 연도는 현재 년도에서 10년 전부터 현재 년도까지, 뒤의 6자리는 랜덤 숫자
   */
  private String generateStudentId() {
    // 연도 (startYear ~ endYear)
    int year = faker.number().numberBetween(startYear, endYear + 1);
    // 뒤의 6자리 랜덤 숫자
    String randomDigits = faker.number().digits(6);
    return String.format("%02d%s", year, randomDigits);
  }

  /**
   * <h3>회원 Mock 데이터 생성 메소드</h3>
   * <p>Mock 회원의 엽전 및 경험치 테이블도 생성합니다.</p>
   * <p>Mock 회원은 충분한 엽전 및 경험치를 소지하도록 생성합니다.</p>
   * @return Member
   */
  @Transactional
  public Member createMockMember() {
    Member member = Member.builder()
        .studentId(Long.parseLong(generateStudentId())) // 임의의 8자리 학생 ID
        .studentName(faker.name().fullName().replace(" ", "").trim()) // 임의의 학생 이름 (한국어)
        .uuidNickname(faker.internet().uuid().substring(0, 8)) // 임의의 UUID 닉네임
        .major(majors.get(random.nextInt(majors.size()))) // 미리 정의한 전공 목록에서 선택
        .academicYear(faker.options().option("1", "2", "3", "4", "초과학기")) // 학년
        .enrollmentStatus(faker.options().option("재학", "휴학", "졸업")) // 재학 상태
        .profileUrl(faker.internet().image()) // 프로필 이미지 URL
        .isNotificationEnabled(faker.bool().bool()) // 알림 설정 여부
        .roles(Set.of(Role.ROLE_USER)) // 회원 : 고정
        .accountStatus(AccountStatus.ACTIVE) // 계정 활성화 상태
        .lastLoginTime(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 30))) // 마지막 로그인 시간
        .isFirstLogin(false) // 첫 로그인 X : 고정
        .build();

    // Mock 회원의 엽전 테이블 생성
    Yeopjeon yeopjeon = Yeopjeon.builder()
        .member(member)
        .yeopjeon(faker.number().numberBetween(500, 10000))
        .build();
    yeopjeonRepository.save(yeopjeon);

    // Mock 회원의 경험치 테이블 생성
    Exp exp = Exp.builder()
        .member(member)
        .exp(faker.number().numberBetween(0, 10000))
        .build();
    expRepository.save(exp);

    return memberRepository.save(member);
  }

  /**
   * <h3>질문글 Mock 데이터 생성 메소드</h3>
   * <ul>
   *   <li>Member를 파라미터로 받아 질문글을 작성합니다.</li>
   *   <li>TODO: 작성시각, 수정시각 설정 로직 작성</li>
   * </ul>
   * @param member
   * @return questionPost
   */
  public QuestionPost createMockQuestionPost(Member member) {

    String subject = subjects.get(random.nextInt(subjects.size()));

    // 교과목에 따른 단과대명 리스트를 String으로 대체
    List<String> faculties = new ArrayList<>();
    List<Course> courses = courseRepository.findAllBySubject(subject);
    for (Course course : courses) {
      faculties.add(course.getFaculty());
    }

    QuestionPost post = QuestionPost.builder()
        .member(member)
        .title(faker.lorem().sentence()) // 임의의 제목
        .content(faker.lorem().paragraph()) // 임의의 본문
        .subject(subject) // 임의의 교과목명
        .faculties(faculties) // String 리스트 설정
        .questionPresetTags(new ArrayList<>(faker.options().option(
            List.of(OUT_OF_CLASS),
            List.of(OUT_OF_CLASS, UNKNOWN_CONCEPT),
            List.of(BETTER_SOLUTION, EXAM_PREPARATION),
            List.of(STUDY_TIPS, OUT_OF_CLASS),
            List.of(UNKNOWN_CONCEPT))
        ))
        .viewCount(faker.number().numberBetween(0, 30000))
        .likeCount(faker.number().numberBetween(0, 1000))
        .answerCount(0)
        .commentCount(0)
        .rewardYeopjeon(faker.number().numberBetween(0, 50) * 10)
        .dailyScore((long) faker.number().numberBetween(0, 300))
        .weeklyScore((long) faker.number().numberBetween(0, 1000))
        .chaetaekStatus(false)
        .createdDate(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10)))
        .updatedDate(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10)))
        .build();

    questionPostRepository.save(post);

    // 커스텀 태그 추가 (0 ~ 4개 랜덤)
    List<String> tags = new ArrayList<>();
    int tagCount = random.nextInt(5);
    for (int i = 0; i < tagCount; i++) {
      String tag = faker.lorem().word();
      tags.add(tag.substring(0, Math.min(tag.length(), 10)));
    }
    questionPostCustomTagService.saveCustomTags(tags, post.getQuestionPostId());

    return questionPostRepository.save(post);
  }

  /**
   * <h3>답변 글 Mock 데이터 생성 메소드</h3>
   *
   * @param member
   * @param questionPost
   * @return
   */
  public AnswerPost createMockAnswerPost(Member member, QuestionPost questionPost) {

    AnswerPost answerPost = AnswerPost.builder()
        .member(member)
        .questionPost(questionPost)
        .content(faker.lorem().paragraph())
        .likeCount(faker.number().numberBetween(0, 300))
        .commentCount(0)
        .isChaetaek(false)
        .isPrivate(false)
        .createdDate(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10)))
        .updatedDate(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10)))
        .build();

    return answerPostRepository.save(answerPost);
  }

  /**
   * <h3>자료 글 Mock 데이터 생성 메소드</h3>
   *
   * @param member
   * @return
   */
  public DocumentPost createMockDocumentPost(Member member) {

    String subject = subjects.get(random.nextInt(subjects.size()));

    // 교과목에 따른 단과대명 리스트를 String으로 대체
    List<String> faculties = new ArrayList<>();
    List<Course> courses = courseRepository.findAllBySubject(subject);
    for (Course course : courses) {
      faculties.add(course.getFaculty());
    }

    DocumentPost post = DocumentPost.builder()
        .member(member)
        .title(faker.lorem().sentence()) // 임의의 제목
        .subject(subject) // 임의의 교과목명
        .faculties(faculties) // String 리스트 설정
        .content(faker.lorem().paragraph()) // 임의의 내용
        .postTier(CHEONMIN) // 글 작성시 천민 계급
        .likeCount(faker.number().numberBetween(0, 130)) // 임의의 좋아요 수
        .dislikeCount(faker.number().numberBetween(0, 30)) // 임의의 싫어요 수
        .viewCount(faker.number().numberBetween(0, 30000)) // 임의의 조회 수
        .commentCount(0)
        .isDepartmentPrivate(faker.bool().bool()) // 학과 비공개 여부
        .dailyScore((long) faker.number().numberBetween(0, 300)) // 임의의 일간 점수
        .weeklyScore((long) faker.number().numberBetween(0, 1000)) // 임의의 주간 점수
        .createdDate(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10))) // 작성일
        .updatedDate(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10))) // 수정일
        .isEdited(faker.bool().bool()) // 수정 여부
        .isDeleted(faker.bool().bool()) // 삭제 여부
        .documentTypes(faker.options().option(
            Arrays.asList(DOCUMENT, PAST_EXAM),
            Arrays.asList(DOCUMENT, SOLUTION),
            Arrays.asList(SOLUTION),
            Arrays.asList(PAST_EXAM)
        )) // 문서 타입
        .build();

    int score = post.getLikeCount() - post.getDislikeCount();
    if (score < postTierConfig.getLikeRequirementCheonmin()) {
      post.setPostTier(CHEONMIN);
    } else if (score < postTierConfig.getLikeRequirementJungin()) {
      post.setPostTier(JUNGIN);
    } else if (score < postTierConfig.getLikeRequirementKing()) {
      post.setPostTier(YANGBAN);
    } else {
      post.setPostTier(KING);
    }

    return documentPostRepository.save(post);
  }

  /**
   * <h3>자료 첨부파일 Mock 데이터 생성 메소드</h3>
   * @param uploader
   * @param post
   * @return
   */
  public DocumentFile createMockDocumentFile(Member uploader, DocumentPost post) {
    long totalDownloadCount = (long) faker.number().numberBetween(0, 300);
    long weeklyDownloadCount = (long) faker.number().numberBetween(0, totalDownloadCount);
    long dailyDownloadCount = (long) faker.number().numberBetween(0, weeklyDownloadCount);

    DocumentFile file = DocumentFile.builder()
        .documentPost(post)
        .uploader(uploader)
        .thumbnailUrl(faker.internet().image()) // 썸네일 URL
        .originalFileName(faker.file().fileName()) // 원본 파일 이름
        .uploadedFileName(faker.file().fileName()) // 업로드된 파일 이름
        .fileSize((long) faker.number().numberBetween(1000, 100000)) // 파일 크기
        .mimeType(faker.options().option(AAC, PDF, DOCX, MP3, JPG, XLSX)) // MIME 타입
        .totalDownloadCount(totalDownloadCount) // 총 다운로드 수
        .weeklyDownloadCount(weeklyDownloadCount) // 주간 다운로드 수
        .dailyDownloadCount(dailyDownloadCount) // 일간 다운로드 수
        .password(null)
        .isInitialPasswordSet(faker.bool().bool()) // 초기 비밀번호 설정 여부
        .createdDate(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10))) // 작성일
        .updatedDate(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10))) // 수정일
        .isEdited(faker.bool().bool()) // 수정 여부
        .isDeleted(faker.bool().bool()) // 삭제 여부
        .build();
    return documentFileRepository.save(file);
  }

  /**
   * <h3>자료 요청 글 Mock 데이터 생성 메소드</h3>
   *
   * @param member
   * @return
   */
  public DocumentRequestPost createMockDocumentRequestPost(Member member) {

    String subject = subjects.get(random.nextInt(subjects.size()));

    // 교과목에 따른 단과대명 리스트를 String으로 대체
    List<String> faculties = new ArrayList<>();
    List<Course> courses = courseRepository.findAllBySubject(subject);
    for (Course course : courses) {
      faculties.add(course.getFaculty());
    }

    DocumentRequestPost post = DocumentRequestPost.builder()
        .member(member)
        .title(faker.lorem().sentence())
        .content(faker.lorem().paragraph())
        .subject(subject)
        .faculties(faculties) // String 리스트 설정
        .documentTypes(new ArrayList<>(faker.options().option(
            Arrays.asList(DOCUMENT),
            Arrays.asList(DOCUMENT, PAST_EXAM),
            Arrays.asList(SOLUTION)
        )))
        .viewCount(faker.number().numberBetween(0, 30000))
        .likeCount(faker.number().numberBetween(0, 1000))
        .commentCount(0)
        .isPrivate(faker.bool().bool())
        .build();
    return documentRequestPostRepository.save(post);
  }

  /**
   * <h3>댓글 Mock 데이터 생성 메소드</h3>
   * @param member 댓글 작성자
   * @param contentType question, answer, document, documentRequest, notice
   * @return
   */
  public Comment createMockComment(Member member, UUID postId, ContentType contentType) {

    Comment comment = Comment.builder()
        .member(member)
        .postId(postId)
        .content(faker.lorem().paragraph())
        .likeCount(faker.number().numberBetween(0, 1000))
        .contentType(contentType)
        .isPrivate(faker.bool().bool())
        .build();
    return commentRepository.save(comment);
  }
}
