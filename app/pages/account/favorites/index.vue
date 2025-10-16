<!-- pages/account/favorites.vue -->
<template>
  <main class="mx-auto max-w-[1400px] min-h-screen text-white px-4 lg:px-8 py-8">
    <header class="mb-6">
      <h2 class="text-3xl md:text-4xl font-bold">Your Favorite Projects</h2>
      <p class="text-white/70 mt-1">Projects you’ve saved. Search, sort and manage them here.</p>
    </header>

    <!-- Controls -->
    <section class="mb-6 rounded-xl border border-white/10 bg-[#0D1117] p-4 lg:p-6">
      <div class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
        <!-- Search -->
        <div class="relative w-full md:max-w-md">
          <UIcon name="i-lucide-search" class="absolute left-3 top-1/2 -translate-y-1/2 size-5 text-white/70" />
          <input
            v-model="q"
            type="search"
            placeholder="Search favorites…"
            class="w-full rounded-xl bg-white/10 border border-white/10 pl-10 pr-10 py-2.5 placeholder:text-white/50 text-white focus:outline-none focus:ring-2 focus:ring-indigo-400/50"
          >
          <button
            v-if="q"
            class="absolute right-2 top-1/2 -translate-y-1/2 text-white/70 hover:text-white"
            aria-label="Clear search"
            @click="q = ''"
          >
            <UIcon name="i-lucide-x" class="size-5" />
          </button>
        </div>

        <!-- Sort -->
        <div class="relative">
          <select
            v-model="sort"
            class="appearance-none rounded-xl bg-white/10 border border-white/10 py-2.5 pl-3 pr-9 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400/50"
            title="Sort favorites"
          >
            <option value="newestFav">Recently Favorited</option>
            <option value="newest">Newest Project</option>
            <option value="popular">Most Liked</option>
            <option value="name">Name (A–Z)</option>
          </select>
          <UIcon name="i-lucide-chevron-down" class="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 size-4 opacity-70" />
        </div>
      </div>
    </section>

    <!-- Grid -->
    <section class="">
      <div
        class="grid gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4"
        :aria-busy="loading ? 'true' : 'false'"
      >
        <!-- Skeletons -->
        <template v-if="loading">
          <div v-for="i in 8" :key="'sk-'+i" class="rounded-2xl overflow-hidden border border-white/10 bg-white/5">
            <div class="h-[200px] animate-pulse bg-gradient-to-b from-white/10 to-white/5" />
            <div class="p-4 space-y-3">
              <div class="h-4 w-2/3 bg-white/10 rounded animate-pulse" />
              <div class="flex items-center gap-2">
                <div class="w-8 h-8 rounded-full bg-white/10 animate-pulse" />
                <div class="h-3 w-24 bg-white/10 rounded animate-pulse" />
              </div>
            </div>
          </div>
        </template>

        <!-- Items -->
        <template v-else>
             <div
              v-for="project in items"
              :key="project.projectId"
              class="group relative overflow-hidden backdrop-blur-sm transition"
            >
              <!-- Cover -->
              <div
                class="group relative w-full rounded-2xl overflow-hidden border border-slate-50/10 h-[250px]"
              >
                <div class="absolute inset-0 z-[2] transition duration-300 group-hover:bg-black/70 pointer-events-none"/>

                <div class="absolute inset-0 grid place-items-center">
          <iframe
                    :srcdoc="project.mainHtmlContent"
                    title="preview"
                    class="rounded-md shadow bg-white pointer-events-none min-w-[320dvw] md:min-w-[120dvw] lg:min-w-auto min-h-[150dvh]"
                    :style="{
                      width: DESKTOP_W + 'px',
                      height: DESKTOP_H + 'px',
                      transform: `scale(${getScale(project.projectId)})`,
                      transformOrigin: 'top left'
                    }"
                    sandbox="allow-scripts allow-same-origin" 
                    referrerpolicy="no-referrer"
                    @load="lockIframe"
                  />
                </div>

                <!-- floating actions -->
                <div class="absolute inset-0 gap-2 z-[20] pointer-events-none">
                  <div class="flex justify-center items-center gap-2 h-full opacity-0 group-hover:opacity-100 transition duration-300 pointer-events-auto">
                    <button
                      class="rounded-lg p-3 text-white/90 cursor-pointer bg-white/10 hover:bg-white/20"
                      @click="openPreview(project)"
                    >
                      Preview
                    </button>

                    <button
                      v-if="loadingFavorite"
                      class="rounded-lg pt-2 pb-1 px-2 text-white/90 cursor-default bg-white/10 hover:bg-white/20"
                    >
                      <UIcon name="i-lucide-loader-circle" class="size-7 animate-spin" />
                    </button>
                    <button
                      v-else
                      v-tooltip="project.isFavorite ? 'Unfavorite' : 'Favorite'"
                      class="rounded-lg pt-2 pb-1 px-2 text-white/90 cursor-pointer"
                      :class="project.isFavorite ? 'bg-red-500 hover:bg-red-700' : 'bg-white/10 hover:bg-white/20'"
                      @click="unfavorite(project)"
                    >
                      <UIcon name="i-lucide-heart" class="size-7" />
                    </button>
                  </div>
                </div>
              </div>

              <!-- Body -->
              <div class="p-4">
                <div class="flex items-start justify-between gap-3">
                  <h3 class="text-base font-bold leading-tight flex flex-wrap gap-2 items-center">
                    {{ project.title }}
                    <span v-if="project.type" class="bg-yellow-200 text-yellow-700 rounded-xl p-2 text-xs">{{ project.type }}</span>
                  </h3>
                </div>

                <!-- Footer -->
                <div class="mt-4 flex flex-col items-start justify-between gap-2 text-sm text-white/70">
                  <div class="flex items-start gap-2">
                    <nuxt-img
                      :src="
                        !project?.user?.avatarUrl
                          ? '/images/accounts/account.jpg'
                          : isValidUrl(project?.user?.avatarUrl)
                            ? project?.user?.avatarUrl
                            : `${config.public.NUXT_PUBLIC_API_BASE}${project?.user?.avatarUrl}`
                      "
                      class="w-8 h-8 rounded-full border border-white/20"
                    />
                    <div class="flex flex-col items-start gap-3">
                      <span class="text-xs">{{ project?.user?.firstname }} {{ project?.user?.lastname }}</span>
                      <div class="flex gap-2">
                        <span v-tooltip="'Likes'" class="inline-flex items-center gap-1 text-xs">
                          <UIcon name="i-lucide-heart" class="size-4" /> {{ format(project.favoriteCount) }}
                        </span>
                        <span v-tooltip="'Copies'" class="inline-flex items-center gap-1 text-xs">
                          <UIcon name="i-lucide-layers" class="size-4" /> {{ format(project.remixCount) }}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Hover overlay CTA -->
              <div class="pointer-events-none absolute inset-0 bg-gradient-to-t from-black/60 via-black/10 to-transparent opacity-0 transition" />
            </div>

          <div v-if="!items.length" class="col-span-full text-center text-white/70 py-12">
            No favorites found.
          </div>
        </template>
      </div>

      <!-- Pagination -->
      <div v-if="total > 0" class="mt-8 flex items-center justify-between">
        <div class="text-sm text-white/70">
          Showing <span class="text-white">{{ startIndex + 1 }}</span>–<span class="text-white">{{ endIndex }}</span>
          of <span class="text-white">{{ total }}</span>
        </div>

        <div class="flex items-center gap-2">
          <button
            class="rounded-xl bg-white/10 border border-white/10 px-3 py-2 text-sm disabled:opacity-40 hover:bg-white/15"
            :disabled="page === 1"
            title="Previous page"
            @click="page = Math.max(1, page - 1)"
          >
            Prev
          </button>

          <template v-for="(p, i) in pagesToShow" :key="`${p}-${i}`">
            <button
              v-if="p !== '...'"
              class="rounded-xl px-3 py-2 text-sm border transition"
              :class="page === (p as number)
                ? 'bg-indigo-500/30 border-indigo-400/40 text-white'
                : 'bg-white/10 border-white/10 hover:bg-white/15 text-white/90 cursor-pointer'"
              :aria-current="page === p ? 'page' : undefined"
              @click="page = p as number"
            >
              {{ p }}
            </button>
            <span v-else class="px-2 text-sm text-white/60 select-none" aria-hidden="true">…</span>
          </template>

          <button
            class="rounded-xl bg-white/10 border border-white/10 px-3 py-2 text-sm disabled:opacity-40 hover:bg-white/15"
            :disabled="page === totalPages"
            title="Next page"
            @click="page = Math.min(totalPages, page + 1)"
          >
            Next
          </button>
        </div>
      </div>
    </section>


    <PublicProjectPreviewDialog
      :selected-project="selectedProject"
      :close-preview="closePreview"
    />
  </main>
