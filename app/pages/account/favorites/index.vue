<!-- pages/account/favorites.vue -->
<template>
  <main
    class="mx-auto max-w-[1400px] min-h-[75dvh] text-black px-4 lg:px-8 py-8"
  >
    <header class="mb-6">
      <h2 class="text-2xl font-bold">Your Favorite Projects</h2>
      <p class="text-black/70 mt-1">
        Projects you’ve saved. Search, sort and manage them here.
      </p>
    </header>

    <!-- Controls -->
    <section class="mb-6 rounded-xl bg-white border border-gray-200 p-4 lg:p-6">
      <div
        class="flex flex-col gap-3 md:flex-row md:items-center md:justify-between"
      >
        <!-- Search -->
        <div class="relative w-full md:max-w-md">
          <UIcon
            name="i-lucide-search"
            class="absolute left-3 top-1/2 -translate-y-1/2 size-5 text-black/70"
          />
          <input
            v-model="q"
            type="text"
            placeholder="Search favorites…"
            class="w-full rounded-xl bg-white/10 border border-gray-200 pl-10 pr-10 py-2.5 placeholder:text-black/50 text-black focus:outline-none focus:border focus:border-indigo-500"
          >
          <button
            v-if="q"
            class="absolute right-2 top-[55%] -translate-y-1/2 text-black/70 hover:text-black"
            aria-label="Clear search"
            @click="q = ''"
          >
            <UIcon name="i-lucide-x" class="size-5 cursor-pointer" />
          </button>
        </div>

        <!-- Sort -->
        <div class="relative">
          <select
            v-model="sort"
            class="w-full lg:w-auto appearance-none rounded-xl bg-white cursor-pointer border border-gray-200 py-2.5 pl-3 pr-9 text-sm focus:outline-none focus:border focus:border-indigo-500"
            title="Sort favorites"
          >
            <option value="newestFav">Recently Favorited</option>
            <option value="newest">Newest Project</option>
            <option value="popular">Most Liked</option>
            <option value="name">Name (A–Z)</option>
          </select>
          <UIcon
            name="i-lucide-chevron-down"
            class="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 size-4 opacity-70"
          />
        </div>
      </div>
    </section>

    <!-- Grid -->
    <section class="">
      <div
        class="grid gap-6 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3"
      >
        <!-- Skeletons -->
        <template v-if="loading">
          <div
            v-for="i in 18"
            :key="'sk-' + i"
            class="group relative overflow-hidden backdrop-blur-sm transition"
          >
            <div
              class="w-full rounded-2xl overflow-hidden border border-gray-50/10 bg-black/5 h-[300px] lg:h-[250px]"
            >
              <div
                class="h-full w-full animate-pulse bg-gradient-to-b from-black/10 to-white/5"
              />
            </div>

            <div class="p-4 space-y-3">
              <div class="h-4 w-3/5 rounded bg-black/10 animate-pulse" />
              <div class="flex items-start gap-2 mt-4">
                <div class="w-8 h-8 rounded-full bg-black/10 animate-pulse" />
                <div class="flex-1 space-y-2">
                  <div class="h-3 w-1/3 rounded bg-black/10 animate-pulse" />
                  <div class="flex gap-3">
                    <div class="h-3 w-12 rounded bg-black/10 animate-pulse" />
                    <div class="h-3 w-12 rounded bg-black/10 animate-pulse" />
                  </div>
                </div>
              </div>
            </div>
          </div>
        </template>

        <!-- Items -->
        <template v-else>
          <div
            v-for="project in items"
            :key="project.projectId"
            class="relative overflow-hidden backdrop-blur-sm transition"
          >
            <!-- Cover -->
            <div
              :ref="el => setPreviewRef(project.projectId, el as HTMLElement)"
              class="cursor-pointer group relative w-full rounded-2xl overflow-hidden border border-gray-200 bg-white h-[300px] lg:h-[250px]"
              @click="openPreview(project)"
            >
              <div
                class="absolute inset-0 z-[2] transition duration-300 bg-white/10 group-hover:bg-black/10 pointer-events-none"
              />

              <div class="absolute inset-0 grid place-items-center">
                  <nuxt-img
                    :src="`${siteUrl}/api/thumbnail/${project.projectId}?v=${timeStamp}`"
                    :alt="project.title || 'No title'"
                    class="w-full aspect-[16/10] object-cover h-full skeleton-animate"
                  />
              </div>

              <!-- floating actions -->
              <div class="absolute inset-0 gap-2 z-[20] pointer-events-none">
                <div
                  class="flex justify-center items-center gap-2 h-full opacity-0 group-hover:opacity-100 transition duration-300 pointer-events-auto"
                >
                  <button
                    class="flex items-center justify-center gap-1 rounded-lg p-2 text-white cursor-pointer bg-black/70"
                  >
                    <UIcon name="i-lucide-eye" class="size-6" />
                    Preview
                  </button>
                </div>
              </div>
            </div>

            <!-- Body -->
            <div class="p-4 flex justify-between gap-4">
              <div class="flex items-start gap-3">
                <nuxt-img
                  :src="
                    !project.user.avatarUrl
                      ? '/images/accounts/account.jpg'
                      : isValidUrl(project.user.avatarUrl)
                      ? project.user.avatarUrl
                      : `${config.public.NUXT_PUBLIC_API_BASE}${project.user.avatarUrl}`
                  "
                  class="w-9 h-9 rounded-full"
                />
                <div class="flex flex-col gap-2">
                  <h3
                    class="text-base font-semibold leading-tight flex flex-wrap gap-2 items-center"
                  >
                    {{
                      project?.title && project.title?.length > 50
                        ? project.title.slice(0, 50) + "..."
                        : project.title || "No title"
                    }}
                  </h3>
                  <div class="flex flex-col items-start gap-3">
                    <div class="flex gap-2">
                      <span
                        v-tooltip="'Likes'"
                        class="inline-flex items-center gap-1 text-xs"
                      >
                        <UIcon
                          name="i-heroicons-heart-20-solid"
                          class="size-6 bg-red-500"
                        />
                        {{ format(project.favoriteCount) }}
                      </span>
                      <span
                        v-tooltip="'Copies'"
                        class="inline-flex items-center gap-1 text-xs"
                      >
                        <UIcon name="i-lucide-shuffle" class="size-5" />
                        {{ format(project.remixCount) }}
                      </span>
                    </div>
                  </div>
                </div>
              </div>
              <div>
                <button
                  v-if="loadingFavorite === project.projectId"
                  class="rounded-lg pt-2 pb-1 px-2 cursor-default"
                >
                  <UIcon
                    name="i-lucide-loader-circle"
                    class="size-7 text-indigo-500 animate-spin"
                  />
                </button>
                <button
                  v-else
                  v-tooltip="project.isFavorite ? 'Unfavorite' : 'Favorite'"
                  class="rounded-lg pt-2 pb-1 px-2 text-black/90 cursor-pointer hover:text-gray-300"
                  @click="toggleFavorite(project)"
                >
                  <UIcon
                    v-if="project.isFavorite"
                    name="i-heroicons-heart-20-solid"
                    class="size-7 text-red-500"
                  />
                  <UIcon
                    v-else
                    name="i-lucide-heart"
                    class="size-7 text-gray-700"
                  />
                </button>
              </div>
            </div>

            <!-- Hover overlay CTA -->
            <div
              class="pointer-events-none absolute inset-0 bg-gradient-to-t from-black/60 via-black/10 to-transparent opacity-0 transition"
            />
          </div>
        </template>
      </div>

       <div v-if="!loading && items.length === 0" class="col-span-3 flex flex-col justify-center items-center min-h-[50dvh] w-full">
            <img
              src="/images/not-found/not-found.png"
              class="w-[250px] h-[250px]"
              alt="Not Found"
            >
            <h4 class="uppercase text-lg font-semibold">Projects Not found</h4>
          </div>
      <!-- Pagination -->
      <div v-if="total > 0" class="mt-8 flex items-center justify-between">
        <div class="text-sm text-black/70">
          Showing <span class="text-black">{{ startIndex + 1 }}</span
          >–<span class="text-black">{{ endIndex }}</span> of
          <span class="text-black">{{ total }}</span>
        </div>

        <div class="flex items-center gap-2">
          <button
            class="cursor-pointer rounded-md border border-gray-200 px-3 py-2 text-sm disabled:opacity-40 hover:bg-gray-200"
            :disabled="page === 1"
            title="Previous page"
            @click="page = Math.max(1, page - 1)"
          >
            Prev
          </button>

          <template v-for="(p, i) in pagesToShow" :key="`${p}-${i}`">
            <button
              v-if="p !== '...'"
              class="rounded-md px-3 py-2 text-sm border transition"
              :class="page === (p as number)
                ? 'bg-indigo-500 text-white'
                : 'bg-white border-gray-200 hover:bg-gray-200 cursor-pointer'"
              :aria-current="page === p ? 'page' : undefined"
              @click="page = p as number"
            >
              {{ p }}
            </button>
            <span
              v-else
              class="px-2 text-sm text-black/60 select-none"
              aria-hidden="true"
              >…</span
            >
          </template>

          <button
            class="cursor-pointer rounded-md border border-gray-200 px-3 py-2 text-sm disabled:opacity-40 hover:bg-gray-200"
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
import axios from "axios";
import { ref, computed, watch, onMounted } from "vue";
import { useRuntimeConfig } from "#app";
import type { PublicProject } from "~/types/PublicProject";
import PublicProjectPreviewDialog from "~/components/dialog/PublicProjectPreviewDialog.vue";
const config = useRuntimeConfig();
const siteUrl = config.public.SITE_URL;

