package com.balsamic.sejongmalsami.util;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 저장을 위한 공통 인터페이스
 */
public interface StorageService {

  /**
   * 파일을 업로드하고 파일 URL을 반환합니다.
   *
   * @param contentType 파일의 ContentType
   * @param file 업로드할 파일
   * @return 업로드된 파일의 URL
   */
  String uploadFile(String contentType, MultipartFile file);

  /**
   * 파일을 삭제합니다.
   *
   * @param fileUrl 삭제할 파일의 URL
   */
  void deleteFile(String fileUrl);

  /**
   * 여러 파일을 업로드하고 파일 URL 리스트를 반환합니다.
   *
   * @param contentType 파일의 ContentType
   * @param files 업로드할 파일 리스트
   * @return 업로드된 파일의 URL 리스트
   */
  default List<String> uploadFiles(String contentType, List<MultipartFile> files) {
    return files.stream()
        .map(file -> uploadFile(contentType, file))
        .toList();
  }
}
