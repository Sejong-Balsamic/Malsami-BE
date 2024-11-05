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
import com.github.javafaker.Faker;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TestDataInit {
  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private DocumentPostRepository documentPostRepository;

  @Autowired
  private DocumentFileRepository documentFileRepository;

  private final Faker faker = new Faker();

  public Member createMember() {
    Member member = Member.builder()
        .studentId(faker.number().randomNumber()) // 임의의 학생 ID
        .studentName(faker.name().fullName()) // 임의의 학생 이름
        .uuidNickname(faker.internet().uuid()) // 임의의 UUID 닉네임
        .major(faker.educator().course()) // 임의의 전공
        .academicYear(faker.options().option("1", "2", "3", "4","초과학기")) // 학년
        .enrollmentStatus(faker.options().option("재학","휴학","졸업")) // 고정값 예시
        .profileUrl(faker.internet().avatar()) // 프로필 이미지 URL
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
        .title(faker.lorem().sentence())
        .subject(faker.lorem().word()) // 임의의 단어
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
        .documentTypes(faker.options().option(Arrays.asList(DOCUMENT, PAST_EXAM), Arrays.asList(DOCUMENT, SOLUTION), Arrays.asList(SOLUTION), Arrays.asList(PAST_EXAM))) // 문서 타입
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
        .fileSize((long)faker.number().numberBetween(1000, 100000)) // 파일 크기
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
