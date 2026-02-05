package com.onlystudents.search.dto;

import lombok.Data;

import java.util.List;

@Data
public class SearchResult<T> {
    
    private List<T> list;
    
    private Long total;
    
    private Integer page;
    
    private Integer size;
    
    private Integer totalPages;
}
