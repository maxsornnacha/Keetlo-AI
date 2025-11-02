<template>
  <div class="relative">
    <!-- Trigger (Avatar or Guest Buttons) -->
    <div class="flex justify-end">
      <!-- Guest View -->
      <div v-if="props.user.isLogin === 'no'" class="flex items-center gap-4">
        <a
          class="font-medium hover:text-gray-500 transition-colors"
          :href="loginUrl"
        >
          Login
        </a>
        <a :href="registerUrl">
          <button
            class="text-white cursor-pointer flex min-w-[84px] items-center justify-center rounded-md h-10 px-2 bg-indigo-500 hover:bg-indigo-600 transition"
          >
            <span>Sign Up</span>
          </button>
        </a>
      </div>

      <!-- Logged-in Avatar -->
      <div v-else-if="props.user.isLogin === 'yes'" ref="profileRef" class="flex gap-2 items-center">
        <button
          class="relative cursor-pointer rounded-full ring-1 ring-transparent hover:ring-gray-50/20 transition"
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
        class="absolute right-0 top-12 max-w-[90vw] min-w-[300px] bg-white backdrop-blur-xl border border-gray-200 rounded-xl shadow-2xl z-50 overflow-hidden"
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
            class="w-10 h-10 rounded-full object-cover border border-gray-50/20"
          />
          <div>
            <p class="text-black font-semibold truncate">
              {{ (props.user.firstname+" "+props.user.lastname).slice(0,22) }}{{(props.user.firstname+" "+props.user.lastname).length > 22 ? '...' : ''  }}
            </p>
            <p class="text-black text-sm truncate max-w-[180px]">{{ props.user.email }}</p>
          </div>
        </div>

        <div class="border-b border-gray-200"/>
        <!-- Subscription Info -->
        <div class="p-4 space-y-3">
          <div v-if="props.user.limitRequests !== 0 && props.user.leftRequests !== 0" class="space-y-2">
            <div class="text-center">
            <p class="text-black font-medium">{{ props.user.subscriptionPlanName }}</p>
          </div>
            <div class="flex justify-between items-center text-black text-sm">
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

            <div class="text-center text-black text-xs">
              Daily credits reset at
              <span class="text-yellow-500 font-medium">midnight Thailand</span>
            </div>
          </div>

          <div v-else class="flex items-center gap-2">
            <UIcon
              name="i-lucide-crown"
              class="text-yellow-500 size-6"
            />
            <div class="text-center">
            <p class="text-black">{{ props.user.subscriptionPlanName }}</p>
          </div>
          </div>
        </div>
        <div class="border-t border-gray-200"/>

        <nav class="p-2 space-y-4">
          <!-- Account Section -->
          <div>
            <p class="text-black text-xs px-2 uppercase tracking-wide mb-1">Account</p>
            <template v-for="item in accountMenu" :key="item.label">
              <component
                :is="item.type === 'link' ? 'a' : 'button'"
                :href="item.type === 'link' ? item.href : undefined"
                class="flex items-center gap-2 w-full text-left px-3 py-2 rounded-md text-sm text-black hover:bg-gray-100 transition"
                @click="item.type === 'action' ? item.action?.() : null"
              >
                <UIcon :name="item.icon" class="w-5 h-5 shrink-0" />
                <span class="truncate">{{ item.label }}</span>
              </component>
            </template>
          </div>

          <!-- Management Section -->
          <div>
            <p class="text-black text-xs px-2 uppercase tracking-wide mb-1">Management</p>
            <template v-for="item in manageMenu" :key="item.label">
              <component
                :is="item.type === 'link' ? 'a' : 'button'"
                :href="item.type === 'link' ? item.href : undefined"
                class="flex items-center gap-2 w-full text-left px-3 py-2 rounded-md text-sm text-black hover:bg-gray-100 transition"
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
                class="cursor-pointer flex items-center gap-2 w-full text-left px-3 py-2 rounded-md text-sm hover:bg-gray-100 text-red-500 transition"
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

const loginUrl = computed(() => `/auth/login?next=${nextQuery.value || route.fullPath}`);
const registerUrl = computed(() => `/auth/register?next=${nextQuery.value || route.fullPath}`);


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
  { type: "link", label: "My Projects", href: "/account/projects", icon: "i-lucide-folder-open" },
  { type: "link", label: "Favorite Projects", href: "/account/favorites", icon: "i-lucide-heart" },
  // { type: "link", label: "Public projects", href: `/public/account/test-profile`, icon: "i-lucide-globe" },
  { type: "link", label: "Orders", href: "/account/payment", icon: "i-lucide-receipt-text" },
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
