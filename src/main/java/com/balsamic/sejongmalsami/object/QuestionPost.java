package com.balsamic.sejongmalsami.object;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Entity
@Setter
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class QuestionPost extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "question_post_id", columnDefinition = "uuid DEFAULT uuid_generate_v4()", updatable = false, nullable = false)
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
    @ColumnDefault("0")
    private int views;

    // 좋아요 수 (일단 해놓고 몽고 좋아요 엔티티 설계후 수정할게요~)
    @ColumnDefault("0")
    private int likes;

    // 댓글 수 (일단 해놓고 나중에 수정)
    @ColumnDefault("0")
    @Column(name = "answer_count")
    private int answerCount;

    // 엽전 현상금
    private int bounty;

    // 내 정보 비공개 여부
    @Builder.Default
    @Column(name = "is_private")
    private Boolean isPrivate = false;
}
