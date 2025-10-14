<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <main class="relative min-h-screen text-white pb-8 lg:pb-16">
    <!-- Decorative background -->
    <div class="flex items-center justify-center pointer-events-none absolute inset-0 -z-10 overflow-hidden">
      <div
        class="absolute inset-0 left-0 top-20 z-[1] w-full h-[50vh] sm:h-full rounded-full blur-xl opacity-70
               bg-no-repeat
               bg-[radial-gradient(120%_90%_at_25%_25%,_var(--tw-gradient-stops))]
               [--tw-gradient-stops:#60a5fa,_#22d3ee_30%,_#a78bfa_55%,_#f97316_75%,_#facc15_100%]"
      />
    </div>

    <!-- Hero -->
    <section class="mx-auto w-full max-w-5xl px-4 py-12 md:py-16 text-center">
      <h2 class="text-5xl md:text-7xl font-black leading-tight tracking-[-0.033em] text-transparent bg-clip-text bg-gradient-to-b from-white to-gray-200">
        Create HTML-Page Projects with AI
      </h2>
      <p class="text-lg md:text-xl text-gray-300/90 mt-4">
        Just describe what you want, and let our AI generate the code and UI for you.
      </p>
      <div class="mt-8">
        <CreateProject />
      </div>
    </section>

    <!-- Controls -->
    <div class="px-4 lg:px-8">
      <section class="w-full p-4 lg:p-6 border border-slate-50/20 rounded-xl bg-[#0D1117]">
        <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <!-- Search -->
          <div class="relative w-full md:max-w-md">
            <UIcon name="i-lucide-search" class="absolute left-3 top-1/2 -translate-y-1/2 size-5 text-white/70" />
            <input
              v-model="query"
              type="search"
              placeholder="Search projects, users, tags…"
              class="w-full rounded-xl bg-white/10 border border-white/10 pl-10 pr-3 py-2.5 placeholder:text-white/50 text-white
                     focus:outline-none focus:ring-2 focus:ring-indigo-400/50"
            >
            <button
              v-if="query"
              v-tooltip="'Clear search'"
              class="absolute right-2 top-1/2 -translate-y-1/2 text-white/70 hover:text-white"
              @click="query = ''"
            >
              <UIcon name="i-lucide-x" class="size-5" />
            </button>
          </div>

          <!-- Sort + density -->
          <div class="flex items-center gap-3">
            <div class="relative">
              <select
                v-model="sortBy"
                v-tooltip="'Sort projects'"
                class="appearance-none rounded-xl bg-white/10 border border-white/10 py-2.5 pl-3 pr-9 text-sm
                       focus:outline-none focus:ring-2 focus:ring-indigo-400/50"
              >
                <option value="newest">Newest</option>
                <option value="popular">Most Liked</option>
                <option value="copyCount">Most Viewed</option>
                <option value="name">Name (A–Z)</option>
              </select>
              <UIcon name="i-lucide-chevron-down" class="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 size-4 opacity-70" />
            </div>

            <button
              v-tooltip="dense ? 'Comfortable grid' : 'Dense grid'"
              class="flex items-center gap-2 rounded-xl bg-white/10 border border-white/10 px-3 py-2 text-sm hover:bg-white/15"
              :class="dense ? 'ring-2 ring-indigo-400/40' : ''"
              @click="dense = !dense"
            >
              <UIcon :name="dense ? 'i-lucide-layout-grid' : 'i-lucide-layout-dashboard'" class="size-4 inline-block mr-1" />
              {{ dense ? "Dense" : "Comfort" }}
            </button>
          </div>
        </div>

        <!-- Tag filters (horizontal scroll with skeleton) -->
        <div class="mt-4 flex items-center gap-2">
          <div class="relative flex-1 overflow-x-auto scrollbar-none">
            <div class="pointer-events-none absolute left-0 top-0 h-full w-6 bg-gradient-to-r from-[#0D1117] to-transparent"/>
            <div class="pointer-events-none absolute right-0 top-0 h-full w-6 bg-gradient-to-l from-[#0D1117] to-transparent"/>

            <!-- Skeleton pills while loading tags -->
            <div v-if="loadingTags" class="flex items-center gap-2 pr-6 pl-6 w-max">
              <span class="h-8 w-14 rounded-full bg-white/10 animate-pulse"/>
              <span class="h-8 w-20 rounded-full bg-white/10 animate-pulse"/>
              <span class="h-8 w-24 rounded-full bg-white/10 animate-pulse"/>
              <span class="h-8 w-16 rounded-full bg-white/10 animate-pulse"/>
              <span class="h-8 w-28 rounded-full bg-white/10 animate-pulse"/>
            </div>

            <!-- Real tags -->
            <div v-else class="flex items-center gap-2 pr-6 pl-6 w-max">
              <button
                v-tooltip="'Show all categories'"
                class="px-3 py-1.5 rounded-full text-sm border transition"
                :class="selectedTags.length === 0 ? 'bg-white/20 border-white/10' : 'bg-white/10 border-white/10 hover:bg-white/15'"
                @click="selectedTags = []"
              >
                All
              </button>

              <button
                v-for="t in allTags"
                :key="t"
                class="px-3 py-1.5 rounded-full text-sm border transition snap-start"
                :class="selectedTags.includes(t) ? 'bg-indigo-500/30 border-indigo-400/40' : 'bg-white/10 border-white/10 hover:bg-white/15'"
                @click="toggleTag(t)"
              >
                #{{ t }}
              </button>
            </div>
          </div>

          <div class="text-sm text-white/70 whitespace-nowrap">
            {{ filteredProjects.length }} results
          </div>
        </div>
      </section>
    </div>

    <!-- Public Projects -->
    <div class="px-4 lg:px-8 mt-4 lg:mt-8">
      <section class="w-full p-4 lg:p-6 border border-slate-50/20 bg-[#0D1117] rounded-xl">
        <div class="flex items-center justify-between mb-8">
          <h2 class="text-2xl md:text-3xl font-bold tracking-tight">Public Projects</h2>
          <div class="flex items-center gap-2">
            <button
              v-tooltip="'Reset filters & sorting'"
              class="cursor-pointer text-sm text-indigo-300 hover:text-indigo-200"
              @click="resetAll"
            >
              Reset
            </button>
          </div>
        </div>

        <!-- Grid (skeleton + real) -->
        <div
          class="grid gap-6"
          :class="dense
            ? 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 xl:grid-cols-5'
            : 'grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4'"
          :aria-busy="loadingProjects ? 'true' : 'false'"
        >
          <!-- Skeleton cards -->
          <template v-if="loadingProjects">
            <div
              v-for="i in skeletonArray"
              :key="'sk-'+i"
              class="group relative overflow-hidden backdrop-blur-sm transition"
            >
              <div
                class="w-full rounded-2xl overflow-hidden border border-slate-50/10 bg-white/5"
                :class="dense ? 'h-[200px]' : 'h-[250px]'"
              >
                <div class="h-full w-full animate-pulse bg-gradient-to-b from-white/10 to-white/5"/>
              </div>

              <div class="p-4 space-y-3">
                <div class="h-4 w-3/5 rounded bg-white/10 animate-pulse"/>
                <div class="flex items-start gap-2 mt-4">
                  <div class="w-8 h-8 rounded-full bg-white/10 animate-pulse"/>
                  <div class="flex-1 space-y-2">
                    <div class="h-3 w-1/3 rounded bg-white/10 animate-pulse"/>
                    <div class="flex gap-3">
                      <div class="h-3 w-12 rounded bg-white/10 animate-pulse"/>
                      <div class="h-3 w-12 rounded bg-white/10 animate-pulse"/>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </template>

          <!-- Real cards -->
          <template v-else>
            <div
              v-for="project in paginated"
              :key="project.projectId"
              class="group relative overflow-hidden backdrop-blur-sm transition"
            >
              <!-- Cover -->
              <div
                :ref="el => setPreviewRef(project.projectId, el as HTMLElement)"
                class="group relative w-full rounded-2xl overflow-hidden border border-slate-50/10"
                :class="dense ? 'h-[200px]' : 'h-[250px]'"
              >
                <div class="absolute inset-0 z-[2] transition duration-300 group-hover:bg-black/70 pointer-events-none"/>

                <div class="absolute inset-0 grid place-items-center">
                  <iframe
                    :srcdoc="project.mainHtmlContent"
                    title="preview"
                    class="rounded-md shadow bg-white pointer-events-none min-w-[320dvw] md:min-w-[180dvw] lg:min-w-auto min-h-[150dvh]"
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
                      @click="toggleFavorite(project)"
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
                    {{ project.title || "No title" }}
                    <span v-if="project.type" class="border border-yellow-500 bg-yellow-200 text-yellow-700 rounded-xl p-2 text-xs font-medium">{{ project.type }}</span>
                  </h3>
                </div>

                <!-- Footer -->
                <div class="mt-4 flex flex-col items-start justify-between gap-2 text-sm text-white/70">
                  <div class="flex items-start gap-2">
                    <nuxt-img
                      :src="
                        !project.user.avatarUrl
                          ? '/images/accounts/account.jpg'
                          : isValidUrl(project.user.avatarUrl)
                            ? project.user.avatarUrl
                            : `${config.public.NUXT_PUBLIC_API_BASE}${project.user.avatarUrl}`
                      "
                      class="w-8 h-8 rounded-full border border-white/20"
                    />
                    <div class="flex flex-col items-start gap-3">
                      <span class="text-xs">{{ project.user.firstname }} {{ project.user.lastname }}</span>
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
          </template>
        </div>

        <!-- Pagination -->
        <div class="mt-8 flex items-center justify-between">
          <div class="text-sm text-white/70">
            Showing <span class="text-white">{{ startIndex + 1 }}</span>–<span class="text-white">{{ endIndex }}</span>
            of <span class="text-white">{{ filteredProjects.length }}</span>
          </div>

          <div class="flex items-center gap-2">
            <button
              v-tooltip="'Previous page'"
              class="rounded-xl bg-white/10 border border-white/10 px-3 py-2 text-sm disabled:opacity-40 hover:bg-white/15"
              :disabled="page === 1"
              @click="page = Math.max(1, page - 1)"
            >
              Prev
            </button>

            <template v-for="(p, i) in pagesToShow" :key="`${p}-${i}`">
              <button
                v-if="p !== '...'"
                class="rounded-xl px-3 py-2 text-sm border transition"
                :class="page === p
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
              v-tooltip="'Next page'"
              class="rounded-xl bg-white/10 border border-white/10 px-3 py-2 text-sm disabled:opacity-40 hover:bg-white/15"
              :disabled="page === totalPages"
              @click="page = Math.min(totalPages, page + 1)"
            >
              Next
            </button>
          </div>
        </div>
      </section>
    </div>

    <PublicProjectPreviewDialog
      :selected-project="selectedProject"
      :close-preview="closePreview"
    />
  </main>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted, onBeforeUnmount } from "vue";
