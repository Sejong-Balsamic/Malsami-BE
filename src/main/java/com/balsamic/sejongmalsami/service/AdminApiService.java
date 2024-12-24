package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.CommonUtil;
import com.balsamic.sejongmalsami.object.AdminCommand;
import com.balsamic.sejongmalsami.object.AdminDto;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.object.MemberYeopjeon;
import com.balsamic.sejongmalsami.object.constants.YeopjeonAction;
import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.TestMember;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.mongo.YeopjeonHistoryRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.TestMemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.YeopjeonRepository;
import com.balsamic.sejongmalsami.util.LogUtil;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminApiService {

  private final MemberRepository memberRepository;
  private final YeopjeonRepository yeopjeonRepository;
  private final TestMemberRepository testMemberRepository;
  private final PasswordEncoder passwordEncoder;
  private final YeopjeonHistoryRepository yeopjeonHistoryRepository;

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

  @Transactional
  public AdminDto manageYeopjeon(AdminCommand command) {
    UUID memberId = CommonUtil.toUUID(command.getMemberIdStr());
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    Integer amount = command.getAmount();

    // 엽전 정보 조회
    Yeopjeon yeopjeon = yeopjeonRepository.findByMember(member)
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
        .memberId(member.getMemberId())
        .yeopjeonChange(amount)
        .yeopjeonAction(YeopjeonAction.ADMIN_ADJUST)
        .resultYeopjeon(newYeopjeon)
        .content("관리자: " + member.getStudentName() + ": " +  member.getStudentId())
        .build();
    yeopjeonHistoryRepository.save(yeopjeonHistory);

    // 로깅
    log.info("관리자 엽전 조정 - 학번: {}, 변동량: {}, 최종잔액: {}",
        member.getStudentId(), amount, newYeopjeon);

    return AdminDto.builder()
        .member(member)
        .yeopjeon(yeopjeon)
        .yeopjeonHistory(yeopjeonHistory)
        .build();
  }

  public AdminDto getMyYeopjeonInfo(Member member) {

    return AdminDto.builder()
        .yeopjeon(yeopjeonRepository.findByMember(member).get())
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
}
