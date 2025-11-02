<template>
  <main v-if="!verifyEmailProcess" class="flex lg:gap-16 xl:gap-32 lg:min-h-screen lg:items-center lg:justify-center py-8 lg:py-0 px-4">
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
      class="lg:m-16 w-full lg:max-w-lg lg:rounded-2xl lg:border lg:border-gray-200 lg:bg-white lg:p-6 md:p-8"
      @submit.prevent="onRegister"
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
        <h2 class="text-2xl font-semibold">
          Create your account
        </h2>
        <p class="text-gray-400 mt-2">Start building your HTML projects with AI.</p>
        </div>
      </div>

      <!-- Form fields -->
      <div class="flex w-full flex-col gap-4">
        <!-- Firstname -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2" for="firstname">Firstname</label>
          <input
            id="firstname"
            v-model.trim="form.firstname"
            required
            maxlength="50"
            autocomplete="given-name"
            class="px-3 py-2 w-full h-11 rounded-lg text-black bg-white border border-gray-200 focus:outline-none focus:border-indigo-500 placeholder:text-gray-500"
            :class="!validFirst ? 'border-rose-500/60 focus:border-rose-400 focus:ring-rose-400/40' : ''"
            placeholder="Enter your firstname"
          >
          <p v-if="!validFirst" class="text-xs text-rose-400 mt-1">Please enter at least 2 characters.</p>
        </div>

        <!-- Lastname -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2" for="lastname">Lastname</label>
          <input
            id="lastname"
            v-model.trim="form.lastname"
            required
            maxlength="50"
            autocomplete="family-name"
                            class="px-3 py-2 w-full h-11 rounded-lg text-black bg-white border border-gray-200 focus:outline-none focus:border-indigo-500 placeholder:text-gray-500"
            :class="!validLast ? 'border-rose-500/60 focus:border-rose-400 focus:ring-rose-400/40' : ''"
            placeholder="Enter your lastname"
          >
          <p v-if="!validLast" class="text-xs text-rose-400 mt-1">Please enter at least 2 characters.</p>
        </div>

        <!-- Email -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2" for="email">Email</label>
          <input
            id="email"
            v-model.trim="form.email"
            required
            maxlength="50"
            autocomplete="email"
            inputmode="email"
                            class="px-3 py-2 w-full h-11 rounded-lg text-black bg-white border border-gray-200 focus:outline-none focus:border-indigo-500 placeholder:text-gray-500"
            :class="emailError ? 'border-rose-500/60 focus:border-rose-400 focus:ring-rose-400/40' : ''"
            placeholder="you@example.com"
            type="email"
          >
          <p v-if="emailError" class="text-xs text-rose-400 mt-1">Enter a valid email address.</p>
        </div>

        <!-- Password -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2" for="password">Password</label>
          <div class="relative">
            <input
              id="password"
              ref="passwordEl"
              v-model="form.password"
              required
              maxlength="50"
              autocomplete="new-password"
              :type="showPassword ? 'text' : 'password'"
                              class="px-3 py-2 w-full h-11 rounded-lg text-black bg-white border border-gray-200 focus:outline-none focus:border-indigo-500 placeholder:text-gray-500"
              :class="passwordError ? 'border-rose-500/60 focus:border-rose-400 focus:ring-rose-400/40' : 'border-[#30363d] focus:border-[#1F6FEB] focus:ring-[#1F6FEB]/40'"
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
              <UIcon :name="showPassword ? 'i-lucide-eye-off' : 'i-lucide-eye'" class="size-4" />
            </button>
          </div>

          <!-- Strength meter -->
          <div class="mt-2">
            <div class="h-2 w-full rounded-full bg-white/10 overflow-hidden">
              <div
                class="h-2 rounded-full transition-all"
                :class="strengthBarClass"
                :style="{ width: strengthBarWidth }"
              />
            </div>
            <div class="mt-1 flex items-center justify-between text-xs">
              <span :class="strengthTextClass">{{ strengthLabel }}</span>
              <span v-if="capsOn" class="text-amber-300">Caps Lock is on</span>
            </div>
          </div>

          <!-- Password rules -->
          <div class="mt-2 space-y-1 text-xs text-gray-400">
            <p class="font-medium text-gray-300">Password must:</p>
            <ul class="list-disc pl-5">
              <li :class="ruleClass(minLenOk)">Be at least 8 characters long</li>
              <li :class="ruleClass(hasLettersOk)">Include letters</li>
              <li :class="ruleClass(hasNumbersOk)">Include numbers</li>
              <li :class="ruleClass(hasSymbolsOk)">Include symbols</li>
            </ul>
          </div>
        </div>

        <!-- Confirm Password -->
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-2" for="confirm-password">Confirm Password</label>
          <div class="relative">
            <input
              id="confirm-password"
              v-model="form.confirmPassword"
              required
              maxlength="50"
              autocomplete="new-password"
              :type="showConfirm ? 'text' : 'password'"
                              class="px-3 py-2 w-full h-11 rounded-lg text-black bg-white border border-gray-200 focus:outline-none focus:border-indigo-500 placeholder:text-gray-500"
              :class="!passwordsMatch ? 'border-rose-500/60 focus:border-rose-400 focus:ring-rose-400/40' : 'border-[#30363d] focus:border-[#1F6FEB] focus:ring-[#1F6FEB]/40'"
              placeholder="Confirm your password"
            >
            <button
              type="button"
                class="cursor-pointer absolute right-2 top-[55%] -translate-y-1/2 rounded-md p-1.5 text-gray-500 hover:text-black hover:bg-white/10"
              :aria-label="showConfirm ? 'Hide password' : 'Show password'"
              @click="showConfirm = !showConfirm"
            >
              <UIcon :name="showConfirm ? 'i-lucide-eye-off' : 'i-lucide-eye'" class="size-4" />
            </button>
          </div>
          <p v-if="form.confirmPassword && !passwordsMatch" class="text-xs text-rose-400 mt-1">Passwords do not match.</p>
        </div>

        <!-- Error -->
        <div v-if="errorMessage" class="rounded-md border border-rose-500/40 bg-rose-500/10 px-3 py-2 text-rose-500">
          {{ errorMessage }}
        </div>

        <!-- Submit -->
        <button
          type="submit"
          class="mt-2 flex w-full min-w-[84px] h-12 items-center justify-center rounded-lg px-4 text-white text-sm tracking-[0.015em] transition-colors"
          :class="canSubmit ? 'bg-indigo-600 hover:bg-indigo-700 cursor-pointer' : 'bg-indigo-500/50 cursor-not-allowed'"
          :disabled="!canSubmit || loadingSubmit"
        >
          <span v-if="loadingSubmit" class="flex items-center gap-2">
            <UIcon name="i-lucide-loader-circle" class="size-6 text-white animate-spin" />
            <span>Loading</span>
          </span>
          <span v-else class="truncate">Sign up</span>
        </button>
      </div>

      <!-- Footer -->
      <div class="mt-6 text-center">
        <span class="text-gray-600">Already have an account?</span>
        <NuxtLink href="/auth/login" class="underline ml-1 hover:text-indigo-500">Log in</NuxtLink>
      </div>

      <p class="text-gray-400 text-xs mt-6 text-center">
        By signing up, you agree to our
        <NuxtLink class="text-gray-500 hover:text-indigo-500 underline" href="/terms-of-service">Terms of Service</NuxtLink>
        and
        <NuxtLink class="text-gray-500 hover:text-indigo-500 underline" href="/privacy-policy">Privacy Policy</NuxtLink>.
      </p>
    </form>
  </main>

  <Emailverification v-else :email="form.email" />
