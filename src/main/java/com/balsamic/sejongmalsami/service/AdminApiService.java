package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.CommonUtil;
import com.balsamic.sejongmalsami.object.AdminCommand;
import com.balsamic.sejongmalsami.object.AdminDto;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.object.MemberYeopjeon;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.QuestionPostCustomTag;
import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.object.postgres.Course;
import com.balsamic.sejongmalsami.object.postgres.Faculty;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.QuestionPost;
import com.balsamic.sejongmalsami.object.postgres.ServerErrorCode;
import com.balsamic.sejongmalsami.object.postgres.TestMember;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.mongo.QuestionBoardLikeRepository;
import com.balsamic.sejongmalsami.repository.mongo.QuestionPostCustomTagRepository;
import com.balsamic.sejongmalsami.repository.mongo.YeopjeonHistoryRepository;
import com.balsamic.sejongmalsami.repository.postgres.CourseRepository;
import com.balsamic.sejongmalsami.repository.postgres.FacultyRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.QuestionPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.ServerErrorCodeRepository;
import com.balsamic.sejongmalsami.repository.postgres.TestMemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.YeopjeonRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.balsamic.sejongmalsami.util.init.CourseService;
import com.balsamic.sejongmalsami.util.log.LogUtil;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminApiService {

  private final MemberRepository memberRepository;
  private final YeopjeonRepository yeopjeonRepository;
  private final TestMemberRepository testMemberRepository;
  private final PasswordEncoder passwordEncoder;
  private final YeopjeonHistoryRepository yeopjeonHistoryRepository;
  private final FacultyRepository facultyRepository;
  private final CourseRepository courseRepository;
  private final CourseService courseService;
  private final ServerErrorCodeRepository serverErrorCodeRepository;
  private final QuestionPostRepository questionPostRepository;
  private final QuestionPostCustomTagRepository questionPostCustomTagRepository;
  private final QuestionBoardLikeRepository questionBoardLikeRepository;

  /**
   * =========================================== 회원 관리 로직 ===========================================
   */

  public AdminDto processUuidPacchingko(AdminCommand command) {
    // member 가져오기
    Member member = command.getMember();

    // 엽전 가져오기
    Yeopjeon yeopjeon = yeopjeonRepository.findByMember(member)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));

    // 엽전 -1 가능한지 확인 후 -1
    if (yeopjeon.getYeopjeon() < 1) {
      throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
    } else {
      yeopjeon.setYeopjeon(yeopjeon.getYeopjeon() - 1);
      yeopjeonRepository.save(yeopjeon);
    }

    // member uuid 변경 후 저장
    String newUuidNickName = UUID.randomUUID().toString().substring(0, 6);
    member.setUuidNickname(newUuidNickName);

    // 로깅
    LogUtil.lineLog("새로운UUID : " + member.getStudentId() + " : " + newUuidNickName);

    return AdminDto.builder()
        .member(member)
        .yeopjeon(yeopjeon)
        .build();
  }

  // 회원 관리 : 필터링 검색
  public MemberDto getFilteredMembers(MemberCommand command) {
    return MemberDto.builder()
        .membersPage(
            memberRepository.findAllDynamic(
                command.getStudentId(),
                command.getStudentName(),
                command.getUuidNickname(),
                command.getMajor(),
                command.getAcademicYear(),
                command.getEnrollmentStatus(),
                command.getAccountStatus(),
                command.getRole(),
                command.getLastLoginStart(),
                command.getLastLoginEnd(),
                command.getIsFirstLogin(),
                command.getIsEdited(),
                command.getIsDeleted(),
                PageRequest.of(
                    command.getPageNumber(),
                    command.getPageSize(),
                    Sort.by(Sort.Direction.fromString(command.getSortDirection()),
                        command.getSortField())
                )
            )
        )
        .build();
  }

  public MemberDto getMemberByMemberIdStr(MemberCommand command) {
    return MemberDto.builder()
        .member(memberRepository.findById(CommonUtil.toUUID(command.getMemberIdStr()))
            .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND)))
        .build();
  }

  public MemberDto createTestMember(MemberCommand command) {
    TestMember testMember =
        testMemberRepository.save(
            TestMember.builder()
                .testStudentId(command.getStudentId())
                .password(passwordEncoder.encode(command.getSejongPortalPassword()))
                .testStudentName(command.getStudentName())
                .testMajor(command.getMajor())
                .testAcademicYear(command.getAcademicYear())
                .testEnrollmentStatus(command.getEnrollmentStatus())
                .createdBy(command.getMember())
                .build());
    log.info("테스트 회원 생성: 테스트회원학번: {}, 생성자: {}", testMember.getTestStudentId(),
        testMember.getCreatedBy().getStudentName());
    return MemberDto.builder()
        .testMember(testMember)
        .build();
  }

  public MemberDto getFilteredTestMembers(MemberCommand command) {
    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        Sort.by(Sort.Direction.fromString(command.getSortDirection()), command.getSortField())
    );

    Page<TestMember> testMembersPage = testMemberRepository.findAllDynamic(
        command.getStudentId(),
        command.getStudentName(),
        pageable
    );

    return MemberDto.builder()
        .testMembersPage(testMembersPage)
        .build();
  }

  /**
   * =========================================== 엽전 관리 로직 ===========================================
   */

  @Transactional
  public AdminDto manageYeopjeon(AdminCommand command) {
    UUID memberId = CommonUtil.toUUID(command.getMemberIdStr());
    // Yeopjeon Target Member
    Member targetMember = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Integer amount = command.getAmount();

    // 관리자
    Member adminMember = command.getMember();

    // 엽전 정보 조회
    Yeopjeon yeopjeon = yeopjeonRepository.findByMember(targetMember)
        .orElseThrow(() -> new CustomException(ErrorCode.YEOPJEON_NOT_FOUND));

    // 감소할 경우 잔액 체크
    if (amount < 0 && yeopjeon.getYeopjeon() + amount < 0) {
      throw new CustomException(ErrorCode.INSUFFICIENT_YEOPJEON);
    }

    // 엽전 수정
    Integer currentYeopjeon = yeopjeon.getYeopjeon();
    Integer newYeopjeon = currentYeopjeon + amount;
    yeopjeon.setYeopjeon(newYeopjeon);
    yeopjeonRepository.save(yeopjeon);

    // 엽전 이력 기록
    YeopjeonHistory yeopjeonHistory = YeopjeonHistory.builder()
        .memberId(targetMember.getMemberId())
        .yeopjeonChange(amount)
        .yeopjeonAction(YeopjeonAction.ADMIN_ADJUST)
        .resultYeopjeon(newYeopjeon)
        .content("관리자: " + adminMember.getStudentName() + ": " + adminMember.getStudentId())
        .build();
    yeopjeonHistoryRepository.save(yeopjeonHistory);

    // 로깅
    log.info("관리자 엽전 조정 - 학번: {}, 변동량: {}, 최종잔액: {}",
        adminMember.getStudentId(), amount, newYeopjeon);

    return AdminDto.builder()
        .member(targetMember)
        .yeopjeon(yeopjeon)
        .yeopjeonHistory(yeopjeonHistory)
        .build();
  }

  public AdminDto getMyYeopjeonInfo(Member member) {

    return AdminDto.builder()
        .yeopjeon(yeopjeonRepository.findByMember(member).get())
        .build();
  }

  public AdminDto getFilteredMembersAndYeopjeons(AdminCommand command) {
    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        Sort.by(Sort.Direction.fromString(command.getSortDirection()), command.getSortField())
    );

    String studentName = CommonUtil.nullIfBlank(command.getStudentName());
    String uuidNickname = CommonUtil.nullIfBlank(command.getUuidNickname());
    UUID memberId = CommonUtil.toUUID(command.getMemberIdStr());

    Page<MemberYeopjeon> memberYeopjeonPage = memberRepository.findMemberYeopjeon(
        command.getStudentId(),
        studentName,
        uuidNickname,
        memberId,
        pageable
    );

    return AdminDto.builder()
        .memberYeopjeonPage(memberYeopjeonPage)
        .build();
  }

  /**
   * =========================================== 에러코드 관리 로직 ===========================================
   */

  public AdminDto getFilteredServerErrorCode(AdminCommand command) {
    // 검색 파라미터
    String errorCode = CommonUtil.nullIfBlank(command.getErrorCode());
    String httpStatusMessage = CommonUtil.nullIfBlank(command.getHttpStatusMessage());
    String message = CommonUtil.nullIfBlank(command.getMessage());
    Integer httpStatusCode = command.getHttpStatusCode();

    // 동적 Pageable
    Pageable pageable = createPageable(command, 100, "errorCode");

    // 검색
    Page<ServerErrorCode> serverErrorCodePage = serverErrorCodeRepository.findAllDynamic(
        errorCode,
        httpStatusCode,
        httpStatusMessage,
        message,
        pageable
    );

    return AdminDto.builder()
        .serverErrorCodesPage(serverErrorCodePage)
        .build();
  }

  /**
   * =========================================== 교과목 관리 로직 ===========================================
   */

  /**
   * <h3>DB에 저장된 모든 단과대 조회</h3>
   *
   * @return
   */
  @Transactional(readOnly = true)
  public AdminDto getAllFaculties() {
    List<Faculty> faculties = facultyRepository.findByIsActiveTrue();

    return AdminDto.builder()
        .faculties(faculties)
        .build();
  }

  /**
   * 교과목 연도 및 학기 조회
   *
   * @return
   */
  @Transactional(readOnly = true)
  public AdminDto getSubjectYearAndSemester() {
    List<Integer> years = courseRepository.findDistinctYears();
    List<Integer> semesters = courseRepository.findDistinctSemesters();

    return AdminDto.builder()
        .years(years)
        .semesters(semesters)
        .build();
  }

  /**
   * <h3>교과목 필터링</h3>
   *
   * @param command subject, faculty, year, semester, pageNumber, pageSize, sortDirection, sortField
   * @return
   */
  @Transactional(readOnly = true)
  public AdminDto getFilteredSubjects(AdminCommand command) {

    Sort sort = Sort.by(Sort.Direction.fromString(command.getSortDirection()), command.getSortField());

    Pageable pageable = PageRequest.of(
        command.getPageNumber(),
        command.getPageSize(),
        sort
    );

    Page<Course> coursePage = courseRepository.findAllByFiltered(
        (command.getSubject() == null || command.getSubject().isBlank() ? null : command.getSubject()),
        (command.getFaculty() == null || command.getFaculty().isBlank() ? null : command.getFaculty()),
        command.getYear(),
        command.getSemester(),
        pageable);

    return AdminDto.builder()
        .coursePage(coursePage)
        .build();
  }

  /**
   * <h3>교과목명 자동완성</h3>
   *
   * @param command String subject
   * @return
   */
  @Transactional(readOnly = true)
  public AdminDto subjectAutoComplete(AdminCommand command) {
    List<Course> courses = courseRepository.findBySubjectContainingIgnoreCase(command.getSubject());

    List<String> subjects = courses.stream()
        .map(Course::getSubject)
        .distinct()
        .collect(Collectors.toList());

    return AdminDto.builder()
        .subjects(subjects)
        .build();
  }

  /**
   * 교과목 엑셀파일 업로드
   *
   * @param command file
   * @return
   */
  @Transactional
  public AdminDto uploadCourseExcelFile(AdminCommand command) {

    MultipartFile multipartFile = command.getMultipartFile();
    if (multipartFile == null || multipartFile.isEmpty()) {
      throw new CustomException(ErrorCode.FILE_NOT_FOUND);
    }

    try {
      String originalFilename = multipartFile.getOriginalFilename();
      File tempFile = new File(System.getProperty("java.io.tmpdir") + File.separator + originalFilename);
      multipartFile.transferTo(tempFile);
      courseService.parseAndSaveCourses(tempFile);
      tempFile.deleteOnExit();
      return AdminDto.builder()
          .fileName(originalFilename)
          .build();
    } catch (IOException e) {
      throw new CustomException(ErrorCode.COURSE_SAVE_ERROR);
    }
  }

  /**
   * =========================================== 게시글 관리 로직 ===========================================
   */

  @Transactional(readOnly = true)
  public AdminDto getFilteredQuestionPost(AdminCommand command) {
    // 1) 페이징/정렬 정보 생성
    Pageable pageable = createPageable(command, 10, "createdDate");

    // 2) QuestionPost 검색
    Page<QuestionPost> questionPostPage = questionPostRepository.findAllDynamicQuestionPosts(
        CommonUtil.nullIfBlank(command.getQuery()),
        CommonUtil.nullIfBlank(command.getSubject()),
        CommonUtil.nullIfBlank(command.getFaculty()),
        command.getChaetaekStatus() == null ? "ALL" : command.getChaetaekStatus().name(),
        command.getQuestionPresetTags() == null || command.getQuestionPresetTags().isEmpty()
            ? null
            : command.getQuestionPresetTags(),
        pageable
    );

    if (questionPostPage.isEmpty()) {
      // 결과가 없으면 바로 반환
      return AdminDto.builder()
          .questionPostPage(questionPostPage)
          .build();
    }

    // 3) 가져온 QuestionPost들의 questionPostId Set 추출
    Set<UUID> questionPostIds = questionPostPage.stream()
        .map(QuestionPost::getQuestionPostId)
        .collect(Collectors.toSet());

    // 4) Mongo에서 CustomTag 조회
    List<QuestionPostCustomTag> customTagList =
        questionPostCustomTagRepository.findAllByQuestionPostIdIn(questionPostIds);

    // 5) 커스텀태그를 Map<questionPostId, List<String>> 로 만들기
    Map<UUID, List<String>> customTagMap = customTagList.stream()
        .collect(Collectors.groupingBy(
            QuestionPostCustomTag::getQuestionPostId,
            Collectors.mapping(QuestionPostCustomTag::getCustomTag, Collectors.toList())
        ));

    // 6) 각 QuestionPost 엔티티에 customTags 세팅(@Transient 필드)
    questionPostPage.getContent().forEach(qp -> {
      List<String> tags = customTagMap.getOrDefault(qp.getQuestionPostId(), Collections.emptyList());
      qp.setCustomTags(tags);
    });

    // 7) 이제 questionPostPage 를 그대로 AdminDto 에 담아 리턴하면,
    //    Jackson(또는 Gson)이 JSON 변환 시 @Transient 필드도 포함해서 보내줄 수 있음.
    return AdminDto.builder()
        .questionPostPage(questionPostPage)
        .build();
  }

  /**
   * =========================================== private method ===========================================
   */

  private Pageable createPageable(AdminCommand command, int defaultPageSize, String defaultSortField) {
    // 1) pageNumber, pageSize
    int pageNumber = (command.getPageNumber() != null) ? command.getPageNumber() : 0;
    int pageSize = (command.getPageSize() != null) ? command.getPageSize() : defaultPageSize;

    // 2) sortField, sortDirection
    String sortField = (command.getSortField() != null) ? command.getSortField() : defaultSortField;
    String sortDirStr = (command.getSortDirection() != null) ? command.getSortDirection().toUpperCase() : "DESC";

    // 3) Sort Direction 파싱
    Sort.Direction direction;
    try {
      direction = Sort.Direction.valueOf(sortDirStr); // "ASC" or "DESC"
    } catch (Exception e) {
      direction = Sort.Direction.DESC; // fallback
    }

    // 4) Sort 객체
    Sort sort = Sort.by(direction, sortField);

    // 5) 최종 Pageable
    return PageRequest.of(pageNumber, pageSize, sort);
  }
}
