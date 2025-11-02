<!-- eslint-disable vue/multi-word-component-names -->
<template>
  <main class="lg:p-8 relative min-h-screen pb-8 lg:pb-16">
    <!-- Hero -->
    <section class="mx-auto w-full max-w-5xl py-28 px-4 text-center">
      <div class="flex justify-center mb-2">
        <nuxt-img
          src="/images/accounts/ai.png"
          class="h-16 w-16 rounded-full object-cover ring-1 ring-white/10 bg-yellow-500"
        />
      </div>
      <h2 class="text-4xl lg:text-5xl">
        Build Your Ideas with Keetlo Assistant
      </h2>
      <p class="text-lg md:text-xl text-gray-500 mt-4">
        Just describe what you want, and let our AI generate the code and UI for
        you.
      </p>
      <div class="mt-8">
        <CreateProject />
      </div>
    </section>

    <!-- Public Projects -->
    <div
      class="mx-auto max-w-[1400px] lg:bg-gray-100 lg:border lg:border-gray-200 px-4 lg:p-8 mt-4 lg:mt-8 rounded-4xl"
    >
      <section class="w-full lg:border lg:border-gray-50/20 rounded-xl">
        <div class="flex items-center justify-between mb-8">
          <h2 class="text-2xl md:text-3xl font-medium tracking-tight">
            Recent Public Projects
          </h2>
          <div class="flex items-center gap-2">
            <NuxtLink
              v-tooltip="'View all public projects'"
              to="/public/projects"
              class="cursor-pointer text-sm hover:text-indigo-500"
            >
              View All
            </NuxtLink>
          </div>
        </div>

        <!-- Grid (skeleton + real) -->
        <div
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

        <div
          v-if="!loadingProjects && publicProjects.length === 0"
          class="flex flex-col justify-center items-center min-h-[50dvh]"
        >
          <img
            src="/images/not-found/not-found.png"
            class="w-[250px] h-[250px]"
            alt="Not Found"
          >
          <h4 class="uppercase text-lg font-semibold">Projects Not found</h4>
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
import { ref, reactive, computed, onMounted, onBeforeUnmount } from "vue";
import axios from "axios";
import CreateProject from "~/components/createProject.vue";
import type { PublicProject } from "~/types/PublicProject";
import PublicProjectPreviewDialog from "~/components/dialog/PublicProjectPreviewDialog.vue";
import type { SortKey } from "~/types/SortKey";

const config = useRuntimeConfig();
const siteUrl = config.public.SITE_URL;
const sortBy = ref<SortKey>("newest");
const page = ref(1);
const pageSize = ref(18);
const publicProjects = ref<PublicProject[]>([]);
const totalCount = ref(0);
const loadingProjects = ref(false);
const skeletonCount = computed(() => 9);
const skeletonArray = computed(() =>
  Array.from({ length: skeletonCount.value }, (_, i) => i)
);
const ready = ref(false);
const selectedProject = ref<PublicProject | null>(null);

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

const openPreview = (project: PublicProject) => {
  selectedProject.value = project;
};

const closePreview = () => {
  selectedProject.value = null;
};
function buildParams() {
  return {
    sort: sortBy.value,
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
