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
import org.springframework.security.core.context.SecurityContextHolder;
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
    public QuestionPostDto savePost(QuestionPostCommand questionPostCommand) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("SecurityContextHolder에 등록된 Name = {}", username);
        Member member = memberRepository.findByStudentId(Long.parseLong(username)).orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));

        QuestionPost questionPost = QuestionPost.builder()
                .member(member)
                .title(questionPostCommand.getTitle())
                .content(questionPostCommand.getContent())
                .subject(questionPostCommand.getSubject())
                .reward(questionPostCommand.getReward())
                .build();

        questionPostRepository.save(questionPost);

        return QuestionPostDto
                .builder()
                .questionPost(questionPost)
                .build();
    }
}
