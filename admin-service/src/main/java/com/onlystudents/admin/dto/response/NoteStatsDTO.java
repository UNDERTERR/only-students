package com.onlystudents.admin.dto.response;

import lombok.Data;
import java.io.Serializable;

@Data
public class NoteStatsDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long totalNotes;
    private Long todayNewNotes;
    private Long weekNewNotes;
    private Long monthNewNotes;
    private Long publishedNotes;
    private Long pendingAuditNotes;
    private Long rejectedNotes;
}
