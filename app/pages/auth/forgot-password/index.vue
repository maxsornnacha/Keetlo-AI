<template>
  <main class="relative lg:min-h-screen text-black">
    <!-- No token: request email -->
    <div v-if="!token" class="flex lg:gap-16 xl:gap-32 lg:min-h-screen lg:items-center lg:justify-center py-16 lg:py-0 px-4">
          <div
        class="hidden min-w-[500px] flex-0 lg:flex flex-col items-center justify-center gap-8"
      >
        <h2
          class="text-center font-semibold tracking-tight leading-tight text-4xl xl:text-5xl uppercase"
        >
          Build Your Dream
          <span class="relative inline-block">
            <span
              class="bg-gradient-to-r from-indigo-500 via-sky-500 to-cyan-400 bg-clip-text text-transparent"
            >
              UX/UI
            </span>
            <span
              aria-hidden="true"
              class="absolute -bottom-1 left-0 right-0 h-[3px] rounded-full bg-gradient-to-r from-indigo-500/70 via-sky-500/70 to-cyan-400/70 blur-[1px]"
            />
          </span>
          With Me
        </h2>
        <nuxt-img
          src="/images/auth/login.png"
          class="lg:w-[380px] lg:h-[380px] xl:w-[450px] xl:h-[450px]"
          alt="Login Image"
        />
      </div>
      <div
        v-if="!sendEmailSuccess"
        class="w-full lg:max-w-md"
      >
        <div
          class="relative overflow-hidden lg:rounded-2xl lg:border lg:border-gray-200 lg:bg-white"
        >
          <!-- Accent line -->
          <div class="absolute inset-x-0 -top-px h-px bg-gradient-to-r from-transparent via-white/40 to-transparent" />

          <div class="p-6 sm:p-8">
            <div class="flex flex-col items-center text-center gap-3">
              <div class="inline-flex size-12 items-center justify-center rounded-full bg-indigo-500 ring-1 ring-indigo-400/30">
                <UIcon name="i-lucide-key-round" class="size-6 text-white" />
              </div>
              <h1 class="text-2xl font-semibold">
                Reset Password
              </h1>
              <p class="text-sm text-gray-400 max-w-sm">
                Enter your email and we’ll send you a secure link to reset your password.
              </p>
            </div>

            <!-- Form -->
            <form class="mt-8 space-y-5" @submit.prevent="onSubmit">
              <!-- Email -->
              <div class="space-y-2">
                <label for="email" class="block text-sm font-medium text-gray-700">Email address</label>
                <div class="relative">
                  <UIcon name="i-lucide-at-sign" class="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 size-5 text-gray-700" />
                  <input
                    id="email"
                    v-model="form.email"
                    autocomplete="email"
                    name="email"
                    placeholder="you@example.com"
                    required
                    type="email"
                    class="block w-full rounded-xl bg-white border border-gray-200 pl-10 pr-3 py-3 text-black placeholder:text-black/50
                           focus:outline-none focus:border-indigo-500 transition"
                  >
                </div>
                <p class="text-xs text-gray-400">
                  We’ll only use this to send your reset link.
                </p>
              </div>

              <!-- Error -->
              <transition
                enter-active-class="transition duration-200 ease-out"
                enter-from-class="opacity-0 -translate-y-1"
                enter-to-class="opacity-100 translate-y-0"
                leave-active-class="transition duration-150 ease-in"
                leave-from-class="opacity-100 translate-y-0"
                leave-to-class="opacity-0 -translate-y-1"
              >
                <div v-if="errorMessage" class="rounded-lg border border-rose-500/30 bg-rose-500/10 px-3 py-2 text-sm text-rose-500">
                  {{ errorMessage }}
                </div>
              </transition>

              <!-- Submit -->
              <button
                type="submit"
                class="cursor-pointer group relative flex w-full items-center justify-center gap-2 rounded-xl h-12 px-4
                       text-white transition
                       focus:outline-none focus:ring-2 focus:ring-blue-400/60
                       disabled:opacity-60 disabled:cursor-not-allowed
                       bg-indigo-500 hover:bg-indigo-600"
                :disabled="loadingSubmit"
              >
                <span v-if="loadingSubmit" class="inline-flex items-center gap-2">
                  <UIcon name="i-lucide-loader-circle" class="size-5 animate-spin" />
                  Sending…
                </span>
                <span v-else class="inline-flex items-center gap-2">
                  <UIcon name="i-lucide-mail" class="size-5" />
                  Send Reset Link
                </span>
                <!-- Shine -->
                <span
