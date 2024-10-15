package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.QuestionCommand;
import com.balsamic.sejongmalsami.object.QuestionDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface QuestionPostControllerDocs {

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.15",
          author = Author.BAEKJIHOON,
          description = "dto 필드 변경에 따른 반환값 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.14",
          author = Author.BAEKJIHOON,
          description = "질문게시판 command, dto 통합"
      ),
      @ApiChangeLog(
          date = "2024.10.11",
          author = Author.BAEKJIHOON,
          description = "질문 글 첨부파일 추가"
      ),
      @ApiChangeLog(
          date = "2024.10.10",
          author = Author.BAEKJIHOON,
          description = "질문 글 커스텀태그 작성 & 인기글"
      ),
      @ApiChangeLog(
          date = "2024.09.25",
          author = Author.BAEKJIHOON,
          description = "질문 글 등록"
      )
  })
  @Operation(
      summary = "질문 글 등록",
      description = """
          **글 등록 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          - **String title**: 질문게시글 제목 (필수)
            _예: "vs에서는 돌아가는데 oj에서는 왜 80점인가요?"_

          - **String content**: 질문게시글 본문 (필수)
            _예: "고c 003분반인데 이번 실습문제 4번 맞게 푼 것 같은데 왜 oj에서 돌리면 80점일까요...? 도와주세요"_
            
          - **String subject**: 교과목 명 (필수)
            _예: "고급C프로그래밍및실습"_
            
          - **List\\<MultipartFile\\> mediaFiles**: 첨부파일 (최대 3개까지만 추가가능, 이미지파일만 업로드가능)
            
          - **Set\\<QuestionPresetTag\\> questionPresetTagSet**: 질문 게시글 정적태그 (최대 2개까지만 선택가능)
            _예: "STUDY_TIPS"_
            
          - **Set\\<String\\> customTagSet**: 질문 게시글 커스텀태그 (최대 4개까지만 추가가능)
            _예: "코딩질문"_
            
          - **Integer reward**: 엽전 현상금 (default = 0)
            _예: "50"_
            
          - **Boolean isPrivate**: 내 정보 비공개 여부 (default = false)
            _기본값은 false입니다. true로 요청할 시 질문 글에 내 정보가 비공개 처리됩니다._

          **정적 태그**
                    
          총 7개의 정적태그가 존재하며 최대 2개까지의 정적태그를 설정할 수 있습니다.
          - **OUT_OF_CLASS** (수업 외 내용)
          - **UNKNOWN_CONCEPT** (개념 모름)
          - **BETTER_SOLUTION** (더 나은 풀이)
          - **EXAM_PREPARATION** (시험 대비)
          - **DOCUMENT_REQUEST** (자료 요청)
          - **STUDY_TIPS** (공부 팁)
          - **ADVICE_REQUEST** (조언 구함)
                    
            _예: "formData.append('questionPresetTagSet', 'DOCUMENT_REQUEST');_

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **QuestionPost questionPost**: 질문 글 정보
            - **List\\<QuestionPost\\> questionPosts**: null
            - **AnswerPost answerPost**: null
            - **List\\<AnswerPost\\> answerPosts**: null
            - **List\\<MediaFile\\> mediaFiles**: 질문 글 첨부파일
            - **Set\\<String\\> customTags**: 질문 글 커스텀태그

          **참고 사항:**

          - 이 API를 통해 사용자는 질문게시판에 질문 글을 동록할 수 있습니다.
          - 글 제목, 본문, 과목명은 null 값이 들어갈 수 없습니다. (required)
          - 질문글 첨부파일은 이미지 파일만 지원합니다.
          - 정적태그, 엽전 현상금, 내 정보 비공개 여부는 프론트에서 설정하지 않으면 default 값이 할당됩니다.
          - 엽전 현상금 null 또는 음수 값 입력시 자동으로 0으로 설정됩니다.
          - 성공적인 등록 후, 등록 된 질문글을 반환합니다.
          - Swagger에서 테스트 시 mediaFiles에 있는 "Send empty value" 체크박스 해제해야합니다.
          """
  )
  ResponseEntity<QuestionDto> saveQuestionPost(
      CustomUserDetails customUserDetails,
      QuestionCommand questionCommand);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.15",
          author = Author.BAEKJIHOON,
          description = "dto 필드 변경에 따른 반환값 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.10",
          author = Author.BAEKJIHOON,
          description = "질문게시판 일간 인기글 init"
      )
  })
  @Operation(
      summary = "일간 인기 질문글",
      description = """
          **질문 일간 인기글 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          없음

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **QuestionPost questionPost**: null
            - **List\\<QuestionPost\\> questionPosts**: 일간 인기 질문 글 리스트
            - **AnswerPost answerPost**: null
            - **List\\<AnswerPost\\>** answerPosts**: null
            - **List\\<MediaFile\\>** mediaFiles: null
            - **Set\\<String\\>** customTags: null

          **참고 사항:**

          - 이 API를 통해 사용자는 일간 인기 질문글을 조회할 수 있습니다.
          - 요청 시각으로부터 24시간 이내에 작성된 상위 30개의 일간 인기글을 조회합니다.
          """
  )
  ResponseEntity<QuestionDto> getDailyPopularQuestionPost(
      QuestionCommand command);

  @ApiChangeLogs({
      @ApiChangeLog(
          date = "2024.10.15",
          author = Author.BAEKJIHOON,
          description = "dto 필드 변경에 따른 반환값 수정"
      ),
      @ApiChangeLog(
          date = "2024.10.10",
          author = Author.BAEKJIHOON,
          description = "질문게시판 주간 인기글 init"
      )
  })
  @Operation(
      summary = "주간 인기 질문글",
      description = """
          **질문 주간 인기글 요청**

          **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

          **입력 파라미터 값:**

          없음

          **반환 파라미터 값:**

          - **QuestionDto**: 질문 게시판 정보 반환
            - **QuestionPost questionPost**: null
            - **List\\<QuestionPost\\> questionPosts**: 주간 인기 질문 글 리스트
            - **AnswerPost answerPost**: null
            - **List\\<AnswerPost\\>** answerPosts**: null
            - **List\\<MediaFile\\>** mediaFiles: null
            - **Set\\<String\\>** customTags: null

          **참고 사항:**

          - 이 API를 통해 사용자는 주간 인기 질문글을 조회할 수 있습니다.
          - 요청 시각으로부터 7일 이내에 작성된 상위 30개의 주간 인기글을 조회합니다.
          """
  )
  ResponseEntity<QuestionDto> getWeeklyPopularQuestionPost(
      QuestionCommand command);
}
