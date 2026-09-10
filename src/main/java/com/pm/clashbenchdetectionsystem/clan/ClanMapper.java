package com.pm.clashbenchdetectionsystem.clan;

import com.pm.clashbenchdetectionsystem.clan.clanDTO.ClanResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClanMapper {

    ClanResponse toResponse(Clan clan);
}
