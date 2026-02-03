package com.pm.clashbenchdetectionsystem.player;


import com.pm.clashbenchdetectionsystem.player.playerDTO.PlayerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    PlayerResponse toResponse(Player player);

    PlayerResponse.HeroDto toHeroDto(PlayerHero hero);

    PlayerResponse.TroopDto toTroopDto(PlayerTroop troop);

    PlayerResponse.SpellDto toSpellDto(PlayerSpell spell);

    PlayerResponse.PetDto toPetDto(PlayerPet pet);

    @Mapping(target = "heroName", source = "heroName")
    PlayerResponse.EquipmentDto toEquipmentDto(PlayerEquipment equipment);
}
