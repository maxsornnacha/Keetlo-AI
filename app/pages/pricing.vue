<template>
  <main
    class="relative flex flex-1 flex-col items-center text-slate-100 overflow-x-hidden"
  >
    <div>
      <!-- Heading -->
      <header
        class="mx-auto flex max-w-3xl flex-col items-center gap-4 text-center"
      >
        <h1
          class="bg-gradient-to-b from-white to-slate-300 bg-clip-text text-4xl font-extrabold tracking-tight text-transparent sm:text-5xl lg:text-6xl"
        >
          Choose the plan that's right for you
        </h1>
        <p class="text-lg text-slate-400">
          Simple, transparent pricing. No hidden fees. Cancel anytime.
        </p>
      </header>

      <!-- Plans -->
      <section
        v-if="!loading && subscriptionPlans.length"
        class="mx-auto mt-10 grid w-full grid-cols-1 gap-6 sm:gap-8"
        :class="
          subscriptionPlans.length > 2
            ? 'md:grid-cols-2 xl:grid-cols-3'
            : subscriptionPlans.length > 1 && subscriptionPlans.length < 3
            ? 'md:grid-cols-2'
            : 'md:grid-cols-1'
        "
      >
        <div
          v-for="plan in subscriptionPlans"
          :key="plan.subscriptionPlanId"
          class="group relative overflow-hidden rounded-2xl border p-7 sm:p-8 shadow-2xl transition backdrop-blur-md hover:shadow-[0_10px_40px_rgba(99,102,241,0.25)]"
          :class="
            plan.mostPopular === 1
              ? 'border-indigo-400/40 bg-gradient-to-b from-indigo-500/10 to-indigo-400/5 ring-1 ring-indigo-400/40'
              : 'border-white/10 bg-white/5 ring-1 ring-white/10'
          "
        >
          <!-- Popular badge -->
          <div
            v-if="plan.mostPopular === 1"
            class="absolute right-4 top-4 inline-flex items-center gap-2 rounded-full bg-indigo-500 px-3 py-1 text-[11px] font-semibold uppercase tracking-wide text-white shadow"
          >
            <UIcon name="i-lucide-sparkles" class="size-3.5" />
            Most Popular
          </div>

          <!-- Accent top line -->
          <div
            class="pointer-events-none absolute inset-x-0 -top-px h-px"
            :class="
              plan.mostPopular === 1
                ? 'bg-gradient-to-r from-transparent via-indigo-300/70 to-transparent'
                : 'bg-gradient-to-r from-transparent via-white/30 to-transparent'
            "
          />

          <!-- Plan header -->
          <div class="flex flex-col gap-2">
            <h2 class="text-2xl font-semibold text-white">{{ plan.name }}</h2>

            <!-- Price block -->
            <div class="mt-2">
              <div class="flex items-baseline gap-2">
                <span class="text-5xl font-extrabold tracking-tight text-white">
                  ${{ plan.price.toFixed(2) }}
                </span>
                <span class="text-sm text-slate-400">/ month</span>
              </div>
              <p class="mt-2 text-sm text-slate-400">
                {{ plan.description }}
              </p>
            </div>
          </div>

          <!-- CTA -->
          <div class="mt-6">
            <button
              v-if="plan.isActive === 1"
              disabled
              class="flex h-11 w-full cursor-default items-center justify-center rounded-xl border border-white/10 bg-white/10 px-6 text-base font-semibold text-white"
            >
              Currently Using
            </button>

            <button
              v-else
              class="flex h-11 w-full items-center justify-center gap-2 rounded-xl text-base font-semibold text-white transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-0"
              :class="
                plan.packageType === 'DEFAULT'
                  ? 'bg-slate-500/50 cursor-not-allowed'
                  : 'bg-indigo-600 hover:bg-indigo-700 cursor-pointer'
              "
              :disabled="plan.packageType === 'DEFAULT'"
              @click="onUpgradePackage(plan.subscriptionPlanId)"
            >
              <UIcon name="i-lucide-rocket" class="size-5" />
              <span class="truncate">Upgrade</span>
            </button>

            <!-- Trust row -->
            <div
              class="mt-3 flex items-center justify-center gap-3 text-xs text-white/60"
            >
              <UIcon name="i-lucide-shield-check" class="size-4" />
              <span>Secure checkout</span>
              <span aria-hidden="true">•</span>
              <span>Cancel anytime</span>
            </div>
          </div>

          <!-- Feature list -->
          <ul
            class="mt-7 space-y-3 border-t border-white/10 pt-6 text-sm text-slate-200"
          >
            <li class="flex items-center gap-3" v-if="plan.requestsPerDay">
              <UIcon name="i-lucide-check" class="size-5 text-emerald-400" />
              <span>{{ plan.requestsPerDay }} requests / day</span>
            </li>
            <li class="flex items-center gap-3" v-else>
              <UIcon name="i-lucide-check" class="size-5 text-emerald-400" />
              <span>Unlimited requests</span>
            </li>

            <li class="flex items-center gap-3">
              <UIcon name="i-lucide-check" class="size-5 text-emerald-400" />
              <span>{{
                plan.advancedFeatures === 1
                  ? "Advanced features"
                  : "Basic features"
              }}</span>
            </li>

            <li
              v-if="plan.communitySupport === 1"
              class="flex items-center gap-3"
            >
              <UIcon name="i-lucide-check" class="size-5 text-emerald-400" />
              <span>Community support</span>
            </li>

            <li
              v-if="plan.prioritySupport === 1"
              class="flex items-center gap-3"
            >
              <UIcon name="i-lucide-check" class="size-5 text-emerald-400" />
              <span>Priority support</span>
            </li>
          </ul>

          <!-- Soft bottom glow -->
          <div
            class="pointer-events-none absolute inset-x-0 bottom-0 h-24 bg-gradient-to-t from-white/10 to-transparent"
          />
        </div>
      </section>

      <!-- Loading skeleton -->
      <section
        v-else-if="loading"
        class="mt-10 grid w-full gap-6 sm:gap-8 grid-cols-1 md:grid-cols-2 lg:grid-cols-3"
      >
        <div
          v-for="i in 3"
          :key="i"
          class="w-full overflow-hidden rounded-2xl border border-white/10 bg-white/5 p-7 sm:p-8"
        >
          <div class="h-6 w-40 rounded bg-white/10 animate-pulse" />
          <div class="mt-4 h-10 w-86 rounded bg-white/10 animate-pulse" />
          <div class="mt-2 h-3 w-3/4 rounded bg-white/10 animate-pulse" />
          <div class="mt-6 h-11 w-full rounded-xl bg-white/10 animate-pulse" />
          <ul class="mt-7 space-y-3 border-t border-white/10 pt-6">
            <li
              v-for="j in 6"
              :key="j"
              class="h-4 w-5/6 rounded bg-white/10 animate-pulse"
            />
          </ul>
        </div>
      </section>

      <!-- Error / empty state -->
      <section
        v-else
        class="mx-auto mt-12 max-w-xl text-center rounded-2xl border border-white/10 bg-white/5 p-8"
      >
        <div
          class="mx-auto mb-4 grid h-16 w-16 place-items-center rounded-full bg-white/10"
        >
          <UIcon name="i-lucide-alert-circle" class="size-8 text-white/80" />
        </div>
        <h3 class="text-xl font-bold text-white">
          {{ errorMessage || "No plans available" }}
        </h3>
        <p class="mt-2 text-white/70">Please try again later.</p>
        <button
          class="cursor-pointer mt-6 rounded-lg bg-white/10 px-4 py-2 text-sm text-white hover:bg-white/15 border border-white/10"
          @click="$router.go(0)"
        >
          Retry
        </button>
      </section>
    </div>

    <!-- Loader overlay for upgrade -->
    <PageLoader v-if="loadingUpgradePackage" />
  </main>
