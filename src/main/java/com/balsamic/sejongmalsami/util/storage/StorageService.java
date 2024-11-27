package com.balsamic.sejongmalsami.util.storage;

import com.balsamic.sejongmalsami.object.constants.ContentType;
import org.springframework.web.multipart.MultipartFile;

/**
 * 파일 저장을 위한 공통 인터페이스
 */
public interface StorageService {

  /**
   * 일반 파일을 업로드하고 파일 URL을 반환합니다.
   *
   * @param file 업로드할 파일
   * @return 업로드된 파일의 URL
   */
  String uploadFile(ContentType contentType, MultipartFile file);

  /**
   * 썸네일을 업로드하고 파일 URL을 반환합니다.
   * * @param file 업로드할 썸네일 파일
   * @return 업로드된 썸네일의 URL
   */
  String uploadThumbnail(ContentType contentType, MultipartFile file);

  /**
   * 이미지 업로드하고 접근가능한 URL 반환
   *
   * @param file 업로드할 이미지 파일
   * @return 업로드된 이미지의 URL
   */
  String uploadImage(ContentType contentType, MultipartFile file);


  /**
   * 파일을 삭제합니다.
   *
   * @param fileUrl 삭제할 파일의 URL
   */
  void deleteFile(ContentType contentType, String fileUrl);
}
