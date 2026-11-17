import { readdir, readFile, stat } from 'node:fs/promises'
import { extname, join, relative, resolve, sep } from 'node:path'
import { fileURLToPath } from 'node:url'

const output = fileURLToPath(new URL('../docs/.vitepress/dist/', import.meta.url))
// The existing documentation is published under this path; preserve that contract.
const base = '/mcn/'
const origin = 'https://mcn.invalid'
const errors = []
const pages = new Map()
const assets = new Set()
let linkCount = 0

function decodeAttribute(value) {
  return value.replace(/&(?:amp|quot|apos|lt|gt|#39);/g, entity => ({
    '&amp;': '&', '&quot;': '"', '&apos;': "'", '&lt;': '<', '&gt;': '>', '&#39;': "'",
  })[entity])
}

async function walk(directory) {
  const entries = await readdir(directory, { withFileTypes: true })
  const files = await Promise.all(entries.map(entry => entry.isDirectory()
    ? walk(join(directory, entry.name)) : [join(directory, entry.name)]))
  return files.flat()
}

async function locate(pathname) {
  const file = resolve(output, decodeURIComponent(pathname.slice(base.length)))
  if (file !== resolve(output) && !file.startsWith(resolve(output) + sep)) return null
  const candidates = pathname.endsWith('/') ? [join(file, 'index.html')]
    : extname(file) ? [file] : [`${file}.html`, join(file, 'index.html')]
  for (const candidate of candidates) {
    try {
      if ((await stat(candidate)).isFile()) return candidate
    } catch (error) {
      if (error.code !== 'ENOENT' && error.code !== 'ENOTDIR') throw error
    }
  }
  return null
}

async function checkReference(value, currentFile, isLink) {
  if (!value || /^(?:data:|mailto:|tel:|javascript:)/i.test(value)) return
  const url = new URL(decodeAttribute(value), `${origin}${base}${relative(output, currentFile).split(sep).join('/')}`)
  if (url.origin !== origin) return
  const source = relative(output, currentFile)
  if (!url.pathname.startsWith(base)) {
    errors.push(`${source}: reference escapes ${base}: ${value}`)
    return
  }
  const target = await locate(url.pathname)
  if (!target) {
    errors.push(`${source}: missing target: ${value}`)
    return
  }
  if (isLink) linkCount++
  else assets.add(target)
  if (isLink && url.hash && extname(target) === '.html') {
    const id = decodeURIComponent(url.hash.slice(1))
    if (!pages.get(target)?.ids.has(id)) errors.push(`${source}: missing anchor: ${value}`)
  }
}

const files = await walk(output)
for (const file of files.filter(file => extname(file) === '.html')) {
  const html = await readFile(file, 'utf8')
  const ids = new Set([...html.matchAll(/\bid="([^"]+)"/g)].map(match => decodeAttribute(match[1])))
  pages.set(file, { html, ids })
  // VitePress emits 404.html as a client-rendered fallback shell.
  if (relative(output, file) !== '404.html' && !/<h1\b/.test(html)) {
    errors.push(`${relative(output, file)}: missing pre-rendered main heading`)
  }
  if (!/<title>[^<]*MCN Boot/.test(html)) errors.push(`${relative(output, file)}: missing product title`)
}

for (const [file, { html }] of pages) {
  for (const [tag, tagName] of html.matchAll(/<(a|img|script|link)\b[^>]*>/g)) {
    const attribute = tagName === 'img' || tagName === 'script' ? 'src' : 'href'
    const value = tag.match(new RegExp(`\\b${attribute}="([^"]*)"`))?.[1]
    if (value !== undefined) await checkReference(value, file, tagName === 'a')
  }
}

if (errors.length) {
  console.error(`Documentation verification failed (${errors.length} issues):\n${errors.join('\n')}`)
  process.exitCode = 1
} else {
  console.log(`Verified ${pages.size} HTML files, ${linkCount} internal links and ${assets.size} assets under ${base}.`)
}
