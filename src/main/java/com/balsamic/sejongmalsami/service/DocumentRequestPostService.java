package com.balsamic.sejongmalsami.service;

import static com.balsamic.sejongmalsami.util.LogUtils.lineLog;
import static com.balsamic.sejongmalsami.util.LogUtils.superLog;

import com.balsamic.sejongmalsami.object.DocumentCommand;
import com.balsamic.sejongmalsami.object.DocumentDto;
import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.Faculty;
import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.postgres.DocumentRequestPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.postgres.CourseRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentRequestPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DocumentRequestPostService {

  private static final Integer DOCUMENT_TYPE_LIMIT = 3;

  private final DocumentRequestPostRepository documentRequestPostRepository;
  private final MemberRepository memberRepository;
  private final YeopjeonService yeopjeonService;
  private final CourseRepository courseRepository;

  /**
   * <h3>자료요청 글 작성</h3>
   * <p>자료요청 게시판은 '중인(엽전 수 : 1000개)' 이상 접근 가능합니다.</p>
   * <p>학과는 기본적으로 로그인한 사용자의 학과를 선택합니다.</p>
   *
   * @param command memberId, title, content, faculty, documentTypes, isPrivate
   * @return
   */
  public DocumentDto createPost(DocumentCommand command) {

    // 사용자 확인
    Member member = memberRepository.findById(command.getMemberId())
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

    // '중인' (엽전수 1000개)이상 접근 가능
    Yeopjeon yeopjeon = yeopjeonService.findMemberYeopjeon(member);
    if (yeopjeon.getYeopjeon() < 1000) {
      log.error("자료요청게시판은 중인 이상 접근이 가능합니다. {} 의 엽전 수: {}",
          member.getStudentId(),
          yeopjeon.getYeopjeon());
      throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
    }

    // 입력한 교과목명에 따른 단과대 설정 (교과목명 존재할 경우)
    List<Faculty> faculties = null;
    if (command.getSubject() != null) {
      faculties = courseRepository
          .findAllBySubject(command.getSubject())
          .stream().map(Course::getFaculty).collect(Collectors.toList());
      log.info("입력된 교과목명 : {}", command.getSubject());
      log.info("단과대 List : {}", faculties);

      if (faculties.isEmpty()) {
        throw new CustomException(ErrorCode.FACULTY_NOT_FOUND);
      }
    }

    // 자료 타입 추가 (선택)
    List<DocumentType> documentTypes = null;
    if (!command.getDocumentTypes().isEmpty()) {
      if (command.getDocumentTypes().size() < DOCUMENT_TYPE_LIMIT) {
        documentTypes = command.getDocumentTypes();
      } else {
        throw new CustomException(ErrorCode.DOCUMENT_TYPE_LIMIT_EXCEEDED);
      }
    }

    DocumentRequestPost documentRequestPost = DocumentRequestPost.builder()
        .member(member)
        .title(command.getTitle())
        .content(command.getContent())
        .subject(command.getSubject())
        .faculties(faculties)
        .documentTypes(documentTypes)
        .viewCount(0)
        .likeCount(0)
        .commentCount(0)
        .isPrivate(Boolean.TRUE.equals(command.getIsPrivate()))
        .build();

    lineLog(null);
    superLog(documentRequestPost);
    lineLog(null);

    return DocumentDto.builder()
        .documentRequestPost(documentRequestPost)
        .build();
  }

}
