package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.object.constants.Role;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.repository.postgres.DocumentFileRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("dev")
@Slf4j
@Transactional
class DocumentPostServiceTest {

  @Autowired
  DocumentPostRepository documentPostRepository;

  @Autowired
  DocumentFileRepository documentFileRepository;

  @Autowired
  MemberRepository memberRepository;

  @Autowired
  DocumentPostService documentPostService;

  private Member member1;
  private Member member2;
  private DocumentPost post1;
  private DocumentPost post2;

  @BeforeEach
  void setUp() {
    Member tempMember1 = Member.builder()
        .studentId(123456L)
        .studentName("홍길동")
        .uuidNickname("hong")
        .major("컴퓨터공학")
        .academicYear("2020")
        .enrollmentStatus("재학")
        .profileUrl("http://example.com/profile1.jpg")
        .isNotificationEnabled(true)
        .role(Role.ROLE_USER) // Enum 타입으로 수정
        .accountStatus(AccountStatus.ACTIVE)
        .lastLoginTime(LocalDateTime.now().minusDays(10))
        .isFirstLogin(true)
        .build();

    Member tempMember2 = Member.builder()
        .studentId(654321L)
        .studentName("김철수")
        .uuidNickname("kim")
        .major("수학")
        .academicYear("2019")
        .enrollmentStatus("재학")
        .profileUrl("http://example.com/profile2.jpg")
        .isNotificationEnabled(true)
        .role(Role.ROLE_USER)
        .accountStatus(AccountStatus.ACTIVE)
        .lastLoginTime(LocalDateTime.now().minusDays(9))
        .isFirstLogin(true)
        .build();

    member1 = memberRepository.save(tempMember1);
    member2 = memberRepository.save(tempMember2);

    DocumentPost tempPost1 = DocumentPost.builder()
        .member(member1)
        .title("자료 제목 1")
        .subject("수학")
        .content("자료 내용 1")
        .postTier(PostTier.CHEONMIN)
        .likeCount(10)
        .dislikeCount(0)
        .commentCount(5)
        .viewCount(100)
        .isDepartmentPrivate(false)
        .dailyScore(50)
        .weeklyScore(200)
        .createdDate(LocalDateTime.now().minusDays(1))
        .updatedDate(LocalDateTime.now().minusDays(1))
        .isEdited(false)
        .isDeleted(false)
        .documentTypes(Arrays.asList(DocumentType.DOCUMENT)) // Set -> List로 변경
        .build();

    DocumentPost tempPost2 = DocumentPost.builder()
        .member(member2)
        .title("자료 제목 2")
        .subject("과학")
        .content("자료 내용 2")
        .postTier(PostTier.CHEONMIN)
        .likeCount(20)
        .dislikeCount(1)
        .commentCount(10)
        .viewCount(200)
        .isDepartmentPrivate(true)
        .dailyScore(100)
        .weeklyScore(400)
        .createdDate(LocalDateTime.now().minusDays(2))
        .updatedDate(LocalDateTime.now().minusDays(2))
        .isEdited(false)
        .isDeleted(false)
        .documentTypes(Arrays.asList(DocumentType.DOCUMENT)) // Set -> List로 변경
        .build();

    post1 = documentPostRepository.save(tempPost1);
    post2 = documentPostRepository.save(tempPost2);

    DocumentFile file1 = DocumentFile.builder()
        .documentPost(post1) // DocumentPost 엔티티 설정
        .uploader(member1)
        .thumbnailUrl("http://example.com/thumbnail1.jpg")
        .originalFileName("original1.pdf")
        .uploadFileName("upload1.pdf")
        .fileSize(102400L)
        .mimeType(MimeType.PDF)
        .downloadCount(10L)
        .password(null)
        .isInitialPasswordSet(false)
        .createdDate(LocalDateTime.now().minusDays(1))
        .updatedDate(LocalDateTime.now().minusDays(1))
        .isEdited(false)
        .isDeleted(false)
        .build();

    DocumentFile file2 = DocumentFile.builder()
        .documentPost(post2) // DocumentPost 엔티티 설정
        .uploader(member2)
        .thumbnailUrl("http://example.com/thumbnail2.jpg")
        .originalFileName("original2.docx")
        .uploadFileName("upload2.docx")
        .fileSize(204800L)
        .mimeType(MimeType.DOCX)
        .downloadCount(20L)
        .password(null)
        .isInitialPasswordSet(false)
        .createdDate(LocalDateTime.now().minusDays(2))
        .updatedDate(LocalDateTime.now().minusDays(2))
        .isEdited(false)
        .isDeleted(false)
        .build();

    // DocumentFiles 저장
    documentFileRepository.saveAll(Arrays.asList(file1, file2));
  }

  @Test
  public void mainTest() {
    searchQuery("createDate"); // 기본 정렬: 생성일
    searchQuery("likeCount");    // 좋아요 수 기준 정렬
    searchQuery("viewCount");    // 조회수 기준 정렬
  }

  public void searchQuery(String sortType) {
    // DocumentCommand 객체 설정
    DocumentCommand command = new DocumentCommand();
    command.setTitle("자료 제목");
    command.setSubject("수학");
    command.setContent("자료 내용");
    command.setPageNumber(0);
    command.setPageSize(10);
    command.setDocumentTypes(Arrays.asList(DocumentType.DOCUMENT)); // Set -> List로 변경
    command.setSort(sortType);

    Sort sort = Sort.by(sortType);
    Pageable pageable = PageRequest.of(command.getPageNumber(), command.getPageSize(), sort);

    Page<DocumentPost> documentPostsPage = documentPostRepository.findDocumentPostsByFilter(
        command.getTitle(),
        command.getSubject(),
        command.getContent(),
        command.getDocumentTypes(),
        pageable
    );

    // 결과 로그 출력
    log.info("검색 결과 (Sort: {}): {}", sortType, documentPostsPage.getContent());

    // 추가적인 로그 출력 (예: 총 요소 수)
    log.info("총 게시글 수: {}", documentPostsPage.getTotalElements());
  }
}