</template>

<script setup lang="ts">
import axios from 'axios'
import Emailverification from '~/components/emailVerification.vue'
import { api, toErrorMessage } from '~/utils/axios'
const config = useRuntimeConfig();
useSeoMeta({
  // Core
  title: 'Create Your Keetlo Account – Start Generating Tailwind HTML Pages with AI',
  description:
    'Sign up for Keetlo to generate production-ready HTML + Tailwind pages with AI. Get live previews, remix ideas, and build faster with an intuitive workflow.',
  robots: 'noindex,nofollow',

  // Open Graph
  ogUrl: `${config.public.SITE_URL}/auth/register`,
  ogSiteName: 'Keetlo',
  ogType: 'website',
  ogTitle: 'Create Your Keetlo Account – Start Generating Tailwind HTML Pages with AI',
  ogDescription:
    'Join Keetlo to quickly turn ideas into beautiful, responsive HTML + Tailwind pages powered by AI and live preview.',
  ogImage: `${config.public.SITE_URL}/images/og/register.png`,

  // Twitter
  twitterCard: 'summary_large_image',
  twitterTitle: 'Create Your Keetlo Account',
  twitterDescription:
    'Generate HTML + Tailwind pages with AI and live preview. Sign up to start building in minutes.',
  twitterImage: `${config.public.SITE_URL}/images/og/register.png`
})

