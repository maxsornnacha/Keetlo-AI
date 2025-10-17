<template>
  <main v-if="!verifyEmailProcess" class="relative text-slate-100 overflow-x-hidden">
    <div class="flex lg:min-h-screen lg:items-center lg:justify-center py-16 lg:py-0 px-4">
      <form
        class="w-full lg:max-w-md lg:rounded-2xl lg:border lg:border-white/10 lg:bg-white/5 lg:p-6 md:p-8 lg:backdrop-blur-md lg:shadow-2xl lg:ring-1 lg:ring-white/10"
        @submit.prevent="onSubmit"
      >
        <!-- Header -->
        <div class="text-center mb-6">
          <h2 class="text-3xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-b from-white to-slate-300">
            Welcome back to Keetlo
          </h2>
          <p class="text-slate-400 mt-2">Login to continue creating your Vue TypeScript project.</p>
        </div>

        <!-- Inputs -->
        <div class="flex w-full flex-col gap-4">
          <!-- Email -->
          <label class="flex flex-col w-full">
            <span class="text-sm font-medium text-slate-200 pb-2">Email</span>
            <div class="relative">
              <input
                v-model.trim="form.email"
                autocomplete="email"
                inputmode="email"
                class="px-3 py-2 w-full h-11 rounded-lg text-white bg-[#161B22] border focus:outline-none
                       placeholder:text-slate-500
                       transition-colors
                       "
                :class="emailError ? 'border-rose-500/60 focus:border-rose-400 focus:ring-rose-400/40' : 'border-[#30363d] focus:border-[#1F6FEB] focus:ring-[#1F6FEB]/40'"
                placeholder="your@email.com"
              >
              <span
                v-if="emailError"
                class="absolute right-3 top-1/2 -translate-y-1/2 text-rose-400 text-sm"
              >!</span>
            </div>
            <p v-if="emailError" class="text-xs text-rose-400 mt-1">Enter a valid email address.</p>
          </label>

          <!-- Password -->
          <label class="flex flex-col w-full">
            <span class="text-sm font-medium text-slate-200 pb-2">Password</span>
            <div class="relative">
              <input
                ref="passwordEl"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                autocomplete="current-password"
                class="px-3 py-2 w-full h-11 rounded-lg text-white bg-[#161B22] border focus:outline-none
                       placeholder:text-slate-500
                       transition-colors"
                :class="passwordError ? 'border-rose-500/60 focus:border-rose-400 focus:ring-rose-400/40' : 'border-[#30363d] focus:border-[#1F6FEB] focus:ring-[#1F6FEB]/40'"
                placeholder="Enter your password"
                @keyup.caps-lock="capsOn = true"
                @keydown="onPwdKey($event)"
              >
              <button
                type="button"
                class="absolute right-2 top-1/2 -translate-y-1/2 rounded-md p-1.5 text-slate-300 hover:text-white hover:bg-white/10"
                :aria-label="showPassword ? 'Hide password' : 'Show password'"
                @click="showPassword = !showPassword"
              >
                <UIcon :name="showPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'" class="size-4" />
              </button>
            </div>
            <div class="flex items-center justify-between mt-2">
              <p v-if="passwordError" class="text-xs text-rose-400">Minimum 6 characters.</p>
              <p v-else-if="capsOn" class="text-xs text-amber-300">Caps Lock is on</p>
            </div>
          </label>
        </div>

        <!-- Links -->
        <div class="flex items-center justify-end mt-3">
          <NuxtLink class="text-sm text-slate-300 hover:text-[#1F6FEB] underline" href="/auth/forgot-password">
            Forgot password?
          </NuxtLink>
        </div>

        <!-- Error -->
        <div v-if="errorMessage" class="mt-3 rounded-md border border-rose-500/40 bg-rose-500/10 px-3 py-2 text-rose-200">
          {{ errorMessage }}
        </div>

        <!-- Submit -->
        <button
          class="mt-5 flex min-w-[84px] w-full items-center justify-center rounded-lg h-12 px-4 text-white text-base font-bold
                 transition-colors"
          :class="canSubmit ? 'bg-indigo-600 hover:bg-indigo-700 cursor-pointer' : 'bg-indigo-500/50 cursor-not-allowed'"
          :disabled="!canSubmit"
        >
          <span v-if="loadingSubmit" class="flex items-center gap-2">
            <UIcon name="i-lucide-loader-circle" class="size-6 text-white animate-spin" />
            <span>Loading</span>
          </span>
          <span v-else>Login</span>
        </button>

        <!-- Divider -->
        <div class="flex items-center w-full my-6">
          <hr class="w-full border-t border-white/10">
          <p class="px-4 text-sm text-slate-400 whitespace-nowrap">Or continue with</p>
          <hr class="w-full border-t border-white/10">
        </div>

        <!-- OAuth -->
        <div class="flex flex-col sm:flex-row w-full gap-3">
          <button
            type="button"
            class="cursor-pointer py-2 flex-1 h-11 rounded-lg border border-white/10 bg-white/5 hover:bg-white/10 transition-colors
                   text-white text-sm font-medium inline-flex items-center justify-center gap-2"
            @click="loginWithGoogle"
          >
            <UIcon name="i-lucide-at-sign" class="size-4" />
            <span>Google</span>
          </button>
          <button
            type="button"
            class="cursor-pointer py-2 flex-1 h-11 rounded-lg border border-white/10 bg-white/5 hover:bg-white/10 transition-colors
                   text-white text-sm font-medium inline-flex items-center justify-center gap-2"
            @click="loginWithGithub"
          >
            <UIcon name="i-lucide-github" class="size-4" />
            <span>GitHub</span>
          </button>
        </div>

        <p class="pt-4 text-center text-sm text-slate-400">
          Don’t have an account?
          <NuxtLink href="/auth/register" class="underline hover:text-[#1F6FEB]">Create your account</NuxtLink>
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
  title: 'Log In to Keetlo – Continue Generating Tailwind HTML Pages with AI',
  description:
    'Access your Keetlo account to keep generating production-ready HTML + Tailwind pages with AI and live preview. Continue projects, remix ideas, and ship faster.',
  robots: 'noindex,nofollow',

  // Open Graph
  ogUrl: `${config.public.SITE_URL}/auth/login`,
  ogSiteName: 'Keetlo',
  ogType: 'website',
  ogTitle: 'Log In to Keetlo',
  ogDescription:
    'Sign in to Keetlo to continue building responsive HTML + Tailwind pages with AI and real-time preview.',
  ogImage: `${config.public.SITE_URL}/images/og/login.png`,

  // Twitter
  twitterCard: 'summary_large_image',
  twitterTitle: 'Log In to Keetlo',
  twitterDescription:
    'Continue generating HTML + Tailwind pages with AI and live preview.',
  twitterImage: `${config.public.SITE_URL}/images/og/login.png`
})


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
const remember = ref<boolean>(false);
const { $modal } = useNuxtApp();

