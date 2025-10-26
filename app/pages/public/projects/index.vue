<template>
  <div class="my-4 lg:my-8">
    <section>
      <div class="flex items-center justify-between mb-8">
        <h2 class="text-2xl md:text-3xl font-medium tracking-tight">
          Public Projects
        </h2>
      </div>
    </section>

    <div class="mb-4 space-y-2">
      <div class="flex flex-col lg:flex-row gap-2 items-center">
        <div class="relative w-full md:max-w-md">
          <UIcon
            name="i-lucide-search"
            class="absolute left-3 top-1/2 -translate-y-1/2 size-5 text-black/70"
          />
          <input
            v-model="search"
            type="text"
            placeholder="Search projects, users, tags…"
            class="w-full rounded-xl bg-white/10 border border-gray-200 pl-10 pr-10 py-2.5 placeholder:text-black/50 text-black focus:outline-none focus:border focus:border-indigo-500"
          />
          <button
            v-if="search"
            class="absolute right-2 top-[55%] -translate-y-1/2 text-black/70 hover:text-black"
            aria-label="Clear search"
            @click="search = ''"
          >
            <UIcon name="i-lucide-x" class="size-5 cursor-pointer" />
          </button>
        </div>
        <div class="flex gap-2 items-center w-full lg:w-auto">
        <div class="relative flex-1">
          <select
            v-model="sortBy"
            v-tooltip="'Sort projects'"
            class="w-full lg:w-auto bg-red-500 cursor-pointer appearance-none rounded-xl bg-white/10 border border-gray-200 py-2.5 pl-3 pr-9 text-sm focus:outline-none focus:border focus:border-indigo-500"
          >
            <option value="newest">Newest</option>
            <option value="popular">Most Liked</option>
            <option value="copyCount">Most Viewed</option>
            <option value="name">Name (A–Z)</option>
          </select>
          <UIcon
            name="i-lucide-chevron-down"
            class="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 size-4 opacity-70"
          />
        </div>
           <div class="relative lg:hidden flex-1">
          <select
            v-model="selectedType"
            v-tooltip="'Sort project types'"
            class="w-full cursor-pointer appearance-none rounded-xl bg-white/10 border border-gray-200 py-2.5 pl-3 pr-9 text-sm focus:outline-none focus:border focus:border-indigo-500"
          >
            <option  v-for="type in types":value="type">{{type}}</option>
          </select>
          <UIcon
            name="i-lucide-chevron-down"
            class="pointer-events-none absolute right-2 top-1/2 -translate-y-1/2 size-4 opacity-70"
          />
        </div>
        </div>
      </div>
      <div class="hidden lg:flex gap-2">
        <button
          :disabled="selectedType === type"
          @click="handleSelectType(type)"
          :key="type"
          v-for="type in types"
          class="border border-gray-200 hover:bg-gray-200 py-2 px-6 rounded-lg"
          :class="
            selectedType === type
              ? 'cursor-default bg-gray-200'
              : 'cursor-pointer bg-white'
          "
        >
          {{ type }}
        </button>
      </div>
    </div>
    <!-- Grid (skeleton + real) -->
    <section
      class="grid gap-8 grid-cols-1 sm:grid-cols-2 lg:grid-cols-3"
      :aria-busy="loadingProjects ? 'true' : 'false'"
    >
      <!-- Skeleton cards -->
      <template v-if="loadingProjects">
        <div
          v-for="i in skeletonArray"
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

      <!-- Real cards -->
      <template v-else>
        <div
          v-for="project in publicProjects"
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
              <img :src="`/api/thumbnail/${project.projectId}`" :alt="project.title || 'No title'" class="w-full aspect-[16/10] object-cover" loading="lazy">
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
    </section>

    <!-- Pagination -->
    <div
      v-if="!loadingProjects && publicProjects.length === 0"
      class="flex flex-col justify-center items-center min-h-[50dvh]"
    >
      <img
        src="/images/not-found/not-found.png"
        class="w-[250px] h-[250px]"
        alt="Not Found"
      />
      <h4 class="uppercase text-lg font-semibold">Projects Not found</h4>
    </div>
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

    <PublicProjectPreviewDialog
      :selected-project="selectedProject"
      :close-preview="closePreview"
    />
  </div>
</template>
<script setup lang="ts">
import { ref, reactive, computed, onMounted, onBeforeUnmount } from "vue";
import axios from "axios";
import type { PublicProject } from "~/types/PublicProject";
import PublicProjectPreviewDialog from "~/components/dialog/PublicProjectPreviewDialog.vue";
import type { SortKey } from "~/types/SortKey";
import debounce from 'lodash/debounce'

