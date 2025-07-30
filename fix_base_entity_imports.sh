#!/bin/bash

echo "🔧 BaseEntity import 추가 시작..."

# PostgreSQL 엔티티들에 BaseEntity import 추가
postgres_entities=(
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/postgres/QuestionPost.java"
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/postgres/AnswerPost.java"
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/postgres/DocumentPost.java"
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/postgres/DocumentRequestPost.java"
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/postgres/Comment.java"
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/postgres/MediaFile.java"
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/postgres/DocumentFile.java"
    "SM-Domain-Notice/src/main/java/com/balsamic/sejongmalsami/notice/object/postgres/NoticePost.java"
    "SM-Domain-AI/src/main/java/com/balsamic/sejongmalsami/ai/object/postgres/PostEmbedding.java"
    "SM-Domain-AI/src/main/java/com/balsamic/sejongmalsami/ai/object/postgres/SearchQueryCache.java"
)

for entity in "${postgres_entities[@]}"; do
    if [ -f "$entity" ]; then
        echo "✅ Adding BaseEntity import to $entity"
        # package 선언 다음에 BaseEntity import 추가
        sed -i '' '/^package /a\
\
import com.balsamic.sejongmalsami.object.postgres.BaseEntity;
' "$entity"
    fi
done

# MongoDB 엔티티들에 BaseMongoEntity import 추가  
mongo_entities=(
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/mongo/QuestionBoardLike.java"
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/mongo/DocumentBoardLike.java"
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/mongo/CommentLike.java"
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/mongo/QuestionPostCustomTag.java"
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/mongo/DocumentPostCustomTag.java"
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/mongo/DocumentPostBookmark.java"
    "SM-Domain-Post/src/main/java/com/balsamic/sejongmalsami/post/object/mongo/PurchaseHistory.java"
    "SM-Domain-Notice/src/main/java/com/balsamic/sejongmalsami/notice/object/mongo/NoticeBoardLike.java"
    "SM-Domain-Auth/src/main/java/com/balsamic/sejongmalsami/auth/object/mongo/RefreshToken.java"
    "SM-Domain-Auth/src/main/java/com/balsamic/sejongmalsami/auth/object/mongo/FcmToken.java"
    "SM-Domain-AI/src/main/java/com/balsamic/sejongmalsami/ai/object/mongo/SearchHistory.java"
)

for entity in "${mongo_entities[@]}"; do
    if [ -f "$entity" ]; then
        echo "✅ Adding BaseMongoEntity import to $entity"
        # package 선언 다음에 BaseMongoEntity import 추가
        sed -i '' '/^package /a\
\
import com.balsamic.sejongmalsami.object.mongo.BaseMongoEntity;
' "$entity"
    fi
done

echo "🎉 BaseEntity import 추가 완료!" 