package com.balsamic.sejongmalsami.service;

import com.balsamic.sejongmalsami.object.Member;
import com.balsamic.sejongmalsami.object.QuestionPost;
import com.balsamic.sejongmalsami.object.QuestionPostCommand;
import com.balsamic.sejongmalsami.object.QuestionPostDto;
import com.balsamic.sejongmalsami.repository.MemberRepository;
import com.balsamic.sejongmalsami.repository.QuestionPostRepository;
import com.balsamic.sejongmalsami.util.exception.CustomException;
import com.balsamic.sejongmalsami.util.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class QuestionPostService {

    private final QuestionPostRepository questionPostRepository;
    private final MemberRepository memberRepository;

    /* 질문 게시글 등록 로직 */
    @Transactional
    public QuestionPostDto saveQuestionPost(String memberId, QuestionPostCommand command) {

        Member member = memberRepository.findById(UUID.fromString(memberId))
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
        log.info("현재 사용자 학번 = {}", member.getStudentId());

        QuestionPost questionPost = QuestionPost.builder()
                .member(member)
                .title(command.getTitle())
                .content(command.getContent())
                .subject(command.getSubject())
                .views(0)
                .likes(0)
                .answerCount(0)
                .reward(command.getReward())
                .isPrivate(false)
                .build();

        questionPostRepository.save(questionPost);

        return QuestionPostDto
                .builder()
                .questionPost(questionPost)
                .build();
    }
}
