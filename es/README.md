# Elasticsearch 索引创建命令

## 1. 创建笔记索引

curl -X PUT "localhost:9200/note_index" -H 'Content-Type: application/json' -d @note-index.json

## 2. 创建用户索引

curl -X PUT "localhost:9200/user_index" -H 'Content-Type: application/json' -d @user-index.json

## 3. 创建搜索建议索引

curl -X PUT "localhost:9200/search_suggestion_index" -H 'Content-Type: application/json' -d @suggestion-index.json

## 4. 查看所有索引

curl -X GET "localhost:9200/_cat/indices?v"

## 5. 删除索引（谨慎使用）

# curl -X DELETE "localhost:9200/note_index"
# curl -X DELETE "localhost:9200/user_index"
# curl -X DELETE "localhost:9200/search_suggestion_index"

## 6. 查看索引映射

curl -X GET "localhost:9200/note_index/_mapping"
curl -X GET "localhost:9200/user_index/_mapping"

## 7. 索引别名（用于无缝切换）

# 创建别名
curl -X POST "localhost:9200/_aliases" -H 'Content-Type: application/json' -d'
{
  "actions": [
    { "add": { "index": "note_index", "alias": "note_read" }}
  ]
}'

# 8. IK分词器测试

curl -X POST "localhost:9200/_analyze" -H 'Content-Type: application/json' -d'
{
  "analyzer": "ik_max_word",
  "text": "高等数学笔记"
}'
