// server/api/thumbnail/[id].get.ts
import { chromium, type Browser } from 'playwright'
import { defineEventHandler, getRouterParam, setHeader, setResponseStatus } from 'h3'

export default defineEventHandler(async (event) => {
  const id = getRouterParam(event, 'id')
  if (!id) {
    setResponseStatus(event, 400)
    return { error: 'Missing id' }
  }

  const config = useRuntimeConfig(event)

  // 1) ดึง HTML ของโปรเจกต์จาก Backend (ปรับ field ให้ตรงของจริง)
  const html = await getProjectHtml(id, config)

  // เสริม <base> ป้องกันลิงก์/asset ภายในหลุดออกนอก และบีบ animation
  const safeHtml = `<!doctype html><html>
<head>
<meta charset="utf-8" />
<base href="/" target="_blank">
<style>
  /* ลด animation/wow factor ให้สกรีนช็อตนิ่งและเร็ว */
  * { animation: none !important; transition: none !important; }
  @media (prefers-reduced-motion: no-preference) { * { animation: none !important; } }
  html, body { margin: 0; padding: 0; background: white; }
</style>
</head>
<body>${html ?? ''}</body></html>`

  let browser: Browser | null = null
  try {
    browser = await chromium.launch({
      args: ['--no-sandbox', '--disable-dev-shm-usage'],
      // headless: 'new'  // ถ้าต้องการระบุ
    })
    const page = await browser.newPage({
      viewport: { width: 1280, height: 800}
    })

    // รอแค่พอเหมาะ อย่าให้ค้างนาน
    await page.setContent(safeHtml, { waitUntil: 'load', timeout: 10_000 })
    // ถ้าเพจมี Lazy asset อาจรอเพิ่มนิดหน่อย
    await page.waitForTimeout(300)

    const buf = await page.screenshot({ type: 'png' }) // เล็ก เร็ว
    setHeader(event, 'Content-Type', 'image/png')
    setHeader(event, 'Cache-Control', 'public, max-age=31536000, immutable')
    return buf
  } catch (err) {
    console.error('[thumbnail] fail', err)
    setResponseStatus(event, 502)
    return { error: 'Thumbnail generation failed' }
  } finally {
    await browser?.close().catch(() => {})
  }
})

async function getProjectHtml(projectId: string, config: { public: { NUXT_PUBLIC_API_BASE: string } }) {
    try{
  const apiBase = config.public.NUXT_PUBLIC_API_BASE
  const data = await $fetch<{ mainHtmlContent: string }>(`${apiBase}/public/projects/${projectId}`)
  return data?.mainHtmlContent ?? '<div/>'
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (_) {
        return '<div/>';
    }
}
