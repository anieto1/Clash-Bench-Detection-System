package com.pm.clashbenchdetectionsystem.player;

import com.pm.clashbenchdetectionsystem.player.playerDTO.PlayerCwlHistoryResponse;
import com.pm.clashbenchdetectionsystem.player.playerDTO.PlayerResponse;
import com.pm.clashbenchdetectionsystem.player.playerDTO.PlayerStatsResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/players")
@Tag(name = "Players", description = "The Players API")
public class PlayerController {

    private final PlayerService playerService;

    @GetMapping("/{tag}")
    public ResponseEntity<PlayerResponse> getPlayer(@PathVariable String tag) {
        PlayerResponse player = playerService.getPlayer(tag);
        return ResponseEntity.ok(player);
    }

    @GetMapping("/{tag}/cwl")
    public ResponseEntity<PlayerCwlHistoryResponse> getPlayerCwlHistory(@PathVariable String tag) {
        PlayerCwlHistoryResponse history = playerService.getPlayerCwlHistory(tag);
        return ResponseEntity.ok(history);
    }

    @GetMapping("/{tag}/stats")
    public ResponseEntity<PlayerStatsResponse> getPlayerStats(@PathVariable String tag) {
        PlayerStatsResponse stats = playerService.getPlayerStats(tag);
        return ResponseEntity.ok(stats);
    }
}
