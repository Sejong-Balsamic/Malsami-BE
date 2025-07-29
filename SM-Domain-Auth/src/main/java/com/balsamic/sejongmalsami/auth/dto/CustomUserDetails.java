package com.balsamic.sejongmalsami.auth.dto;

import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.Role;
import com.balsamic.sejongmalsami.object.postgres.Member;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
public class CustomUserDetails implements UserDetails {

  private final Member member;
  private Map<String, Object> attributes;

  public CustomUserDetails(Member member) {
    this.member = member;
  }

  public CustomUserDetails(Member member, Map<String, Object> attributes) {
    this.member = member;
    this.attributes = attributes;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    Set<Role> roles = member.getRoles();
    return roles.stream()
        .map(role -> new SimpleGrantedAuthority(role.name()))
        .collect(Collectors.toSet());
  }

  @Override
  public String getPassword() {
    return null; // OAuth2 기반의 인증이라 비밀번호가 없음
  }

  @Override
  public String getUsername() {
    return member.getMemberId().toString(); // UUID를 문자열로 변환하여 반환
  }

  @Override
  public boolean isAccountNonExpired() {
    // AccountStatus가 DELETED인 경우, 계정이 만료된 것으로 간주
    return member.getAccountStatus() != AccountStatus.DELETED;
  }

  @Override
  public boolean isAccountNonLocked() {
    // AccountStatus가 DELETED인 경우에만 계정을 잠긴 것으로 간주
    return member.getAccountStatus() != AccountStatus.DELETED;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true; // 인증 정보는 항상 유효함
  }

  @Override
  public boolean isEnabled() {
    // AccountStatus가 ACTIVE인 경우에만 계정이 활성화됨
    return member.getAccountStatus() != AccountStatus.DELETED;
  }

  // 사용자 PK 반환
  public UUID getMemberId() {
    return member.getMemberId();
  }
}