</template>
<script setup lang="ts">
import axios from "axios";
import PageLoader from "~/components/ui/PageLoader.vue";
import type { SubscriptionPlan } from "~/types/SubscriptionPlan";
const { $modal } = useNuxtApp();

const router = useRouter();
const loading = ref(true);
let subscriptionPlans = reactive<SubscriptionPlan[]>([]);
const config = useRuntimeConfig();
const errorMessage = ref<string | null>(null);
const loadingUpgradePackage = ref(false);

useSeoMeta({
  // Core
  title: "Keetlo Pricing – AI HTML Generator To Build Faster with AI",
  description:
    "Simple, transparent pricing for Keetlo. Start free and upgrade for higher daily requests, advanced features, and priority support.",
  robots: "index,follow,max-image-preview:large",

  // Open Graph
  ogUrl: `${config.public.SITE_URL}/pricing`,
  ogSiteName: "Keetlo",
  ogType: "website",
  ogTitle: "Keetlo Pricing – AI HTML Generator To Build Faster with AI",
  ogDescription:
    "Choose a plan that fits your workflow. Generate HTML + Tailwind pages with live preview, remix templates, and export instantly.",
  ogImage: `${config.public.SITE_URL}/images/og/pricing.png`,

  // Twitter
  twitterCard: "summary_large_image",
  twitterTitle: "Keetlo Pricing – AI HTML Generator To Build Faster with AI",
  twitterDescription:
    "From Free to Pro: pick the plan that lets you generate more pages, unlock advanced features, and get priority support.",
  twitterImage: `${config.public.SITE_URL}/images/og/pricing.png`,
});

onMounted(async () => {
  try {
    const response = await api.get(
      `${config.public.NUXT_PUBLIC_API_BASE}/subscription/info`
    );
    subscriptionPlans = response.data.subscriptionPlans;
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      errorMessage.value = toErrorMessage(error?.response?.data?.message);
    }
  } finally {
    loading.value = false;
  }
});

const onUpgradePackage = async (subscriptionPlanId: string) => {
  try {
    const ok = await $modal.confirm({
      title: "Confirm",
      html: "<p>Would you like to upgrade the package?</p>",
      confirmText: "Upgrade now",
      cancelText: "Cancel",
      variant: "default",
    });
    if (!ok) {
      return;
    }

    loadingUpgradePackage.value = true;
    const payload = {
      subscriptionPlanId: subscriptionPlanId,
    };
    const response = await api.post(
      `${config.public.NUXT_PUBLIC_API_BASE}/stripe/create-payment`,
      payload
    );
    if (response.data.sessionUrl) {
      window.location.href = response.data.sessionUrl;
    }
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      if (error.status === 401) {
        router.push(`/auth/login?next=${window.location.href}`);
        return;
      }
      await $modal.alert({
        title: "Error",
        html: `<p>${toErrorMessage(error?.response?.data?.message)}</p>`,
        variant: "danger",
      });
    }
  } finally {
    loadingUpgradePackage.value = false;
  }
};
</script>
