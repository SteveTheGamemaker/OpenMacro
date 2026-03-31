#!/usr/bin/env python3
"""Helper to call local Qwen model. Usage: python scripts/qwen.py <prompt_file> [max_tokens]"""

import sys, json, urllib.request

ENDPOINT = "http://192.168.0.150:8085/v1/chat/completions"
MODEL = "Qwen3.5-27B-Q3_K_M.gguf"

def call_qwen(system: str, user: str, max_tokens: int = 32000, temperature: float = 0.3) -> dict:
    payload = json.dumps({
        "model": MODEL,
        "messages": [
            {"role": "system", "content": system},
            {"role": "user", "content": user},
        ],
        "temperature": temperature,
        "max_tokens": max_tokens,
    }).encode()

    req = urllib.request.Request(ENDPOINT, data=payload, headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req, timeout=600) as resp:
        r = json.loads(resp.read())

    msg = r["choices"][0]["message"]
    content = msg.get("content") or ""
    t = r.get("timings", {})
    return {
        "content": content,
        "tokens_in": t.get("prompt_n", 0),
        "tokens_out": t.get("predicted_n", 0),
        "speed": t.get("predicted_per_second", 0),
        "finish": r["choices"][0].get("finish_reason", "unknown"),
    }

if __name__ == "__main__":
    prompt_file = sys.argv[1] if len(sys.argv) > 1 else None
    max_tokens = int(sys.argv[2]) if len(sys.argv) > 2 else 32000

    if prompt_file:
        with open(prompt_file) as f:
            data = json.load(f)
        result = call_qwen(data.get("system", ""), data["user"], max_tokens)
    else:
        user_input = sys.stdin.read()
        result = call_qwen(
            "You are a Kotlin Android developer. Write only code, no explanations. Follow the exact patterns shown in examples.",
            user_input,
            max_tokens,
        )

    print(result["content"])
    print(f"\n--- {result['tokens_in']} in / {result['tokens_out']} out | {result['speed']:.1f} tok/s | {result['finish']}", file=sys.stderr)
