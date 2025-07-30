#!/bin/bash

echo "🔧 Entity import 경로 수정 시작..."

# Post 도메인 Entity imports
post_entities=(
    "QuestionPost"
    "AnswerPost"
    "DocumentPost"
    "DocumentRequestPost"
    "Comment"
    "MediaFile"
    "DocumentFile"
    "QuestionBoardLike"
    "DocumentBoardLike"
    "CommentLike"
    "QuestionPostCustomTag"
    "DocumentPostCustomTag"
    "DocumentPostBookmark"
    "PurchaseHistory"
)

for entity in "${post_entities[@]}"; do
    echo "✅ Fixing $entity imports..."
    find . -name "*.java" -exec sed -i '' "s/import com\.balsamic\.sejongmalsami\.object\.\(postgres\|mongo\)\.$entity;/import com.balsamic.sejongmalsami.post.object.\1.$entity;/g" {} \;
done

# Notice 도메인 Entity imports
notice_entities=(
    "NoticePost"
    "NoticeBoardLike"
)

for entity in "${notice_entities[@]}"; do
    echo "✅ Fixing $entity imports..."
    find . -name "*.java" -exec sed -i '' "s/import com\.balsamic\.sejongmalsami\.object\.\(postgres\|mongo\)\.$entity;/import com.balsamic.sejongmalsami.notice.object.\1.$entity;/g" {} \;
done

# Auth 도메인 Entity imports
auth_entities=(
    "RefreshToken"
    "FcmToken"
)

for entity in "${auth_entities[@]}"; do
    echo "✅ Fixing $entity imports..."
    find . -name "*.java" -exec sed -i '' "s/import com\.balsamic\.sejongmalsami\.object\.mongo\.$entity;/import com.balsamic.sejongmalsami.auth.object.mongo.$entity;/g" {} \;
done

# AI 도메인 Entity imports
ai_entities=(
    "PostEmbedding"
    "SearchQueryCache"
    "SearchHistory"
)

for entity in "${ai_entities[@]}"; do
    echo "✅ Fixing $entity imports..."
    find . -name "*.java" -exec sed -i '' "s/import com\.balsamic\.sejongmalsami\.object\.\(postgres\|mongo\)\.$entity;/import com.balsamic.sejongmalsami.ai.object.\1.$entity;/g" {} \;
done

# Academic 도메인 Entity imports
academic_entities=(
    "CourseFile"
    "DepartmentFile"
)

for entity in "${academic_entities[@]}"; do
    echo "✅ Fixing $entity imports..."
    find . -name "*.java" -exec sed -i '' "s/import com\.balsamic\.sejongmalsami\.object\.postgres\.$entity;/import com.balsamic.sejongmalsami.academic.object.postgres.$entity;/g" {} \;
done

# GeneralPost DTO import 수정
echo "✅ Fixing GeneralPost DTO imports..."
find . -name "*.java" -exec sed -i '' "s/import com\.balsamic\.sejongmalsami\.dto\.GeneralPost;/import com.balsamic.sejongmalsami.post.dto.GeneralPost;/g" {} \;

echo "🎉 Entity import 경로 수정 완료!" 