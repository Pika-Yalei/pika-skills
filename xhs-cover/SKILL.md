---
name: xhs-cover
description: "按亚雷时光机的小红书风格制作 3:4 纯文字封面，支持双细线与大双引号两种排版、浅色底、宋体和彩色关键词。用户要求用我的封面风格、亚雷封面、双引号封面或只改文字时使用；输出可编辑 HTML/CSS 和 PNG。"
---

# 亚雷小红书封面

复用 `assets/cover.html`，不要每次重新设计。用户已选择的个人风格优先于通用 social-card 的纹理、内容密度、配图或 Swiss 规则。风格依据与配色见 [references/style.md](references/style.md)。

## 输入与排版

- 接受标题文字；通常拆成 2–4 行，最多 5 行。按语义换行，不拆英文产品名、数字与单位。保留给定文字；需要缩写时先给出具体建议，不悄悄删改。
- `**文字**` 表示强调色。默认突出结论、变化或关键数字；若用户给了强调范围，遵守它。
- 默认 `green`；用户要求橙色／ChatGPT 参考则用 `orange`，蓝色／OpenAI 参考则用 `blue`。产品与颜色仅为参考习惯，不是强制映射。
- 固定 1080 × 1440。保留宋体、左对齐、大面积留白。除非用户要求，不加角标、署名、图片、纹理或额外信息。
- `style` 默认 `lines`（原有双细线）；用户指定双引号、引语、金句风格时用 `quote`：去掉横线，左上开引号 `“`、右下闭引号 `”`，引号使用强调色。两种风格都支持三个配色。引号由模板生成，文案无需手动再加引号；若用户只用一对外层引号表示引语，移至装饰引号，保留正文中的引号。
- 只替换模板中 `script#cover-data` 的 JSON：`{"style":"quote","theme":"green","text":"把时间留给\n**真正重要的事**"}`。缺少 style 的旧输入仍使用双细线。HTML/CSS 默认保持不动。JSON 中的 `<` 序列化为 `\u003c`，防止结束 script。以 textContent 写入文字，不解释用户 HTML。

## 生成

将输入 JSON 写到当前任务 work/，产物放 outputs/，不要写进 skill 目录。运行：

```text
node <skill-root>/scripts/render.mjs <absolute-copy.json> <absolute-output-directory>
```

输出 `cover.html`、`cover-serif.woff2`、字体许可 `OFL.txt` 和 `cover.png`。渲染依赖 Playwright，优先使用本 skill 的依赖，其次复用现有 social-card-skill 安装；缺失时在本 skill 目录执行 `npm install` 和 `npx playwright install chromium`。字体已打包，出图无需联网。交付字体须保留其许可。

用户可双击 HTML，修改文字、切风格和配色，直接下载 PNG。实时预览；“保存当前 HTML”保留文案、风格和配色，保存后仍需与字体放在同一目录。浏览器 PNG 下载使用 Canvas，自动化渲染直接截图 HTML/CSS。

## 验证与交付

等待 `window.coverState.ready`，确认 `valid`，导出 1080 × 1440。模板仅在必要时从 120px 缩至 84px；仍放不下就中止出图，建议语义换行或缩短，不能隐藏裁切。

检查实际 PNG 的中文字体、强调色、横线或引号和边缘。引号不能压住标题。先展示图片，附可编辑 HTML；不要为通用密度检查填满留白。展示本地图片使用绝对路径。

此 skill 只制作封面，不发布小红书，不把参考标题当成已核实的新消息。只给标题时无需访问原主页；仅在用户要求重新比对风格时浏览来源。
