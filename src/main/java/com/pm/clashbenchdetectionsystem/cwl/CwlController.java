package com.pm.clashbenchdetectionsystem.cwl;

import com.pm.clashbenchdetectionsystem.cwl.cwlDTO.CwlSeasonResponse;
import com.pm.clashbenchdetectionsystem.cwl.cwlDTO.CwlWarResponse;
import com.pm.clashbenchdetectionsystem.cwl.cwlDTO.LeaderBoardResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@Tag(name = "CWL", description = "Clan War League tracking and scoring")
public class CwlController {

    private final CwlService cwlService;

    @PostMapping("/api/clans/{clanTag}/cwl/sync")
    public ResponseEntity<CwlSeasonResponse> syncLeagueGroup(@PathVariable String clanTag) {
        CwlSeasonResponse response = cwlService.syncLeagueGroup(clanTag);
        if (response == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/clans/{clanTag}/cwl")
    public ResponseEntity<List<CwlSeasonResponse>> getSeasonHistory(@PathVariable String clanTag) {
        return ResponseEntity.ok(cwlService.getSeasonHistory(clanTag));
    }

    @GetMapping("/api/clans/{clanTag}/cwl/{season}")
    public ResponseEntity<CwlSeasonResponse> getSeason(@PathVariable String clanTag,
                                                        @PathVariable String season) {
        return ResponseEntity.ok(cwlService.getSeason(clanTag, season));
    }

    @GetMapping("/api/cwl/wars/{warTag}")
    public ResponseEntity<CwlWarResponse> getWar(@PathVariable String warTag) {
        return ResponseEntity.ok(cwlService.getWar(warTag));
    }

    @GetMapping("/api/clans/{clanTag}/cwl/{season}/leaderboard")
    public ResponseEntity<LeaderBoardResponse> getLeaderboard(@PathVariable String clanTag,
                                                               @PathVariable String season) {
        return ResponseEntity.ok(cwlService.getLeaderboard(clanTag, season));
    }
}
