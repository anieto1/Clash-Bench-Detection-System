package com.pm.clashbenchdetectionsystem.cocAPI;

import com.pm.clashbenchdetectionsystem.cocAPI.dto.CocClanResponse;
import com.pm.clashbenchdetectionsystem.cocAPI.dto.CocPlayerResponse;
import com.pm.clashbenchdetectionsystem.cocAPI.dto.CocCwlWarResponse;
import com.pm.clashbenchdetectionsystem.cocAPI.dto.LeagueGroupResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

@HttpExchange
public interface CocApiClient {

    @GetExchange("/players/{tag}")
    CocPlayerResponse getPlayer(@PathVariable String tag);

    @GetExchange("/clans/{tag}")
    CocClanResponse getClan(@PathVariable String tag);

    @GetExchange("/clans/{tag}/currentwar/leaguegroup")
    LeagueGroupResponse getLeagueGroup(@PathVariable String tag);

    @GetExchange("/clanwarleagues/wars/{warTag}")
    CocCwlWarResponse getCwlWar(@PathVariable String warTag);
}
