package com.pm.clashbenchdetectionsystem.cwl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CwlWarRepository extends JpaRepository<CwlWar, String> {

    List<CwlWar> findByClanTagAndSeasonOrderByDayNumber(String clanTag, String season);
}