</template>

<script setup lang="ts">
import axios from 'axios'
import { ref, computed, watch, onMounted } from 'vue'
import { useRuntimeConfig } from '#app'
import type { PublicProject } from '~/types/PublicProject'
import PublicProjectPreviewDialog from '~/components/dialog/PublicProjectPreviewDialog.vue'
const config = useRuntimeConfig()

useSeoMeta({
  // Core
  title: 'Your Favorite Projects – Keetlo',
  description:
  'View all your saved Keetlo projects—AI-generated HTML + Tailwind pages—with fast search, flexible sorting (recent, popular, A–Z), instant preview, and one-click unfavorite. Keep inspirations organized and jump back into building anytime.',
  robots: 'noindex,nofollow',
  // Open Graph
  ogUrl: `${config.public.SITE_URL}/account/favorites`,
  ogSiteName: 'Keetlo',
  ogType: 'website',
  ogTitle: 'Your Favorite Projects – Keetlo',
  ogDescription:
    'Quickly find and manage the projects you’ve favorited on Keetlo.',
  ogImage: `${config.public.SITE_URL}/images/og/account-favorites.png`,

  // Twitter
  twitterCard: 'summary_large_image',
  twitterTitle: 'Your Favorite Projects – Keetlo',
  twitterDescription:
    'Search, sort, preview, and manage your saved projects.',
  twitterImage: `${config.public.SITE_URL}/images/og/account-favorites.png`,
})


