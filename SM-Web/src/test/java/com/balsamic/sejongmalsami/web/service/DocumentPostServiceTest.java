package com.balsamic.sejongmalsami.web.service;

import static com.balsamic.sejongmalsami.util.log.LogUtil.superLog;

import com.balsamic.sejongmalsami.application.test.TestDataGenerator;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.post.repository.postgres.DocumentFileRepository;
import com.balsamic.sejongmalsami.post.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.post.service.DocumentPostService;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

  @Autowired
  TestDataGenerator testDataGenerator;

  private Member member1;
  private Member member2;
  private DocumentPost post1;
  private DocumentPost post2;

  @BeforeEach
  void setUp() {
    Member member1 = testDataGenerator.createMockMember();

    Member member2 = testDataGenerator.createMockMember();

    DocumentPost post1 = testDataGenerator.createMockDocumentPost(member1);

    DocumentPost post2 = testDataGenerator.createMockDocumentPost(member1);

    DocumentFile file1 = testDataGenerator.createMockDocumentFile(member1, post1);

    DocumentFile file2 = testDataGenerator.createMockDocumentFile(member2, post2);

    superLog(member1);
    superLog(member2);
    superLog(post1);
    superLog(post2);
    superLog(file1);
    superLog(file2);
  }

  @Test
  public void mainTest() {
//    searchQuery("createDate"); // 기본 정렬: 생성일
//    searchQuery("likeCount");    // 좋아요 수 기준 정렬
//    searchQuery("viewCount");    // 조회수 기준 정렬
  }

//  public void searchQuery(SortType sortType) {
//    // DocumentCommand 객체 설정
//    DocumentCommand command = new DocumentCommand();
//    command.setTitle("자료 제목");
//    command.setSubject("수학");
//    command.setContent("자료 내용");
//    command.setPageNumber(0);
//    command.setPageSize(10);
//    command.setDocumentTypes(Arrays.asList(DocumentType.DOCUMENT)); // Set -> List로 변경
//    command.setSortType(sortType);
//
//    Sort sort;
//    if (sortType.equals(SortType.MOST_LIKED)) {
//      sort = Sort.by(Order.desc("likeCount"));
//    } else if (sortType.equals(SortType.VIEW_COUNT)) {
//      sort = Sort.by(Order.desc("viewCount"));
//    } else {
//      sort = Sort.by(Order.desc("createdDate"));
//    }
//    Pageable pageable = PageRequest.of(command.getPageNumber(), command.getPageSize(), sort);
//
//    Page<DocumentPost> documentPostsPage = documentPostRepository.findDocumentPostsByFilter(
//        command.getTitle(),
//        command.getSubject(),
//        command.getContent(),
//        command.getDocumentTypes(),
//        pageable
//    );
//
//    // 결과 로그 출력
//    log.info("검색 결과 (Sort: {}): {}", sortType, documentPostsPage.getContent());
//
//    // 추가적인 로그 출력 (예: 총 요소 수)
//    log.info("총 게시글 수: {}", documentPostsPage.getTotalElements());
//  }
}
