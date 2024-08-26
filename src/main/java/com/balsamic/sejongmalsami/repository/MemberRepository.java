package com.balsamic.sejongmalsami.repository;

import com.balsamic.sejongmalsami.object.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

}
