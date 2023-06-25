# match-scoreboard-demo

This repository provides library for managing real time match scoreboard in thread-safe manner.
Functionality is intentionally enriched with multiple similar methods to provide different options how clients could operate with matches, just matter of taste. 
It's possible to operate with the following options:
- by match ID
- by home and away team IDs
- by home and away team names

In order to activate match, initially teams should be registered, and after that match should be started.
When match is finished, we could unregister it.

Calculation of scoreboard done on any match update only, and during fetching it just returns already precalculated result in constant time complexity.
Update has linear time complexity due to traversing all active matches to build scoreboard.
Here we suggest that updates will be invoked with much lower rate than read-only fetch of scoreboard.
Expectation that it will be maximum few thousands of active matches at specific time period (but in reality even much less, up to tens).
Internally, library stores match details in both maps `matchIdToMatchDetailsMapping` and `matchTeamIdsToMatchDetailsMapping` in order to speed up search by teams and do not make full scan by all teams.

Library has multiple verifications, e.g.:
- whether team is registered prior to operate with match
- whether match is registered prior to operate with match
- whether match is active prior to update match score
- specific team is allowed to actively play only is a single match at specific time
- update match score can't be negative
- match status transfers

It doesn't verify that updated score might decrease, as in reality it's possible that referee rejected recent goal.
Also, we don't try to fix cases with reordered events from invoker side. 
If it's needed, we could additionally introduce client update version, and do not update match score if we detected reordered events.

In order to see how to operate with library, please take a look at functional test `DemoFunctionalTest`.
