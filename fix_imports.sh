#!/bin/bash

echo "🔧 Repository import 경로 수정 시작..."

# Post 도메인 Repository imports
repositories=(
    "QuestionPostRepository"
    "AnswerPostRepository" 
    "DocumentPostRepository"
    "DocumentRequestPostRepository"
    "CommentRepository"
    "MediaFileRepository"
    "DocumentFileRepository"
    "QuestionBoardLikeRepository"
    "DocumentBoardLikeRepository"
    "CommentLikeRepository"
    "QuestionPostCustomTagRepository"
    "DocumentPostCustomTagRepository"
    "DocumentPostBookmarkRepository"
    "PurchaseHistoryRepository"
)

for repo in "${repositories[@]}"; do
    echo "✅ Fixing $repo imports..."
    find . -name "*.java" -exec sed -i '' "s/import com\.balsamic\.sejongmalsami\.repository\.\(postgres\|mongo\)\.$repo;/import com.balsamic.sejongmalsami.post.repository.\1.$repo;/g" {} \;
done

# Notice 도메인 Repository imports
notice_repos=(
    "NoticePostRepository"
    "NoticeBoardLikeRepository"
)

for repo in "${notice_repos[@]}"; do
    echo "✅ Fixing $repo imports..."
    find . -name "*.java" -exec sed -i '' "s/import com\.balsamic\.sejongmalsami\.repository\.\(postgres\|mongo\)\.$repo;/import com.balsamic.sejongmalsami.notice.repository.\1.$repo;/g" {} \;
done

# Auth 도메인 Repository imports
auth_repos=(
    "RefreshTokenRepository"
    "FcmTokenRepository"
)

for repo in "${auth_repos[@]}"; do
    echo "✅ Fixing $repo imports..."
    find . -name "*.java" -exec sed -i '' "s/import com\.balsamic\.sejongmalsami\.repository\.mongo\.$repo;/import com.balsamic.sejongmalsami.auth.repository.mongo.$repo;/g" {} \;
done

# AI 도메인 Repository imports
ai_repos=(
    "PostEmbeddingRepository"
    "SearchQueryCacheRepository"
    "SearchHistoryRepository"
)

for repo in "${ai_repos[@]}"; do
    echo "✅ Fixing $repo imports..."
    find . -name "*.java" -exec sed -i '' "s/import com\.balsamic\.sejongmalsami\.repository\.\(postgres\|mongo\)\.$repo;/import com.balsamic.sejongmalsami.ai.repository.\1.$repo;/g" {} \;
done

# Academic 도메인 Repository imports
academic_repos=(
    "CourseFileRepository"
    "DepartmentFileRepository"
)

for repo in "${academic_repos[@]}"; do
    echo "✅ Fixing $repo imports..."
    find . -name "*.java" -exec sed -i '' "s/import com\.balsamic\.sejongmalsami\.repository\.postgres\.$repo;/import com.balsamic.sejongmalsami.academic.repository.postgres.$repo;/g" {} \;
done

echo "🎉 Repository import 경로 수정 완료!" 