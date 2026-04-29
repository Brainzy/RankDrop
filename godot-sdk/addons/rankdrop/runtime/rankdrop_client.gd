extends RefCounted
class_name RankDropClient

const URL_PREFIX := "/api/v1/leaderboards/"
const DEFAULT_LEADERBOARD := "global-high-scores"

var _runtime_server_url := ""
var _runtime_game_secret := ""
var _runtime_default_leaderboard := ""

func configure(server_url: String, game_secret: String, default_leaderboard: String = DEFAULT_LEADERBOARD) -> void:
	_runtime_server_url = server_url.strip_edges()
	_runtime_game_secret = game_secret.strip_edges()
	var next_leaderboard := default_leaderboard.strip_edges()
	if next_leaderboard.is_empty():
		next_leaderboard = DEFAULT_LEADERBOARD
	_runtime_default_leaderboard = next_leaderboard

func clear_runtime_config() -> void:
	_runtime_server_url = ""
	_runtime_game_secret = ""
	_runtime_default_leaderboard = ""

func submit_score(player_alias: String, score: float, leaderboard: String = "", timeout_seconds: float = 8.0, owner: Node = null) -> Dictionary:
	var payload := {
		"playerAlias": player_alias,
		"scoreValue": score
	}
	var url := "%s%s/scores" % [_base_url(), _slug(leaderboard)]
	return await _request_json(owner, HTTPClient.METHOD_POST, url, payload, timeout_seconds)

func get_top_scores(limit: int = 10, leaderboard: String = "", timeout_seconds: float = 8.0, owner: Node = null) -> Dictionary:
	var url := "%s%s/top?limit=%s" % [_base_url(), _slug(leaderboard), limit]
	return await _request_json(owner, HTTPClient.METHOD_GET, url, null, timeout_seconds)

func get_player_rank_and_surrounding(player_alias: String, surrounding: int = 5, with_meta: bool = false, leaderboard: String = "", timeout_seconds: float = 8.0, owner: Node = null) -> Dictionary:
	var path := "%s%s/players/%s" % [_base_url(), _slug(leaderboard), player_alias.uri_encode()]
	if with_meta:
		path += "/metadata"
	var url := "%s?surrounding=%s" % [path, surrounding]
	return await _request_json(owner, HTTPClient.METHOD_GET, url, null, timeout_seconds)

func get_top_scores_and_player_rank(player_alias: String, top_limit: int = 10, surrounding: int = 5, with_meta: bool = false, leaderboard: String = "", timeout_seconds: float = 8.0, owner: Node = null) -> Dictionary:
	var path := "%s%s/combined" % [_base_url(), _slug(leaderboard)]
	if with_meta:
		path += "/metadata"
	var url := "%s?playerAlias=%s&topLimit=%s&surrounding=%s" % [path, player_alias.uri_encode(), top_limit, surrounding]
	return await _request_json(owner, HTTPClient.METHOD_GET, url, null, timeout_seconds)

func _base_url() -> String:
	return _trim_trailing_slash(_get_server_url()) + URL_PREFIX

func _slug(leaderboard: String) -> String:
	if leaderboard.strip_edges().is_empty():
		return _get_default_leaderboard()
	return leaderboard

func _request_json(owner: Node, method: HTTPClient.Method, url: String, body: Variant, timeout_seconds: float) -> Dictionary:
	var target_owner := _resolve_owner(owner)
	if target_owner == null:
		return {"ok": false, "error": "Could not resolve request owner node."}

	var game_secret := _get_game_secret().strip_edges()
	if game_secret.is_empty():
		return {"ok": false, "error": "Game secret is empty."}
	if _get_server_url().strip_edges().is_empty():
		return {"ok": false, "error": "Server URL is empty."}

	var request := HTTPRequest.new()
	request.timeout = timeout_seconds
	target_owner.add_child(request)

	var headers := PackedStringArray([
		"X-Game-Key: %s" % game_secret,
		"Content-Type: application/json"
	])

	var body_text := ""
	if body != null:
		body_text = JSON.stringify(body)

	var err := request.request(url, headers, method, body_text)
	if err != OK:
		request.queue_free()
		return {"ok": false, "error": "Request failed to start (%s)" % err}

	var data: Array = await request.request_completed
	request.queue_free()

	var result: int = data[0]
	var response_code: int = data[1]
	var response_body: String = data[3].get_string_from_utf8()

	if result == HTTPRequest.RESULT_TIMEOUT:
		return {"ok": false, "error": "Request timed out."}
	if result != HTTPRequest.RESULT_SUCCESS:
		return {"ok": false, "error": "Network error (%s)" % result, "status": response_code}

	if response_code >= 400:
		return {"ok": false, "error": "RankDrop [%s]: %s" % [response_code, response_body], "status": response_code}

	var parsed: Variant = JSON.parse_string(response_body)
	if parsed == null:
		return {"ok": true, "data": {}, "status": response_code}
	return {"ok": true, "data": parsed, "status": response_code}

func _resolve_owner(owner: Node) -> Node:
	if owner != null:
		return owner
	var main_loop := Engine.get_main_loop()
	if main_loop is SceneTree:
		return (main_loop as SceneTree).root
	return null

func _get_server_url() -> String:
	if not _runtime_server_url.is_empty():
		return _runtime_server_url
	return ""

func _get_default_leaderboard() -> String:
	if not _runtime_default_leaderboard.is_empty():
		return _runtime_default_leaderboard
	return DEFAULT_LEADERBOARD

func _get_game_secret() -> String:
	if not _runtime_game_secret.is_empty():
		return _runtime_game_secret
	return ""

func _trim_trailing_slash(value: String) -> String:
	if value.ends_with("/"):
		return value.substr(0, value.length() - 1)
	return value