import axios from "axios";
import { useRoute, useRouter } from "vue-router";
import CreateProject from "~/components/createProject.vue";
import type { PublicProject } from "~/types/PublicProject";
import PublicProjectPreviewDialog from "~/components/dialog/PublicProjectPreviewDialog.vue";

type SortKey = "newest" | "popular" | "copyCount" | "name";

/* ---------- UI STATE ---------- */
const query = ref("");
const sortBy = ref<SortKey>("newest");
const selectedTags = ref<string[]>([]);
const dense = ref(false);
const page = ref(1);
const pageSize = ref(16);

/* ---------- DATA ---------- */
const publicProjects = ref<PublicProject[]>([]);
const totalCount = ref(0);
const allTags = ref<string[]>([]);

/* Loading flags */
const loadingProjects = ref(false);
const loadingTags = ref(false);

/* Skeleton sizing */
const skeletonCount = computed(() => (dense.value ? 10 : 8));
const skeletonArray = computed(() => Array.from({ length: skeletonCount.value }, (_, i) => i));

/* ---------- COMMS / ROUTER ---------- */
const config = useRuntimeConfig();
const route = useRoute();
const router = useRouter();

/* ---------- URL <-> STATE SYNC ---------- */
const ready = ref(false);
let updatingUrl = false;

