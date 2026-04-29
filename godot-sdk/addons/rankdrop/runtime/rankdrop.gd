extends RefCounted
class_name RankDrop

const RankDropClientScript = preload("res://addons/rankdrop/runtime/rankdrop_client.gd")

static var _client: RankDropClient = RankDropClientScript.new()

static func configure(server_url: String, game_secret: String, default_leaderboard: String = RankDropClient.DEFAULT_LEADERBOARD) -> void:
	_client.configure(server_url, game_secret, default_leaderboard)

static func clear_runtime_config() -> void:
	_client.clear_runtime_config()

static func submit_score(player_alias: String, score: float, leaderboard: String = "", timeout_seconds: float = 8.0, owner: Node = null) -> Dictionary:
	return await _client.submit_score(player_alias, score, leaderboard, timeout_seconds, owner)

static func get_top_scores(limit: int = 10, leaderboard: String = "", timeout_seconds: float = 8.0, owner: Node = null) -> Dictionary:
	return await _client.get_top_scores(limit, leaderboard, timeout_seconds, owner)

static func get_player_rank_and_surrounding(player_alias: String, surrounding: int = 5, with_meta: bool = false, leaderboard: String = "", timeout_seconds: float = 8.0, owner: Node = null) -> Dictionary:
	return await _client.get_player_rank_and_surrounding(player_alias, surrounding, with_meta, leaderboard, timeout_seconds, owner)

static func get_top_scores_and_player_rank(player_alias: String, top_limit: int = 10, surrounding: int = 5, with_meta: bool = false, leaderboard: String = "", timeout_seconds: float = 8.0, owner: Node = null) -> Dictionary:
	return await _client.get_top_scores_and_player_rank(player_alias, top_limit, surrounding, with_meta, leaderboard, timeout_seconds, owner)