useSeoMeta({
  // Core
  title: "Your Favorite Projects – Keetlo",
  description:
    "View all your saved Keetlo projects—AI-generated HTML + Tailwind pages—with fast search, flexible sorting (recent, popular, A–Z), instant preview, and one-click unfavorite. Keep inspirations organized and jump back into building anytime.",
  robots: "noindex,nofollow",
  // Open Graph
  ogUrl: `${config.public.SITE_URL}/account/favorites`,
  ogSiteName: "Keetlo",
  ogType: "website",
  ogTitle: "Your Favorite Projects – Keetlo",
  ogDescription:
    "Quickly find and manage the projects you’ve favorited on Keetlo.",
  ogImage: `${config.public.SITE_URL}/images/og/account-favorites.png`,

  // Twitter
  twitterCard: "summary_large_image",
  twitterTitle: "Your Favorite Projects – Keetlo",
  twitterDescription: "Search, sort, preview, and manage your saved projects.",
  twitterImage: `${config.public.SITE_URL}/images/og/account-favorites.png`,
});

type SortKey = "newestFav" | "newest" | "popular" | "name";

/* State */
const q = ref("");
const sort = ref<SortKey>("newestFav");
const page = ref(1);
const pageSize = ref(18);

const items = ref<PublicProject[]>([]);
const total = ref(0);
const loading = ref(false);

