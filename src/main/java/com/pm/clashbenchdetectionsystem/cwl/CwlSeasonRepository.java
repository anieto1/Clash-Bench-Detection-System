package com.pm.clashbenchdetectionsystem.cwl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CwlSeasonRepository extends JpaRepository<CwlSeason, CwlSeasonId> {

    Optional<CwlSeason> findByClanTagAndSeason(String clanTag, String season);

    List<CwlSeason> findByClanTagOrderBySeasonDesc(String clanTag);
}
