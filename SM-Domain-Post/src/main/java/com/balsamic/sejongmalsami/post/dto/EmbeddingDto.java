package com.balsamic.sejongmalsami.post.dto;

import com.balsamic.sejongmalsami.post.dto.GeneralPost;
import com.balsamic.sejongmalsami.object.postgres.PostEmbedding;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@ToString
@AllArgsConstructor
@Builder
@Getter
@Setter
public class EmbeddingDto {
  private Page<PostEmbedding> postEmbeddingsPage;
  private Page<GeneralPost> generalPostsPage;
}
