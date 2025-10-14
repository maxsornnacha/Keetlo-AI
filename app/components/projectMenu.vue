<template>
  <div class="relative">
    <!-- Trigger Button -->
    <button
      v-if="props.project"
      ref="projectRef"
      v-tooltip="'Project menu'"
      class="cursor-pointer flex items-center gap-2 hover:text-gray-300"
      @click="onOpen"
    >
      <UIcon name="i-lucide-ellipsis" class="size-7" />
    </button>

    <!-- Dropdown Menu -->
         <transition
      enter-active-class="transition ease-out duration-150"
      enter-from-class="opacity-0 scale-95"
      enter-to-class="opacity-100 scale-100"
      leave-active-class="transition ease-in duration-100"
      leave-from-class="opacity-100 scale-100"
      leave-to-class="opacity-0 scale-95"
    >
    <div
      v-if="opened"
      ref="menuRef"
      class="absolute right-0 top-12 w-[280px] bg-[#0D1117] backdrop-blur-xl border border-slate-700 rounded-xl shadow-2xl z-50 overflow-hidden transition-all"
    >
      <!-- Project Header -->
      <div class="flex flex-col p-4 border-b border-slate-700">
        <h3 class="text-white font-semibold truncate">{{ props.project?.title }}</h3>
        <p class="text-xs text-gray-400 truncate">{{ props.project?.description || 'No description' }}</p>
      </div>

      <!-- Action List -->
      <ul class="divide-y divide-slate-800">
        <li
          v-for="(item, index) in menuItems"
          :key="index"
          class="flex items-center gap-3 px-4 py-3 text-sm text-gray-300 hover:bg-[#1e2632] hover:text-white cursor-pointer transition"
          @click="item.onClick()"
        >
          <UIcon
            :name="item.icon" class="w-4 h-4 text-gray-400" 
            :class="item.label === 'Public'? project?.isPublic === 1 ? 'text-green-500 animate-pulse' : 'text-red-500': ''"
          />
          <span>{{ item.label }}</span>
        </li>
      </ul>
    </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import type { WebPage } from '~/modules/pageStream';
import type { Project } from '~/types/Projects'

const props = defineProps<{ 
    project?: Project;
    downloadProject?: () => void;
    webPages?: WebPage[];
    onOpenChangePublicStatusDialog?: () => void;
    openDetail?: ()=>void;
    openEdit?: ()=>void;
    onDelete?: (arg: string)=>void;
 }>()

const opened = ref(false)
const menuRef = ref<HTMLElement | null>(null)
const projectRef = ref<HTMLElement | null>(null)

// Example menu items array (can come from API too)
const menuItems = [
  {
    label: 'Detail',
    icon: 'i-lucide-folder-open',
    onClick: () => {
        if(props.openDetail)
        props.openDetail();
        onClose();
    }
  },
  {
    label: 'Edit Project',
    icon: 'i-lucide-edit-3',
    onClick: () => {
        if(props.openEdit)
        props.openEdit();
        onClose();
    }
  },
  {
    label: 'Public',
    icon: 'i-lucide-globe',
    onClick: () => {
        if(props.onOpenChangePublicStatusDialog)
        props.onOpenChangePublicStatusDialog();
        onClose();
    }
  },
    {
    label: 'Download',
    icon: 'i-lucide-download',
    onClick: () => {
    if(props.downloadProject)  
        props.downloadProject();
        onClose();
    }
  },
  {
    label: 'Delete',
    icon: 'i-lucide-trash-2',
    onClick: () => {
      if(props.onDelete && props?.project?.projectId)
      props.onDelete(props?.project?.projectId);
      onClose();
    }
  }
]

const onOpen = () => {
  opened.value = !opened.value
}


const onClose = () => {
    opened.value = false
}

const handleClickOutside = (event: MouseEvent) => {
  if (
    menuRef.value &&
    !menuRef.value.contains(event.target as Node) &&
    projectRef.value &&
    !projectRef.value.contains(event.target as Node)
  ) {
    opened.value = false
  }
}

onMounted(() => {
  document.addEventListener('click', handleClickOutside)
})

onBeforeUnmount(() => {
  document.removeEventListener('click', handleClickOutside)
})
</script>