const selectedProject = ref<PublicProject | null>(null);

const openPreview = (project: PublicProject) => {
  selectedProject.value = project;
}

const closePreview = () => {
  selectedProject.value = null;
}

useSeoMeta({
  title: 'Keetlo – Create AI-Generated HTML Projects',
  description:
    'Describe what you want and Keetlo generates beautiful, production-ready HTML pages with tailwindcss styling. Explore trending projects, remix ideas, and ship faster with AI.',
  ogUrl: `${config.public.SITE_URL}`,
  ogTitle: 'Keetlo – Create AI-Generated HTML Projects',
  ogDescription:
    'Explore trending AI-generated HTML projects. Remix, favorite, and build your own with Keetlo.',
  ogType: 'website',
  ogImage: `${config.public.SITE_URL}/images/og/home.png`,   // ideally a 1200x630 image
  twitterCard: 'summary_large_image',
  twitterTitle: 'Keetlo – Create AI-Generated HTML Projects',
  twitterDescription:
    'Explore trending AI-generated HTML projects. Remix, favorite, and build your own with Keetlo.',
  twitterImage: `${config.public.SITE_URL}/images/og/home.png`,
})

function serializeStateToQuery() {
  return {
    q: query.value || undefined,
    sort: sortBy.value !== "newest" ? sortBy.value : undefined,
    tags: selectedTags.value.length ? selectedTags.value.join(",") : undefined,
    page: page.value > 1 ? String(page.value) : undefined,
    pageSize: pageSize.value !== 16 ? String(pageSize.value) : undefined,
    dense: dense.value ? "1" : undefined,
  };
}
function setIfChanged<T>(r: { value: T }, next: T) {
  if (Array.isArray(next)) {
    const cur = r.value as unknown as unknown[];
    if (!Array.isArray(cur) || cur.length !== next.length || next.some((v, i) => v !== cur[i])) {
      r.value = next as unknown as T;
    }
  } else {
    if (r.value !== next) r.value = next;
  }
}
function hydrateStateFromQuery() {
  const sortWhitelist = ["popular", "copyCount", "name", "newest"] as const;

  const q = typeof route.query.q === "string" ? route.query.q : "";
  const sort = sortWhitelist.includes(route.query.sort as SortKey)
    ? (route.query.sort as SortKey)
    : "newest";
  const tags = typeof route.query.tags === "string" ? route.query.tags.split(",").filter(Boolean) : [];

  const pNum = parseInt(String(route.query.page ?? ""), 10);
  const psNum = parseInt(String(route.query.pageSize ?? ""), 10);
  const dn = route.query.dense === "1";

  setIfChanged(query, q);
  setIfChanged(sortBy, sort);
  setIfChanged(selectedTags, tags);
  setIfChanged(page, Number.isFinite(pNum) && pNum > 0 ? pNum : 1);
  setIfChanged(pageSize, Number.isFinite(psNum) && psNum > 0 ? psNum : 16);
  setIfChanged(dense, dn);
}
function shallowEqualQuery(a: Record<string, unknown>, b: Record<string, unknown>) {
  const ka = Object.keys(a);
  const kb = Object.keys(b);
  if (ka.length !== kb.length) return false;
  for (const k of ka) if (a[k] !== b[k]) return false;
  return true;
}
let urlTimer: number | undefined;
function scheduleUrlUpdate(resetPage = false) {
  if (resetPage) page.value = 1;
  if (urlTimer) window.clearTimeout(urlTimer);
  urlTimer = window.setTimeout(() => {
    const nextQuery = serializeStateToQuery();
    const current = route.query as Record<string, unknown>;
    if (!shallowEqualQuery(nextQuery, current)) {
      updatingUrl = true;
      router.replace({ query: nextQuery }).finally(() => { updatingUrl = false; });
    }
  }, 200);
}
watch(() => route.query, () => {
  if (updatingUrl) return;
  const before = serializeStateToQuery();
  hydrateStateFromQuery();
  const after = serializeStateToQuery();
  if (!shallowEqualQuery(before, after)) {
    if (ready.value) fetchPublicProjects();
  }
});

