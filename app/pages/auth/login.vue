<template>
  <main v-if="!verifyEmailProcess" class="relative overflow-x-hidden">
    <div
      class="flex lg:gap-16 xl:gap-32 lg:min-h-screen lg:items-center lg:justify-center py-16 lg:py-0 px-4"
    >
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
      <form
        class="w-full lg:max-w-md lg:rounded-2xl lg:border lg:border-gray-200 lg:bg-white lg:p-6 md:p-8"
        @submit.prevent="onSubmit"
      >
        <!-- Header -->
        <div
          class="text-center mb-8 flex justify-center flex-col items-center gap-4"
        >
          <NuxtLink to="/" class="flex items-center gap-0">
            <nuxt-img src="/icon-256.png" class="size-6" />
            <h1 class="text-3xl font-semibold tracking-tight">EETLO</h1>
          </NuxtLink>
          <div>
            <h2 class="text-2xl font-semibold">Welcome back</h2>
            <p class="text-gray-400 mt-2">
              Login to continue creating your project.
            </p>
          </div>
        </div>

        <!-- Inputs -->
        <div class="flex w-full flex-col gap-4">
          <!-- Email -->
          <label class="flex flex-col w-full">
            <span class="text-sm font-medium text-gray-700 pb-2">Email</span>
            <div class="relative">
              <input
                v-model.trim="form.email"
                required
                autocomplete="email"
                inputmode="email"
                class="px-3 py-2 w-full h-11 rounded-lg text-black bg-white border border-gray-200 focus:outline-none focus:border-indigo-500 placeholder:text-gray-500"
                :class="
                  emailError ? 'border-rose-500/60 focus:border-rose-400' : ''
                "
                placeholder="your@email.com"
              >
              <span
                v-if="emailError"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-rose-400 text-sm"
                >!</span
              >
            </div>
            <p v-if="emailError" class="text-xs text-rose-400 mt-1">
              Enter a valid email address.
            </p>
          </label>

          <!-- Password -->
          <label class="flex flex-col w-full">
            <span class="text-sm font-medium text-gray-700 pb-2">Password</span>
            <div class="relative">
              <input
                ref="passwordEl"
                v-model="form.password"
                required
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                class="px-3 py-2 w-full h-11 rounded-lg text-black bg-white border border-gray-200 focus:outline-none focus:border-indigo-500 placeholder:text-gray-500"
                :class="
                  passwordError
                    ? 'border-rose-500/60 focus:border-rose-400 focus:ring-rose-400/40'
                    : ''
                "
                placeholder="Enter your password"
                @keyup.caps-lock="capsOn = true"
                @keydown="onPwdKey($event)"
              >
              <button
                type="button"
                class="cursor-pointer absolute right-2 top-[55%] -translate-y-1/2 rounded-md p-1.5 text-gray-500 hover:text-black hover:bg-white/10"
                :aria-label="showPassword ? 'Hide password' : 'Show password'"
                @click="showPassword = !showPassword"
              >
                <UIcon
                  :name="showPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'"
                  class="size-4"
                />
              </button>
            </div>
            <div class="flex items-center justify-between mt-2">
              <p v-if="passwordError" class="text-xs text-rose-400">
                Minimum 6 characters.
              </p>
              <p v-else-if="capsOn" class="text-xs text-amber-300">
                Caps Lock is on
              </p>
            </div>
          </label>
        </div>

        <!-- Links -->
        <div class="flex items-center justify-end mt-3">
          <NuxtLink
            class="text-sm text-gray-700 hover:text-indigo-500 underline"
            href="/auth/forgot-password"
          >
            Forgot password?
          </NuxtLink>
        </div>

        <!-- Error -->
        <div
          v-if="errorMessage"
          class="mt-3 rounded-md border border-rose-500/40 bg-rose-500/10 px-3 py-2 text-rose-500"
        >
          {{ errorMessage }}
        </div>

        <!-- Submit -->
        <button
          class="mt-5 flex min-w-[84px] w-full items-center justify-center rounded-lg h-12 px-4 text-white transition-colors"
          :class="
            canSubmit
              ? 'bg-indigo-500 hover:bg-indigo-600 cursor-pointer'
              : 'bg-indigo-400 cursor-not-allowed'
          "
          :disabled="!canSubmit"
        >
          <span v-if="loadingSubmit" class="flex items-center gap-2">
            <UIcon
              name="i-lucide-loader-circle"
              class="size-6 text-white animate-spin"
            />
            <span>Loading</span>
          </span>
          <span v-else>Login</span>
        </button>

        <!-- Divider -->
        <div class="flex items-center w-full my-6">
          <hr class="w-full border-t border-gray-200" >
          <p class="px-4 text-sm text-gray-400 whitespace-nowrap">
            Or continue with
          </p>
          <hr class="w-full border-t border-gray-200" >
        </div>

        <!-- OAuth -->
        <div class="flex flex-col sm:flex-row w-full gap-3">
          <button
            type="button"
            class="cursor-pointer py-2 flex-1 h-11 rounded-lg border border-gray-200 hover:bg-gray-200 transition-colors text-black text-sm font-medium inline-flex items-center justify-center gap-2"
            @click="loginWithGoogle"
          >
            <nuxt-img 
            src="/images/auth/google.png"
            alt="google"
            class="w-[25px] h-[25px] rounded-full object-cover"
            />
            <span>Google</span>
          </button>
          <button
            type="button"
            class="cursor-pointer py-2 flex-1 h-11 rounded-lg border border-gray-200 hover:bg-gray-200 transition-colors text-black text-sm font-medium inline-flex items-center justify-center gap-2"
            @click="loginWithGithub"
          >
                      <nuxt-img 
            src="/images/auth/github.png"
            alt="google"
            class="w-[25px] h-[25px] rounded-full object-cover"
            />
            <span>GitHub</span>
          </button>
        </div>

        <p class="pt-4 text-center text-sm text-gray-700">
          Don’t have an account?
          <NuxtLink
            href="/auth/register"
            class="underline hover:text-indigo-500"
            >Create your account</NuxtLink
          >
        </p>
      </form>
    </div>
  </main>

  <!-- Email verification step -->
  <EmailVerification v-else :email="form.email" />
