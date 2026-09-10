package com.pm.clashbenchdetectionsystem.clan;

import com.pm.clashbenchdetectionsystem.clan.clanDTO.ClanResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/tracking/clans")
@Tag(name = "Tracking", description = "Clan tracking management")
public class TrackingController {

    private final ClanService clanService;

    @PostMapping
    public ResponseEntity<Void> startTracking(@RequestBody TrackRequest request) {
        clanService.startTracking(request.clanTag());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{tag}")
    public ResponseEntity<Void> stopTracking(@PathVariable String tag) {
        clanService.stopTracking(tag);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ClanResponse>> getTrackedClans() {
        return ResponseEntity.ok(clanService.getTrackedClans());
    }

    public record TrackRequest(String clanTag) {}
}
