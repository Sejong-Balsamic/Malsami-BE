package com.balsamic.sejongmalsami.util.init;

import com.balsamic.sejongmalsami.object.constants.HashType;
import com.balsamic.sejongmalsami.object.constants.SystemType;
import com.balsamic.sejongmalsami.object.postgres.Department;
import com.balsamic.sejongmalsami.object.postgres.Faculty;
import com.balsamic.sejongmalsami.repository.postgres.DepartmentRepository;
import com.balsamic.sejongmalsami.repository.postgres.FacultyRepository;
import com.balsamic.sejongmalsami.service.HashRegistryService;
import com.balsamic.sejongmalsami.util.FileUtil;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class DepartmentService {

  private final DepartmentRepository departmentRepository;
  private final FacultyRepository facultyRepository;
  private final ObjectMapper objectMapper;
  private final HashRegistryService hashRegistryService;

  /**
   * JSON 파일을 로드하고 Faculty 및 Department 데이터를 저장합니다. 파일의 해시값을 확인하여 중복 처리를 방지합니다.
   *
   * @param filePath JSON 파일의 경로
   */
  @Transactional
  public void loadDepartments(Path filePath) {
    String fileName = filePath.getFileName().toString();
    String fileHash = calculateFileHash(filePath);

    log.info("파일 로드 시작: {}", fileName);
    LocalDateTime startTime = LocalDateTime.now();

    try {
      // 기존 해시값 조회
      String storedHash = hashRegistryService.getHashValue(HashType.DEPARTMENT_JSON);

      if (storedHash != null && storedHash.equals(fileHash)) {
        log.info("이미 처리된 파일입니다. 파일 이름: {}, 해시: {}", fileName, fileHash);
        return;
      } else {
        if (storedHash != null) {
          log.info("파일이 변경되었거나 이전 처리에 실패했습니다. 재처리 진행: {}", fileName);
        } else {
          log.info("처음으로 파일을 처리합니다: {}", fileName);
        }
      }

      // JSON 파일 읽기
      InputStream inputStream = Files.newInputStream(filePath);
      JsonNode rootNode = objectMapper.readTree(inputStream);

      List<Department> departments = new ArrayList<>();

      // 'dl_code_deptCdGrid' 배열 추출
      JsonNode deptCdGridNode = rootNode.path("dl_code_deptCdGrid");
      if (!deptCdGridNode.isMissingNode() && deptCdGridNode.isArray()) {
        for (JsonNode deptNode : deptCdGridNode) {
          Department department = objectMapper.treeToValue(deptNode, Department.class);
          departments.add(department);
        }
      } else {
        log.warn("'dl_code_deptCdGrid' 배열을 찾을 수 없습니다.");
      }

      // 'dl_code_deptCd' 배열 추출
      JsonNode deptCdNode = rootNode.path("dl_code_deptCd");
      if (!deptCdNode.isMissingNode() && deptCdNode.isArray()) {
        for (JsonNode deptNode : deptCdNode) {
          Department department = objectMapper.treeToValue(deptNode, Department.class);

          // deptCd 검증
          if (department.getDeptCd() == null || department.getDeptCd().trim().isEmpty()) {
            log.warn("Department 객체의 deptCd가 설정되지 않았습니다: {}", department);
            continue; // 유효하지 않은 데이터는 건너뜁니다
          }

          departments.add(department);
        }
      } else {
        log.warn("'dl_code_deptCd' 배열을 찾을 수 없습니다.");
      }

      if (departments.isEmpty()) {
        log.warn("저장할 Department 데이터가 없습니다.");
      } else {
        // 0. 사전에 isActive() 설정
        for (Department dept : departments) {
          boolean isActive = (dept.getCloseDt() == null && "Y".equals(dept.getLastDeptYn()));
          dept.setIsActive(isActive);
        }

        // 1. Faculty 정보 추출 및 저장
        Set<String> facultyNames = departments.stream()
            .filter(Department::getIsActive) // 활성화된 Department만 처리
            .map(Department::getDeptLDegree) // 'dept_l_degree' 필드를 facultyName으로 사용
            .filter(Objects::nonNull)
            .map(String::trim)
            .collect(Collectors.toSet());

        // 기존 Faculty 조회
        List<Faculty> existingFaculties = facultyRepository.findByNameIn(facultyNames);
        Map<String, Faculty> existingFacultyMap = existingFaculties.stream()
            .collect(Collectors.toMap(Faculty::getName, Function.identity()));

        List<Faculty> facultiesToSave = new ArrayList<>();

        for (String facultyName : facultyNames) {
          if (!existingFacultyMap.containsKey(facultyName)) {
            Faculty faculty = Faculty.builder()
                .name(facultyName)
                .build();
            facultiesToSave.add(faculty);
            log.info("새로운 Faculty 추가: {}", facultyName);
          }
        }

        if (!facultiesToSave.isEmpty()) {
          facultyRepository.saveAll(facultiesToSave);
          log.info("새로운 Faculty 저장 완료: {}개", facultiesToSave.size());
        }

        // 업데이트된 Faculty 목록 다시 조회
        existingFaculties = facultyRepository.findByNameIn(facultyNames);
        existingFacultyMap = existingFaculties.stream()
            .collect(Collectors.toMap(Faculty::getName, Function.identity()));

        // 2. Department 정보 업데이트 및 저장
        // deptCd 리스트 추출
        List<String> deptCds = departments.stream()
            .map(Department::getDeptCd)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // 기존 Department 데이터 조회
        List<Department> existingDepartments = departmentRepository.findByDeptCdIn(deptCds);
        Map<String, Department> existingDeptMap = existingDepartments.stream()
            .collect(Collectors.toMap(
                Department::getDeptCd,
                Function.identity(),
                (existing, duplicate) -> {
                  log.warn("중복된 deptCd 발견: {}, 이름: {} {} {}",
                      duplicate.getDeptCd(),
                      duplicate.getDeptSPrint(),
                      duplicate.getDeptMPrint(),
                      duplicate.getDeptLPrint());
                  return existing; // 기존 값을 유지
                }
            ));


        List<Department> departmentsToSave = new ArrayList<>();

        for (Department dept : departments) {
          if (dept.getDeptCd() == null || dept.getDeptCd().trim().isEmpty()) {
            continue; // 유효하지 않은 데이터는 건너뜁니다
          }

          // Faculty 매핑
          String facultyName = dept.getDeptLDegree();
          Faculty faculty = existingFacultyMap.get(facultyName);
          if (faculty == null) {
            log.warn("해당 Faculty를 찾을 수 없습니다: {}", facultyName);
            continue; // Faculty가 없으면 Department를 저장하지 않음
          }

          if (existingDeptMap.containsKey(dept.getDeptCd())) {
            Department existingDept = existingDeptMap.get(dept.getDeptCd());

            // 기존 부서의 필드를 업데이트
            existingDept.setKeyDeptCd(dept.getKeyDeptCd());
            existingDept.setAdmDeptAliasEng(dept.getAdmDeptAliasEng());
            existingDept.setDeptDegreeEng(dept.getDeptDegreeEng());
            existingDept.setDeptAlias(dept.getDeptAlias());
            existingDept.setDeptLDegreeEng(dept.getDeptLDegreeEng());
            existingDept.setDeptPrintEng(dept.getDeptPrintEng());
            existingDept.setDeptNm(dept.getDeptNm());
            existingDept.setDeptSCd(dept.getDeptSCd());
            existingDept.setDeptLPrintEng(dept.getDeptLPrintEng());
            existingDept.setDeptSAlias(dept.getDeptSAlias());
            existingDept.setDeptPrint(dept.getDeptPrint());
            existingDept.setDeptMPrint(dept.getDeptMPrint());
            existingDept.setDeptMPrintEng(dept.getDeptMPrintEng());
            existingDept.setAdmDeptAlias(dept.getAdmDeptAlias());
            existingDept.setDeptMvinYn(dept.getDeptMvinYn());
            existingDept.setDeptMAlias(dept.getDeptMAlias());
            existingDept.setSchDeptAliasEng(dept.getSchDeptAliasEng());
            existingDept.setCloseDt(dept.getCloseDt());
            existingDept.setDeptAliasEng(dept.getDeptAliasEng());
            existingDept.setDeptMDegreeEng(dept.getDeptMDegreeEng());
            existingDept.setDegreeCd(dept.getDegreeCd());
            existingDept.setOrgnClsfGrpdetCd(dept.getOrgnClsfGrpdetCd());
            existingDept.setDeptMNm(dept.getDeptMNm());
            existingDept.setDeptLPrint(dept.getDeptLPrint());
            existingDept.setOrgnClsfCd(dept.getOrgnClsfCd());
            existingDept.setDeptDegree(dept.getDeptDegree());
            existingDept.setLastDeptYn(dept.getLastDeptYn());
            existingDept.setDeptSPrintEng(dept.getDeptSPrintEng());
            existingDept.setDeptLNmEng(dept.getDeptLNmEng());
            existingDept.setDeptNmEng(dept.getDeptNmEng());
            existingDept.setDeptSPrint(dept.getDeptSPrint());
            existingDept.setSpecialDivCd(dept.getSpecialDivCd());
            existingDept.setRegisterPartCd(dept.getRegisterPartCd());
            existingDept.setPrtOrd(dept.getPrtOrd());
            existingDept.setDeptLDegree(dept.getDeptLDegree());
            existingDept.setDeptSAliasEng(dept.getDeptSAliasEng());
            existingDept.setDeptLAliasEng(dept.getDeptLAliasEng());
            existingDept.setDeptMCd(dept.getDeptMCd());
            existingDept.setDeptLNm(dept.getDeptLNm());
            existingDept.setDeptSDegreeEng(dept.getDeptSDegreeEng());
            existingDept.setOrgnClsfGrpCd(dept.getOrgnClsfGrpCd());
            existingDept.setEtcPartCd(dept.getEtcPartCd());
            existingDept.setScholarPartCd(dept.getScholarPartCd());
            existingDept.setDeptMvotYn(dept.getDeptMvotYn());
            existingDept.setSchDeptAlias(dept.getSchDeptAlias());
            existingDept.setDeptLCd(dept.getDeptLCd());
            existingDept.setDeptLevelCd(dept.getDeptLevelCd());
            existingDept.setInsidePartCd(dept.getInsidePartCd());
            existingDept.setDeptSNm(dept.getDeptSNm());
            existingDept.setDeptMAliasEng(dept.getDeptMAliasEng());
            existingDept.setDeptMNmEng(dept.getDeptMNmEng());
            existingDept.setDeptLAlias(dept.getDeptLAlias());
            existingDept.setTrackDeptYn(dept.getTrackDeptYn());
            existingDept.setDeptSDegree(dept.getDeptSDegree());
            existingDept.setDeptSNmEng(dept.getDeptSNmEng());
            existingDept.setDeptMDegree(dept.getDeptMDegree());

            // Faculty 설정
            existingDept.setFaculty(faculty);

            departmentsToSave.add(existingDept);
            log.debug("Department 업데이트됨: dept_cd={}, dept_nm={}", existingDept.getDeptCd(), existingDept.getDeptNm());
          } else {
            // 새로운 Department 삽입
            dept.setFaculty(faculty);
            departmentsToSave.add(dept);
            log.debug("새로운 Department 추가됨: dept_cd={}, dept_nm={}", dept.getDeptCd(), dept.getDeptNm());
          }
        }

        // 데이터베이스에 저장
        departmentRepository.saveAll(departmentsToSave);
        log.info("Departments 데이터 저장 완료: {}개", departmentsToSave.size());

        // HashRegistry 업데이트
        hashRegistryService.updateHashValue(HashType.DEPARTMENT_JSON, fileHash);
        log.info("HashRegistry 업데이트 완료: HashType={}, HashValue={}", HashType.DEPARTMENT_JSON, fileHash);
      }

      // 처리 시간 계산
      LocalDateTime endTime = LocalDateTime.now();
      Duration duration = Duration.between(startTime, endTime);

      log.info("Departments 데이터 저장 완료: 소요 시간: {}초", duration.getSeconds());
    } catch (IOException e) {
      log.error("Departments 데이터 로드 중 IO 오류 발생: {}", e.getMessage(), e);
      throw new CustomException(ErrorCode.DEPARTMENT_IO_ERROR);
    } catch (Exception e) {
      log.error("Departments 데이터 로드 실패: {}", e.getMessage(), e);
      throw e; // 트랜잭션 롤백
    }
  }

  /**
   * 파일의 SHA-256 해시값을 계산합니다.
   *
   * @param filePath 파일의 경로
   * @return 해시값 문자열
   */
  private String calculateFileHash(Path filePath) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] fileBytes = Files.readAllBytes(filePath);
      byte[] hashBytes = digest.digest(fileBytes);
      StringBuilder sb = new StringBuilder();
      for (byte b : hashBytes) {
        sb.append(String.format("%02x", b));
      }
      return sb.toString();
    } catch (Exception e) {
      log.error("파일 해시 계산 실패: {}", e.getMessage(), e);
      throw new RuntimeException("파일 해시 계산 실패", e);
    }
  }

  /**
   * 현재 department.json 파일의 해시값을 반환합니다.
   *
   * @return department.json 파일의 해시값 문자열
   */
  public String getCurrentFileHash() {
    Path deptPath = getDepartmentFilePath();
    return calculateFileHash(deptPath);
  }

  /**
   * 시스템 타입에 따라 departments.json 파일의 경로를 결정합니다.
   *
   * @return departments.json 파일의 Path
   */
  private Path getDepartmentFilePath() {
    SystemType systemType = FileUtil.getCurrentSystem();
    Path deptPath;

    switch (systemType) {
      case LINUX:
        // 서버 환경: /mnt/sejong-malsami/department/departments.json
        deptPath = Paths.get("/mnt/sejong-malsami/department/departments.json");
        log.info("서버 환경: departments.json 경로 설정됨 = {}", deptPath);
        break;
      case WINDOWS:
      case MAC:
      case OTHER:
      default:
        // 로컬 환경: src/main/resources/departments.json
        try {
          deptPath = Paths.get(
              getClass().getClassLoader().getResource("departments.json").toURI()
          );
          log.info("로컬 환경: departments.json 경로 설정됨 = {}", deptPath);
        } catch (Exception e) {
          log.error("로컬 환경에서 departments.json 파일을 찾을 수 없습니다.", e);
          throw new RuntimeException("departments.json 파일을 찾을 수 없습니다.", e);
        }
        break;
    }

    return deptPath;
  }
}