const DESKTOP_W = 1500;
const DESKTOP_H = 900;
const previewRefs = reactive(new Map<string, HTMLElement>());
const scales = reactive<Record<string, number>>({});
const selectedProject = ref<PublicProject | null>(null);

function setPreviewRef(id: string, el?: HTMLElement | null) {
  if (!el) {
    previewRefs.delete(id);
    // eslint-disable-next-line @typescript-eslint/no-dynamic-delete
    delete scales[id];
    return;
  }
  previewRefs.set(id, el);
  resizeOne(id);
}
function resizeOne(id: string) {
  const el = previewRefs.get(id);
  if (!el) return;
  const cw = el.clientWidth;
  const ch = el.clientHeight;
  const s = Math.max(0.05, Math.min(cw / DESKTOP_W, ch / DESKTOP_H));
  scales[id] = s;
}
function queueResizeAll() {
  requestAnimationFrame(() => {
    previewRefs.forEach((_el, id) => resizeOne(id));
  });
}
function onResize() {
  queueResizeAll();
}
onMounted(() => window.addEventListener("resize", onResize));
onBeforeUnmount(() => window.removeEventListener("resize", onResize));

const openPreview = (project: PublicProject) => {
  selectedProject.value = project;
};

const closePreview = () => {
  selectedProject.value = null;
};

