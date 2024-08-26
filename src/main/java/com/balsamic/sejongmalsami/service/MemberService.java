package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.Member;
import com.balsamic.sejongmalsami.object.MemberCommand;
import com.balsamic.sejongmalsami.object.MemberDto;
import com.balsamic.sejongmalsami.repository.MemberRepository;
import com.balsamic.sejongmalsami.util.SejongPortalAuthenticator;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
  private final MemberRepository memberRepository;
  private final SejongPortalAuthenticator sejongPortalAuthenticator;

  public MemberDto createMember(MemberCommand command){
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
