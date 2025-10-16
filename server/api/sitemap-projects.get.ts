// server/api/sitemap-projects.get.ts
type Url = { loc: string; lastmod?: string; changefreq?: string; priority?: number }
type Project = { slug?: string; projectId?: string; updatedAt?: string }

const sleep = (ms: number) => new Promise(r => setTimeout(r, ms))

export default cachedEventHandler(async () => {
  const { public: { NUXT_PUBLIC_API_BASE } } = useRuntimeConfig()

  const urls: Url[] = []
  const SEEN = new Set<string>()
  const PAGE_SIZE = 200
  let page = 1

  async function fetchPublicProjects(p: number, attempt = 1): Promise<Project[]> {
    try {
      const data = await $fetch<{ items?: Project[] }>(
        `${NUXT_PUBLIC_API_BASE}/public/projects`,
        { query: { page: p, pageSize: PAGE_SIZE }, timeout: 15_000, retry: 0 }
      )
      return data?.items ?? []
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
    } catch (err) {
      if (attempt < 3) {
        await sleep(200 * attempt)
        return fetchPublicProjects(p, attempt + 1)
      }
      return []
    }
  }

  // Page until empty (or short page)
  // Keep a hard cap just in case (≈50k urls)
  for (let guard = 0; guard < 250; guard++) {
    const items = await fetchPublicProjects(page)
    if (!items.length) break

    for (const p of items) {
      const id = p.slug || p.projectId
      if (!id || SEEN.has(id)) continue
      SEEN.add(id)

      urls.push({
        // ⬇️ change the path to match YOUR dynamic route
        loc: `/public/projects/preview/${encodeURIComponent(id)}`,
        lastmod: p.updatedAt,
        changefreq: 'weekly',
        priority: 0.6,
      })
    }

    if (items.length < PAGE_SIZE) break
    page += 1
    await sleep(25) // be nice to the remote API
  }

  return urls
}, {
  maxAge: 60 * 10, // cache for 10 minutes
})
