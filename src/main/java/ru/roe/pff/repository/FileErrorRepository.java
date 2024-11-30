package ru.roe.pff.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.roe.pff.entity.FeedFile;
import ru.roe.pff.entity.FileError;
import ru.roe.pff.enums.ErrorType;

import java.util.List;
import java.util.UUID;

@Repository
public interface FileErrorRepository extends JpaRepository<FileError, UUID> {
    List<FileError> findAllByFeedFile(FeedFile feedFile);


    Page<FileError> findAllByFeedFileOrderByCreatedAtDesc(Pageable pageable, FeedFile feedFile);

    Page<FileError> findAllByErrorTypeAndFeedFileOrderByCreatedAtDesc(Pageable pageable, FeedFile feedFile, ErrorType errorType);
}
