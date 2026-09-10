package com.pm.clashbenchdetectionsystem.cwl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pm.clashbenchdetectionsystem.cocAPI.dto.CocPlayerResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class StatsSnapshotHelper {

    private static final Logger log = LoggerFactory.getLogger(StatsSnapshotHelper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private StatsSnapshotHelper() {}

    /**
     * Build a minimal snapshot when we can't fetch the full player profile.
     */
    public static String buildSnapshot(String name, int townHallLevel) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("townHallLevel", townHallLevel);
        snapshot.put("name", name);
        snapshot.put("heroes", List.of());
        snapshot.put("equipment", List.of());
        snapshot.put("pets", List.of());
        snapshot.put("troops", List.of());
        snapshot.put("spells", List.of());

        try {
            return MAPPER.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize stats snapshot for {}: {}", name, e.getMessage());
            return "{\"townHallLevel\":" + townHallLevel + ",\"name\":\"" + name + "\"}";
        }
    }

    /**
     * Build a full snapshot from the complete player API response.
     * This is called once at CWL start and the result is immutable.
     */
    public static String buildFullSnapshot(CocPlayerResponse player) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        snapshot.put("townHallLevel", player.townHallLevel());
        snapshot.put("name", player.name());
        snapshot.put("snapshotTakenAt", Instant.now().toString());

        // Heroes with their equipped items
        List<Map<String, Object>> heroes = player.homeHeroes().stream()
                .map(h -> {
                    Map<String, Object> heroMap = new LinkedHashMap<>();
                    heroMap.put("name", h.name());
                    heroMap.put("level", h.level());
                    heroMap.put("maxLevel", h.maxLevel());
                    if (h.equipment() != null) {
                        heroMap.put("equipment", h.equipment().stream()
                                .map(eq -> {
                                    Map<String, Object> eqMap = new LinkedHashMap<>();
                                    eqMap.put("name", eq.name());
                                    eqMap.put("level", eq.level());
                                    eqMap.put("maxLevel", eq.maxLevel());
                                    return eqMap;
                                })
                                .toList());
                    }
                    return heroMap;
                })
                .toList();
        snapshot.put("heroes", heroes);

        // Pets
        List<Map<String, Object>> pets = player.pets().stream()
                .map(p -> {
                    Map<String, Object> petMap = new LinkedHashMap<>();
                    petMap.put("name", p.name());
                    petMap.put("level", p.level());
                    petMap.put("maxLevel", p.maxLevel());
                    return petMap;
                })
                .toList();
        snapshot.put("pets", pets);

        // Troops
        List<Map<String, Object>> troops = player.homeTroops().stream()
                .map(t -> {
                    Map<String, Object> troopMap = new LinkedHashMap<>();
                    troopMap.put("name", t.name());
                    troopMap.put("level", t.level());
                    troopMap.put("maxLevel", t.maxLevel());
                    return troopMap;
                })
                .toList();
        snapshot.put("troops", troops);

        // Spells
        List<Map<String, Object>> spells = player.homeSpells().stream()
                .map(s -> {
                    Map<String, Object> spellMap = new LinkedHashMap<>();
                    spellMap.put("name", s.name());
                    spellMap.put("level", s.level());
                    spellMap.put("maxLevel", s.maxLevel());
                    return spellMap;
                })
                .toList();
        snapshot.put("spells", spells);

        try {
            return MAPPER.writeValueAsString(snapshot);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize full stats snapshot for {}: {}", player.name(), e.getMessage());
            return buildSnapshot(player.name(), player.townHallLevel());
        }
    }

    public static int extractTownHallLevel(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            return root.path("townHallLevel").asInt(1);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse stats snapshot for town hall level: {}", e.getMessage());
            return 1;
        }
    }

    public static String extractName(String json) {
        try {
            JsonNode root = MAPPER.readTree(json);
            return root.path("name").asText(null);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse stats snapshot for name: {}", e.getMessage());
            return null;
        }
    }
}
