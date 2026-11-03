#!/usr/bin/env python3
"""Generate Edge voiceovers and sentence-aligned SRT without an API key."""

import argparse
import asyncio
import json
import os
from pathlib import Path
import sys
from tempfile import TemporaryDirectory

import aiohttp
import edge_tts


VOICES = {
    "xiaoxiao": "zh-CN-XiaoxiaoNeural",
    "yunxi": "zh-CN-YunxiNeural",
    "yunyang": "zh-CN-YunyangNeural",
}


def parse_args():
    parser = argparse.ArgumentParser(description=__doc__)
    commands = parser.add_subparsers(dest="command", required=True)
    voices = commands.add_parser("voices", help="List currently available voices")
    voices.add_argument("--locale", default="zh-CN", help="Locale prefix, e.g. zh or en-US")
    speech = commands.add_parser("synthesize", help="Create MP3 and matching SRT")
    source = speech.add_mutually_exclusive_group(required=True)
    source.add_argument("--text", help="Exact text to speak")
    source.add_argument("--file", type=Path, help="UTF-8 plain-text file")
    speech.add_argument("--voice", default="xiaoxiao", help="Alias or full Edge Voice ID")
    speech.add_argument("--rate", default="-5%")
    speech.add_argument("--volume", default="+0%")
    speech.add_argument("--pitch", default="+0Hz")
    speech.add_argument("--output", required=True, type=Path, help="Destination .mp3 file")
    speech.add_argument("--overwrite", action="store_true")
    return parser, parser.parse_args()


async def list_voices(locale):
    voices = await asyncio.wait_for(edge_tts.list_voices(), timeout=30)
    matches = [
        {
            "voice": voice["ShortName"],
            "gender": voice["Gender"],
            "locale": voice["Locale"],
            "tags": voice.get("VoiceTag", {}),
        }
        for voice in voices
        if voice["Locale"].lower().startswith(locale.lower())
    ]
    print(json.dumps(matches, ensure_ascii=True, indent=2))


async def synthesize(args):
    text = args.file.read_text(encoding="utf-8-sig") if args.file else args.text
    if not text.strip():
        raise ValueError("The narration text is empty")

    audio = args.output.expanduser().resolve()
    if audio.suffix.lower() != ".mp3":
        raise ValueError("--output must end in .mp3; transcode separately for other formats")

    subtitles = audio.with_suffix(".srt")
    for path in (audio, subtitles):
        if path.exists() and not args.overwrite:
            raise FileExistsError(f"Output already exists: {path}; use --overwrite to replace")

    voice = VOICES.get(args.voice, args.voice)
    speech = edge_tts.Communicate(
        text,
        voice=voice,
        rate=args.rate,
        volume=args.volume,
        pitch=args.pitch,
        boundary="SentenceBoundary",
        connect_timeout=10,
        receive_timeout=30,
    )
    audio.parent.mkdir(parents=True, exist_ok=True)
    timing = edge_tts.SubMaker()
    with TemporaryDirectory(prefix=".edge-tts-", dir=audio.parent) as directory:
        temporary_audio = Path(directory) / "voice.mp3"
        temporary_srt = Path(directory) / "voice.srt"
        with temporary_audio.open("wb") as stream:
            async for chunk in speech.stream():
                if chunk["type"] == "audio":
                    stream.write(chunk["data"])
                elif chunk["type"] == "SentenceBoundary":
                    timing.feed(chunk)

        if not temporary_audio.stat().st_size or not timing.cues:
            raise ValueError("The service returned no complete audio or sentence timings")

        for current, following in zip(timing.cues, timing.cues[1:]):
            if following.start <= current.start:
                raise ValueError("The service returned non-increasing sentence start times")

            current.end = min(current.end, following.start)

        temporary_srt.write_text(timing.get_srt(), encoding="utf-8")
        for path in (audio, subtitles):
            if path.exists() and not args.overwrite:
                raise FileExistsError(f"Output appeared during generation: {path}")

        os.replace(temporary_audio, audio)
        os.replace(temporary_srt, subtitles)

    print(json.dumps({
        "audio": str(audio),
        "subtitles": str(subtitles),
        "voice": voice,
        "rate": args.rate,
        "boundary": "SentenceBoundary",
        "cue_count": len(timing.cues),
        "last_cue_end_seconds": max(cue.end.total_seconds() for cue in timing.cues),
    }, ensure_ascii=True, indent=2))


def main():
    parser, args = parse_args()
    try:
        if args.command == "voices":
            asyncio.run(list_voices(args.locale))
        else:
            asyncio.run(synthesize(args))
    except (OSError, ValueError, asyncio.TimeoutError, aiohttp.ClientError,
            edge_tts.exceptions.EdgeTTSException) as error:
        parser.exit(1, f"Voiceover failed ({type(error).__name__}): {error}\n")


if __name__ == "__main__":
    sys.exit(main())
