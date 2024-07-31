package com.balsamic.sejongmalsami.member.domain.repository;

import com.balsamic.sejongmalsami.member.domain.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

}
