package ru.roe.pff.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.roe.pff.entity.FeedFile;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileRepository extends JpaRepository<FeedFile, UUID> {

    @Query(value = "select * from public.feed_file order by created_at desc limit 1", nativeQuery = true)
    Optional<FeedFile> getLatest();

    @Query(value = "select * from public.feed_file where status = 'COMPLETED' order by created_at desc limit 1", nativeQuery = true)
    Optional<FeedFile> getLatestFixedLink();

}