const route = useRoute();
const config = useRuntimeConfig();
const sortBy = ref<SortKey>(
  route.query.sort ? (route.query.sort as SortKey) : "newest"
);
const page = ref(route.query.page ? parseInt(route.query.page as string) : 1);
const pageSize = ref(18);
const publicProjects = ref<PublicProject[]>([]);
const total = ref(0);
const loadingProjects = ref(false);
const skeletonCount = computed(() => 9);
const skeletonArray = computed(() =>
  Array.from({ length: skeletonCount.value }, (_, i) => i)
);
const ready = ref(false);
const selectedProject = ref<PublicProject | null>(null);

const types = [
  "All",
  "Website",
  "Project",
  "Natural",
  "Internal Tools",
  "Travel",
  "Information",
  "Others",
];
const selectedType = ref(route.query.type ? route.query.type : "All");
const search = ref(route.query.search ? route.query.search : "");

const handleSelectType = (type: string) => {
  selectedType.value = type;
};

useSeoMeta({
  title: "Keetlo – Create AI-Generated HTML Projects",
  description:
    "Describe what you want and Keetlo generates beautiful, production-ready HTML pages with tailwindcss styling. Explore trending projects, remix ideas, and ship faster with AI.",
  ogUrl: `${config.public.SITE_URL}`,
  ogTitle: "Keetlo – Create AI-Generated HTML Projects",
  ogDescription:
    "Explore trending AI-generated HTML projects. Remix, favorite, and build your own with Keetlo.",
  ogType: "website",
  ogImage: `${config.public.SITE_URL}/images/og/home.png`, // ideally a 1200x630 image
  twitterCard: "summary_large_image",
  twitterTitle: "Keetlo – Create AI-Generated HTML Projects",
  twitterDescription:
    "Explore trending AI-generated HTML projects. Remix, favorite, and build your own with Keetlo.",
  twitterImage: `${config.public.SITE_URL}/images/og/home.png`,
});

const totalPages = computed(() =>
  Math.max(1, Math.ceil(total.value / pageSize.value))
);
const startIndex = computed(() => (page.value - 1) * pageSize.value);
const endIndex = computed(() =>
  Math.min(total.value, startIndex.value + pageSize.value)
);

const pagesToShow = computed(() => {
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

const openPreview = (project: PublicProject) => {
  selectedProject.value = project;
};

const closePreview = () => {
  selectedProject.value = null;
};

const writeSearch = debounce((value: string) => {
  setQueryParam("search", value || null);
  page.value = 1;
}, 300);
watch(search, (value) => writeSearch(value as string));

watch(sortBy, (value) => {
  setQueryParam("sort", value || null);
  page.value = 1;
});

watch(selectedType, (value) => {
  setQueryParam("type", (value as string) || null);
  page.value = 1;
});

watch(page, (value) => {
  setQueryParam("page", value || null);
});

watch(
  () => route.query,
  async () => {
    window.scrollTo({ top: 0, behavior: "smooth" });
    await fetchPublicProjects();
  },
  { immediate: true, deep: true }
);

function buildParams() {
  return {
    q: search.value || undefined,
    sort: sortBy.value,
    page: page.value,
    type: selectedType.value,
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
    total.value = data.total ?? 0;
    queueResizeAll();
  } catch (err) {
    if (axios.isAxiosError(err)) {
      console.warn("Fetch projects failed:", err.response?.data ?? err.message);
    } else {
      console.warn("Fetch projects failed:", err);
    }
    publicProjects.value = [];
    total.value = 0;
  } finally {
    loadingProjects.value = false;
  }
}
onMounted(async () => {
  await fetchPublicProjects();
  ready.value = true;
});

const loadingFavorite = ref<string | null>(null);
const toggleFavorite = async (project: PublicProject) => {
  try {
    loadingFavorite.value = project.projectId;
    await api.post(
      `${config.public.NUXT_PUBLIC_API_BASE}/public/projects/${project.projectId}/favorite`,
      { favorite: !project.isFavorite }
    );
    project.isFavorite = !project.isFavorite;
    loadingFavorite.value = null;
  } catch (error: unknown) {
    if (axios.isAxiosError(error) && error?.response?.status === 401) {
      window.location.href = `/auth/login?next=${window.location.href}`;
    } else {
      alert(JSON.stringify(error));
    }
  }
};

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
</script>
