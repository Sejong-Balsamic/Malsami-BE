package com.balsamic.sejongmalsami.repository.postgres;

import com.balsamic.sejongmalsami.object.postgres.Department;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, UUID> {

  List<Department> findByDeptCdIn(List<String> deptCds);
  Optional<Department> findByDeptCd(String deptCd);
  Optional<Department> findByDeptMPrint(String deptMPrint);
  Department findByDeptSPrint(String deptSPrint);

  Optional<Department> findByDeptNm(String departmentName);

  List<Department> findByDeptNmContaining(String departmentName);

  // 추가된 메서드: dept_m_print 또는 dept_s_print이 특정 값과 일치하는 첫 번째 Department를 찾음
  Optional<Department> findTopByDeptMPrintOrDeptSPrint(String deptMPrint, String deptSPrint);

  Optional<Department> findTopByDeptMPrintOrDeptSPrintOrDeptLPrint(String departmentName, String departmentName1, String departmentName2);
}