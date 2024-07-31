package com.balsamic.sejongmalsami.member.application;

import com.balsamic.sejongmalsami.common.config.auth.dto.response.SejongStudentAuthResponse;
import com.balsamic.sejongmalsami.member.domain.entity.Member;
import com.balsamic.sejongmalsami.member.domain.repository.MemberRepository;
import com.balsamic.sejongmalsami.member.dto.response.CreateMemberResponse;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class MemberService {
  private final MemberRepository memberRepository;

  public CreateMemberResponse createMember(SejongStudentAuthResponse response){
    Member savedMember = memberRepository.save(
        Member.builder()
        .studentId(Long.parseLong(response.getStudentIdString()))
        .studentName(response.getStudentName())
        .uuidNickname(UUID.randomUUID().toString().substring(0, 6))
        .major(response.getMajor())
        .academicYear(response.getAcademicYear())
        .enrollmentStatus(response.getEnrollmentStatus())
        .build()
    );

    log.info("saved member : {}", savedMember.toString());
    return CreateMemberResponse.from(savedMember);
  }

}
