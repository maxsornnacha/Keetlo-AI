<template>
  <main v-if="form.isLogin === 'yes'" class="mx-auto max-w-[1200px] my-8 relative">
    <form
      class="lg:rounded-2xl lg:border lg:border-gray-200 lg:bg-white px-4"
      @submit.prevent="onSaveChanges"
    >
      <!-- Header -->
      <div class="lg:p-6 pb-4 lg:pb-0 lg:border-b lg:border-white/10">
        <div class="flex items-start justify-between gap-4 pb-8">
          <div>
            <h2 class="text-2xl font-semibold text-black">Profile</h2>
            <p class="text-sm text-gray-400 mt-1">
              Manage your profile settings and connected accounts.
            </p>
          </div>

          <!-- Unsaved badge (desktop) -->
          <div v-if="hasDirty" class="hidden lg:flex items-center gap-2 rounded-full bg-amber-500/10 text-amber-300 border border-amber-500/30 px-3 py-1 text-xs">
            <span class="inline-block h-2 w-2 rounded-full bg-amber-400"/> Unsaved changes
          </div>
        </div>
      </div>

      <!-- Content -->
      <div class="lg:p-6 space-y-8">
        <!-- Avatar -->
        <section class="flex flex-col lg:flex-row items-center lg:items-start gap-6">
          <div
            class="relative w-24 h-24 rounded-full overflow-hidden ring-1 ring-white/10 bg-gray-800/60"
            @dragover.prevent
            @drop.prevent="handleFileDrop"
          >
            <nuxt-img :src="avatarSrc" class="w-full h-full object-cover" />
            <!-- overlay hint -->
            <div class="absolute inset-0 bg-black/0 hover:bg-black/30 transition grid place-items-center text-xs text-black/90">
              <div class="hidden group-hover:block">Drop to upload</div>
            </div>
          </div>

          <div class="flex-1 flex flex-col items-center lg:items-start">
            <label class="block text-sm font-medium text-gray-700 mb-2">Avatar</label>
            <div class="flex items-center gap-2 w-full">
              <input ref="fileInput" type="file" accept="image/*" class="hidden" @change="handleFileChange" >
              <button
                type="button"
                class="w-full lg:w-auto h-9 px-3 rounded-lg bg-white border border-gray-200 bg-white text-black text-sm font-semibold cursor-pointer hover:bg-gray-200 active:scale-[.99]"
                @click="fileInput?.click()"
              >
                Upload
              </button>
              <button
                type="button"
                class="w-full lg:w-auto h-9 px-3 rounded-lg border border-gray-200 bg-white text-black text-sm font-semibold hover:bg-gray-200 active:scale-[.99] cursor-pointer"
                :disabled="!avatarBase64 && !form.avatarUrl"
                @click="removeAvatar"
              >
                Remove
              </button>
            </div>
            <p class="text-xs text-gray-400 mt-2">JPG, GIF, PNG or WEBP. 2MB max. You can also drop an image onto the avatar.</p>
          </div>
        </section>

        <!-- Name Inputs -->
        <section class="grid grid-cols-1 md:grid-cols-2 gap-6">
          <div>
            <label for="firstname" class="block text-sm font-medium text-gray-700 mb-2">First Name</label>
            <div class="relative">
              <input
                id="firstname"
                v-model.trim="form.firstname"
                required
                maxlength="50"
                type="text"
                placeholder="Enter your first name"
              class="px-3 py-2 w-full min-h-11 rounded-lg text-black bg-white border border-gray-200 focus:outline-none focus:border-indigo-500 placeholder:text-gray-500"
              >
            </div>
            <p v-if="!validFirst" class="mt-1 text-xs text-rose-400">Use at least 2 characters.</p>
          </div>

          <div>
            <label for="lastname" class="block text-sm font-medium text-gray-700 mb-2">Last Name</label>
            <div class="relative">
              <input
                id="lastname"
                v-model.trim="form.lastname"
                required
                maxlength="50"
                type="text"
                placeholder="Enter your last name"
              class="px-3 py-2 w-full min-h-11 rounded-lg text-black bg-white border border-gray-200 focus:outline-none focus:border-indigo-500 placeholder:text-gray-500"
              >
            </div>
            <p v-if="!validLast" class="mt-1 text-xs text-rose-400">Use at least 2 characters.</p>
          </div>
        </section>

        <!-- Email (read-only) -->
        <section>
          <label for="email" class="block text-sm font-medium text-gray-700 mb-2">Email</label>
          <div class="flex gap-2">
            <input
              id="email"
              v-model="form.email"
              type="email"
              disabled
              class="px-3 py-2 w-full h-11 rounded-lg text-black bg-gray-100 border border-gray-200 placeholder:text-gray-500"
            >
            <button
              type="button"
              class="hidden md:inline-flex py-2 px-6 flex items-center rounded-lg border border-gray-200 bg-white text-black text-sm hover:bg-gray-200 active:scale-[.99] cursor-pointer"
              @click="copy(form.email)"
            >
              Copy
            </button>
          </div>
        </section>

        <!-- Password -->
        <section class="border-t border-white/10 pt-6 space-y-4">
          <h3 class="text-lg font-medium text-black">Password</h3>
          <div class="flex flex-col lg:flex-row items-start lg:items-center justify-between gap-3">
            <p class="text-sm text-gray-400">To change your password, click the button below.</p>
            <a
              :href="`/auth/forgot-password`"
              class="flex min-w-[140px] items-center justify-center h-9 px-4 rounded-lg border border-gray-200 bg-white text-black text-sm hover:bg-gray-200 active:scale-[.99]"
            >
              Change Password
          </a>
          </div>
        </section>

        <!-- Connected Accounts -->
        <section class="border-t border-white/10 pt-6 space-y-4">
          <h3 class="text-lg font-medium text-black">Connected Accounts</h3>
          <p class="text-sm text-gray-400">Manage your third-party account connections.</p>

          <div class="space-y-3">
            <!-- GitHub -->
            <div class="flex items-center justify-between p-4 rounded-xl border border-gray-200">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-github" class="size-5 text-black" />
                <span class="text-sm font-medium text-black">GitHub</span>
              </div>
              <span
                v-if="form.isGithubConnected === 1"
                class="inline-flex items-center gap-2 rounded-full bg-emerald-500/10 text-emerald-300 border border-emerald-500/30 px-3 py-1 text-xs"
              >
                <span class="h-2 w-2 rounded-full bg-emerald-400"/> Connected
              </span>
              <span v-else class="inline-flex items-center gap-2 rounded-full bg-white/5 text-black border border-white/10 px-3 py-1 text-xs">
                <span class="h-2 w-2 rounded-full bg-gray-300"/> Disconnected
              </span>
            </div>

            <!-- Google -->
            <div class="flex items-center justify-between p-4 rounded-xl border border-gray-200">
              <div class="flex items-center gap-3">
                <UIcon name="i-lucide-at-sign" class="size-5 text-black" />
                <span class="text-sm font-medium text-black">Google</span>
              </div>
              <span
                v-if="form.isGoogleConnected === 1"
                class="inline-flex items-center gap-2 rounded-full bg-emerald-500/10 text-emerald-300 border border-emerald-500/30 px-3 py-1 text-xs"
              >
                <span class="h-2 w-2 rounded-full bg-emerald-400"/> Connected
              </span>
              <span v-else class="inline-flex items-center gap-2 rounded-full bg-white/5 text-black border border-white/10 px-3 py-1 text-xs">
                <span class="h-2 w-2 rounded-full bg-gray-300"/> Disconnected
              </span>
            </div>
          </div>
        </section>
      </div>

      <!-- Action bar -->
      <div class="lg:p-6 py-6 bg-white/20 lg:border-t lg:border-gray-200 rounded-b-2xl sticky bottom-0 backdrop-blur-sm supports-backdrop-blur:bg-white/5">
        <div class="flex items-center justify-end gap-3">
          <div v-if="hasDirty" class="inline-flex items-center gap-2 rounded-full bg-black text-white border border-black px-3 py-1 text-xs">
            <span class="h-2 w-2 rounded-full bg-white"/> Unsaved changes
          </div>

          <button
            type="submit"
            :disabled="!canSave || isSaving"
            class="w-full lg:w-auto flex min-w-[140px] items-center justify-center h-10 px-4 rounded-lg text-sm cursor-pointer
                   transition active:scale-[.99]"
            :class="canSave && !isSaving ? 'bg-indigo-500 hover:bg-indigo-600 text-white' : 'bg-indigo-300 cursor-not-allow text-white'"
          >
            <svg v-if="isSaving" class="animate-spin -ml-1 mr-2 h-4 w-4 text-black" viewBox="0 0 24 24" fill="none">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
              <path class="opacity-75" d="M4 12a8 8 0 018-8" stroke="currentColor" stroke-width="4" stroke-linecap="round"/>
            </svg>
            {{ isSaving ? 'Saving…' : 'Save Changes' }}
          </button>
        </div>
      </div>
    </form>

    <!-- Toast -->
    <transition
      enter-active-class="transition duration-200 ease-out"
      enter-from-class="opacity-0 translate-y-1"
      enter-to-class="opacity-100 translate-y-0"
      leave-active-class="transition duration-150 ease-in"
      leave-from-class="opacity-100 translate-y-0"
      leave-to-class="opacity-0 translate-y-1"
    >
      <div
        v-if="toast"
        class="fixed bottom-6 left-1/2 -translate-x-1/2 rounded-xl border border-white/10 bg-white/10 px-4 py-2 text-sm backdrop-blur-md shadow-lg"
      >
        {{ toast }}
      </div>
    </transition>
    <PageLoader v-if="loadingOnSubmit"/>
  </main>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onBeforeUnmount, reactive } from "vue";
