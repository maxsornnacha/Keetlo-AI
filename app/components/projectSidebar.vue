<template>
  <aside
    ref="scrollEl"
    class="overflow-y-auto backdrop-blur-sm flex flex-col w-96 lg:border-r lg:border-gray-200"
    :class="props.heightFull ? 'h-screen' : 'h-[90dvh]'"
  >
    <!-- Search -->
    <div
      class="sticky top-0 z-30 p-4 backdrop-blur-sm border-b border-gray-200 bg-white"
    >
      <label for="project-search" class="sr-only">Search projects</label>
      <div class="relative">
        <UIcon
          name="i-lucide-search"
          class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 size-4 text-gray-400"
        />
        <input
          id="project-search"
          v-model="searchTerm"
          type="text"
          placeholder="Search projects…"
          class="w-full rounded-md border border-gray-200 bg-transparent pl-10 pr-3 py-2 text-sm text-black placeholder:text-gray-500 focus:outline-none focus:border focus:border-indigo-500"
          @input="onSearchChange"
        >
        <!-- Clear -->
        <button
          v-if="searchTerm"
          class="absolute right-2 top-1/2 -translate-y-1/2 p-1 rounded hover:bg-white/10 cursor-pointer"
          @click="clearSearch"
        >
          <UIcon name="i-lucide-x" class="size-4 text-gray-300" />
        </button>
      </div>
    </div>

    <!-- Initial loading skeletons -->
    <div v-if="loadingProjects && projects.length === 0" class="p-3 space-y-2">
      <div
        v-for="i in 9"
        :key="i"
        class="flex gap-3 items-center p-3 rounded-md border border-white/5 bg-black/5 animate-pulse"
      >
        <div class="w-[50px] h-[50px] rounded-md bg-black/10" />
        <div class="flex-1 space-y-2">
          <div class="h-3 w-3/4 rounded bg-black/10" />
          <div class="h-3 w-1/2 rounded bg-black/10" />
        </div>
      </div>
    </div>

    <!-- List -->
    <div v-else class="p-3">
      <template v-if="projects.length">
        <NuxtLink
          v-for="project in projects"
          :key="project.projectId"
          :to="`/account/projects/${project.projectId}`"
          class="group block rounded-md p-3 border border-transparent hover:border-gray-200 hover:bg-gray-100 transition-colors"
          :class="props.project?.projectId === project.projectId ? 'border-gray-200 bg-gray-100' :''"
        >
          <div class="flex gap-3 items-center">
            <!-- Mini preview (iframe) -->
            <div
              class="flex-none w-[50px] h-[50px] overflow-hidden border border-gray-200 rounded-md shadow bg-white relative"
            >
             <img :src="`/api/thumbnail/${project.projectId}`" alt="" class="w-full aspect-[16/10] object-cover" loading="lazy">
            </div>

            <!-- Meta -->
            <div class="min-w-0 flex-1">
              <p class="font-medium text-sm text-black truncate">
                {{ project.title || "No title" }}
              </p>
              <p class="text-xs text-gray-400 truncate">
                {{ project.type || "No type" }}
              </p>
              <p class="text-[11px] text-gray-500 mt-0.5">
                Updated: {{ formatDate(project.updatedAt) }}
              </p>
            </div>

            <UIcon
              name="i-lucide-chevron-right"
              class="size-4 text-gray-500 opacity-0 group-hover:opacity-100 transition-opacity"
            />
          </div>
        </NuxtLink>

        <!-- Loading more -->
        <div v-if="loadingMore" class="p-3 text-center text-gray-400">
          Loading more…
        </div>

        <!-- Sentinel for infinite scroll -->
        <div v-if="currentPage < totalPages" ref="sentinel" class="h-6" />
      </template>

      <!-- Empty -->
      <div v-else class="p-3 text-gray-400">No projects found.</div>

      <!-- Error -->
      <div
        v-if="errorMessage"
        class="mt-3 p-3 rounded border border-red-500/30 bg-red-500/10 text-red-300 text-sm"
      >
        {{ errorMessage }}
      </div>
    </div>
  </aside>
</template>

<script setup lang="ts">
import type { Project } from "~/types/Projects";
import axios from "axios";
import { toErrorMessage } from "@/utils/axios";

const config = useRuntimeConfig();
const props = defineProps<{
  heightFull?: boolean;
  project?: Project;
}>();
const projects = ref<Project[]>([]);
const searchTerm = ref("");
const startDate = ref<string | null>(null); // keep for API compatibility if you later add UI
const endDate = ref<string | null>(null);

const loadingProjects = ref(true);
const loadingMore = ref(false);
const errorMessage = ref<string | null>(null);

let currentPage = 1;
const pageSize = 10;
let totalPages = 1;

const scrollEl = ref<HTMLElement | null>(null);
const sentinel = ref<HTMLElement | null>(null);
let io: IntersectionObserver | null = null;

const fetchProjects = async (page = 1) => {
  try {
    if (page === 1) loadingProjects.value = true;
    else loadingMore.value = true;

    const params: Record<string, unknown> = { page, pageSize };
    if (searchTerm.value) params.search = searchTerm.value;
    if (startDate.value) params.startDate = startDate.value;
    if (endDate.value) params.endDate = endDate.value;

    const { data } = await api.get(
      `${config.public.NUXT_PUBLIC_API_BASE}/project/me`,
      { params }
    );

    const newProjects: Project[] = (data.projects || []).map((p: Project) => ({
      ...p,
      updatedAt: new Date(p.updatedAt),
    }));

    if (page === 1) projects.value = newProjects;
    else projects.value.push(...newProjects);

    totalPages = data.totalPages ?? 1;
    currentPage = page;
    errorMessage.value = null;
  } catch (error: unknown) {
    errorMessage.value = axios.isAxiosError(error)
      ? toErrorMessage(error?.response?.data?.message)
      : "An unexpected error occurred.";
  } finally {
    loadingProjects.value = false;
    loadingMore.value = false;
  }
};

// Debounced search
let searchTimeout: number | undefined;
const onSearchChange = () => {
  if (searchTimeout) clearTimeout(searchTimeout);
  searchTimeout = window.setTimeout(() => fetchProjects(1), 300);
};
const clearSearch = () => {
  searchTerm.value = "";
  if (searchTimeout) clearTimeout(searchTimeout);
  fetchProjects(1);
};

// Date formatting (tiny helper)
const formatDate = (d: Date | string | number | null | undefined) => {
  if (!d) return "—";
  const date = d instanceof Date ? d : new Date(d);
  return date.toLocaleDateString(undefined, {
    year: "numeric",
    month: "short",
    day: "numeric",
  });
};

onMounted(async () => {
  await fetchProjects(1);

  // IntersectionObserver for infinite loading
  io = new IntersectionObserver(
    (entries) => {
      const entry = entries[0]!;
      if (
        entry.isIntersecting &&
        !loadingMore.value &&
        currentPage < totalPages
      ) {
        fetchProjects(currentPage + 1);
      }
    },
    {
      root: scrollEl.value ?? null, // confine to aside scroll
      rootMargin: "0px 0px 200px 0px", // prefetch before bottom
      threshold: 0,
    }
  );

  if (sentinel.value) io.observe(sentinel.value);
});

onBeforeUnmount(() => {
  if (io) {
    io.disconnect();
    io = null;
  }
  if (searchTimeout) clearTimeout(searchTimeout);
});
</script>