onMounted(() => {
  // Prefill email if remembered
  const remembered = localStorage.getItem("keetlo_email");
  if (remembered) form.email = remembered;
});
watch(remember, (v) => {
  if (v) localStorage.setItem("keetlo_email", form.email || "");
  else localStorage.removeItem("keetlo_email");
});
watch(() => form.email, (v) => {
  if (remember.value) localStorage.setItem("keetlo_email", v || "");
});

/** Validation */
const emailError = computed(() => !/^\S+@\S+\.\S+$/.test(form.email.trim()));
const passwordError = computed(() => (form.password ?? "").length < 6);
const canSubmit = computed(() => !emailError.value && !passwordError.value && !loadingSubmit.value);

/** CapsLock hint */
function onPwdKey(e: KeyboardEvent) {
  capsOn.value = !!e.getModifierState?.("CapsLock");
}

const loginWithGoogle = () => {
  const width = 500
  const height = 600
  const left = (window.innerWidth - width) / 2
  const top = (window.innerHeight - height) / 2

  const popup = window.open(
    `${config.public.NUXT_PUBLIC_API_BASE}/oauth2/authorization/google`, // your backend login URL
    'GoogleLogin',
    `width=${width},height=${height},top=${top},left=${left}`
  )

  window.addEventListener('message', (event) => {
    if (event.origin !== window.location.origin) return
    if (event.data.success) {
      setToken(event.data.token)
      popup?.close()
      window.location.href = nextUrl ? nextUrl as string : "/";
    }
  })
}

const loginWithGithub = () => {
  const width = 500
  const height = 600
  const left = (window.innerWidth - width) / 2
  const top = (window.innerHeight - height) / 2

  const popup = window.open(
    `${config.public.NUXT_PUBLIC_API_BASE}/oauth2/authorization/github`, // your backend login URL
    'GoogleLogin',
    `width=${width},height=${height},top=${top},left=${left}`
  )

  window.addEventListener('message', (event) => {
    if (event.origin !== window.location.origin) return
    if (event.data.success) {
      setToken(event.data.token)
      popup?.close()
      window.location.href = nextUrl ? nextUrl as string : "/";
    }
  })
}

/** Submit */
const onSubmit = async () => {
  if (!canSubmit.value) return;
  try {
    loadingSubmit.value = true;
    errorMessage.value = null;

    const payload = { email: form.email.trim(), password: form.password };
    const response = await api.post(`${config.public.NUXT_PUBLIC_API_BASE}/auth/login`, payload);

    const token = response.data.token;
    setToken(token);
      const successOk = await $modal.alert({ title: 'Success', html: `<p>Login successfully</p>`, variant: 'success' });
    if(successOk){
      window.location.href = (nextUrl as string) || "/";;
    }
    setTimeout(()=>{
      window.location.href = (nextUrl as string) || "/";;
    }, 1500)

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