import PageLoader from "~/components/ui/PageLoader.vue";
import type { User } from "~/types/User";
const config = useRuntimeConfig();

useSeoMeta({
  // Core
  title: 'Keetlo Profile Management – Update Name, Avatar, and Settings',
  description:
    'Edit your Keetlo profile: update name and avatar, manage connected Google/GitHub accounts, and adjust security preferences with a clear, accessible UI.',
  robots: 'noindex,nofollow',

  // Open Graph
  ogUrl: `${config.public.SITE_URL}/account`,
  ogSiteName: 'Keetlo',
  ogType: 'website',
  ogTitle: 'Keetlo Profile Management – Update Name, Avatar, and Settings',
  ogDescription:
    'Manage your profile, connected accounts, and security settings for your Keetlo workspace.',
  ogImage: `${config.public.SITE_URL}/images/og/account-profile.png`,

  // Twitter
  twitterCard: 'summary_large_image',
  twitterTitle: 'Account Settings – Keetlo',
  twitterDescription:
    'Update profile info and manage Google/GitHub connections and security settings.',
  twitterImage: `${config.public.SITE_URL}/images/og/account-profile.png`,
})


const props = defineProps<{ user: User }>();

const form = reactive({
  email: "",
  firstname: "",
  lastname: "",
  avatarUrl: null as string | null,
  limitRequests: null as number | null,
  leftRequests: null as number | null,
  subscriptionPlanName: null as string | null,
  isLogin: "no",
  isGoogleConnected: 0,
  isGithubConnected: 0,
});
const originalForm = reactive({ ...form });