/* Fetch */
async function fetchFavorites() {
  loading.value = true;
  try {
    const { data } = await api.get(
      `${config.public.NUXT_PUBLIC_API_BASE}/user/favorites`,
      {
        params: {
          q: q.value || undefined,
          sort: sort.value,
          page: page.value,
          pageSize: pageSize.value,
        },
      }
    );
    items.value = data.items ?? [];
    total.value = data.total ?? 0;

    // If current page is now empty but total > 0, step back a page and refetch
    if (!items.value.length && total.value > 0 && page.value > 1) {
      page.value = page.value - 1;
      const { data: data2 } = await api.get(
        `${config.public.NUXT_PUBLIC_API_BASE}/user/favorites`,
        {
          params: {
            q: q.value || undefined,
            sort: sort.value,
            page: page.value,
            pageSize: pageSize.value,
          },
        }
      );
      items.value = data2.items ?? [];
      total.value = data2.total ?? 0;
    }
  } catch (err) {
    if (axios.isAxiosError(err)) {
      console.warn(
        "Favorites fetch failed:",
        err.response?.data ?? err.message
      );
    } else {
      console.warn("Favorites fetch failed:", err);
    }
    items.value = [];
    total.value = 0;
  } finally {
    loading.value = false;
  }
}

/* Unfavorite (reuse your existing favorite endpoint) */
const loadingFavorite = ref<null | string>(null);
const toggleFavorite = async (p: PublicProject) => {
  try {
    loadingFavorite.value = p.projectId;
    await api.post(
      `${config.public.NUXT_PUBLIC_API_BASE}/public/projects/${p.projectId}/favorite`,
      { favorite: false }
    );
    // Optimistic update
    items.value = items.value.filter((i) => i.projectId !== p.projectId);
    total.value = Math.max(0, total.value - 1);
    // If page emptied, go back one and refetch
    if (!items.value.length && page.value > 1) {
      page.value -= 1;
      fetchFavorites();
    }
    // eslint-disable-next-line @typescript-eslint/no-unused-vars
  } catch (err) {
    // ignore / toast
  } finally {
    loadingFavorite.value = null;
  }
};

/* Pagination helpers */
const totalPages = computed(() =>
  Math.max(1, Math.ceil(total.value / pageSize.value))
);
const startIndex = computed(() => (page.value - 1) * pageSize.value);
const endIndex = computed(() =>
  Math.min(total.value, startIndex.value + pageSize.value)
);

const pagesToShow = computed(() => {
  window.scrollTo({ top: 0, behavior: "smooth" });
  const curr = page.value;
  const totalP = totalPages.value;
  const delta = 1;
  const out: (number | "...")[] = [];
  if (totalP <= 7) {
    for (let i = 1; i <= totalP; i++) out.push(i);
    return out;
  }
  const left = Math.max(2, curr - delta);
  const right = Math.min(totalP - 1, curr + delta);
  out.push(1);
  if (left > 2) out.push("...");
  for (let i = left; i <= right; i++) out.push(i);
  if (right < totalP - 1) out.push("...");
  out.push(totalP);
  return out;
});

/* Watchers (debounced search/sort/page) */
let timer: number | undefined;
function scheduleFetch() {
  if (timer) window.clearTimeout(timer);
  timer = window.setTimeout(fetchFavorites, 250);
}
watch([q, sort], () => {
  page.value = 1;
  scheduleFetch();
});
watch(page, () => scheduleFetch());

function format(n: number) {
  return new Intl.NumberFormat(undefined, { notation: "compact" }).format(n);
}

/* Init */
onMounted(fetchFavorites);
</script>

<style>
/* optional: minimal */
</style>
