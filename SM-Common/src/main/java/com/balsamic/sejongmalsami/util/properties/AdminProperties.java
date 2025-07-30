package com.balsamic.sejongmalsami.util.properties;

import jakarta.annotation.PostConstruct;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Setter
@Component
@ConfigurationProperties(prefix = "admin")
public class AdminProperties {

  private List<String> ids; // 관리자 ID 목록

  private Set<String> adminIdSet; // 관리자 ID를 빠르게 조회하기 위한 Set

  @PostConstruct
  public void initAdminIds() {
    adminIdSet = new HashSet<>(ids); // 초기화 시 List를 Set으로 변환
  }

  /**
   * 주어진 ID가 관리자 ID인지 확인
   *
   * @param id 확인할 사용자 ID
   * @return 관리자 여부
   */
  public boolean isAdmin(String id) {
    return adminIdSet.contains(id); // ID가 Set에 포함되어 있는지 확인
  }
}