const fileInput = ref<HTMLInputElement | null>(null);
const avatarBase64 = ref<string | null>(null);
const isSaving = ref(false);
const toast = ref<string | null>(null);
const { $modal }  = useNuxtApp();
const loadingOnSubmit = ref(false);

function updateForm(user: User) {
  form.email = user.email;
  form.firstname = user.firstname ? user.firstname : "";
  form.lastname = user.lastname ? user.lastname : "";
  form.avatarUrl = user.avatarUrl || null;
  form.limitRequests = user.limitRequests ?? null;
  form.leftRequests = user.leftRequests ?? null;
  form.subscriptionPlanName = user.subscriptionPlanName ?? null;
  form.isLogin = user.isLogin;
  form.isGithubConnected = user.isGithubConnected;
  form.isGoogleConnected = user.isGoogleConnected;

  Object.assign(originalForm, JSON.parse(JSON.stringify(form)));
}
updateForm(props.user);

watch(() => props.user, (u) => u && updateForm(u), { deep: true, immediate: true });

const avatarSrc = computed(() => {
  if (avatarBase64.value) return avatarBase64.value;
  if (!form.avatarUrl) return "/images/accounts/account.jpg";
  try {
    new URL(form.avatarUrl);
    return form.avatarUrl;
  } catch {
    return `${config.public.NUXT_PUBLIC_API_BASE}${form.avatarUrl}`;
  }
});

