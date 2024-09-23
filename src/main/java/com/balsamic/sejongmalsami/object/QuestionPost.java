package com.balsamic.sejongmalsami.object;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class QuestionPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
    private UUID questionPostId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    // 제목
    @Column(nullable = false)
    private String title;

    // 본문
    @Lob
    @Column(nullable = false)
    private String content;

    // 과목 명
    @Column(nullable = false)
    private String subject;

    // 작성자
    @Column(nullable = false)
    private String writer;

    // 조회 수
    @Builder.Default
    private int views = 0;

    // 좋아요 수 (추천 수)
    @Builder.Default
    private int likes = 0;

    // 답변 수
    @Builder.Default
    private int answerCount = 0;

    // 댓글 수
    @Builder.Default
    private int commentCount = 0;

    // 엽전 현상금
    private int reward;

    // 내 정보 비공개 여부
    @Builder.Default
    private Boolean isPrivate = false;
}
