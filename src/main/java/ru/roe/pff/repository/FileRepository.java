package ru.roe.pff.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.roe.pff.entity.FeedFile;

import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FeedFile, UUID> {
}
