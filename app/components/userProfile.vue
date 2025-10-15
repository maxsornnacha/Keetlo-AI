<template>
  <div class="relative">
    <!-- Trigger (Avatar or Guest Buttons) -->
    <div class="flex justify-end">
      <!-- Guest View -->
      <div v-if="props.user.isLogin === 'no'" class="flex items-center gap-4">
        <a
          class="text-sm font-medium text-gray-300 hover:text-white transition-colors"
          :href="loginUrl"
        >
          Login
        </a>
        <a :href="registerUrl">
          <button
            class="flex min-w-[84px] items-center justify-center rounded-md h-10 px-5 bg-indigo-600 text-white text-sm font-semibold hover:bg-indigo-700 transition"
          >
            <span>Sign Up</span>
          </button>
        </a>
      </div>

      <!-- Logged-in Avatar -->
      <div v-else-if="props.user.isLogin === 'yes'" ref="profileRef" class="flex gap-2 items-center">
        <button
          class="cursor-pointer rounded-full ring-1 ring-transparent hover:ring-slate-50/20 transition"
          @click="opened = !opened"
        >
          <nuxt-img
            :src="
              !props.user.avatarUrl
                ? '/images/accounts/account.jpg'
                : isValidUrl(props.user.avatarUrl)
                ? props.user.avatarUrl
                : `${config.public.NUXT_PUBLIC_API_BASE}${props.user.avatarUrl}`
            "
            class="w-9 h-9 rounded-full object-cover"
            alt="Profile image"
            title="Profile image"
          />
        </button>
      </div>
    </div>

    <!-- Dropdown -->
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
        class="absolute right-0 top-12 min-w-[300px] bg-[#0D1117] backdrop-blur-xl border border-slate-700 rounded-xl shadow-2xl z-50 overflow-hidden"
      >
        <!-- User Info -->
        <div class="flex items-center gap-3 p-4">
          <nuxt-img
            :src="
              !props.user.avatarUrl
                ? '/images/accounts/account.jpg'
                : isValidUrl(props.user.avatarUrl)
                ? props.user.avatarUrl
                : `${config.public.NUXT_PUBLIC_API_BASE}${props.user.avatarUrl}`
            "
            class="w-10 h-10 rounded-full object-cover border border-slate-50/20"
          />
          <div>
            <p class="text-white font-semibold truncate">
              {{ props.user.firstname }} {{ props.user.lastname }}
            </p>
            <p class="text-gray-400 text-sm truncate max-w-[180px]">{{ props.user.email }}</p>
          </div>
        </div>

        <div class="border-b border-slate-700"/>
        <!-- Subscription Info -->
        <div class="p-4 space-y-3 bg-[#0d1117]">
          <div class="text-center">
            <p class="text-white font-medium">{{ props.user.subscriptionPlanName }}</p>
          </div>

          <div v-if="props.user.limitRequests !== 0" class="space-y-2">
            <div class="flex justify-between items-center text-gray-300 text-sm">
              <span>Requests</span>
              <span>
                <span class="text-yellow-500 font-semibold">{{ props.user.leftRequests }}</span>
                / {{ props.user.limitRequests }} Left
              </span>
            </div>

            <div v-if="props.user.leftRequests && props.user.limitRequests">
              <ProgressBar
                :used="props.user.leftRequests"
                :total="props.user.limitRequests"
              />
            </div>

            <div class="text-center text-gray-400 text-xs">
              Daily credits reset at
              <span class="text-yellow-500 font-medium">midnight Thailand</span>
            </div>
          </div>

          <div v-else class="flex justify-center py-2">
            <UIcon
              name="i-lucide-crown"
              class="text-yellow-500 w-8 h-8 animate-bounce"
            />
          </div>
        </div>
        <div class="border-t border-slate-700"/>

        <nav class="p-2 space-y-4">
          <!-- Account Section -->
          <div>
            <p class="text-gray-400 text-xs px-2 uppercase tracking-wide mb-1">Account</p>
            <template v-for="item in accountMenu" :key="item.label">
              <component
                :is="item.type === 'link' ? 'a' : 'button'"
                :href="item.type === 'link' ? item.href : undefined"
                class="flex items-center gap-2 w-full text-left px-3 py-2 rounded-md text-sm text-gray-300 hover:bg-slate-800 transition"
                @click="item.type === 'action' ? item.action?.() : null"
              >
                <UIcon :name="item.icon" class="w-5 h-5 shrink-0" />
                <span class="truncate">{{ item.label }}</span>
              </component>
            </template>
          </div>

          <!-- Management Section -->
          <div>
            <p class="text-gray-400 text-xs px-2 uppercase tracking-wide mb-1">Management</p>
            <template v-for="item in manageMenu" :key="item.label">
              <component
                :is="item.type === 'link' ? 'a' : 'button'"
                :href="item.type === 'link' ? item.href : undefined"
                class="flex items-center gap-2 w-full text-left px-3 py-2 rounded-md text-sm text-gray-300 hover:bg-slate-800 transition"
                @click="item.type === 'action' ? item.action?.() : null"
              >
                <UIcon :name="item.icon" class="w-5 h-5 shrink-0" />
                <span class="truncate">{{ item.label }}</span>
              </component>
            </template>
          </div>

          <!-- Logout -->
          <div>
            <template v-for="item in actionMenu" :key="item.label">
              <button
                class="cursor-pointer flex items-center gap-2 w-full text-left px-3 py-2 rounded-md text-sm text-gray-300 hover:bg-red-600/10 hover:text-red-500 transition"
                @click="item.action?.()"
              >
                <UIcon :name="item.icon" class="w-5 h-5 shrink-0" />
                <span class="truncate">{{ item.label }}</span>
              </button>
            </template>
          </div>
        </nav>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