/* ---------- FETCHING ---------- */
function buildParams() {
  return {
    q: query.value || undefined,
    sort: sortBy.value,
    tags: selectedTags.value.join(",") || undefined,
    page: page.value,
    pageSize: pageSize.value,
  };
}
async function fetchPublicProjects() {
  loadingProjects.value = true;
  try {
    const { data } = await api.get(
      `${config.public.NUXT_PUBLIC_API_BASE}/public/projects`,
      { params: buildParams() }
    );
    publicProjects.value = data.items ?? [];
    totalCount.value = data.total ?? 0;
    queueResizeAll();
  } catch (err) {
    if (axios.isAxiosError(err)) {
      console.warn("Fetch projects failed:", err.response?.data ?? err.message);
    } else {
      console.warn("Fetch projects failed:", err);
    }
    publicProjects.value = [];
    totalCount.value = 0;
  } finally {
    loadingProjects.value = false;
  }
}
async function fetchAllTags() {
  loadingTags.value = true;
  try {
    const { data } = await api.get(
      `${config.public.NUXT_PUBLIC_API_BASE}/public/projects/tags`
    );
    allTags.value = Array.isArray(data) ? data : (data?.tags ?? []);
  } catch {
    const set = new Set<string>();
    publicProjects.value.forEach(p => p.tags.forEach(t => set.add(t)));
    allTags.value = Array.from(set).sort();
  } finally {
    loadingTags.value = false;
  }
}