const form = reactive({
  email: '',
  password: '',
  confirmPassword: '',
  firstname: '',
  lastname: '',
})
const errorMessage = ref<string | null>(null)
const verifyEmailProcess = ref<boolean>(false)
const loadingSubmit = ref(false)

/** UI state */
const showPassword = ref(false)
const showConfirm = ref(false)
const capsOn = ref(false)
const passwordEl = ref<HTMLInputElement | null>(null)

/** Simple validators */
const emailError = computed(() => form.email && !/^\S+@\S+\.\S+$/.test(form.email.trim()))
const validFirst = computed(() => !form.firstname || form.firstname.trim().length >= 2)
const validLast  = computed(() => !form.lastname || form.lastname.trim().length >= 2)

/** Password checks */
const minLenOk      = computed(() => form.password.length >= 8)
const hasLettersOk  = computed(() => /[a-zA-Z]/.test(form.password))
const hasNumbersOk  = computed(() => /\d/.test(form.password))
const hasSymbolsOk  = computed(() =>  /[!@#$%^&*(),.?":{}|<>]/.test(form.password))

const passwordError = computed(() => form.password && !(minLenOk.value && hasLettersOk.value && hasNumbersOk.value && hasSymbolsOk.value))
const passwordsMatch = computed(() => !form.confirmPassword || (form.password !== '' && form.password === form.confirmPassword))

/** Strength heuristic (0–4) */
const strengthScore = computed(() => {
  let s = 0
  if (minLenOk.value) s++
  if (hasLettersOk.value) s++
  if (hasNumbersOk.value) s++
  if (hasSymbolsOk.value) s++
  if (form.password.length >= 12 && s >= 3) s++ // bonus for length
  return Math.min(s, 4)
})
const strengthLabel = computed(() => ['Very weak','Weak','Okay','Good','Strong'][strengthScore.value])
const strengthBarWidth = computed(() => ['20%','35%','55%','75%','100%'][strengthScore.value])
const strengthBarClass = computed(() => [
  'bg-rose-500',
  'bg-orange-500',
  'bg-yellow-400',
  'bg-emerald-500',
  'bg-green-500'
][strengthScore.value])
const strengthTextClass = computed(() => [
  'text-rose-300',
  'text-orange-300',
  'text-yellow-300',
  'text-emerald-300',
  'text-green-300'
][strengthScore.value])

/** Helpers */
function ruleClass(ok: boolean) {
  return ok ? 'text-emerald-300' : 'text-gray-400'
}
function onPwdKey(e: KeyboardEvent) {
  capsOn.value = !!e.getModifierState?.('CapsLock')
}

/** Can submit */
const canSubmit = computed(() =>
  validFirst.value &&
  validLast.value &&
  !emailError.value &&
  !passwordError.value &&
  passwordsMatch.value &&
  !loadingSubmit.value
)

/** Submit */
const onRegister = async () => {
  // immediate client guard
  if (!canSubmit.value) {
    if (!validFirst.value) { errorMessage.value = 'Please enter your firstname.'; return }
    if (!validLast.value)  { errorMessage.value = 'Please enter your lastname.';  return }
    if (emailError.value)  { errorMessage.value = 'Please enter a valid email.';  return }
    if (passwordError.value) { errorMessage.value = 'Password does not meet requirements.'; return }
    if (!passwordsMatch.value) { errorMessage.value = 'Passwords do not match.'; return }
  }

  const { $modal } = useNuxtApp();

  try {
    loadingSubmit.value = true
    const payload = {
      firstname: form.firstname.trim(),
      lastname: form.lastname.trim(),
      email: form.email.trim(),
      password: form.password,
      confirmPassword: form.confirmPassword
    }
    await api.post(`${config.public.NUXT_PUBLIC_API_BASE}/auth/register`, payload);
    await $modal.alert({ title: 'Success', html: `<p>New account got registered successfully!</p>`, variant: 'success' });
    verifyEmailProcess.value = true
    errorMessage.value = null
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      errorMessage.value = toErrorMessage(error?.response?.data?.message)
    } else {
      errorMessage.value = 'Something went wrong. Please try again.'
    }
  } finally {
    loadingSubmit.value = false
  }
}
</script>
