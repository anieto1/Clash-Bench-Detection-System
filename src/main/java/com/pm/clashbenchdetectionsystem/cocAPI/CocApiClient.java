package com.pm.clashbenchdetectionsystem.cocAPI;

import com.pm.clashbenchdetectionsystem.cocAPI.dto.CocPlayerResponse;
import com.pm.clashbenchdetectionsystem.cocAPI.dto.ClanResponse;
import com.pm.clashbenchdetectionsystem.cocAPI.dto.CwlWarResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.DeleteExchange;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

import java.util.List;


@Component
@HttpExchange
public interface CocApiClient {

    @GetExchange("/players/{tag}")
    CocPlayerResponse getPlayer(@PathVariable String tag);

    @GetExchange("/clans/{tag}")
    ClanResponse getClan(@PathVariable String tag);

    @GetExchange("/clans/{tag}/currentwar/leaguegroup")
    CwlWarResponse getWar(@PathVariable String tag);

    @GetExchange("/api/clans/{tag}")
    Object getClanProfile(@PathVariable String tag);

    @GetExchange("/api/clans/{tag}/members")
    List<Object> getClanMembers(@PathVariable String tag);

    @GetExchange("/api/clans/{tag}/cwl")
    List<Object> getClanCwlHistory(@PathVariable String tag);

    @GetExchange("/api/clans/{tag}/cwl/{season}")
    Object getClanCwlSeason(@PathVariable String tag, @PathVariable String season);

    @GetExchange("/api/players/{tag}")
    Object getPlayerProfile(@PathVariable String tag);

    @GetExchange("/api/players/{tag}/cwl")
    List<Object> getPlayerCwlHistory(@PathVariable String tag);

    @GetExchange("/api/players/{tag}/stats")
    Object getPlayerStats(@PathVariable String tag);

    @PostExchange("/api/tracking/clans")
    Void startTrackingClan(@RequestBody String tag);

    @DeleteExchange("/api/tracking/clans/{tag}")
    Void stopTrackingClan(@PathVariable String tag);

    @GetExchange("/api/tracking/clans")
    List<Object> getTrackedClans();

    @GetExchange("/api/leaderboard/cwl/{season}")
    Object getSeasonLeaderboard(@PathVariable String season);

    @GetExchange("/api/leaderboard/overall")
    Object getOverallLeaderboard();

}
