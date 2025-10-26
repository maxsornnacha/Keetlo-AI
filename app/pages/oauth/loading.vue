<template>
  <main class="relative min-h-screen text-gray-700 overflow-hidden">

    <!-- SUCCESS -->
    <section v-if="isSuccess" class="grid place-items-center min-h-screen px-4">
      <div class="w-full max-w-md text-center rounded-2xl border border-gray-200 bg-white p-8" role="alert" aria-live="polite">
        <div class="flex justify-center mb-4">
          <UIcon name="i-lucide-check-circle" class="size-12 text-emerald-400" />
        </div>
        <h1 class="text-4xl font-extrabold text-emerald-400 mb-2">Success</h1>
        <p class="text-gray-400">Your login has been completed successfully.</p>

        <!-- Popup close / redirect actions -->
        <div class="mt-6 space-y-2">
          <div v-if="isPopup" class="text-sm text-gray-400">
            This window will close automatically in
            <span class="font-semibold text-gray-400">{{ countdown }}</span>s.
          </div>
          <button
            v-else
            class="w-full h-11 rounded-lg bg-indigo-600 hover:bg-indigo-700 text-black font-semibold cursor-pointer"
            @click="continueToApp"
          >
            Continue to app
          </button>
        </div>
      </div>
    </section>

    <!-- LOADING -->
    <section v-else-if="!errorMessage" class="grid place-items-center min-h-screen px-4">
      <div class="w-full max-w-md text-center rounded-2xl p-8">
        <div class="mx-auto w-16 h-16 border-4 border-indigo-500 border-t-transparent rounded-full animate-spin" aria-label="Loading"/>
        <p class="mt-6 text-gray-700" aria-live="polite">Signing you in…</p>
      </div>
    </section>

    <!-- ERROR -->
    <section v-else class="grid place-items-center min-h-screen px-4">
      <div class="w-full max-w-md rounded-2xl border border-gray-200 bg-white p-8 backdrop-blur-md shadow-2xl ring-1 ring-gray-200">
        <div class="flex justify-center mb-4">
          <UIcon name="i-lucide-x-circle" class="size-12 text-rose-400" />
        </div>
        <h1 class="text-4xl font-extrabold text-rose-400 text-center mb-3">Error</h1>
        <p class="text-gray-400 text-center">Your login failed.</p>

        <!-- brief message -->
        <p class="mt-2 text-sm text-gray-500 text-center break-words">
          {{ briefError }}
        </p>

        <!-- details toggler -->
        <details class="mt-4 rounded-md border border-gray-200 bg-white px-3 py-2">
          <summary class="cursor-pointer text-sm text-gray-500">Technical details</summary>
          <pre class="mt-2 whitespace-pre-wrap text-xs text-gray-400">{{ errorMessage }}</pre>
        </details>

        <div class="mt-6 grid grid-cols-1 sm:grid-cols-3 gap-3">
          <button
            class="h-10 rounded-lg bg-white hover:bg-gray-200 border border-gray-200 text-black text-sm cursor-pointer"
            @click="retry"
          >
            Try again
          </button>
          <button
            class="h-10 rounded-lg bg-white hover:bg-gray-200 border border-gray-200 text-black text-sm cursor-pointer"
            @click="copyError"
          >
            Copy details
          </button>
          <button
            class="h-10 rounded-lg bg-white hover:bg-gray-200 border border-gray-200 text-black text-sm cursor-pointer"
            @click="closeWindow"
          >
            Close
          </button>
        </div>
      </div>
    </section>
  </main>
</template>

<script setup lang="ts">
import axios from "axios";

const errorMessage = ref<string | null>(null);
const isSuccess = ref(false);
const countdown = ref(3);

const route = useRoute();
const config = useRuntimeConfig();

const next = computed(() => {
  const q = route.query?.next;
  return Array.isArray(q) ? q[0] : (q as string | undefined);
});
const isPopup = computed(() => typeof window !== "undefined" && !!window.opener && !window.opener.closed);

const briefError = computed(() => {
  if (!errorMessage.value) return "";
  const msg = errorMessage.value.toString();
  const m = msg.match(/(Network Error|timeout|401|403|404|500|invalid|expired|denied)/i);
  return m ? `(${m[0]})` : "Please try again.";
});

function qStr(v: unknown): string | undefined {
  if (typeof v === "string") return v;
  if (Array.isArray(v)) return String(v[0]);
  return undefined;
}

function continueToApp() {
  window.location.href = next.value || "/";
}

function closeWindow() {
  if (isPopup.value) window.close();
  else continueToApp();
}

function retry() {
  window.location.href = "/auth/login";
}

async function copyError() {
  try {
    await navigator.clipboard.writeText(errorMessage?.value ?? "");
    // (optional) toast could be added
  } catch { /* empty */ }
}

onMounted(async () => {
  const oauthToken = qStr(route.query?.oauthToken);
  const registrationId = qStr(route.query?.registrationId);
  const status = qStr(route.query?.status);
  const errFromQuery = qStr(route.query?.error) || qStr(route.query?.error_description);

  if (status === "success" && oauthToken) {
    try {
      const resp = await axios.get(
        `${config.public.NUXT_PUBLIC_API_BASE}/auth/oauth/login`,
        { params: { oauthToken, registrationId }, withCredentials: true }
      );

      const token = resp.data?.token;
      if (!token) throw new Error("No token returned from server.");

      setToken(token);
      isSuccess.value = true;

      // If opened by a parent window, inform and auto-close
      if (isPopup.value) {
        const origin = window.location.origin;
        window.opener?.postMessage({ success: true, token }, origin);

        const t = setInterval(() => {
          countdown.value -= 1;
          if (countdown.value <= 0) {
            clearInterval(t);
            window.close();
          }
        }, 1000);
      }
    } catch (error : unknown) {
         if (axios.isAxiosError(error)) {
      errorMessage.value =
        (axios.isAxiosError(error) && (error.response?.data?.message || error.message)) ||
        (error?.message ?? "Login failed");
         }
    }
  } else {
    errorMessage.value = errFromQuery || "Invalid or missing OAuth parameters.";
  }
});
</script>
