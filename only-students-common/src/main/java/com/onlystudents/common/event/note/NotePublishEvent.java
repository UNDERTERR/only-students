package com.onlystudents.common.event.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 笔记发布事件
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NotePublishEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long noteId;
    
    private Long userId;
    
    private String title;
    
    private String coverImage;
}
