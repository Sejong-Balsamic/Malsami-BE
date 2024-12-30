package com.balsamic.sejongmalsami.object;

import com.balsamic.sejongmalsami.object.mongo.YeopjeonHistory;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.object.postgres.ServerErrorCode;
import com.balsamic.sejongmalsami.object.postgres.Yeopjeon;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.domain.Page;

@Builder
@Getter
@Setter
@ToString
public class AdminDto {
  private Member member;
  private Page<MemberYeopjeon> memberYeopjeonPage;
  private Yeopjeon yeopjeon;
  private YeopjeonHistory yeopjeonHistory;
  private Page<ServerErrorCode> serverErrorCodesPage;
}
