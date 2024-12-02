package com.balsamic.sejongmalsami.object.postgres;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "departments")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class Department extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
  private UUID departmentId;

  // 고유 필드
  @Column(name = "dept_cd", nullable = false, unique = true)
  @JsonProperty("DEPT_CD")
  private String deptCd;

  @Column(name = "key_dept_cd")
  @JsonProperty("KEY_DEPT_CD")
  private String keyDeptCd;

  @Column(name = "adm_dept_alias_eng")
  @JsonProperty("ADM_DEPT_ALIAS_ENG")
  private String admDeptAliasEng;

  @Column(name = "dept_degree_eng")
  @JsonProperty("DEPT_DEGREE_ENG")
  private String deptDegreeEng;

  @Column(name = "dept_alias")
  @JsonProperty("DEPT_ALIAS")
  private String deptAlias;

  @Column(name = "dept_l_degree_eng")
  @JsonProperty("DEPT_L_DEGREE_ENG")
  private String deptLDegreeEng;

  @Column(name = "dept_print_eng")
  @JsonProperty("DEPT_PRINT_ENG")
  private String deptPrintEng;

  @Column(name = "dept_nm")
  @JsonProperty("DEPT_NM")
  private String deptNm;

  @Column(name = "dept_s_cd")
  @JsonProperty("DEPT_S_CD")
  private String deptSCd;

  @Column(name = "dept_l_print_eng")
  @JsonProperty("DEPT_L_PRINT_ENG")
  private String deptLPrintEng;

  @Column(name = "dept_s_alias")
  @JsonProperty("DEPT_S_ALIAS")
  private String deptSAlias;

  @Column(name = "dept_print")
  @JsonProperty("DEPT_PRINT")
  private String deptPrint;

  @Column(name = "dept_m_print")
  @JsonProperty("DEPT_M_PRINT")
  private String deptMPrint;

  @Column(name = "dept_m_print_eng")
  @JsonProperty("DEPT_M_PRINT_ENG")
  private String deptMPrintEng;

  @Column(name = "adm_dept_alias")
  @JsonProperty("ADM_DEPT_ALIAS")
  private String admDeptAlias;

  @Column(name = "dept_mvin_yn")
  @JsonProperty("DEPT_MVIN_YN")
  private String deptMvinYn;

  @Column(name = "dept_m_alias")
  @JsonProperty("DEPT_M_ALIAS")
  private String deptMAlias;

  @Column(name = "sch_dept_alias_eng")
  @JsonProperty("SCH_DEPT_ALIAS_ENG")
  private String schDeptAliasEng;

  @Column(name = "close_dt")
  @JsonProperty("CLOSE_DT")
  private String closeDt;

  @Column(name = "dept_alias_eng")
  @JsonProperty("DEPT_ALIAS_ENG")
  private String deptAliasEng;

  @Column(name = "dept_m_degree_eng")
  @JsonProperty("DEPT_M_DEGREE_ENG")
  private String deptMDegreeEng;

  @Column(name = "degree_cd")
  @JsonProperty("DEGREE_CD")
  private String degreeCd;

  @Column(name = "orgn_clsf_grpdet_cd")
  @JsonProperty("ORGN_CLSF_GRPDET_CD")
  private String orgnClsfGrpdetCd;

  @Column(name = "dept_m_nm")
  @JsonProperty("DEPT_M_NM")
  private String deptMNm;

  @Column(name = "dept_l_print")
  @JsonProperty("DEPT_L_PRINT")
  private String deptLPrint;

  @Column(name = "orgn_clsf_cd")
  @JsonProperty("ORGN_CLSF_CD")
  private String orgnClsfCd;

  @Column(name = "dept_degree")
  @JsonProperty("DEPT_DEGREE")
  private String deptDegree;

  @Column(name = "last_dept_yn")
  @JsonProperty("LAST_DEPT_YN")
  private String lastDeptYn;

  @Column(name = "dept_s_print_eng")
  @JsonProperty("DEPT_S_PRINT_ENG")
  private String deptSPrintEng;

  @Column(name = "dept_l_nm_eng")
  @JsonProperty("DEPT_L_NM_ENG")
  private String deptLNmEng;

  @Column(name = "dept_nm_eng")
  @JsonProperty("DEPT_NM_ENG")
  private String deptNmEng;

  @Column(name = "dept_s_print")
  @JsonProperty("DEPT_S_PRINT")
  private String deptSPrint;

  @Column(name = "special_div_cd")
  @JsonProperty("SPECIAL_DIV_CD")
  private String specialDivCd;

  @Column(name = "register_part_cd")
  @JsonProperty("REGISTER_PART_CD")
  private String registerPartCd;

  @Column(name = "prt_ord")
  @JsonProperty("PRT_ORD")
  private String prtOrd;

  @Column(name = "dept_l_degree")
  @JsonProperty("DEPT_L_DEGREE")
  private String deptLDegree;

  @Column(name = "dept_s_alias_eng")
  @JsonProperty("DEPT_S_ALIAS_ENG")
  private String deptSAliasEng;

  @Column(name = "dept_l_alias_eng")
  @JsonProperty("DEPT_L_ALIAS_ENG")
  private String deptLAliasEng;

  @Column(name = "dept_m_cd")
  @JsonProperty("DEPT_M_CD")
  private String deptMCd;

  @Column(name = "dept_l_nm")
  @JsonProperty("DEPT_L_NM")
  private String deptLNm;

  @Column(name = "dept_s_degree_eng")
  @JsonProperty("DEPT_S_DEGREE_ENG")
  private String deptSDegreeEng;

  @Column(name = "orgn_clsf_grp_cd")
  @JsonProperty("ORGN_CLSF_GRP_CD")
  private String orgnClsfGrpCd;

  @Column(name = "etc_part_cd")
  @JsonProperty("ETC_PART_CD")
  private String etcPartCd;

  @Column(name = "scholar_part_cd")
  @JsonProperty("SCHOLAR_PART_CD")
  private String scholarPartCd;

  @Column(name = "dept_mvot_yn")
  @JsonProperty("DEPT_MVOT_YN")
  private String deptMvotYn;

  @Column(name = "sch_dept_alias")
  @JsonProperty("SCH_DEPT_ALIAS")
  private String schDeptAlias;

  @Column(name = "dept_l_cd")
  @JsonProperty("DEPT_L_CD")
  private String deptLCd;

  @Column(name = "dept_level_cd")
  @JsonProperty("DEPT_LEVEL_CD")
  private String deptLevelCd;

  @Column(name = "inside_part_cd")
  @JsonProperty("INSIDE_PART_CD")
  private String insidePartCd;

  @Column(name = "dept_s_nm")
  @JsonProperty("DEPT_S_NM")
  private String deptSNm;

  @Column(name = "dept_m_alias_eng")
  @JsonProperty("DEPT_M_ALIAS_ENG")
  private String deptMAliasEng;

  @Column(name = "dept_m_nm_eng")
  @JsonProperty("DEPT_M_NM_ENG")
  private String deptMNmEng;

  @Column(name = "dept_l_alias")
  @JsonProperty("DEPT_L_ALIAS")
  private String deptLAlias;

  @Column(name = "track_dept_yn")
  @JsonProperty("TRACK_DEPT_YN")
  private String trackDeptYn;

  @Column(name = "dept_s_degree")
  @JsonProperty("DEPT_S_DEGREE")
  private String deptSDegree;

  @Column(name = "dept_s_nm_eng")
  @JsonProperty("DEPT_S_NM_ENG")
  private String deptSNmEng;

  @Column(name = "dept_m_degree")
  @JsonProperty("DEPT_M_DEGREE")
  private String deptMDegree;
}
