package com.balsamic.sejongmalsami.controller;

import com.balsamic.sejongmalsami.object.CustomUserDetails;
import com.balsamic.sejongmalsami.object.QuestionPostCommand;
import com.balsamic.sejongmalsami.object.QuestionPostDto;
import com.balsamic.sejongmalsami.object.constants.Author;
import com.balsamic.sejongmalsami.util.log.ApiChangeLog;
import com.balsamic.sejongmalsami.util.log.ApiChangeLogs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.ResponseEntity;

public interface QuestionPostControllerDocs {
    @ApiChangeLogs({
            @ApiChangeLog(
                    date = "2024.09.25",
                    author = Author.BAEKJIHOON,
                    description = "질문게시판 init"
            )
    })
    @Operation(
            summary = "질문 글 등록",
            description = """
    **글 등록 요청**

    **이 API는 인증이 필요하며, JWT 토큰이 존재해야합니다.**

    **입력 파라미터 값:**

    - **String title**: 질문게시글 제목 (required)
      _예: "vs에서는 돌아가는데 oj에서는 왜 80점인가요?"_

    - **String content**: 질문게시글 본문 (required)
      _예: "고c 003분반인데 이번 실습문제 4번 맞게 푼 것 같은데 왜 oj에서 돌리면 80점일까요...? 도와주세요"_
      
    - **String subject**: 과목 명 (required)
      _예: "고급C프로그래밍및실습"_
      
    - **int reward**: 엽전 현상금 (default = 0)
      _예: "50"_
      
    - **Boolean isPrivate**: 내 정보 비공개 여부 (default = false)
      _기본값은 false입니다. true로 요청할 시 질문 글에 내 정보가 비공개 처리됩니다._

    **DB에 저장되는 질문 글 정보:**
    - **String title**: 질문 글 제목
    - **String content**: 질문 글 본문
    - **String subject**: 과목
    - **String writer**: 작성자
    - **int views**: 조회 수
    - **int likes**: 추천 수
    - **int answerCount**: 답변 수
    - **int commentCount**: 댓글 수
    - **int reward**: 엽전 현상금
    - **Boolean isPrivate**: 내 정보 비공개 여부

    **반환 파라미터 값:**

    - **QuestionPostDto**: 작성 된 질문 글 반환
      - **QuestionPost questionPost**: 질문 글 정보

    **참고 사항:**

    - 이 API를 통해 사용자는 질문게시판에 질문 글을 동록할 수 있습니다.
    - 글 제목, 본문, 과목명은 null 값이 들어갈 수 없습니다.
    - 성공적인 등록 후, 등록 된 질문글을 반환합니다.
    """
    )
    ResponseEntity<QuestionPostDto> savePost(CustomUserDetails customUserDetails, QuestionPostCommand questionPostCommand);
}
