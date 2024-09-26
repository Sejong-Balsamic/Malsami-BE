package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.*;
import com.balsamic.sejongmalsami.repository.MemberRepository;
import com.balsamic.sejongmalsami.repository.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionPostService {

    private final QuestionPostRepository questionPostRepository;
    private final MemberRepository memberRepository;

    /* 질문 게시글 등록 로직 */
    @Transactional
    public QuestionPostDto savePost(
            CustomUserDetails customUserDetails,
            QuestionPostCommand questionPostCommand) {
        String username = customUserDetails.getUsername();
        log.info("현재 사용자 username = {}", username);
        Member member = memberRepository.findByStudentId(Long.parseLong(username))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        QuestionPost questionPost = QuestionPost.builder()
                .member(member)
                .title(questionPostCommand.getTitle())
                .content(questionPostCommand.getContent())
                .subject(questionPostCommand.getSubject())
                .views(0)
                .likes(0)
                .answerCount(0)
                .reward(questionPostCommand.getReward())
                .isPrivate(false)
                .build();

        questionPostRepository.save(questionPost);

        return QuestionPostDto
                .builder()
                .questionPost(questionPost)
                .build();
    }
}
