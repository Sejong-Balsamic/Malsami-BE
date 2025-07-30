package com.balsamic.sejongmalsami.post.dto;

import com.balsamic.sejongmalsami.constants.ContentType;
import com.balsamic.sejongmalsami.constants.DocumentType;
import com.balsamic.sejongmalsami.constants.PostTier;
import com.balsamic.sejongmalsami.constants.QuestionPresetTag;
import com.balsamic.sejongmalsami.post.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.post.object.postgres.QuestionPost;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GeneralPost {
  // 공통 필드
  private UUID postId;
  private String title;
  private String content;
  private String subject;
  private List<String> faculties;
  private String thumbnailUrl;
  private Integer viewCount;
  private Integer likeCount;
  private Integer commentCount;
  private ContentType contentType;
  private Boolean isLiked;
  private Boolean isEdited;
  private Boolean isDeleted;
  private LocalDateTime createdDate;
  private LocalDateTime updatedDate;
  private Long dailyScore;
  private Long weeklyScore;

  // Question 전용 필드
  private Integer rewardYeopjeon;
  private Boolean chaetaekStatus;
  private Boolean isPrivate;
  private List<QuestionPresetTag> questionPresetTags;
  private Integer answerCount;
  private List<String> questionCustomTags;

  // Document 전용 필드
  private List<DocumentType> documentTypes;
  private PostTier postTier;
  private Integer attendedYear;
  private Integer dislikeCount;
  private Boolean isDepartmentPrivate;
  private Boolean isPopular;
  private List<String> documentCustomTags;

  public static GeneralPost fromQuestionPost(QuestionPost post, List<String> customTags) {
    return GeneralPost.builder()
        .postId(post.getQuestionPostId())
        .title(post.getTitle())
        .content(post.getContent())
        .subject(post.getSubject())
        .faculties(post.getFaculties())
        .thumbnailUrl(post.getThumbnailUrl())
        .viewCount(post.getViewCount())
        .likeCount(post.getLikeCount())
        .commentCount(post.getCommentCount())
        .contentType(ContentType.QUESTION)
        .isLiked(post.getIsLiked())
        .isEdited(post.getIsEdited())
        .isDeleted(post.getIsDeleted())
        .createdDate(post.getCreatedDate())
        .updatedDate(post.getUpdatedDate())
        .dailyScore(post.getDailyScore())
        .weeklyScore(post.getWeeklyScore())
        // Question 전용 필드
        .rewardYeopjeon(post.getRewardYeopjeon())
        .chaetaekStatus(post.getChaetaekStatus())
        .isPrivate(post.getIsPrivate())
        .questionPresetTags(post.getQuestionPresetTags())
        .answerCount(post.getAnswerCount())
        .questionCustomTags(customTags)
        .build();
  }

  public static GeneralPost fromDocumentPost(DocumentPost post, List<String> customTags) {
    return GeneralPost.builder()
        .postId(post.getDocumentPostId())
        .title(post.getTitle())
        .content(post.getContent())
        .subject(post.getSubject())
        .faculties(post.getFaculties())
        .thumbnailUrl(post.getThumbnailUrl())
        .viewCount(post.getViewCount())
        .likeCount(post.getLikeCount())
        .commentCount(post.getCommentCount())
        .contentType(ContentType.DOCUMENT)
        .isLiked(post.getIsLiked())
        .isEdited(post.getIsEdited())
        .isDeleted(post.getIsDeleted())
        .createdDate(post.getCreatedDate())
        .updatedDate(post.getUpdatedDate())
        .dailyScore(post.getDailyScore())
        .weeklyScore(post.getWeeklyScore())
        // Document 전용 필드
        .documentTypes(post.getDocumentTypes())
        .postTier(post.getPostTier())
        .attendedYear(post.getAttendedYear())
        .dislikeCount(post.getDislikeCount())
        .isDepartmentPrivate(post.getIsDepartmentPrivate())
        .isPopular(post.getIsPopular())
        .documentCustomTags(customTags)
        .build();
  }
}
