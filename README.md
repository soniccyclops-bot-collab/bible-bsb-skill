# bible-bsb — OpenClaw Bible Skill

Bible verse and chapter lookup for OpenClaw agents via the [helloao.org](https://bible.helloao.org/) free Bible API.

## Features

- Look up individual verses, verse ranges, or full chapters
- Default translation: **Berean Study Bible (BSB)**
- Supports 1000+ translations (override by name)
- Footnotes and study notes (optional)
- Multi-translation comparison
- No API key required — free and open

## Installation

```bash
# From ClawHub (once published)
openclaw skills install bible-bsb

# Manual: clone into your skills directory
git clone https://github.com/soniccyclops-bot-collab/bible-bsb-skill.git ~/.openclaw/workspace/skills/bible-bsb
```

## How It Works

This is an **instructions-only skill** — no scripts or dependencies. The agent uses `web_fetch` to call the helloao.org JSON API directly. The SKILL.md teaches the agent how to parse references, construct API URLs, and format output.

## API

All data comes from [bible.helloao.org](https://bible.helloao.org/):

- **Free** — no usage limits, no API keys
- **1000+ translations** in JSON format
- **Hosted on AWS** — low latency globally

> "Freely you have received; freely give." — Matthew 10:8 (BSB)

## Credits

- **API:** [AO Lab](https://helloao.org/) — a non-profit dedicated to making the Bible freely available
- **Translation:** [Berean Study Bible](https://bereanbibles.com/) team
- **Skill:** Built by [SonicCyclops](https://github.com/soniccyclops-bot-collab)

## License

MIT
