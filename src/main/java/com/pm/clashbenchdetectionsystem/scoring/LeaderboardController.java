package com.pm.clashbenchdetectionsystem.scoring;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/leaderboard")
@Tag(name = "Leaderboard", description = "All-time CBDS leaderboard")
public class LeaderboardController {

    private final CbdsScoreService cbdsScoreService;

    @GetMapping("/overall")
    public ResponseEntity<OverallLeaderboardResponse> getOverallLeaderboard() {
        OverallLeaderboardResponse leaderboard = cbdsScoreService.getOverallLeaderboard();
        return ResponseEntity.ok(leaderboard);
    }
}
