package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.constants.FileStatus;
import com.balsamic.sejongmalsami.object.postgres.Department;
import com.balsamic.sejongmalsami.object.postgres.DepartmentFile;
import com.balsamic.sejongmalsami.repository.postgres.DepartmentFileRepository;
import com.balsamic.sejongmalsami.repository.postgres.DepartmentRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
  private final DepartmentFileRepository departmentFileRepository;
  private final ObjectMapper objectMapper;

  /**
   * JSON 파일을 로드하고 Department 데이터를 저장합니다.
   * 파일의 해시값을 확인하여 중복 처리를 방지합니다.
   *
   * @param filePath JSON 파일의 경로
   */
  @Transactional
  public void loadDepartments(Path filePath) {
    String fileName = filePath.getFileName().toString();
    String fileHash = calculateFileHash(filePath);

    log.info("파일 로드 시작: {}", fileName);
    LocalDateTime startTime = LocalDateTime.now();

    Optional<DepartmentFile> existingFileOpt = departmentFileRepository.findByFileName(fileName);

    if (existingFileOpt.isPresent()) {
      DepartmentFile existingFile = existingFileOpt.get();
      if (existingFile.getFileHash().equals(fileHash) && existingFile.getFileStatus() == FileStatus.SUCCESS) {
        log.info("이미 처리된 파일입니다. 파일 이름: {}, 해시: {}", fileName, fileHash);
        return;
      } else {
        log.info("파일이 변경되었거나 이전 처리에 실패했습니다. 재처리 진행: {}", fileName);
      }
    }

    DepartmentFile departmentFile = DepartmentFile.builder()
        .fileName(fileName)
        .fileHash(fileHash)
        .fileStatus(FileStatus.PENDING)
        .processedAt(startTime)
        .build();

    departmentFileRepository.save(departmentFile);

    try {
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
        // deptCd 리스트 추출
        List<String> deptCds = departments.stream()
            .map(Department::getDeptCd)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());

        // 기존 Department 데이터 조회
        List<Department> existingDepartments = departmentRepository.findByDeptCdIn(deptCds);
        Map<String, Department> existingDeptMap = existingDepartments.stream()
            .collect(Collectors.toMap(Department::getDeptCd, Function.identity()));

        List<Department> departmentsToSave = new ArrayList<>();

        for (Department dept : departments) {
          if (dept.getDeptCd() == null || dept.getDeptCd().trim().isEmpty()) {
            continue; // 유효하지 않은 데이터는 건너뜁니다
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

            departmentsToSave.add(existingDept);
          } else {
            // 새로운 Department 삽입
            departmentsToSave.add(dept);
          }
        }

        // 데이터베이스에 저장
        departmentRepository.saveAll(departmentsToSave);
      }

      // 처리 시간 계산
      LocalDateTime endTime = LocalDateTime.now();
      Duration duration = Duration.between(startTime, endTime);

      // DepartmentFile 상태 업데이트
      departmentFile.setFileStatus(FileStatus.SUCCESS);
      departmentFile.setProcessedAt(endTime);
      departmentFile.setDurationSeconds(duration.getSeconds());
      departmentFileRepository.save(departmentFile);

      log.info("Departments 데이터 저장 완료: {}개, 소요 시간: {}초", departments.size(), duration.getSeconds());
    } catch (Exception e) {
      log.error("Departments 데이터 로드 실패: {}", e.getMessage(), e);

      // DepartmentFile 상태 업데이트
      departmentFile.setFileStatus(FileStatus.FAILURE);
      departmentFile.setErrorMessage(e.getMessage());
      departmentFile.setDurationSeconds(Duration.between(startTime, LocalDateTime.now()).getSeconds());
      departmentFileRepository.save(departmentFile);
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
}
