package ru.roe.pff.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.roe.pff.repository.FeedFileLinkRepository;
import ru.roe.pff.repository.FileRepository;
import ru.roe.pff.service.FileProcessingService;

@Slf4j
@Component
@RequiredArgsConstructor
public class FeedFileUpdateScheduler {

    private final FileRepository fileRepository;
    private final FeedFileLinkRepository linkRepository;
    private final FileProcessingService processingService;


    @Scheduled(cron = "0 0 */3 * * *")
    @Transactional
    public void handleUpdateByLink() {
        var links = linkRepository.findAll();
        if (links.isEmpty()) {
            log.debug("No links found to auto-update => skipping");
        }
        links.forEach(link -> processingService.submitLinkToProcess(link.getLink()));
    }

}
