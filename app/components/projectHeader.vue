<template>
  <div class="border-b border-solid border-[#30363d]">
    <header
      class="mx-auto max-w-[1400px] w-full flex items-center justify-between whitespace-nowrap px-4 py-4"
    >
    <Teleport to="body">
          <div
        v-if="mobileSidebarOpen"
        class="fixed inset-0 bg-black/70 z-40"
        @click="onCloseMobileSidebar"
      />
      <div class="flex items-center gap-2">
      <aside
        class="h-screen bg-[#0D1117] opacity-90 border border-slate-50/20 fixed inset-y-0 left-0 z-50 transform transition-transform duration-300"
        :class="mobileSidebarOpen ? 'translate-x-0' : '-translate-x-full'"
      >
        <div class="bg-[#0D1117] border border-[#30363d] flex justify-between items-center p-2">
          <div>
            <h2 class="text-base">Total Projects</h2>
          </div>
          <button class="pt-1 px-1 hover:bg-slate-50/20 border border-slate-50/20 rounded-lg cursor-pointer" @click="onCloseMobileSidebar">
            <UIcon name="i-lucide-x" class="size-6"/>
          </button>
        </div>
        <ProjectSidebar :project="props.project"/>
      </aside>
      </div>
       </Teleport>
       <div class="flex items-center gap-2">
         <button
           class="lg:hidden hover:bg-slate-50/20 pt-1 px-1 rounded-md cursor-pointer"
           @click="onOpenMobileSidebar"
          >
            <UIcon name="i-lucide-menu" class="size-6" />
          </button>
          <button
          v-if="route.params.projectId"
           class="hidden lg:block hover:bg-slate-50/20 pt-1 px-1 rounded-md cursor-pointer"
           @click="onOpenMobileSidebar"
          >
            <UIcon name="i-lucide-menu" class="size-6" />
          </button>
        <a href="/account/projects" class="flex gap-2 items-center">
        <div class="flex items-center gap-3 bg-white p-2 rounded-md">
          <nuxt-img src="/logo.png" class="h-5 w-5" />
        </div>
        <p class="text-xl font-semibold">Projects</p>
        </a>
      </div>
      <div class="flex items-center gap-4">
      <a href="/" class="flex items-center gap-2 border border-slate-50/20 py-1 px-3 rounded-md hover:bg-slate-50/20 duration-300 transition">
          <UIcon name="i-lucide-home" class="w-6 h-6 shrink-0 mt-1" />
          <span>Home</span>
      </a> 
      <ProjectMenu
       v-if="project"
       :project="project"
       :download-project="downloadProject"
       :web-pages="props?.webPages"
       :on-open-change-public-status-dialog = "onOpenChangePublicStatusDialog"
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
  project?:Project;
  downloadProject?: () => void;
  webPages?: WebPage[];
  onOpenChangePublicStatusDialog?: () => void;
  openDetail?: () => void;
  openEdit?: () => void;
  onDelete?: (arg: string)=>void;
}>();
const mobileSidebarOpen = ref(false);
const route = useRoute();


const onOpenMobileSidebar = () => {
  mobileSidebarOpen.value = true;
}

const onCloseMobileSidebar = () => {
  mobileSidebarOpen.value = false;
}
</script>
