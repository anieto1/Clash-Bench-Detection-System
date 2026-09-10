package com.pm.clashbenchdetectionsystem.clan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackedClanRepository extends JpaRepository<TrackedClan, String> {

    List<TrackedClan> findByActiveTrue();
}
