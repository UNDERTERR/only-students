package com.onlystudents.search.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery.Builder;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import com.onlystudents.search.client.UserClient;
import com.onlystudents.search.dto.NoteSearchResult;
import com.onlystudents.search.dto.SearchResult;
import com.onlystudents.search.dto.UserSearchResult;
import com.onlystudents.search.entity.NoteDocument;
import com.onlystudents.search.service.ElasticsearchIndexService;
import com.onlystudents.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient elasticsearchClient;
    private final RedisTemplate<String, Object> redisTemplate;
    private final UserClient userClient;
    private final ElasticsearchIndexService elasticsearchIndexService;

    private static final String HOT_KEYWORDS_KEY = "search:hot:keywords";
    private static final String USER_SUGGEST_KEY_PREFIX = "search:suggest:user:";

    @Override
    public SearchResult<NoteSearchResult> searchNotes(String keyword, Integer subjectId, Integer educationLevel,
                                                      Integer priceType, Integer sortType, Integer page, Integer size) {
        log.info("搜索笔记：keyword={}, subjectId={}, educationLevel={}, priceType={}, sortType={}, page={}, size={}",
                keyword, subjectId, educationLevel, priceType, sortType, page, size);

        try {
            // 构建查询条件
            SearchRequest.Builder requestBuilder = new SearchRequest.Builder()
                    .index("notes")
                    .from((page - 1) * size)
                    .size(size)
                    .source(s -> s.filter(f -> f.includes(
                            "noteId", "title", "content", "tags", "categoryId", "categoryName",
                            "userId", "authorNickname", "authorAvatar",
                            "educationLevel", "schoolId", "schoolName", "subject",
                            "visibility", "price", "status", "hotScore",
                            "viewCount", "likeCount", "favoriteCount", "commentCount", "shareCount",
                            "rating", "ratingCount", "coverImage", "publishTime", "createdAt"
                    )));

            // 构建 bool 查询
            Builder boolQuery = new Builder();

            // 关键词搜索（标题、内容、标签）
            if (keyword != null && !keyword.trim().isEmpty()) {
                // 对于纯数字且长度<=3的搜索词，使用通配符查询补充
                if (keyword.matches("\\d+") && keyword.length() <= 3) {
                    boolQuery.must(m -> m
                            .bool(b -> b
                                    .should(s -> s.multiMatch(mm -> mm
                                            .fields("title^3", "content^2", "tags", "authorNickname")
                                            .query(keyword)
                                            .type(TextQueryType.BestFields)))
                                    .should(s -> s.wildcard(w -> w.field("title").value("*" + keyword + "*")))
                                    .should(s -> s.wildcard(w -> w.field("content").value("*" + keyword + "*")))
                            )
                    );
                } else {
                    boolQuery.must(m -> m
                            .multiMatch(mm -> mm
                                    .fields("title^3", "content^2", "tags", "authorNickname")
                                    .query(keyword)
                                    .type(TextQueryType.BestFields)
                            )
                    );
                }

                // 高亮配置
                Map<String, HighlightField> highlightFields = new HashMap<>();
                highlightFields.put("title", HighlightField.of(hf -> hf));
                highlightFields.put("content", HighlightField.of(hf -> hf.preTags("<em>").postTags("</em>").fragmentSize(150)));

                requestBuilder.highlight(h -> h
                        .fields(highlightFields)
                        .preTags("<em>")
                        .postTags("</em>")
                );
            }

            // 学科筛选
            if (subjectId != null && subjectId > 0) {
                boolQuery.filter(f -> f.term(t -> t.field("subject").value(subjectId)));
            }

            // 教育阶段筛选
            if (educationLevel != null && educationLevel > 0) {
                boolQuery.filter(f -> f.term(t -> t.field("educationLevel").value(educationLevel)));
            }

            // 价格筛选（简化为只判断是否为免费）
            if (priceType != null) {
                if (priceType == 1) {
                    // 免费：price = 0
                    boolQuery.filter(f -> f.term(t -> t.field("price").value(0)));
                } else if (priceType == 2) {
                    // 付费：price > 0，使用 script query 或者简单处理
                    // 暂时跳过复杂的价格区间筛选
                }
            }

            // 只搜索未删除的笔记
            boolQuery.filter(f -> f.term(t -> t.field("deleted").value(0)));

            // 只搜索已发布的笔记
            boolQuery.filter(f -> f.term(t -> t.field("status").value(2)));

            // 可见性筛选（只搜索公开的）
            boolQuery.filter(f -> f.term(t -> t.field("visibility").value(0)));

            requestBuilder.query(q -> q.bool(boolQuery.build()));

            // 排序
            if (sortType != null) {
                switch (sortType) {
                    case 1: // 相关度（默认）
                        requestBuilder.sort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
                        break;
                    case 2: // 最新发布
                        requestBuilder.sort(s -> s.field(f -> f.field("publishTime").order(SortOrder.Desc)));
                        break;
                    case 3: // 热度最高
                        requestBuilder.sort(s -> s.field(f -> f.field("hotScore").order(SortOrder.Desc)));
                        break;
                    case 4: // 价格升序
                        requestBuilder.sort(s -> s.field(f -> f.field("price").order(SortOrder.Asc)));
                        break;
                    case 5: // 价格降序
                        requestBuilder.sort(s -> s.field(f -> f.field("price").order(SortOrder.Desc)));
                        break;
                    case 6: // 评分最高
                        requestBuilder.sort(s -> s.field(f -> f.field("ratingAvg").order(SortOrder.Desc)));
                        break;
                    default:
                        requestBuilder.sort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
                }
            } else {
                requestBuilder.sort(s -> s.score(sc -> sc.order(SortOrder.Desc)));
            }

            // 执行搜索
            SearchResponse<NoteDocument> response = elasticsearchClient.search(
                    requestBuilder.build(),
                    NoteDocument.class
            );
            // 处理结果
            List<NoteSearchResult> results = response.hits().hits().stream()
                    .map(hit -> {
                        NoteDocument doc = hit.source();
                        NoteSearchResult result = new NoteSearchResult();
                        BeanUtils.copyProperties(doc, result);
                        result.setId(doc.getNoteId());
                        result.setAuthorId(doc.getUserId());
                        result.setAuthorName(doc.getAuthorNickname());
                        result.setAuthorAvatar(doc.getAuthorAvatar());
                        result.setPrice(doc.getPrice() != null ? doc.getPrice().intValue() : 0);

                        // 处理高亮
                        if (hit.highlight() != null && hit.highlight().containsKey("title")) {
                            result.setTitle(hit.highlight().get("title").get(0));
                        }
                        if (hit.highlight() != null && hit.highlight().containsKey("content")) {
                            result.setSummary(hit.highlight().get("content").get(0));
                        } else if (doc.getContent() != null) {
                            // 截取前150个字符作为摘要
                            result.setSummary(doc.getContent().length() > 150
                                    ? doc.getContent().substring(0, 150) + "..."
                                    : doc.getContent());
                        }

                        return result;
                    })
                    .collect(Collectors.toList());

            // 构建返回结果
            SearchResult<NoteSearchResult> searchResult = new SearchResult<>();
            searchResult.setList(results);
            searchResult.setTotal(response.hits().total() != null ? response.hits().total().value() : 0);
            searchResult.setPage(page);
            searchResult.setSize(size);
            searchResult.setTotalPages((int) Math.ceil((double) searchResult.getTotal() / size));

            recordSearchHistory(keyword);

            return searchResult;

        } catch (IOException e) {
            log.error("搜索笔记失败：keyword={}", keyword, e);
            // 返回空结果
            SearchResult<NoteSearchResult> emptyResult = new SearchResult<>();
            emptyResult.setList(new ArrayList<>());
            emptyResult.setTotal(0L);
            emptyResult.setPage(page);
            emptyResult.setSize(size);
            emptyResult.setTotalPages(0);
            return emptyResult;
        }
    }

    @Override
    public SearchResult<UserSearchResult> searchUsers(String keyword, Integer educationLevel,
                                                      Integer isCreator, Integer page, Integer size) {
        log.info("搜索用户：keyword={}, educationLevel={}, isCreator={}, page={}, size={}",
                keyword, educationLevel, isCreator, page, size);

        try {
            // 调用 User-Service 进行 MySQL 搜索
            var response = userClient.searchUsers(keyword, educationLevel, isCreator, page, size);

            if (response.getCode() != 200 || response.getData() == null) {
                log.warn("搜索用户失败：{}", response.getMessage());
                SearchResult<UserSearchResult> emptyResult = new SearchResult<>();
                emptyResult.setList(new ArrayList<>());
                emptyResult.setTotal(0L);
                emptyResult.setPage(page);
                emptyResult.setSize(size);
                emptyResult.setTotalPages(0);
                return emptyResult;
            }

            List<UserSearchResult> results = response.getData().stream()
                    .map(user -> {
                        UserSearchResult result = new UserSearchResult();
                        BeanUtils.copyProperties(user, result);
                        result.setId(user.getId());
                        return result;
                    })
                    .collect(Collectors.toList());

            SearchResult<UserSearchResult> searchResult = new SearchResult<>();
            searchResult.setList(results);
            // MySQL 查询暂时不返回总数，后续优化
            searchResult.setTotal((long) results.size());
            searchResult.setPage(page);
            searchResult.setSize(size);
            searchResult.setTotalPages(1); // 简化处理

            return searchResult;

        } catch (Exception e) {
            log.error("搜索用户失败：keyword={}", keyword, e);
            SearchResult<UserSearchResult> emptyResult = new SearchResult<>();
            emptyResult.setList(new ArrayList<>());
            emptyResult.setTotal(0L);
            emptyResult.setPage(page);
            emptyResult.setSize(size);
            emptyResult.setTotalPages(0);
            return emptyResult;
        }
    }

    @Override
    public SearchResult<NoteSearchResult> searchNotesByTag(String tag, Integer page, Integer size) {
        log.info("按标签搜索笔记：tag={}, page={}, size={}", tag, page, size);

        try {
            SearchRequest request = SearchRequest.of(s -> s
                    .index("notes")
                    .from((page - 1) * size)
                    .size(size)
                    .query(q -> q
                            .term(t -> t.field("tags").value(tag))
                    )
                    .sort(sort -> sort.field(f -> f.field("hotScore").order(SortOrder.Desc)))
            );

            SearchResponse<NoteDocument> response = elasticsearchClient.search(request, NoteDocument.class);

            List<NoteSearchResult> results = response.hits().hits().stream()
                    .map(hit -> {
                        NoteDocument doc = hit.source();
                        NoteSearchResult result = new NoteSearchResult();
                        BeanUtils.copyProperties(doc, result);
                        result.setId(doc.getNoteId());
                        result.setAuthorId(doc.getUserId());
                        result.setAuthorName(doc.getAuthorNickname());
                        result.setAuthorAvatar(doc.getAuthorAvatar());
                        return result;
                    })
                    .collect(Collectors.toList());

            SearchResult<NoteSearchResult> searchResult = new SearchResult<>();
            searchResult.setList(results);
            searchResult.setTotal(response.hits().total() != null ? response.hits().total().value() : 0);
            searchResult.setPage(page);
            searchResult.setSize(size);
            searchResult.setTotalPages((int) Math.ceil((double) searchResult.getTotal() / size));

            return searchResult;

        } catch (IOException e) {
            log.error("按标签搜索笔记失败：tag={}", tag, e);
            SearchResult<NoteSearchResult> emptyResult = new SearchResult<>();
            emptyResult.setList(new ArrayList<>());
            emptyResult.setTotal(0L);
            emptyResult.setPage(page);
            emptyResult.setSize(size);
            emptyResult.setTotalPages(0);
            return emptyResult;
        }
    }

    @Override
    public SearchResult<String> getHotKeywords(Integer limit) {
        log.info("获取热门关键词：limit={}", limit);

        try {
            // 从Redis获取热门搜索词
            Set<Object> hotKeywords = redisTemplate.opsForZSet()
                    .reverseRange(HOT_KEYWORDS_KEY, 0, limit - 1);

            List<String> keywords = new ArrayList<>();
            if (hotKeywords != null && !hotKeywords.isEmpty()) {
                keywords = hotKeywords.stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
            } else {
                // 默认热门词
                keywords = Arrays.asList("数学", "英语", "物理", "高考", "考研", "Python", "Java", "化学");
            }

            SearchResult<String> result = new SearchResult<>();
            result.setList(keywords);
            result.setTotal((long) keywords.size());
            result.setPage(1);
            result.setSize(limit);
            result.setTotalPages(1);

            return result;

        } catch (Exception e) {
            log.error("获取热门关键词失败", e);
            SearchResult<String> result = new SearchResult<>();
            result.setList(Arrays.asList("数学", "英语", "物理", "高考", "考研"));
            result.setTotal(5L);
            result.setPage(1);
            result.setSize(limit);
            result.setTotalPages(1);
            return result;
        }
    }

    @Override
    public SearchResult<String> getSuggestions(String prefix, Integer limit) {
        log.info("获取搜索建议：prefix={}, limit={}", prefix, limit);

        if (prefix == null || prefix.trim().isEmpty()) {
            SearchResult<String> emptyResult = new SearchResult<>();
            emptyResult.setList(new ArrayList<>());
            emptyResult.setTotal(0L);
            emptyResult.setPage(1);
            emptyResult.setSize(limit);
            emptyResult.setTotalPages(0);
            return emptyResult;
        }

        try {
            // 使用前缀匹配查询
            SearchRequest request = SearchRequest.of(s -> s
                    .index("notes")
                    .size(limit)
                    .query(q -> q
                            .prefix(p -> p.field("title").value(prefix))
                    )
                    .source(source -> source.fetch(false))
            );

            SearchResponse<NoteDocument> response = elasticsearchClient.search(request, NoteDocument.class);

            // 提取建议词
            List<String> suggestions = response.hits().hits().stream()
                    .map(hit -> hit.source() != null ? hit.source().getTitle() : null)
                    .filter(Objects::nonNull)
                    .distinct()
                    .limit(limit)
                    .collect(Collectors.toList());

            SearchResult<String> result = new SearchResult<>();
            result.setList(suggestions);
            result.setTotal((long) suggestions.size());
            result.setPage(1);
            result.setSize(limit);
            result.setTotalPages(1);

            return result;

        } catch (IOException e) {
            log.error("获取搜索建议失败：prefix={}", prefix, e);
            SearchResult<String> result = new SearchResult<>();
            result.setList(new ArrayList<>());
            result.setTotal(0L);
            result.setPage(1);
            result.setSize(limit);
            result.setTotalPages(0);
            return result;
        }
    }

    /**
     * 记录搜索历史
     */
    private void recordSearchHistory(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return;
        }

        try {
            // 增加搜索词热度
            redisTemplate.opsForZSet().incrementScore(HOT_KEYWORDS_KEY, keyword, 1);

            // 设置过期时间（7天）
            redisTemplate.expire(HOT_KEYWORDS_KEY, java.time.Duration.ofDays(7));

        } catch (Exception e) {
            log.warn("记录搜索历史失败：keyword={}", keyword, e);
        }
    }
}
