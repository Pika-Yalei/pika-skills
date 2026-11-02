# Reference and visual decisions

Source: https://www.xiaohongshu.com/user/profile/65b5d272000000000d01c6e5

Observed in the user's browser on 2026-11-02: 亚雷时光机. Four visible covers: Codex 重置卡已到账; Codex 即将重置额度，还会送重置卡; ChatGPT Pro 双倍额度回来了; OpenAI 开始送 API 额度了.

- Portrait 3:4, flat pale background, no photo, badges, footer, texture, or logo.
- Large bold Songti/serif, flush left; inline colored keywords. The original font cannot be identified from pixels; bundled Noto Serif SC is a close open-source substitute.
- Two thin rules span the text width. Large empty areas above and below are intentional.
- At 1080 × 1440, approximate left/right inset 112; content width 856; rule 3; title 120 with 1.4 line height. Short titles start around y=360; four/five lines around y=300. Frame padding 60.
- Green: background #f6f7ef, ink #203e32, accent #438b75, rule #647f70.
- Orange: background #fff8ef, ink #191b17, accent #cf6648, rule #c8684c.
- Blue: background #f1f7fc, ink #191d1b, accent #288db9, rule #378eaf.

These are visual approximations, not recovered original CSS or exact design tokens. The original headlines are historical examples, not verified current product claims.

Font source: https://github.com/google/fonts/tree/main/ofl/notoserifsc . Noto Serif SC variable font; SIL Open Font License retained beside the font. Bundled WOFF2 retains available Unicode repertoire and weights 200–900.

## Quote variant

Added at the user's request as a second style, not observed on the source profile. `style: "quote"` replaces the two rules with oversized accent-colored curly quotation marks: opening at upper left and closing at lower right. Same flat backgrounds, 700-weight serif, keyword accents, and left-aligned title. The composition moves up and type reduces slightly for longer quotes, preserving space around both marks.
