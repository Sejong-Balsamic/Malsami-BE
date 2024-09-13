package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.Member;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.repository.MemberRepository;
import com.balsamic.sejongmalsami.util.SejongPortalAuthenticator;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService implements UserDetailsService {
  private final MemberRepository memberRepository;
  private final SejongPortalAuthenticator sejongPortalAuthenticator;

  @Override
  public CustomUserDetails loadUserByUsername(String stringMemberId)
      throws UsernameNotFoundException {
    UUID memberId;
    try {
      memberId = UUID.fromString(stringMemberId);
    } catch (IllegalArgumentException e) {
      throw new UsernameNotFoundException("유효하지 않은 UUID 형식입니다.");
    }

    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    return new CustomUserDetails(member);
  }

  public MemberDto createMember(MemberCommand command) {
    MemberDto dto = sejongPortalAuthenticator.getMemberAuthInfos(command);

    Member savedMember = memberRepository.save(
        Member.builder()
            .studentId(Long.parseLong(dto.getStudentIdString()))
            .studentName(dto.getStudentName())
            .uuidNickname(UUID.randomUUID().toString().substring(0, 6))
            .major(dto.getMajor())
            .academicYear(dto.getAcademicYear())
            .enrollmentStatus(dto.getEnrollmentStatus())
            .build());
    return MemberDto.builder()
        .member(savedMember)
        .build();
  }
}
