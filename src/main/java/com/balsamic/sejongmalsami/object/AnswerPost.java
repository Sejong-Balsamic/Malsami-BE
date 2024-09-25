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
public class AnswerPost extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
    private UUID answerPostId;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    private QuestionPost questionPost;

    // 답변 내용
    @Lob
    @Column(nullable = false)
    private String content;

    // 학과
    @Column(nullable = false)
    private String major;

    // 좋아요 수 (추천 수)
    @Builder.Default
    private Integer likes = 0;

    // 댓글 수
    @Builder.Default
    private Integer commentCount = 0;

    // 답변 채택 여부
    @Builder.Default
    private Boolean isChaetaek = false;
}
