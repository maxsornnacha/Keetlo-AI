<template>
<div v-if="!loadingAuthAccessibility">
  <div v-if="layout1">
    <Header :user="user"/>
    <div class="min-h-[70vh]">
      <NuxtPage :user="user"/>
    </div>
    <Footer />
  </div>
  <div v-else-if="layout2">
    <Header :user="user"/>
    <div class="min-h-[70vh]">
      <NuxtPage :user="user"/>
    </div>
  </div>
  <div v-else-if="layout3">
    <NuxtPage :user="user"/>
  </div>
  <div v-else>
    <Header :user="user"/>
    <div class="container mx-auto px-4 sm:px-6 lg:px-8 py-8 lg:py-16 min-h-[70vh]">
      <NuxtPage :user="user"/>
    </div>
    <Footer/>
  </div>
   <ModalRoot/>
</div>
</template>
<script setup lang="ts">
import { ref, onMounted, watch } from "vue";
import { useRoute } from "vue-router";
import { useUserStore } from "#imports";
import { getToken } from "@/utils/auth";
import type { User } from './types/User';
import ModalRoot from "./components/modal/ModalRoot.vue";

const route = useRoute();
const layout1 = route.path === "/" || route.path.includes("/account/favorites") ;
const layout2 = route.fullPath.includes("/auth") ;
const layout3 =
  route.path.includes("/account/projects") ||
  route.path === "/oauth/loading" ||
  route.path === "/payment/success" ||
  route.path === "/payment/cancel" ||
  route.path.includes("/public/projects/preview/");
// User Store
const userStore = useUserStore();
const user = ref<User>({
  email: "",
  firstname: "",
  lastname: "",
  avatarUrl: null,
  isLogin: "loading",
  limitRequests: 100,
  leftRequests: 100,
  subscriptionPlanName: "",
  isGithubConnected: 0,
  isGoogleConnected: 0,
});

// Loading flag
const loadingAuthAccessibility = ref(true);

// Function to sync local user ref with store
const syncUser = () => {
  if (userStore.user) {
    user.value = {
      email: userStore.user.email,
      firstname: userStore.user.firstname,
      lastname: userStore.user.lastname,
      avatarUrl: userStore.user.avatarUrl,
      limitRequests: userStore.user.limitRequests,
      leftRequests: userStore.user.leftRequests,
      subscriptionPlanName: userStore.user.subscriptionPlanName,
      isGithubConnected: userStore.user.isGithubConnected,
      isGoogleConnected: userStore.user.isGoogleConnected,
      isLogin: "yes",
    };
  } else {
    user.value.isLogin = "no";
  }
};

onMounted(async () => {
  await userStore.initializeUser();
  syncUser();

  // Middleware checks
  const inaccessiblePathsAfterLoggedIn = () => {
    const inaccessiblePaths = ["/auth/login", "/auth/register"];
    if (getToken() && inaccessiblePaths.includes(route.path)) {
      window.location.href = "/";
    }
  };
  const inaccessiblePathsAfterLoggedOut = () => {
    const accountBasePath = "/account";
      if (!getToken() && route.path.startsWith(accountBasePath)) {
      window.location.href = `/auth/login?next=${window.location.href}`;
    }
  };
  inaccessiblePathsAfterLoggedIn();
  inaccessiblePathsAfterLoggedOut();

  loadingAuthAccessibility.value = false;
});

watch(
  () => userStore.user,
  () => {
    syncUser();
  },
  { deep: true }
);
</script>

