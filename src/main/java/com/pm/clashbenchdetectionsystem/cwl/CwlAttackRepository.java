package com.pm.clashbenchdetectionsystem.cwl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CwlAttackRepository extends JpaRepository<CwlAttack, CwlAttackId> {

    List<CwlAttack> findByWarTag(String warTag);

    List<CwlAttack> findByAttackerTag(String attackerTag);
}
