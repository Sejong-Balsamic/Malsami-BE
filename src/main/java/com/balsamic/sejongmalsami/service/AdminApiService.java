package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.AdminCommand;
import com.balsamic.sejongmalsami.object.AdminDto;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.TestMember;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.TestMemberRepository;
import com.balsamic.sejongmalsami.repository.postgres.YeopjeonRepository;
import com.balsamic.sejongmalsami.util.LogUtils;
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

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminApiService {

  private final MemberRepository memberRepository;
  private final YeopjeonRepository yeopjeonRepository;
  private final TestMemberRepository testMemberRepository;
  private final PasswordEncoder passwordEncoder;

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
    LogUtils.lineLog("새로운UUID : " + member.getStudentId() + " : " + newUuidNickName);

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
    log.info("테스트 회원 생성: 테스트회원학번: {}, 생성자: {}", testMember.getTestStudentId(), testMember.getCreatedBy().getStudentName());
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
}
