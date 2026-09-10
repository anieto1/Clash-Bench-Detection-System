package com.pm.clashbenchdetectionsystem.clan;

import com.pm.clashbenchdetectionsystem.clan.clanDTO.ClanResponse;
import com.pm.clashbenchdetectionsystem.player.PlayerService;
import com.pm.clashbenchdetectionsystem.player.playerDTO.PlayerResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/clans")
@Tag(name = "Clans", description = "The Clans API")
public class ClanController {

    private final ClanService clanService;
    private final PlayerService playerService;

    @GetMapping("/{tag}")
    public ResponseEntity<ClanResponse> getClan(@PathVariable String tag) {
        return ResponseEntity.ok(clanService.getClan(tag));
    }

    @GetMapping("/{tag}/members")
    public ResponseEntity<List<PlayerResponse>> getClanMembers(@PathVariable String tag) {
        return ResponseEntity.ok(playerService.getPlayersByClan(tag));
    }
}
