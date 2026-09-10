package com.pm.clashbenchdetectionsystem.player;


import com.pm.clashbenchdetectionsystem.player.playerDTO.PlayerResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PlayerMapper {

    PlayerResponse toResponse(Player player);

    @Mapping(target = "name", source = "heroName")
    PlayerResponse.HeroDto toHeroDto(PlayerHero hero);

    @Mapping(target = "name", source = "troopName")
    PlayerResponse.TroopDto toTroopDto(PlayerTroop troop);

    @Mapping(target = "name", source = "spellName")
    PlayerResponse.SpellDto toSpellDto(PlayerSpell spell);

    @Mapping(target = "name", source = "petName")
    PlayerResponse.PetDto toPetDto(PlayerPet pet);

    PlayerResponse.EquipmentDto toEquipmentDto(PlayerEquipment equipment);
}