import axios from "axios";
import type { User } from "../types/User";
import ProgressBar from "./ui/ProgressBar.vue";

const { $modal } = useNuxtApp();
const props = defineProps<{ user: User }>();
const config = useRuntimeConfig();
const route = useRoute();
const opened = ref(false);
const menuRef = ref<HTMLElement | null>(null);
const profileRef = ref<HTMLElement | null>(null);
const nextQuery = ref(route.query.next);
const currentUrl = ref("");

onMounted(() => {
  currentUrl.value = window.location.href;
});

const loginUrl = computed(() => `/auth/login?next=${nextQuery.value || currentUrl.value}`);
const registerUrl = computed(() => `/auth/register?next=${nextQuery.value || currentUrl.value}`);


interface MenuItem {
  type: 'link' | 'action';
  label: string;
  icon: string;
  href?: string;
  action?: () => void; 
}

// Menu Data
const accountMenu : MenuItem[] = [
  { type: "link", label: "Profile", href: "/account", icon: "i-lucide-user" },
];

const manageMenu : MenuItem[] = [
  { type: "link", label: "Projects", href: "/account/projects", icon: "i-lucide-folder-open" },
  { type: "link", label: "Favorite Projects", href: "/account/favorites", icon: "i-lucide-heart" },
  // { type: "link", label: "Public projects", href: `/public/account/test-profile`, icon: "i-lucide-globe" },
];

const actionMenu : MenuItem[] = [
  { type: "action", label: "Logout", icon: "i-lucide-log-out", action: () => onLogout() },
];

const onLogout = async () => {
   const ok = await $modal.confirm({
    title: 'Confirm',
    html: '<p>Would you like to logout of the system?</p>',
    confirmText: 'Logout',
    cancelText: 'Cancel',
    variant: 'default'
  })
  if (!ok){return;}
  try{
    await api.post(`${config.public.NUXT_PUBLIC_API_BASE}/auth/logout`);
      const successOk =   await $modal.alert({ 
      title: 'Success', 
      html: `<p>Used Logged out successfully!</p>`, 
      variant: 'success' 
    });
    if(successOk){
     window.location.reload();
    }
    setTimeout(()=>{
     window.location.reload();
    }, 1500)
    opened.value = false;
  } catch (error) {
      if (axios.isAxiosError(error)) {
      await $modal.alert({ 
      title: 'Error', 
      html: `<p>${error?.response?.data?.message || "An error occurred"}</p>`, 
      variant: 'danger' 
    });
    } else {
         await $modal.alert({ 
      title: 'Error', 
      html: `<p>An unexpected error occurred.</p>`, 
      variant: 'danger' 
    });
    }
  }
  
};

const handleClickOutside = (event: MouseEvent) => {
  if (
    menuRef.value &&
    !menuRef.value.contains(event.target as Node) &&
    profileRef.value &&
    !profileRef.value.contains(event.target as Node)
  ) {
    opened.value = false;
  }
};

onMounted(() => {
  document.addEventListener("click", handleClickOutside);
});

onBeforeUnmount(() => {
  document.removeEventListener("click", handleClickOutside);
});
</script>
