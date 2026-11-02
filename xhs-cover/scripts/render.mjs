#!/usr/bin/env node
import { createRequire } from 'node:module';
import { readFile, writeFile, mkdir, copyFile } from 'node:fs/promises';
import { resolve, dirname, join } from 'node:path';
import { fileURLToPath, pathToFileURL } from 'node:url';
import { homedir } from 'node:os';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const [input, output] = process.argv.slice(2);

if (!input || !output) {
  console.error('Usage: node scripts/render.mjs <copy.json> <output-directory>');
  process.exit(1);
}

const config = JSON.parse((await readFile(resolve(input),'utf8')).replace(/^\uFEFF/,''));

if (typeof config.text !== 'string' || !['green','orange','blue'].includes(config.theme ?? 'green') || !['lines','quote'].includes(config.style ?? 'lines')) {
  throw new Error('Expected {"style":"lines|quote", "theme":"green|orange|blue", "text":"line 1\\n**line 2**"}');
}

let playwright;
const candidates = [join(root,'package.json'), join(process.env.CODEX_HOME || join(homedir(),'.codex'),'skills','social-card-skill','package.json')];

for (const candidate of candidates) {
  try {
    playwright = createRequire(candidate)('playwright');
    break;
  } catch (error) {
    if (error.code !== 'MODULE_NOT_FOUND') {
      throw error;
    }
  }
}

if (!playwright) {
  throw new Error('Install renderer dependencies in this skill: npm install && npx playwright install chromium');
}

const destination = resolve(output);
await mkdir(destination,{recursive:true});
const serialized = JSON.stringify({style:config.style ?? 'lines',theme:config.theme ?? 'green',text:config.text}).replace(/</g,'\\u003c');
const template = await readFile(join(root,'assets','cover.html'),'utf8');
const html = template.replace(/(<script id="cover-data" type="application\/json">)[\s\S]*?(<\/script>)/,(_,start,end) => start + '\n' + serialized + '\n' + end);
const htmlPath = join(destination,'cover.html');
await writeFile(htmlPath,html);
await copyFile(join(root,'assets','cover-serif.woff2'),join(destination,'cover-serif.woff2'));
await copyFile(join(root,'assets','OFL.txt'),join(destination,'OFL.txt'));
const browser = await playwright.chromium.launch({headless:true});

try {
  const page = await browser.newPage({viewport:{width:1080,height:1440},deviceScaleFactor:1});
  await page.goto(pathToFileURL(htmlPath).href + '?render=1');
  await page.waitForFunction(() => window.coverState?.ready || window.coverState?.error,{},{timeout:30000});
  const state = await page.evaluate(() => window.coverState);

  if (state.error) {
    throw new Error(state.error);
  }

  if (!state.valid) {
    throw new Error('Text does not fit. Add line breaks or shorten it. No cropped PNG was generated.');
  }

  await page.locator('#cover').screenshot({path:join(destination,'cover.png')});
  console.log(JSON.stringify({html:htmlPath,png:join(destination,'cover.png'),width:1080,height:1440,...state},null,2));
} finally {
  await browser.close();
}
