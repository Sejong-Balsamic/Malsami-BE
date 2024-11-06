package com.balsamic.sejongmalsami.util;

import static com.balsamic.sejongmalsami.object.constants.DocumentType.DOCUMENT;
import static com.balsamic.sejongmalsami.object.constants.DocumentType.PAST_EXAM;
import static com.balsamic.sejongmalsami.object.constants.DocumentType.SOLUTION;
import static com.balsamic.sejongmalsami.object.constants.MimeType.AAC;
import static com.balsamic.sejongmalsami.object.constants.MimeType.DOCX;
import static com.balsamic.sejongmalsami.object.constants.MimeType.JPG;
import static com.balsamic.sejongmalsami.object.constants.MimeType.MP3;
import static com.balsamic.sejongmalsami.object.constants.MimeType.PDF;
import static com.balsamic.sejongmalsami.object.constants.MimeType.XLSX;
import static com.balsamic.sejongmalsami.object.constants.PostTier.CHEONMIN;
import static com.balsamic.sejongmalsami.object.constants.PostTier.JUNGIN;
import static com.balsamic.sejongmalsami.object.constants.PostTier.KING;
import static com.balsamic.sejongmalsami.object.constants.PostTier.YANGBAN;

import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.Role;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.repository.postgres.DocumentFileRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import net.datafaker.Faker;
import org.springframework.stereotype.Component;

// TODO: ENUM 타입 랜덤으로 입력받기, course 관련정보 동적으로 input 받기 ( 서버시작시 course정보 자동 화인 및 등록 로직 필요)
@Component
@RequiredArgsConstructor
public class TestDataGenerator {

  private final MemberRepository memberRepository;
  private final DocumentPostRepository documentPostRepository;
  private final DocumentFileRepository documentFileRepository;

  private final Faker faker = new Faker(new Locale("ko"));
  private final Random random = new Random();

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
   * 학생 ID 생성 메서드
   * 연도는 현재 년도에서 10년 전부터 현재 년도까지, 뒤의 6자리는 랜덤 숫자
   */
  private String generateStudentId() {
    // 연도 (startYear ~ endYear)
    int year = faker.number().numberBetween(startYear, endYear + 1);
    // 뒤의 6자리 랜덤 숫자
    String randomDigits = faker.number().digits(6);
    return String.format("%02d%s", year, randomDigits);
  }

  public Member createMember() {
    Member member = Member.builder()
        .studentId(Long.parseLong(generateStudentId())) // 임의의 8자리 학생 ID
        .studentName(faker.name().fullName().replace(" ", "").trim()) // 임의의 학생 이름 (한국어)
        .uuidNickname(faker.internet().uuid().substring(0,8)) // 임의의 UUID 닉네임
        .major(majors.get(random.nextInt(majors.size()))) // 미리 정의한 전공 목록에서 선택
        .academicYear(faker.options().option("1", "2", "3", "4", "초과학기")) // 학년
        .enrollmentStatus(faker.options().option("재학", "휴학", "졸업")) // 재학 상태
        .profileUrl(faker.internet().image()) // 프로필 이미지 URL
        .isNotificationEnabled(faker.bool().bool()) // 알림 설정 여부
        .role(Role.ROLE_USER) // 회원 : 고정
        .accountStatus(AccountStatus.ACTIVE) // 계정 활성화 상태
        .lastLoginTime(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 30))) // 마지막 로그인 시간
        .isFirstLogin(false) // 첫 로그인 X : 고정
        .build();
    return memberRepository.save(member);
  }

  public DocumentPost createDocumentPost(Member member) {
    DocumentPost post = DocumentPost.builder()
        .member(member)
        .title(faker.lorem().sentence()) // 임의의 제목
        .subject(faker.educator().course()) // 임의의 교과목명
        .content(faker.lorem().paragraph()) // 임의의 내용
        .postTier(faker.options().option(CHEONMIN, JUNGIN, YANGBAN, KING)) // 고정 계급
        .likeCount(faker.number().numberBetween(0, 1000)) // 임의의 좋아요 수
        .viewCount(faker.number().numberBetween(0, 30000)) // 임의의 조회 수
        .isDepartmentPrivate(faker.bool().bool()) // 학과 비공개 여부
        .dailyScore(faker.number().numberBetween(0, 300)) // 임의의 일간 점수
        .weeklyScore(faker.number().numberBetween(0, 1000)) // 임의의 주간 점수
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
    return documentPostRepository.save(post);
  }

  public DocumentFile createDocumentFile(DocumentPost post, Member uploader) {
    DocumentFile file = DocumentFile.builder()
        .documentPost(post)
        .uploader(uploader)
        .thumbnailUrl(faker.internet().image()) // 썸네일 URL
        .originalFileName(faker.file().fileName()) // 원본 파일 이름
        .uploadFileName(faker.file().fileName()) // 업로드된 파일 이름
        .fileSize((long) faker.number().numberBetween(1000, 100000)) // 파일 크기
        .mimeType(faker.options().option(AAC, PDF, DOCX, MP3, JPG, XLSX)) // MIME 타입
        .downloadCount((long) faker.number().numberBetween(0, 300)) // 다운로드 수
        .password(null)
        .isInitialPasswordSet(faker.bool().bool()) // 초기 비밀번호 설정 여부
        .createdDate(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10))) // 작성일
        .updatedDate(LocalDateTime.now().minusDays(faker.number().numberBetween(1, 10))) // 수정일
        .isEdited(faker.bool().bool()) // 수정 여부
        .isDeleted(faker.bool().bool()) // 삭제 여부
        .build();
    return documentFileRepository.save(file);
  }
}