class="pointer-events-none absolute inset-0 -translate-x-full bg-gradient-to-r from-transparent via-white/20 to-transparent
                             transition-transform duration-1000 group-hover:translate-x-full" />
              </button>

              <div class="text-center text-sm text-gray-400">
                Remembered your password?
                <a href="/auth/login" class="text-gray-700 underline hover:text-indigo-500">Login</a>
              </div>
            </form>
          </div>
        </div>
      </div>

      <!-- Email sent state -->
      <div v-else class="w-full lg:max-w-md">
        <div class="overflow-hidden lg:rounded-2xl lg:border lg:border-emerald-400/20 lg:bg-emerald-400/10 lg:backdrop-blur-md lg:p-6 sm:p-8 lg:ring-1 lg:ring-emerald-400/30">
          <div class=" px-4 lg:px-0 flex items-start gap-4">
            <div class="my-1 p-2 inline-flex size-10 items-center justify-center rounded-lg bg-emerald-500 ring-1 ring-emerald-400/40">
              <UIcon name="i-lucide-check-circle-2" class="size-6 text-white" />
            </div>
            <div>
              <h2 class="text-xl font-bold text-emerald-500">Check your inbox</h2>
              <p class="mt-1 text-gray-700">
                We’ve sent a password reset link to <span class="text-black font-medium">{{ form.email }}</span>.
                It expires shortly—follow the instructions to create a new password.
              </p>
            </div>
          </div>
          <EmailSuccessSent class="mt-6" />
        </div>
      </div>
    </div>

    <!-- With token: validate + change password -->
    <div v-else class="px-4 py-16 sm:py-24">
      <!-- Loading check -->
      <div v-if="loadingTokenValid" class="mx-auto grid place-items-center">
        <UIcon name="i-lucide-loader-circle" class="size-12 text-indigo-400 animate-spin" />
        <p class="mt-3 text-sm text-gray-700">Validating your reset link…</p>
      </div>

      <!-- Valid -->
       <div v-else-if="isTokenValid" class="flex lg:gap-16 xl:gap-32 lg:items-center lg:justify-center py-16 lg:py-0">
              <div
        class="hidden min-w-[500px] flex-0 lg:flex flex-col items-center justify-center gap-8"
      >
        <h2
          class="text-center font-semibold tracking-tight leading-tight text-4xl xl:text-5xl uppercase"
        >
          Build Your Dream
          <span class="relative inline-block">
            <span
              class="bg-gradient-to-r from-indigo-500 via-sky-500 to-cyan-400 bg-clip-text text-transparent"
            >
              UX/UI
            </span>
            <span
              aria-hidden="true"
              class="absolute -bottom-1 left-0 right-0 h-[3px] rounded-full bg-gradient-to-r from-indigo-500/70 via-sky-500/70 to-cyan-400/70 blur-[1px]"
            />
          </span>
          With Me
        </h2>
        <nuxt-img
          src="/images/auth/login.png"
          class="lg:w-[380px] lg:h-[380px] xl:w-[450px] xl:h-[450px]"
          alt="Login Image"
        />
      </div>
        <div class="lg:mx-auto overflow-hidden lg:rounded-2xl lg:border lg:border-gray-200 lg:bg-white">
          <div class="sm:p-8">
            <div class="hidden lg:block mb-6 flex items-center gap-3">
              <div class="inline-flex size-10 items-center justify-center rounded-lg bg-indigo-500 ring-1 ring-indigo-400/30">
                <UIcon name="i-lucide-lock" class="size-5 text-white" />
              </div>
              <div>
                <h2 class="text-xl font-bold">Create a new password</h2>
                <p class="text-sm text-gray-400">Choose something strong and unique.</p>
              </div>
            </div>
            <ChangePassword />
          </div>
        </div>
      </div>

      <!-- Invalid -->
      <div v-else class="mx-auto w-full max-w-lg">
        <div class="overflow-hidden lg:rounded-2xl lg:border lg:border-rose-400/20 lg:bg-rose-400/10 lg:p-4">
          <div class="flex items-start gap-4">
            <div class="mt-1 inline-flex size-10 items-center justify-center rounded-lg bg-rose-500 ring-1 ring-rose-400/40">
              <UIcon name="i-lucide-alert-octagon" class="size-6 text-white" />
            </div>
            <div>
              <h2 class="text-xl font-bold text-rose-500">This reset link is invalid or expired</h2>
              <p class="mt-1 text-gray-400">
                Please request a new password reset link and try again.
              </p>
              <a href="/auth/forgot-password" class="mt-4 inline-flex items-center gap-2 rounded-lg border border-gray-200 bg-white px-3 py-2 text-sm hover:bg-gray-200">
                <UIcon name="i-lucide-mail" class="size-4" />
                Request a new link
              </a>
            </div>
          </div>
          <InvalidToken class="mt-6" />
        </div>
      </div>
    </div>
  </main>
