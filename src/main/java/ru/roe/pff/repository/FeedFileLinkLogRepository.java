package ru.roe.pff.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.roe.pff.entity.FixedFeedFileLog;

import java.util.UUID;

@Repository
public interface FeedFileLinkLogRepository extends JpaRepository<FixedFeedFileLog, UUID> {
}
