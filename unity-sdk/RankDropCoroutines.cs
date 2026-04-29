using System;
using System.Collections;
using UnityEngine;

namespace TurnKit.RankDrop
{
    /// <summary>
    /// Coroutine wrappers around RankDrop async methods.
    /// Drop this on any GameObject or let it auto-create itself.
    /// </summary>
    public class RankDropCoroutines : MonoBehaviour
    {
        // ── Singleton ─────────────────────────────────────────────────────────
        private static RankDropCoroutines _instance;

        public static RankDropCoroutines Instance
        {
            get
            {
                if (_instance != null) return _instance;

                Debug.LogError("Requested RankDropCoroutines but it was null, you need to manage the game object");
                return null;
            }
        }

        private void Awake()
        {
            _instance = this;
        }

        // ── Coroutine API ─────────────────────────────────────────────────────

        public static Coroutine SubmitScore(
            string playerAlias,
            long score,
            Action<RankDropEntry> onSuccess,
            Action<string> onError = null,
            string leaderboard = null)
        {
            return Instance.StartCoroutine(
                Wrap(() => RankDrop.SubmitScore(playerAlias, score, leaderboard), onSuccess, onError));
        }

        public static Coroutine GetTopScores(
            Action<RankDropTopScores> onSuccess,
            Action<string> onError = null,
            int limit = 10,
            string leaderboard = null)
        {
            return Instance.StartCoroutine(
                Wrap(() => RankDrop.GetTopScores(limit, leaderboard), onSuccess, onError));
        }

        public static Coroutine GetPlayerRankAndSurrounding(
            string playerAlias,
            Action<RankDropPlayerScore> onSuccess,
            Action<string> onError = null,
            int surrounding = 5,
            bool withMeta = false,
            string leaderboard = null)
        {
            return Instance.StartCoroutine(
                Wrap(() => RankDrop.GetPlayerRankAndSurrounding(playerAlias, surrounding, withMeta, leaderboard),
                    onSuccess, onError));
        }

        public static Coroutine GetTopScoresAndPlayerRank(
            string playerAlias,
            Action<RankDropPlayerAndTopScores> onSuccess,
            Action<string> onError = null,
            int topLimit = 10,
            int surrounding = 5,
            bool withMeta = false,
            string leaderboard = null)
        {
            return Instance.StartCoroutine(
                Wrap(() => RankDrop.GetTopScoresAndPlayerRank(playerAlias, topLimit, surrounding, withMeta, leaderboard),
                    onSuccess, onError));
        }

        // ── Task → Coroutine bridge ───────────────────────────────────────────

        private static IEnumerator Wrap<T>(
            Func<System.Threading.Tasks.Task<T>> taskFactory,
            Action<T> onSuccess,
            Action<string> onError)
        {
            var task = taskFactory();
            while (!task.IsCompleted) yield return null;

            if (task.IsFaulted)
                onError?.Invoke(task.Exception?.InnerException?.Message ?? "Unknown error");
            else
                onSuccess?.Invoke(task.Result);
        }
    }
}