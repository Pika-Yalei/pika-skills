---
name: edge-tts-voiceover
description: 使用 Edge 在线自然语音生成中文配音、男女声试听、MP3 和同步 SRT 字幕。用户要求免费中文配音、朗读讲稿、云希/晓晓音色，或给视频更换旁白时使用；适合与 Remotion 等视频流程配合。
---

# Edge 中文配音

通过 Python `edge-tts` 调用 Microsoft Edge 在线语音服务，生成真实可播放的 MP3 和服务返回时间信息对应的 SRT。无需 Microsoft 账号或 API Key；需要联网。这是第三方客户端接入在线服务，不承诺永久免费、无限调用、稳定 SLA 或音频商用授权。必须离线或文稿不得出机时，改选本地方案，不使用本技能发送文稿。

## 输入与声音选择

- 优先沿用用户提供的讲稿、音色、语速和输出位置。保持原文，不擅自改写已确定的旁白。
- 未指定声音时，中文默认 `xiaoxiao`；用户要求男声时使用 `yunxi`。这两种声音可用相同的一两句话生成试听，便于比较。
- 科普试听可从语速 `-5%` 开始；这只是建议，尊重用户指定的语速。用标点组织自然停顿，不为制造停顿随意增加旁白文字。

| 别名 | Voice ID | 选声参考 |
| --- | --- | --- |
| `xiaoxiao` | `zh-CN-XiaoxiaoNeural` | 女声，温和讲解 |
| `yunxi` | `zh-CN-YunxiNeural` | 男声，明快讲解 |
| `yunyang` | `zh-CN-YunyangNeural` | 男声，新闻播报 |

声音是否合适以实际试听为准。需要其他语言、口音或当前可用音色时，先查询声音列表，不凭旧列表声称音色仍可用。

## 运行

在技能目录或其脚本的绝对路径上执行。优先使用任务已有的 Python 环境；依赖缺失时，在任务虚拟环境中安装，避免改动无关环境。

```sh
python -m pip install -r requirements.txt
python scripts/voiceover.py voices --locale zh-CN
python scripts/voiceover.py synthesize --text "月亮为什么时圆时缺？" --voice yunxi --rate=-5% --output outputs/voice.mp3
```

长文稿使用 UTF-8 文本文件，避免命令行引号和长度问题：

```sh
python scripts/voiceover.py synthesize --file work/narration.txt --voice xiaoxiao --rate=-5% --output outputs/narration.mp3
```

还可传入完整 Voice ID、`--volume=+0%`、`--pitch=+0Hz`。负值参数建议用等号形式，例如 `--rate=-5%`。脚本默认拒绝覆盖同名 MP3 或 SRT；确需替换本次任务产物时再用 `--overwrite`。

## 输出与检查

脚本同时输出 `name.mp3` 和 `name.srt`，使用同一次流式合成返回的 **SentenceBoundary** 生成句级字幕，不估算时间。服务返回的相邻句边界可能有轻微重叠；脚本保留原始起点，仅把上一句结束时间裁到下一句开始，避免字幕同时叠出。句级时间信息不能直接充当逐字高亮的时间轴。不要把 MP3 改后缀冒充 WAV；其他音频格式需真正转码。

检查文件非空、音频可解码、字幕包含实际旁白。宿主支持试听时播放样例，重点检查数字、英文和多音字；无法听取声音时如实说明，不声称已听审。交付实际生成的音频和字幕链接，说明声音及语速。脚本不会自动提交、发布或上传文稿和产物到仓库。

声音列表或合成失败时报告失败。明显的临时连接问题最多重试一次；不要无限重试，不悄悄换音色或切换付费配音服务。网络生成完成前仅写临时文件，失败时不把不完整文件当成成品。

## 与 Remotion 或剪辑流程衔接

只有用户要求处理视频时才进入这一步；本技能本身负责声音和字幕。

1. 先合成最终旁白，用 `ffprobe`、媒体工具或播放器测量实际音频时长。
2. 按音频和 SRT 的真实时间调整分镜。脚本打印的最后字幕结束时间不是 MP3 容器总时长。
3. 复用成功生成的音频，再预览和导出视频。不要反复合成同一段旁白，也不要默认拉伸声音去凑固定时长。

参考项目：[rany2/edge-tts](https://github.com/rany2/edge-tts)。
