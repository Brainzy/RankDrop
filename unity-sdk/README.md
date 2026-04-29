# RankDrop SDK for Unity

How to use:
1. Copy the `unity-sdk` folder into your Unity project.
2. Make sure `RankDropConfig.cs` contains your `server_url`, `game_secret`, and `default_leaderboard` values.
3. Call `RankDrop.SubmitScore(...)`, `RankDrop.GetTopScores(...)`, or use `RankDropCoroutines` if you prefer coroutine-based calls.
