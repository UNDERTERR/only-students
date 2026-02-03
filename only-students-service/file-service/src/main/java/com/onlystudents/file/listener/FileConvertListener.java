package com.onlystudents.file.listener;

import com.onlystudents.file.service.FileConvertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileConvertListener {
    
    private final FileConvertService fileConvertService;
    
    @RabbitListener(queues = "file.convert.queue")
    public void handleConvertTask(Long taskId) {
        log.info("收到PDF转换任务: taskId={}", taskId);
        fileConvertService.processConvertTask(taskId);
    }
}