</template>

<script setup lang="ts">
import axios from "axios";
import EmailVerification from "~/components/emailVerification.vue";
const config = useRuntimeConfig();
useSeoMeta({
  // Core (auth pages should not be indexed)
  title: "Log In to Keetlo – Continue Generating Tailwind HTML Pages with AI",
  description:
    "Access your Keetlo account to keep generating production-ready HTML + Tailwind pages with AI and live preview. Continue projects, remix ideas, and ship faster.",
  robots: "noindex,nofollow",

  // Open Graph
  ogUrl: `${config.public.SITE_URL}/auth/login`,
  ogSiteName: "Keetlo",
  ogType: "website",
  ogTitle: "Log In to Keetlo",
  ogDescription:
    "Sign in to Keetlo to continue building responsive HTML + Tailwind pages with AI and real-time preview.",
  ogImage: `${config.public.SITE_URL}/images/og/login.png`,

  // Twitter
  twitterCard: "summary_large_image",
  twitterTitle: "Log In to Keetlo",
  twitterDescription:
    "Continue generating HTML + Tailwind pages with AI and live preview.",
  twitterImage: `${config.public.SITE_URL}/images/og/login.png`,
});

const route = useRoute();
const nextUrl = route.query.next;

const errorMessage = ref<string | null>(null);
const loadingSubmit = ref(false);
const verifyEmailProcess = ref(false);

const form = reactive({
  email: "",
  password: "",
});

const showPassword = ref(false);
const capsOn = ref(false);
const passwordEl = ref<HTMLInputElement | null>(null);
const { $modal } = useNuxtApp();

/** Validation */
const emailError = computed(() => form.email && !/^\S+@\S+\.\S+$/.test(form.email.trim()));
const passwordError = computed(() => form.email && (form.password ?? "").length < 6);
const canSubmit = computed(
  () => !emailError.value && !passwordError.value && !loadingSubmit.value
);

/** CapsLock hint */
function onPwdKey(e: KeyboardEvent) {
  capsOn.value = !!e.getModifierState?.("CapsLock");
}

const loginWithGoogle = () => {
  const width = 500;
  const height = 600;
  const left = (window.innerWidth - width) / 2;
  const top = (window.innerHeight - height) / 2;

  const popup = window.open(
    `${config.public.NUXT_PUBLIC_API_BASE}/oauth2/authorization/google`, // your backend login URL
    "GoogleLogin",
    `width=${width},height=${height},top=${top},left=${left}`
  );

  window.addEventListener("message", (event) => {
    if (event.origin !== window.location.origin) return;
    if (event.data.success) {
      setToken(event.data.token);
      popup?.close();
      window.location.href = nextUrl ? (nextUrl as string) : "/";
    }
  });
};

const loginWithGithub = () => {
  const width = 500;
  const height = 600;
  const left = (window.innerWidth - width) / 2;
  const top = (window.innerHeight - height) / 2;

  const popup = window.open(
    `${config.public.NUXT_PUBLIC_API_BASE}/oauth2/authorization/github`, // your backend login URL
    "GoogleLogin",
    `width=${width},height=${height},top=${top},left=${left}`
  );

  window.addEventListener("message", (event) => {
    if (event.origin !== window.location.origin) return;
    if (event.data.success) {
      setToken(event.data.token);
      popup?.close();
      window.location.href = nextUrl ? (nextUrl as string) : "/";
    }
  });
};

/** Submit */
const onSubmit = async () => {
  if (!canSubmit.value) return;
  try {
    loadingSubmit.value = true;
    errorMessage.value = null;

    const payload = { email: form.email.trim(), password: form.password };
    const response = await api.post(
      `${config.public.NUXT_PUBLIC_API_BASE}/auth/login`,
      payload
    );

    const token = response.data.token;
    setToken(token);
    const successOk = await $modal.alert({
      title: "Success",
      html: `<p>Login successfully</p>`,
      variant: "success",
    });
    if (successOk) {
      window.location.href = (nextUrl as string) || "/";
    }
    setTimeout(() => {
      window.location.href = (nextUrl as string) || "/";
    }, 1500);
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      errorMessage.value = toErrorMessage(error?.response?.data?.message);
      if (error.response?.data?.requiresVerification) {
        verifyEmailProcess.value = true;
      }
    } else {
      errorMessage.value = "Something went wrong. Please try again.";
    }
    loadingSubmit.value = false;
  }
};
</script>
