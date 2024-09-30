package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.constants.PostType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@ToString(callSuper = true)
public class Comment extends BaseEntity{

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private UUID commentId;

  @ManyToOne(fetch = FetchType.LAZY)
  private Member member;

  private String content;

  // 댓글이 속한 게시글의 ID
  private UUID postId;

  // 댓글이 속한 게시글의 유형
  private PostType postType;

  // 닉네임 비공개 여부
  @Builder.Default
  private Boolean isPrivate = false;
}
