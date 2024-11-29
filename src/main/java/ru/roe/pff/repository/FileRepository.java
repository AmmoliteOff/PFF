package ru.roe.pff.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.roe.pff.entity.FeedFile;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FeedFile, UUID> {

    @Query("SELECT f FROM FeedFile f ORDER BY f.createdAt DESC")
    Optional<FeedFile> getLatest();

    @Query("SELECT f.link FROM FeedFile f ORDER BY f.createdAt DESC")
    Optional<String> getLatestFixedLink();

}
