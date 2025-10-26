<template>
  <div>
    <header
      class="w-full flex items-center justify-between whitespace-nowrap p-2"
    >
      <Teleport to="body">
        <div
          v-if="mobileSidebarOpen"
          class="fixed inset-0 bg-black/70 z-40"
          @click="onCloseMobileSidebar"
        />
        <div class="flex items-center gap-2">
          <aside
            class="h-screen bg-white opacity-90 fixed inset-y-0 left-0 z-50 transform transition-transform duration-300"
            :class="mobileSidebarOpen ? 'translate-x-0' : '-translate-x-full'"
          >
            <div
              class="bg-white border border-gray-200 flex justify-between items-center p-2"
            >
              <div>
                <h2 class="text-base">Total Projects</h2>
              </div>
              <button
                class="pt-1 px-1 hover:bg-gray-200 rounded-lg cursor-pointer"
                @click="onCloseMobileSidebar"
              >
                <UIcon name="i-lucide-x" class="size-6" />
              </button>
            </div>
            <ProjectSidebar :project="props.project" />
          </aside>
        </div>
      </Teleport>
      <div class="flex items-center gap-2">
        <button
          v-tooltip="'Dock'"
          class="lg:hidden hover:bg-gray-200 pt-1 px-1 rounded-md cursor-pointer"
          @click="onOpenMobileSidebar"
        >
          <UIcon name="i-lucide-panel-left" class="size-6" />
        </button>
        <button
          v-if="route.params.projectId"
          v-tooltip="'Dock'"
          class="hidden lg:block hover:bg-gray-200 pt-1 px-1 rounded-md cursor-pointer"
          @click="onOpenMobileSidebar"
        >
          <UIcon name="i-lucide-panel-left" class="size-6" />
        </button>
        <a href="/account/projects" class="flex gap-0 items-center">
          <nuxt-img src="/icon-256.png" class="h-4 w-4" />
          <p class="text-lg font-semibold text-black">PROJECTS</p>
        </a>
      </div>
      <div class="flex items-center gap-2">
        <NuxtLink
          v-tooltip="'Home'"
          to="/"
          class="flex items-center gap-2 p-2 rounded-md hover:bg-gray-200 duration-300 transition"
        >
          <UIcon name="i-lucide-home" class="w-6 h-6 shrink-0" />
        </NuxtLink>
        <ProjectMenu
          v-if="project"
          :project="project"
          :download-project="downloadProject"
          :web-pages="props?.webPages"
          :on-open-change-public-status-dialog="onOpenChangePublicStatusDialog"
          :open-detail="props?.openDetail"
          :open-edit="props?.openEdit"
          :on-delete="props?.onDelete"
        />
        <UserProfile :user="props.user" />
      </div>
    </header>
  </div>
</template>
<script setup lang="ts">
import type { Project } from "~/types/Projects";
import type { User } from "../types/User";
import ProjectMenu from "./projectMenu.vue";
import type { WebPage } from "~/modules/pageStream";
const props = defineProps<{
  user: User;
  project?: Project;
  downloadProject?: () => void;
  webPages?: WebPage[];
  onOpenChangePublicStatusDialog?: () => void;
  openDetail?: () => void;
  openEdit?: () => void;
  onDelete?: (arg: string) => void;
}>();
const mobileSidebarOpen = ref(false);
const route = useRoute();

const onOpenMobileSidebar = () => {
  mobileSidebarOpen.value = true;
};

const onCloseMobileSidebar = () => {
  mobileSidebarOpen.value = false;
};
</script>