</template>
<script setup lang="ts">
import axios from "axios";
import ChangePassword from "~/components/changePassword.vue";
import EmailSuccessSent from "~/components/emailSuccessSent.vue";
import InvalidToken from "~/components/invalidToken.vue";
const config = useRuntimeConfig();

useSeoMeta({
  // Core (auth flows should not be indexed)
  title:'Create a New Password – Keetlo',
  description: 'Enter a strong new password to secure your Keetlo account. This link is single-use and time-limited for your protection. After verification, you’ll set a new password and regain access to your projects, history, and settings.',
  robots: 'noindex,nofollow',

  // Open Graph
  ogUrl: `${config.public.SITE_URL}/auth/forgot-password`,
  ogSiteName: 'Keetlo',
  ogType: 'website',
  ogTitle: 'Reset Your Password – Keetlo',
  ogDescription: 'We’ll email you a secure reset link so you can quickly get back to building.',
  ogImage:`${config.public.SITE_URL}/images/og/reset-password.png`,

  // Twitter
  twitterCard: 'summary_large_image',
  twitterTitle: 'Reset Your Password – Keetlo',
  twitterDescription: 'Request a secure password reset link for your Keetlo account.',
  twitterImage: `${config.public.SITE_URL}/images/og/reset-password.png`
})

const route = useRoute();
const loadingSubmit = ref(false);
const token = computed(() => route.query.token);
const errorMessage = ref<string | null>(null);
const form = reactive({
  email: "",
});
const sendEmailSuccess = ref(false);
const loadingTokenValid = ref(true);
const isTokenValid = ref(false);

const onSubmit = async () => {
  try {
    loadingSubmit.value = true;
    const payload = {
      email: form.email,
    };
    await api.post(
      `${config.public.NUXT_PUBLIC_API_BASE}/auth/forgot-password`,
      payload
    );
    errorMessage.value = null;
    sendEmailSuccess.value = true;
    loadingSubmit.value = false;
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      errorMessage.value = toErrorMessage(error?.response?.data?.message);
      loadingSubmit.value = false;
    }
  }
};

watch(
  token,
  async (value) => {
    if (value) {
      try {
        const payload = {
          token: value,
        };
        await api.post(
          `${config.public.NUXT_PUBLIC_API_BASE}/auth/reset-password-token-check`,
          payload
        );
        isTokenValid.value = true;
      } catch (error: unknown) {
        if (axios.isAxiosError(error)) {
          isTokenValid.value = false;
        }
      } finally {
        loadingTokenValid.value = false;
      }
    }
  },
  { immediate: true }
);
</script>