/* ---------- Debounced refetch with guard ---------- */
let refetchTimer: number | undefined;
watch([query, sortBy, selectedTags, page], ([qN, sN, tN], [qO, sO, tO]) => {
  if (!ready.value) return;
  const reset =
    qN !== qO ||
    sN !== sO ||
    (tN?.length !== (tO?.length ?? 0) || tN?.some((v, i) => v !== (tO?.[i])));
  scheduleUrlUpdate(reset);
  if (refetchTimer) window.clearTimeout(refetchTimer);
  refetchTimer = window.setTimeout(() => {
    fetchPublicProjects();
  }, 250);
});
watch(pageSize, () => {
  if (!ready.value) return;
  scheduleUrlUpdate(true);
  if (refetchTimer) window.clearTimeout(refetchTimer);
  refetchTimer = window.setTimeout(() => {
    fetchPublicProjects();
  }, 250);
});

/* ---------- Initial load ---------- */
onMounted(async () => {
  hydrateStateFromQuery();
  await fetchPublicProjects();
  await fetchAllTags();
  ready.value = true;
});

/* ---------- Pagination math ---------- */
const totalPages = computed(() => Math.max(1, Math.ceil(totalCount.value / pageSize.value)));
const startIndex = computed(() => (page.value - 1) * pageSize.value);
const endIndex = computed(() => Math.min(totalCount.value, startIndex.value + pageSize.value));

/* Expose same names */
const paginated = computed(() => publicProjects.value);
const filteredProjects = computed(() => ({ length: totalCount.value } as unknown as PublicProject[]));

/* ---------- UI actions ---------- */
function toggleTag(tag: string) {
  if (selectedTags.value.includes(tag)) {
    selectedTags.value = selectedTags.value.filter(t => t !== tag);
  } else {
    selectedTags.value = [...selectedTags.value, tag];
  }
}
function resetAll() {
  query.value = "";
  selectedTags.value = [];
  sortBy.value = "newest";
  page.value = 1;
  scheduleUrlUpdate(true);
}
const loadingFavorite = ref(false);
const toggleFavorite = async (project: PublicProject) => {
  try{
  loadingFavorite.value = true;
    await api.post(
    `${config.public.NUXT_PUBLIC_API_BASE}/public/projects/${project.projectId}/favorite`,
    { favorite: !project.isFavorite });
       project.isFavorite = !project.isFavorite;
           loadingFavorite.value = false;
  }catch(error: unknown){
    if (axios.isAxiosError(error) && error?.response?.status === 401) {
      window.location.href = `/auth/login?next=${window.location.href}`;
    } else {
      alert(JSON.stringify(error));
    }
  }
}

/* ---------- Preview scaling (per-card) ---------- */
const DESKTOP_W = 1500;
const DESKTOP_H = 900;

const previewRefs = reactive(new Map<string, HTMLElement>());
const scales = reactive<Record<string, number>>({});

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
function getScale(id: string) {
  return scales[id] ?? 0.3;
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

/* ---------- Utils ---------- */
function format(n: number) {
  return new Intl.NumberFormat(undefined, { notation: "compact" }).format(n);
}

/* Numbered pages with ellipses */
const pagesToShow = computed(() => {
  const curr = page.value;
  const total = totalPages.value;
  const delta = 1;
  const pages: (number | '...')[] = [];

  if (total <= 7) {
    for (let i = 1; i <= total; i++) pages.push(i);
    return pages;
  }

  const left = Math.max(2, curr - delta);
  const right = Math.min(total - 1, curr + delta);

  pages.push(1);
  if (left > 2) pages.push('...');
  for (let i = left; i <= right; i++) pages.push(i);
  if (right < total - 1) pages.push('...');
  pages.push(total);

  return pages;
});
</script>

<style>
@media (prefers-reduced-motion: reduce) {
  .animate-pulse { animation: none !important; }
}
</style>
