package com.balsamic.sejongmalsami.util.config;

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
public class AdminConfig {

  private List<String> ids;

  private Set<String> adminIdSet;

  @PostConstruct
  public void initAdminIds() {
    adminIdSet = new HashSet<>(ids);
  }

  public boolean isAdmin(String id) {
    return adminIdSet.contains(id);
  }
}