type SortKey = 'newestFav' | 'newest' | 'popular' | 'name'

/* State */
const q = ref('')
const sort = ref<SortKey>('newestFav')
const page = ref(1)
const pageSize = ref(16)

const items = ref<PublicProject[]>([])
const total = ref(0)
const loading = ref(false)

const DESKTOP_W = 1200;
const DESKTOP_H = 900;
const scales = reactive<Record<string, number>>({});
const selectedProject = ref<PublicProject | null>(null);

function getScale(id: string) {
  return scales[id] ?? 0.3;
}

const openPreview = (project: PublicProject) => {
  selectedProject.value = project;
}

const closePreview = () => {
  selectedProject.value = null;
}

/* Fetch */
async function fetchFavorites() {
  loading.value = true
  try {
    const { data } = await api.get(
      `${config.public.NUXT_PUBLIC_API_BASE}/user/favorites`,
      { params: { q: q.value || undefined, sort: sort.value, page: page.value, pageSize: pageSize.value } }
    )
    items.value = data.items ?? []
    total.value = data.total ?? 0

    // If current page is now empty but total > 0, step back a page and refetch
    if (!items.value.length && total.value > 0 && page.value > 1) {
      page.value = page.value - 1
      const { data: data2 } = await api.get(
        `${config.public.NUXT_PUBLIC_API_BASE}/user/favorites`,
        { params: { q: q.value || undefined, sort: sort.value, page: page.value, pageSize: pageSize.value } }
      )
      items.value = data2.items ?? []
      total.value = data2.total ?? 0
    }
  } catch (err) {
    if (axios.isAxiosError(err)) {
      console.warn('Favorites fetch failed:', err.response?.data ?? err.message)
    } else {
      console.warn('Favorites fetch failed:', err)
    }
    items.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

/* Unfavorite (reuse your existing favorite endpoint) */
const loadingFavorite = ref(false);
async function unfavorite(p: PublicProject) {
  try {
    loadingFavorite.value = true
    await api.post(
      `${config.public.NUXT_PUBLIC_API_BASE}/public/projects/${p.projectId}/favorite`,
      { favorite: false }
    )
    // Optimistic update
    items.value = items.value.filter(i => i.projectId !== p.projectId)
    total.value = Math.max(0, total.value - 1)
    // If page emptied, go back one and refetch
    if (!items.value.length && page.value > 1) {
      page.value -= 1
      fetchFavorites()
    }
  // eslint-disable-next-line @typescript-eslint/no-unused-vars
  } catch (err) {
    // ignore / toast
  } finally {
    loadingFavorite.value = false;
  }
}

/* Pagination helpers */
const totalPages = computed(() => Math.max(1, Math.ceil(total.value / pageSize.value)))
const startIndex = computed(() => (page.value - 1) * pageSize.value)
const endIndex = computed(() => Math.min(total.value, startIndex.value + pageSize.value))

const pagesToShow = computed(() => {
  const curr = page.value
  const totalP = totalPages.value
  const delta = 1
  const out: (number | '...')[] = []
  if (totalP <= 7) {
    for (let i = 1; i <= totalP; i++) out.push(i)
    return out
  }
  const left = Math.max(2, curr - delta)
  const right = Math.min(totalP - 1, curr + delta)
  out.push(1)
  if (left > 2) out.push('...')
  for (let i = left; i <= right; i++) out.push(i)
  if (right < totalP - 1) out.push('...')
  out.push(totalP)
  return out
})

/* Watchers (debounced search/sort/page) */
let timer: number | undefined
function scheduleFetch() {
  if (timer) window.clearTimeout(timer)
  timer = window.setTimeout(fetchFavorites, 250)
}
watch([q, sort], () => {
  page.value = 1
  scheduleFetch()
})
watch(page, () => scheduleFetch())


function format(n: number) {
  return new Intl.NumberFormat(undefined, { notation: 'compact' }).format(n)
}

/* Init */
onMounted(fetchFavorites)
</script>

<style>
/* optional: minimal */
</style>
