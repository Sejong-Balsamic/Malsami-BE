package com.balsamic.sejongmalsami.util;

import com.balsamic.sejongmalsami.object.constants.AccountStatus;
import com.balsamic.sejongmalsami.object.constants.DocumentType;
import com.balsamic.sejongmalsami.object.constants.MimeType;
import com.balsamic.sejongmalsami.object.constants.PostTier;
import com.balsamic.sejongmalsami.object.constants.Role;
import com.balsamic.sejongmalsami.object.postgres.DocumentFile;
import com.balsamic.sejongmalsami.object.postgres.DocumentPost;
import com.balsamic.sejongmalsami.object.postgres.Member;
import com.balsamic.sejongmalsami.repository.postgres.DocumentFileRepository;
import com.balsamic.sejongmalsami.repository.postgres.DocumentPostRepository;
import com.balsamic.sejongmalsami.repository.postgres.MemberRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

//TODO: Faker 도입
@Component
@Transactional
public class TestDataInit {
  @Autowired
  private MemberRepository memberRepository;

  @Autowired
  private DocumentPostRepository documentPostRepository;

  @Autowired
  private DocumentFileRepository documentFileRepository;

  public Member createMember(String name, long studentId, String uuidNickname, String major, String year) {
    Member member = Member.builder()
        .studentId(studentId)
        .studentName(name)
        .uuidNickname(uuidNickname)
        .major(major)
        .academicYear(year)
        .enrollmentStatus("재학")
        .profileUrl("http://example.com/profile.jpg")
        .isNotificationEnabled(true)
        .role(Role.ROLE_USER)
        .accountStatus(AccountStatus.ACTIVE)
        .lastLoginTime(LocalDateTime.now().minusDays(10))
        .isFirstLogin(true)
        .build();
    return memberRepository.save(member);
  }

  public DocumentPost createDocumentPost(Member member, String title, String subject, int likeCount, int viewCount) {
    DocumentPost post = DocumentPost.builder()
        .member(member)
        .title(title)
        .subject(subject)
        .content("내용 예시")
        .postTier(PostTier.CHEONMIN)
        .likeCount(likeCount)
        .viewCount(viewCount)
        .isDepartmentPrivate(false)
        .dailyScore(50)
        .weeklyScore(200)
        .createdDate(LocalDateTime.now().minusDays(1))
        .updatedDate(LocalDateTime.now().minusDays(1))
        .isEdited(false)
        .isDeleted(false)
        .documentTypes(Arrays.asList(DocumentType.DOCUMENT))
        .build();
    return documentPostRepository.save(post);
  }
  public DocumentFile createDocumentFile(DocumentPost post, Member uploader, String originalFileName, String uploadFileName, long fileSize) {
    DocumentFile file = DocumentFile.builder()
        .documentPost(post)
        .uploader(uploader)
        .thumbnailUrl("http://example.com/thumbnail.jpg")
        .originalFileName(originalFileName)
        .uploadFileName(uploadFileName)
        .fileSize(fileSize)
        .mimeType(MimeType.PDF)
        .downloadCount(0L)
        .password(null)
        .isInitialPasswordSet(false)
        .createdDate(LocalDateTime.now().minusDays(1))
        .updatedDate(LocalDateTime.now().minusDays(1))
        .isEdited(false)
        .isDeleted(false)
        .build();
    return documentFileRepository.save(file);
  }

}
