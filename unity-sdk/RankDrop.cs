using System;
using System.Collections.Generic;
using System.Text;
using System.Threading.Tasks;
using UnityEngine;
using UnityEngine.Networking;

namespace TurnKit.RankDrop
{
    // ── Response models ───────────────────────────────────────────────────────
    /// <summary>Single score entry. n = player alias, s = score, m = optional metadata.</summary>
    [Serializable]
    public class RankDropEntry
    {
        public string n = "";
        public long s;
        public string m = "";
    }

    /// <summary>Response from GetTopScores.</summary>
    [Serializable]
    public class RankDropTopScores
    {
        public List<RankDropEntry> scores = new();
    }

    /// <summary>
    /// Response from GetPlayerRankAndSurrounding.
    /// startRank is the rank of scores[0].
    /// </summary>
    [Serializable]
    public class RankDropPlayerScore
    {
        public int startRank;
        public List<RankDropEntry> scores = new();
    }
    
    /// <summary>Response from GetTopScoresAndPlayerRank.</summary>
    [Serializable]
    public class RankDropPlayerAndTopScores
    {
        public List<RankDropEntry> topScores = new();
        public RankDropPlayerScore playerScore = new();
    }
    
    public static class RankDrop
    {
        // ── Client ────────────────────────────────────────────────────────────────
        
        private const string UrlPrefix = "/api/v1/leaderboards/";

        /// <summary>
        /// Submits a score for a player.
        /// </summary>
        /// <param name="playerAlias">Display name shown on the leaderboard.</param>
        /// <param name="score">Score value to submit.</param>
        /// <param name="leaderboard">Leaderboard slug. Uses RankDropConfig.DefaultLeaderboard if null.</param>
        public static Task<RankDropEntry> SubmitScore(
            string playerAlias,
            double score,
            string leaderboard = null)
        {
            var url = $"{Base}{Slug(leaderboard)}/scores";
            var json = $"{{\"playerAlias\":\"{Esc(playerAlias)}\",\"scoreValue\":{score}}}";
            return Post<RankDropEntry>(url, json);
        }

        /// <summary>
        /// Fetches the top N scores from a leaderboard.
        /// </summary>
        /// <param name="limit">Number of entries to return (default 10).</param>
        /// <param name="leaderboard">Leaderboard slug. Uses RankDropConfig.DefaultLeaderboard if null.</param>
        public static Task<RankDropTopScores> GetTopScores(
            int limit = 10,
            string leaderboard = null)
        {
            var url = $"{Base}{Slug(leaderboard)}/top?limit={limit}";
            return Get<RankDropTopScores>(url);
        }

        /// <summary>
        /// Fetches a player's rank and the entries surrounding them.
        /// startRank in the response is the rank of the first entry in scores.
        /// </summary>
        /// <param name="playerAlias">Player to look up.</param>
        /// <param name="surrounding">Entries above and below to include (default 5).</param>
        /// <param name="withMeta">Include metadata (default false)</param>
        /// <param name="leaderboard">Leaderboard slug. Uses RankDropConfig.DefaultLeaderboard if null.</param>
        public static Task<RankDropPlayerScore> GetPlayerRankAndSurrounding(
            string playerAlias,
            int surrounding = 5,
            bool withMeta = false,
            string leaderboard = null)
        {
            var url = $"{Base}{Slug(leaderboard)}/players/{UnityWebRequest.EscapeURL(playerAlias)}" + (withMeta ? "/metadata" : "") +
                      $"?surrounding={surrounding}";
            return Get<RankDropPlayerScore>(url);
        }

        /// <summary>
        /// Fetches top scores and the player's rank/surrounding in one request.
        /// </summary>
        /// <param name="playerAlias">Player to look up.</param>
        /// <param name="topLimit">Number of top entries (default 10).</param>
        /// <param name="surrounding">Entries around the player (default 5).</param>
        /// /// <param name="withMeta">Include metadata (default false)</param>
        /// <param name="leaderboard">Leaderboard slug. Uses RankDropConfig.DefaultLeaderboard if null.</param>
        public static Task<RankDropPlayerAndTopScores> GetTopScoresAndPlayerRank(
            string playerAlias,
            int topLimit = 10,
            int surrounding = 5,
            bool withMeta = false,
            string leaderboard = null)
        {
            var url = $"{Base}{Slug(leaderboard)}/combined" +
                      $"?playerAlias={UnityWebRequest.EscapeURL(playerAlias)}" + (withMeta ? "/metadata" : "") +
                      $"&topLimit={topLimit}&surrounding={surrounding}";
            return Get<RankDropPlayerAndTopScores>(url);
        }

        // ── HTTP ──────────────────────────────────────────────────────────────

        private static async Task<T> Post<T>(string url, string json) where T : new()
        {
            var bytes = Encoding.UTF8.GetBytes(json);
            var req = new UnityWebRequest(url, "POST")
            {
                uploadHandler = new UploadHandlerRaw(bytes),
                downloadHandler = new DownloadHandlerBuffer()
            };
            req.SetRequestHeader("Content-Type", "application/json");
            req.SetRequestHeader("X-Game-Key", RankDropConfig.GameSecret);

            var op = req.SendWebRequest();
            while (!op.isDone) await Task.Yield();

            if (req.result != UnityWebRequest.Result.Success)
                throw new Exception($"RankDrop [{req.responseCode}]: {req.downloadHandler.text}");

            return JsonUtility.FromJson<T>(req.downloadHandler.text) ?? new T();
        }

        private static async Task<T> Get<T>(string url) where T : new()
        {
            var req = UnityWebRequest.Get(url);
            req.SetRequestHeader("X-Game-Key", RankDropConfig.GameSecret);

            var op = req.SendWebRequest();
            while (!op.isDone) await Task.Yield();

            if (req.result != UnityWebRequest.Result.Success)
                throw new Exception($"RankDrop [{req.responseCode}]: {req.downloadHandler.text}");

            return JsonUtility.FromJson<T>(req.downloadHandler.text) ?? new T();
        }

        // ── Helpers ───────────────────────────────────────────────────────────

        private static string Base =>
            RankDropConfig.ServerUrl.TrimEnd('/') + UrlPrefix;

        private static string Slug(string leaderboard) =>
            !string.IsNullOrWhiteSpace(leaderboard) ? leaderboard : RankDropConfig.DefaultLeaderboard;

        private static string Esc(string s) =>
            s.Replace("\\", "\\\\").Replace("\"", "\\\"");
    }
}