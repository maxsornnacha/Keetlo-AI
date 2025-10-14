<template>
  <main class="relative min-h-[80vh] grid place-items-center overflow-hidden text-slate-100 px-4 py-16">
    <!-- Background -->
    <div class="pointer-events-none absolute inset-0 -z-10 overflow-hidden">
      <div class="absolute inset-0 bg-[#0b0f14]"/>
      <div
        class="absolute inset-0 opacity-90"
        :style="{
          backgroundImage: [
            `radial-gradient(1200px 700px at 50% -12%, rgba(144,180,255,.35), rgba(144,180,255,0) 70%)`,
            `radial-gradient(1100px 800px at 50% 92%, rgba(255,172,70,.28), rgba(255,120,110,0) 70%)`
          ].join(', ')
        }"
      />
      <div class="absolute inset-0 bg-[radial-gradient(1200px_700px_at_50%_50%,transparent_60%,rgba(0,0,0,.55))]"/>
    </div>

    <div class="w-full max-w-md">
      <div class="rounded-2xl border border-white/10 bg-white/5 p-6 md:p-8 backdrop-blur-md shadow-2xl ring-1 ring-white/10 text-center">
        <!-- Header -->
        <div class="flex items-center justify-center gap-3 mb-3">
          <UIcon name="i-lucide-shield-check" class="size-6 text-indigo-300" />
          <span class="text-sm text-indigo-200/90">Secure Verification</span>
        </div>

        <h1 class="text-3xl sm:text-4xl font-extrabold tracking-tight bg-clip-text text-transparent bg-gradient-to-b from-white to-slate-300">
          Enter Verification Code
        </h1>

        <p class="mt-3 text-slate-400">
          We’ve sent a 6-digit code to
          <span class="font-medium text-white">{{ props.email }}</span>.
          The code can be used up to 5 times within 10 minutes.
        </p>

        <!-- OTP form -->
        <form class="mt-8" autocomplete="one-time-code" @submit.prevent="onSubmit">
          <div class="grid grid-cols-6 gap-1 sm:gap-3 justify-items-center">
            <input
              v-for="(_, i) in otp"
              :ref="(el) => (inputs[i] = el as HTMLInputElement)"
              :key="i"
              v-model="otp[i]"
              inputmode="numeric"
              autocomplete="one-time-code"
              pattern="[0-9]*"
              maxlength="1"
              type="text"
              class="text-center text-xl sm:text-2xl font-semibold rounded-xl
                     h-14 w-14 sm:h-14 sm:w-14
                      text-white placeholder:text-slate-500
                     border border-white/10 focus:outline-none
                     focus:border-[#1F6FEB] focus:ring-2 focus:ring-[#1F6FEB]/40
                     transition"
              :class="otp[i] ? 'ring-2 ring-emerald-400/70 border-emerald-400/40' : ''"
              @input="onInput($event, i)"
              @keydown="onKeyDown($event, i)"
            >
          </div>

          <!-- Error / help -->
          <div v-if="errorMessage" class="mt-4 rounded-md border border-rose-500/40 bg-rose-500/10 px-3 py-2 text-rose-200">
            {{ errorMessage }}
          </div>
          <p v-else class="mt-4 text-xs text-slate-400">
            Tip: The form auto-submits when all 6 digits are entered.
          </p>

          <!-- Actions -->
          <div class="mt-6 flex flex-col items-center gap-4">
            <button
              :disabled="!onValidate()"
              class="cursor-pointer flex w-full max-w-xs items-center justify-center rounded-lg px-4 py-3 text-sm font-bold text-white transition
                     disabled:cursor-not-allowed disabled:bg-indigo-500/50
                     enabled:bg-indigo-600 enabled:hover:bg-indigo-700"
              :class="loadingSubmit ? 'cursor-default' : ''"
              type="submit"
            >
              <span v-if="loadingSubmit" class="flex items-center gap-2">
                <UIcon name="i-lucide-loader-circle" class="size-6 text-white animate-spin" />
                <span>Verifying…</span>
              </span>
              <span v-else>Verify</span>
            </button>

            <div class="text-sm text-slate-400">
              Didn’t receive the code?
              <button
                v-if="!isResendDisabled"
                class="font-medium text-blue-400 hover:text-blue-300 underline-offset-4 hover:underline cursor-pointer"
                type="button"
                @click="onResend"
              >
                <span v-if="loadingResend" class="inline-flex items-center gap-2">
                  <UIcon name="i-lucide-loader-circle" class="size-4 text-white animate-spin" />
                  Sending…
                </span>
                <span v-else>Resend Code</span>
              </button>
              <span v-else class="font-medium text-blue-400">
                Please wait {{ resendCooldown }}s
              </span>
            </div>
          </div>
        </form>
      </div>
    </div>
  </main>
</template>
<script setup lang="ts">
import axios from 'axios';

const router = useRouter();
const route = useRoute();
const props = defineProps<{email: string}>();
const otp = reactive(Array(6).fill(''));
const inputs = ref<(HTMLInputElement | null)[]>(Array(6).fill(null));
const loadingSubmit = ref(false);
const loadingResend = ref(false);
const isResendDisabled = ref(false);
const resendCooldown = ref(180); // 180 seconds = 3 minutes
const resendTimer = ref<NodeJS.Timeout | null>(null);
const config = useRuntimeConfig();
const errorMessage = ref<string | null>(null);
const nextUrl = route.query.next;
function onInput(event: Event, index: number) {
  const input = event.target as HTMLInputElement
  const value = input.value.replace(/\D/g, '') 
  otp[index] = value;
  if (value && index < inputs.value.length - 1) {
    inputs.value[index+1]?.focus();
  }
}
const { $modal } = useNuxtApp();

const onKeyDown = (event: KeyboardEvent, index: number) => {
  const input = event.target as HTMLInputElement
  if (event.key === 'Backspace' && !input.value && index > 0) {
    inputs.value[index - 1]?.focus()
  }
}

watch(otp ,(newOtpValue)=>{
  if(newOtpValue.join('').length === 6){
    onSubmit();
  }
})

const getOtpString = () => {
  return otp.join('')
}

const onResend = async () => {
try{
    loadingResend.value = true;
    await api.post(`${config.public.NUXT_PUBLIC_API_BASE}/auth/otp/resend?email=${props.email}`);
      isResendDisabled.value = true;
      resendCooldown.value = 180;

     resendTimer.value = setInterval(() => {
      if (resendCooldown.value > 0) {
        resendCooldown.value--;
      } else {
        isResendDisabled.value = false;
        if (resendTimer.value) clearInterval(resendTimer.value);
      }
    }, 1000);
  } catch (error: unknown) {
    if(axios.isAxiosError(error)){
      errorMessage.value = toErrorMessage(error?.response?.data?.message);
    } 
  } finally {
    loadingResend.value = false;
  }
}

const onValidate = () => {
  const otpCode = getOtpString()
  return otpCode.length === 6;
}


const onSubmit = async () => {
  const otpCode = getOtpString()
  try{
    loadingSubmit.value = true;
    const payload = {
      email: props.email,
      otpCode: otpCode
    }
    const response = await api.post(`${config.public.NUXT_PUBLIC_API_BASE}/auth/otp/verify`, payload);
    const token = response.data.token;
    if(token){
      setToken(token);
        const successOk = await $modal.alert({ 
          title: 'Success', 
          html: `<p>Account verified successfully!</p>`, 
          variant: 'success' 
        });
    if(successOk){
       window.location.href = nextUrl ? nextUrl as string : "/";
    }
    setTimeout(()=>{
      window.location.href = nextUrl ? nextUrl as string : "/";
    }, 1500)
    } else {
      errorMessage.value = "User can't login to to the system, please try again"
      router.push("/auth/login");
    }

  } catch (error: unknown) {
    if(axios.isAxiosError(error)){
      errorMessage.value = toErrorMessage(error?.response?.data?.message);
      loadingSubmit.value = false;
    } 
  } 
}
</script>