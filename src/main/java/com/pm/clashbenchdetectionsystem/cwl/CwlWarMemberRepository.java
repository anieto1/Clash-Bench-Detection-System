package com.pm.clashbenchdetectionsystem.cwl;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CwlWarMemberRepository extends JpaRepository<CwlWarMember, CwlWarMemberId> {

    List<CwlWarMember> findByWarTag(String warTag);

    Optional<CwlWarMember> findByWarTagAndPlayerTag(String warTag, String playerTag);
}
