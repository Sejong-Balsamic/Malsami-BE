//// src/main/java/com/balsamic/sejongmalsami/service/admin/AdminApiService.java
//package com.balsamic.sejongmalsami.service.admin;
//
//import com.balsamic.sejongmalsami.dto.admin.StatisticsDto;
//import com.balsamic.sejongmalsami.dto.admin.SystemResourcesDto;
//import com.balsamic.sejongmalsami.dto.admin.WarningLogDto;
//import com.balsamic.sejongmalsami.object.constants.SystemType;
//import com.balsamic.sejongmalsami.object.postgres.Member;
//import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
//import com.balsamic.sejongmalsami.util.FileUtil;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//@Service
//@RequiredArgsConstructor
//public class AdminApiService {
//
//  private final MemberRepository memberRepository;
//
//  public AdminWebDto getStatistics() {
//    SystemType currentSystem = FileUtil.getCurrentSystem();
//    if (currentSystem == SystemType.LINUX) {
//      // 실제 데이터 로직
//      int totalMembers = memberRepository.count();
//      int activeUsers = (int) memberRepository.findAll().stream().filter(Member::getIsNotificationEnabled).count();
//      int totalPosts = 1000; // 예시
//      int todayPosts = 50;    // 예시
//      int dormantUsers = 100; // 예시
//      int newUsers = 25;      // 예시
//      return StatisticsDto.builder()
//          .totalMembers(totalMembers)
//          .activeUsers(activeUsers)
//          .totalPosts(totalPosts)
//          .todayPosts(todayPosts)
//          .dormantUsers(dormantUsers)
//          .newUsers(newUsers)
//          .build();
//    } else {
//      // 개발 환경: 랜덤 데이터
//      Random random = new Random();
//      return StatisticsDto.builder()
//          .totalMembers(100 + random.nextInt(900))
//          .activeUsers(50 + random.nextInt(450))
//          .totalPosts(500 + random.nextInt(1500))
//          .todayPosts(10 + random.nextInt(90))
//          .dormantUsers(20 + random.nextInt(80))
//          .newUsers(5 + random.nextInt(45))
//          .build();
//    }
//  }
//
//  public SystemResourcesDto getSystemResources() {
//    SystemType currentSystem = FileUtil.getCurrentSystem();
//    if (currentSystem == SystemType.LINUX) {
//      // 실제 시스템 리소스 로직
//      double cpuUsage = getActualCpuUsage();
//      double memoryUsage = getActualMemoryUsage();
//      double diskUsage = getActualDiskUsage();
//      return SystemResourcesDto.builder()
//          .cpuUsage(cpuUsage)
//          .memoryUsage(memoryUsage)
//          .diskUsage(diskUsage)
//          .build();
//    } else {
//      // 개발 환경: 랜덤 데이터
//      Random random = new Random();
//      return SystemResourcesDto.builder()
//          .cpuUsage(10 + (90 - 10) * random.nextDouble())
//          .memoryUsage(20 + (80 - 20) * random.nextDouble())
//          .diskUsage(30 + (70 - 30) * random.nextDouble())
//          .build();
//    }
//  }
//
//  public List<WarningLogDto> getWarningLogs() {
//    SystemType currentSystem = FileUtil.getCurrentSystem();
//    if (currentSystem == SystemType.LINUX) {
//      // 실제 경고 로그 로직
//      return fetchActualWarningLogs();
//    } else {
//      // 개발 환경: 랜덤 로그
//      List<WarningLogDto> logs = new ArrayList<>();
//      Random random = new Random();
//      int logCount = random.nextInt(5); // 최대 4개의 로그
//      for (int i = 0; i < logCount; i++) {
//        logs.add(WarningLogDto.builder()
//            .title("임의 경고 " + (i + 1))
//            .message("이것은 개발 환경에서 생성된 임의의 경고 메시지입니다.")
//            .timestamp(java.time.LocalDateTime.now().minusMinutes(random.nextInt(60)))
//            .build());
//      }
//      return logs;
//    }
//  }
//
//  // 실제 시스템 리소스 데이터를 가져오는 메소드 (예시)
//  private double getActualCpuUsage() {
//    // 서버의 실제 CPU 사용량을 가져오는 로직을 구현
//    return 75.5; // 예시 값
//  }
//
//  private double getActualMemoryUsage() {
//    // 서버의 실제 메모리 사용량을 가져오는 로직을 구현
//    return 65.3; // 예시 값
//  }
//
//  private double getActualDiskUsage() {
//    // 서버의 실제 디스크 사용량을 가져오는 로직을 구현
//    return 55.7; // 예시 값
//  }
//
//  // 실제 경고 로그를 가져오는 메소드 (예시)
//  private List<WarningLogDto> fetchActualWarningLogs() {
//    List<WarningLogDto> logs = new ArrayList<>();
//    logs.add(WarningLogDto.builder()
//        .title("CPU 사용량 경고")
//        .message("CPU 사용량이 80%를 초과했습니다.")
//        .timestamp(java.time.LocalDateTime.now().minusMinutes(10))
//        .build());
//    logs.add(WarningLogDto.builder()
//        .title("메모리 사용량 경고")
//        .message("메모리 사용량이 80%를 초과했습니다.")
//        .timestamp(java.time.LocalDateTime.now().minusMinutes(5))
//        .build());
//    return logs;
//  }
//}