/** Validation */
const validFirst = computed(() => form.firstname.trim().length >= 2);
const validLast  = computed(() => form.lastname.trim().length >= 2);

const hasDirty = computed(() =>
  form.firstname !== originalForm.firstname ||
  form.lastname !== originalForm.lastname ||
  form.avatarUrl !== originalForm.avatarUrl ||
  avatarBase64.value !== null
);
const canSave = computed(() => hasDirty.value && validFirst.value && validLast.value && !isSaving.value);

/** Uploads */
function handleFileChange(e: Event) {
  const file = (e.target as HTMLInputElement).files?.[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = () => (avatarBase64.value = reader.result as string);
  reader.readAsDataURL(file);
}
function handleFileDrop(e: DragEvent) {
  const file = e.dataTransfer?.files?.[0];
  if (!file) return;
  const reader = new FileReader();
  reader.onload = () => (avatarBase64.value = reader.result as string);
  reader.readAsDataURL(file);
}
function removeAvatar() {
  avatarBase64.value = null;
  form.avatarUrl = null;
}

/** Save */
async function onSaveChanges() {
  if (!canSave.value) return;
    const ok = await $modal.confirm({
    title: 'Confirm',
    html: '<p>Would you like to update profile?</p>',
    confirmText: 'Update',
    cancelText: 'Cancel',
    variant: 'default'
  })
  if (!ok){return;}
  try {
    isSaving.value = true;

    const payload = {
      firstname: form.firstname.trim(),
      lastname: form.lastname.trim(),
      avatarUrl: avatarBase64.value ? avatarBase64.value : form.avatarUrl,
    };
    await api.post(`${config.public.NUXT_PUBLIC_API_BASE}/user/update`, payload);

    showToast("Profile updated successfully!");
    setTimeout(()=>{
      window.location.reload();
    }, 1000)
  } catch (err) {
    console.error(err);
    showToast("Failed to update profile");
  } finally {
    isSaving.value = false;
  }
}

/** UX helpers */
function showToast(message: string) {
  toast.value = message;
  setTimeout(() => (toast.value = null), 1500);
}
function copy(text: string) {
  navigator?.clipboard?.writeText?.(text).then(() => showToast("Email copied"));
}

/** Ctrl/Cmd+S to save */
function onKey(e: KeyboardEvent) {
  if ((e.metaKey || e.ctrlKey) && e.key.toLowerCase() === "s") {
    e.preventDefault();
    onSaveChanges();
  }
}
onMounted(() => window.addEventListener("keydown", onKey));
onBeforeUnmount(() => window.removeEventListener("keydown", onKey));
</script>
