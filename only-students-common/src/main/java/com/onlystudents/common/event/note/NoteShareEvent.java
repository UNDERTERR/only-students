package com.onlystudents.common.event.note;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 笔记分享事件（接收自 rating-service）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoteShareEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long noteId;
    private Long userId;
    private Integer shareType;
    private String shareCode;
}
